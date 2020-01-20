package com.wx.wechat.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wx.wechat.Utils.*;
import com.wx.wechat.config.WeChatAccountConfig;
import com.wx.wechat.constant.Constants;
import com.wx.wechat.constant.HttpStatusEnum;
import com.wx.wechat.dao.Menu;
import com.wx.wechat.dao.WxImage;
import com.wx.wechat.dao.WxImageXml;
import com.wx.wechat.dao.xml;
import com.wx.wechat.temp.WxTempMsg;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.wx.wechat.constant.Constants.USER_SUBSCRIBE_WX;
import static com.wx.wechat.constant.Constants.WXTOKEN;

@Service
@Slf4j
public class WechatService {
    @Autowired
    private NRedisUtil NRedisUtil;

    @Autowired
    private WxMpService wwxMpService;

    @Autowired
    private WeChatAccountConfig weChatAccountConfig;

    private static String GET_ACCTOKEN_BY_CODE = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
    // 菜单创建（POST） 限100（次/天）
    public static String MENU_CREATE_URL = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=%s";
    private static final String SEND_TEMPLATE_MSG = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s";
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String JS_API_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";
    // 微信开放平台临时二维码url
    public final static String OPEN_QRCODE_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s";
    // 用ticket换取二维码图片
    public final static String OPEN_REDIRECT_URL = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s";
    // 删除微信公众号菜单接口
    public final static String DELETE_WX_MENU = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=%s";
    public final static String SUBSCRIBE_URL = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=%s";
    /** 微信上传文件接口 **/
    public final static String UPLOAD_FILE = "https://api.weixin.qq.com/cgi-bin/material/add_material?access_token=%s&type=%s";

    private static final String OURWORK_WECHAT_TICKET = "OURWORK_WECHAT_TICKET";
    private static final long EXPIRES_IN_SECONDS = 7000;
    private static long LAST_GETTOKEN_TIME;

    public net.sf.json.JSONObject getAccessTokenByCode(String code) {
        String getAccTokenUrl = String.format(GET_ACCTOKEN_BY_CODE, weChatAccountConfig.getAPP_ID(), weChatAccountConfig.getAPP_SECRET(), code);
        return JsonUtils.fromObject(HttpClientUtil.doGet(getAccTokenUrl));
    }

    /**
     *  获取token的方法需加锁 防止并发获取 出现 redis中保存时效token的问题
     *
     */
    public synchronized String getAccessToken() {
        String access_token = String.valueOf(NRedisUtil.get("wx_access_token"));
        log.info("accessTokenJson:{}", access_token);
        if (!EmptyUtils.isEmpty(access_token) && !"null".equals(access_token) && System.currentTimeMillis() - LAST_GETTOKEN_TIME < 7000 * 1000) {
            return access_token;
        }
        Map<String, String> param = new HashMap<String, String>();
        param.put("grant_type", "client_credential");
        param.put("appid", weChatAccountConfig.getAPP_ID());
        param.put("secret", weChatAccountConfig.getAPP_SECRET());
        String accessTokenJson = HttpClientUtil.doGet(ACCESS_TOKEN_URL, param);

        log.info("accessTokenJson:{}", accessTokenJson);
        JSONObject accessTokenJsonObj = JSON.parseObject(accessTokenJson);
        LAST_GETTOKEN_TIME = System.currentTimeMillis();
        NRedisUtil.set("wx_access_token", accessTokenJsonObj.getString("access_token"), 7100);
        return accessTokenJsonObj.getString("access_token");
    }

    public Boolean wxSignature(String timestamp, String nonce, String signature) {
        String[] paramArr = new String[]{weChatAccountConfig.getWX_TOKEN(),timestamp,nonce};
        Arrays.sort(paramArr);
        //将排序后的结果拼成一个字符串
        String content = paramArr[0].concat(paramArr[1]).concat(paramArr[2]);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1").digest(content.getBytes());
            content = byteToStr(digest);
            log.info("our String:{},others:{}",content,signature);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return content.equals(signature);
    }


    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param byteArray
     * @return
     */
    private static String byteToStr(byte[] byteArray) {
        String strDigest = "";
        for (int i = 0; i < byteArray.length; i++) {
            strDigest += byteToHexStr(byteArray[i]);
        }
        return strDigest;
    }

