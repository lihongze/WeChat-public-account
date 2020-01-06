package com.wx.wechat.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wx.wechat.Utils.EmptyUtils;
import com.wx.wechat.Utils.HttpClientUtil;
import com.wx.wechat.Utils.JsonUtils;
import com.wx.wechat.Utils.NRedisUtil;
import com.wx.wechat.config.WeChatAccountConfig;
import com.wx.wechat.constant.HttpStatusEnum;
import com.wx.wechat.dao.Menu;
import com.wx.wechat.temp.WxTempMsg;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

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
    private static final String OURWORK_WECHAT_TICKET = "OURWORK_WECHAT_TICKET";
    private static final long EXPIRES_IN_SECONDS = 7000;

    public net.sf.json.JSONObject getAccessTokenByCode(String code) {
        String getAccTokenUrl = String.format(GET_ACCTOKEN_BY_CODE, weChatAccountConfig.getAPP_ID(), weChatAccountConfig.getAPP_SECRET(), code);
        return JsonUtils.fromObject(HttpClientUtil.doGet(getAccTokenUrl));
    }

    public String getAccessToken() {
        String access_token = String.valueOf(NRedisUtil.get("wx_access_token"));
        log.info("accessTokenJson:{}", access_token);
        if (!EmptyUtils.isEmpty(access_token) && !"null".equals(access_token)) {
            return access_token;
        }
        Map<String, String> param = new HashMap<String, String>();
        param.put("grant_type", "client_credential");
        param.put("appid", weChatAccountConfig.getAPP_ID());
        param.put("secret", weChatAccountConfig.getAPP_SECRET());
        String accessTokenJson = HttpClientUtil.doGet(ACCESS_TOKEN_URL, param);

        log.info("accessTokenJson:{}", accessTokenJson);
        JSONObject accessTokenJsonObj = JSON.parseObject(accessTokenJson);
        NRedisUtil.set("wx_access_token", accessTokenJsonObj.getString("access_token"), 7100);
        return accessTokenJsonObj.getString("access_token");
    }

    public Boolean wxSignature(String timestamp, String nonce, String signature) {
        String ourSignature = weChatAccountConfig.getWX_TOKEN() + timestamp + nonce;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1").digest(ourSignature.getBytes());
            ourSignature = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return ourSignature.equals(signature);
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


}