    /**
     * 将字节转换为十六进制字符串
     *
     * @param mByte
     * @return
     */
    private static String byteToHexStr(byte mByte) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];

        String s = new String(tempArr);
        return s;
    }

    /**
     * 发送微信公众号模板消息
     *
     * @param wxTempMsg 模板消息对象
     * @param openId    接收人id
     */
    public String sendTemplateMsg(WxTempMsg wxTempMsg, String openId) {
        WxMpTemplateMessage templateMessage = new WxMpTemplateMessage();
        //模板id
        templateMessage.setTemplateId(wxTempMsg.getTempMsgId());
        templateMessage.setToUser(openId);
        templateMessage.setData(wxTempMsg.getTempData());
        templateMessage.setUrl(wxTempMsg.getUrl());
        try {
            String dage = wwxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            log.info("消息id：{}", dage);
            return HttpStatusEnum.OK.getDescription();
        } catch (WxErrorException e) {
            log.info("公众号消息发送失败：wxtempmsg:{},exception:{}", JsonUtils.fromObject(templateMessage), e);
            return HttpStatusEnum.Fail.getDescription();
        }
    }

    /**
     * 发送微信公众号消息
     *
     * @param openId 接收人openid
     */
    public void sendTemplateMsg(String openId,String content) {
        String sendWxMsgUrl = String.format(SUBSCRIBE_URL, getAccessToken());
        Map param = new HashedMap();
        Map contentMap = new HashedMap();
        param.put("touser",openId);
        param.put("msgtype","text");
        contentMap.put("content",content);
        param.put("text",contentMap);
        net.sf.json.JSONObject result = JsonUtils.fromObject(HttpClientUtil.doPostJson(sendWxMsgUrl, JSONObject.toJSONString(param)));
        log.info("自动消息回复：{}",result);
    }

    /**
     * 创建菜单
     *
     * @param menu 菜单实例
     * @return 0表示成功，其他值表示失败
     */
    public int createMenu(Menu menu) {
        int result = 0;
        // 拼装创建菜单的url
        String url = String.format(MENU_CREATE_URL, getAccessToken());
        // 将菜单对象转换成json字符串
        String jsonMenu = JSONObject.toJSON(menu).toString();
        // 调用接口创建菜单
        net.sf.json.JSONObject jsonObject = JsonUtils.fromObject(HttpClientUtil.doPostJson(url, jsonMenu));
        if (null != jsonObject) {
            if (0 != jsonObject.getInt("errcode")) {
                result = jsonObject.getInt("errcode");
                log.error("创建菜单失败 errcode:{} errmsg:{}", result, jsonObject.getString("errmsg"));
            }
        }

        return result;
    }

    /**
     * 用户关注公众号自动回复
     */
    public String sendWxAutomaticAttentionMsg(String toUserName,String openId) {
        log.info("自动回复：{}",toUserName);
        xml xml = new xml();
        xml.setContent(USER_SUBSCRIBE_WX);
        xml.setCreateTime(DateUtil.formatDateNormal(new Date()));
        xml.setFromUserName(toUserName);
        xml.setMsgType("text");
        xml.setToUserName(openId);
        return XMLConverUtil.convertToXML(xml);
    }

    /**
     * 用户发送带？消息，自动回复
     */
    public String sendWxAutomaticUserMsg(String toUserName,String openId) {
        WxImageXml xml = new WxImageXml();
        WxImage wxImage = new WxImage();
        wxImage.setMediaId(weChatAccountConfig.getIMAGE_ID());
        xml.setWxImage(wxImage);
        xml.setCreateTime(DateUtil.formatDateNormal(new Date()));
        xml.setFromUserName(toUserName);
        xml.setMsgType("image");
        xml.setToUserName(openId);
        return XMLConverUtil.convertToXML(xml);
    }

    public String dealDiffEvent(Map wxParam, String openId) {
        String wx_token = (String) NRedisUtil.get(WXTOKEN);
        switch (String.valueOf(wxParam.get("Event"))) {
            // 公众号推送回调事件
            case Constants.TEMPLATESENDJOBFINISH:
                if (Constants.STATUS_SUCCESS.equals(wxParam.get("Status"))) {
                    break;
                }
                // 点击关注回调事件类型
            case Constants.SUBSCRIBE:
                String ret = sendWxAutomaticAttentionMsg(String.valueOf(wxParam.get("ToUserName")), openId);
                openId = openId + ":0";
                NRedisUtil.set(wx_token, openId, 300);
                return ret;
            // 已关注 扫码回调事件类型
            case Constants.SCAN:
                openId = openId + ":1";
                NRedisUtil.set(wx_token, openId, 300);
                break;
            case Constants.CLICK:
//                    messageService.sendWxTempMsg(new SendMsgSignupPwd("1","1","1","1"),227L,"123","2",null);
                break;
            case Constants.UNSUBSCRIBE:
                break;
            default:
                break;
        }
        if (!EmptyUtils.isEmpty(wxParam.get("MsgType"))) {
            String msgType = String.valueOf(wxParam.get("MsgType"));
            log.info("公众号收到的用户消息：{}", String.valueOf(wxParam.get("Content")));
            if (Constants.TEXT.equals(msgType) && (String.valueOf(wxParam.get("Content")).contains("？") ||
                    (String.valueOf(wxParam.get("Content")).contains("?")))) {
                return sendWxAutomaticUserMsg(String.valueOf(wxParam.get("ToUserName")), openId);
            }
        }
        return "";
    }

    /**
     * 上传永久素材
     *
     * @param file
     * @param type
     * @param title        type为video时需要,其他类型设null
     * @param introduction type为video时需要,其他类型设null
     * @return {"media_id":MEDIA_ID,"url":URL}
     */
    public String uploadPermanentMaterial(ClassPathResource file, String type, String title, String introduction) {

        String url = String.format(UPLOAD_FILE, getAccessToken(), type);
        String result = null;

        try {
            URL uploadURL = new URL(url);

            HttpURLConnection conn = (HttpURLConnection) uploadURL.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Charset", "UTF-8");
            String boundary = "-----------------------------" + System.currentTimeMillis();
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream output = conn.getOutputStream();
            output.write(("--" + boundary + "\r\n").getBytes("utf-8"));
            output.write(String.format("Content-Disposition: form-data; name=\"media\";filelength=\"%s\";filename=\"%s\"\r\n", file.contentLength(), "image.png").getBytes("utf-8"));
            output.write("Content-Type: application/octet-stream\r\n\r\n".getBytes("utf-8"));

            byte[] data = new byte[1024];
            int len = 0;
            InputStream inputStream = file.getInputStream();
            while ((len = inputStream.read(data)) > -1) {
                output.write(data, 0, len);
            }

            /*对类型为video的素材进行特殊处理*/
            if ("video".equals(type)) {
                output.write(("--" + boundary + "\r\n").getBytes());
                output.write("Content-Disposition: form-data; name=\"description\";\r\n\r\n".getBytes());
                output.write(String.format("{\"title\":\"%s\", \"introduction\":\"%s\"}", title, introduction).getBytes());
            }

            output.write(("\r\n--" + boundary + "--\r\n").getBytes("utf-8"));
            output.flush();
            output.close();
            inputStream.close();

            InputStream resp = conn.getInputStream();

            StringBuffer sb = new StringBuffer();

            while ((len = resp.read(data)) > -1) {
                sb.append(new String(data, 0, len, "utf-8"));
            }
            resp.close();
            result = sb.toString();
        } catch (IOException e) {
            //....
            log.info("error:{}", e.getCause());
        }

        return result;
    }

}

