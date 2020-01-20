package com.wx.wechat.controller;

import com.wx.wechat.Utils.*;
import com.wx.wechat.constant.Constants;
import com.wx.wechat.dao.Button;
import com.wx.wechat.dao.CommonButton;
import com.wx.wechat.dao.ComplexButton;
import com.wx.wechat.dao.Menu;
import com.wx.wechat.service.WechatService;
import com.wx.wechat.temp.wxTempImpl.WxTempMsgTest;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import static com.wx.wechat.constant.Constants.USER_SUBSCRIBE_WX;
import static com.wx.wechat.constant.Constants.WXTOKEN;
import static com.wx.wechat.service.WechatService.*;

@Slf4j
@Controller
@RequestMapping(value = "/we/service")
public class WechatController{
    @Resource
    private WechatService wechatService;
    @Autowired
    private NRedisUtil NRedisUtil;
    @Value("${wechat.appId}")
    public String APP_ID;

    /**
     * 微信获取二维码
     *  此处获取的是带参数的二维码  该参数可以自由定义  后面可以用该参数做其他功能  例如注册绑定之类
     *
     */
    @GetMapping("/login_url")
    @ResponseBody
    public JSONObject loginUrl(@RequestParam(value = "phoneNumber", required = false) String phoneNumber, HttpServletRequest request) {
        String accessToken = wechatService.getAccessToken();
        log.info("token", accessToken);
        String firstUrl = String.format(OPEN_QRCODE_URL, accessToken);
        String jsonParam = "{\"expire_seconds\": 300, \"action_name\": \"QR_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": " + phoneNumber + "}}}";
        JSONObject ticket = JSONObject.fromObject(HttpClientUtil.doPostJson(firstUrl, jsonParam));
        String qrcodeUrl = null;
        try {
            qrcodeUrl = String.format(OPEN_REDIRECT_URL, URLEncoder.encode(String.valueOf(ticket.get("ticket")), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return JsonUtils.fromObject(qrcodeUrl);
    }

    /**
     * 绑定公众号回调接口 (公众号管理平台绑定服务器用的校验接口，上送的token值在配置中，需与公众号界面token配置一致)
     */
    @RequestMapping(value = "/check_wx", method = {RequestMethod.GET})
    @ResponseBody
    public String getUrlSign(@RequestParam String signature,
                             @RequestParam String timestamp,
                             @RequestParam String nonce,
                             @RequestParam String echostr) {
        log.info("绑定服务器调用-----------");
        if (wechatService.wxSignature(timestamp, nonce, signature)) {
            log.info("校验结果：{}", wechatService.wxSignature(timestamp, nonce, signature));
            return echostr;
        } else {
            return "fail";
        }
    }

    /**
     * 微信公众号事件回调接口
     */
    @RequestMapping(value = "/check_wx", method = {RequestMethod.POST})
    @ResponseBody
    public String wxBackMsg(HttpServletRequest request) {
        try {
            Map wxParam = XMLConverUtil.parseXml(request);
            log.info("微信回调信息：{}",JsonUtils.fromObject(wxParam));
            String openId = (String) wxParam.get("FromUserName");
            String[] phoneNumber = String.valueOf(wxParam.get("EventKey")).split("_");
            if (phoneNumber.length == 11) {
                Map param = new HashedMap();
                param.put("openId", openId);
                param.put("phoneNumber", phoneNumber[1]);
//                if (userService.updateUserOpenIdByPhoneNumber(param) < 1) {
                    log.error("用户绑定微信号失败：phoneNumber：{}", phoneNumber);
                    return "";
//                }
//                return "";
            }
            String wxReply = wechatService.dealDiffEvent(wxParam,openId);
            if (!EmptyUtils.isEmpty(wxReply)) {
                return wxReply;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 前端轮询获取用户是否已扫码
     */
    @RequestMapping(value = "/check_is_click", method = {RequestMethod.GET})
    @ResponseBody
    public JSONObject checkIsClick(@RequestParam(required = true) String wx_token, HttpServletRequest request) {
        String openId = String.valueOf(NRedisUtil.get(wx_token));
        if (EmptyUtils.isEmpty(openId)) {
            return JsonUtils.fromObject("未扫码");
        } else {
            if (openId.split(":").length < 2) {
                return JsonUtils.fromObject("未关注");
            }
        }
        String nowOpenId = openId.split(":")[0];
        String type = openId.split(":")[1];
        if ("1".equals(type)) {
            // 若type为1  说明用户已经关注  直接登陆
        }
        return JsonUtils.fromObject("扫码成功");
    }

    /**
     * 获取微信登陆授权信息 （前端获取网页微信授权时用到的参数）
     */
    @RequestMapping(value = "/get_wx_info", method = {RequestMethod.GET})
    @ResponseBody
    public JSONObject getWxInfo(HttpServletRequest request) {
        Map wxParam = new HashedMap();
        wxParam.put("appid", APP_ID);
        wxParam.put("scope", "snsapi_login");
        wxParam.put("state", request.getSession(true).getId());
        return JsonUtils.fromObject(wxParam);
    }

    /**
     * 微信授权登陆接口
     */
    @RequestMapping(value = "/wx_sign_up", method = {RequestMethod.GET})
    @ResponseBody
    public JSONObject wxSignUp(@RequestParam String code, HttpServletRequest request) {
        JSONObject result = wechatService.getAccessTokenByCode(code);
        String openId = String.valueOf(result.get("openid"));
        // 此处用code获取到openid后可以做授权登陆等操作
        return JsonUtils.fromObject("登陆成功");
    }

    /**
     * 发送微信模板消息测试接口
     */
    @RequestMapping(value = "/send_wx_temp", method = {RequestMethod.GET})
    @ResponseBody
    public JSONObject send_wx_temp( HttpServletRequest request) {
        // 此处可用request获取用户信息  拿到openid
        wechatService.sendTemplateMsg(new WxTempMsgTest("111","222","333"),"openId");
        return JsonUtils.fromObject("登陆成功");
    }

    /**
     * 上传图片
     */
    @RequestMapping(value = "/upload_img", method = {RequestMethod.GET})
    @ResponseBody
    public JSONObject uploadImg() {
        // 获取项目resources文件夹下的图片文件，文件名自行修改
        ClassPathResource resource = new ClassPathResource("image.png");
        String code = wechatService.uploadPermanentMaterial(resource, "image", "image", "image");
        log.info("返回内容：{}", code);
        return JsonUtils.fromObject(code);
    }

    /**
     * 设置微信菜单
     */
    @RequestMapping(value = "/set_wx_menu", method = {RequestMethod.GET})
    @ResponseBody
    public JSONObject setWxMenu() {
        int code = wechatService.createMenu(getMenu());
        return JsonUtils.fromObject(code);
    }

    /**
     * 删除微信菜单
     */
    @RequestMapping(value = "/delete_wx_menu", method = {RequestMethod.GET})
    @ResponseBody
    public JSONObject deleteWxMenu() {
        String deleteUrl = String.format(DELETE_WX_MENU, wechatService.getAccessToken());
        JSONObject result = JsonUtils.fromObject(HttpClientUtil.doGet(deleteUrl));
        if ("0".equals(String.valueOf(result.get("errcode")))) {
            return JsonUtils.fromObject("菜单删除成功");
        }
        return JsonUtils.fromObject("菜单删除失败");
    }

    /**
     * 组装菜单数据
     *
     * @return
     */
    private static Menu getMenu() {
        CommonButton btn31 = new CommonButton();
        btn31.setName("主菜单1");
        btn31.setType("view");
        btn31.setKey("31");
        btn31.setUrl("https://www.baidu.com");

        CommonButton btn32 = new CommonButton();
        btn32.setName("主菜单2");
        btn32.setType("view");
        btn32.setKey("32");
        btn32.setUrl("https://www.baidu.com");

        CommonButton btn33 = new CommonButton();
        btn33.setName("主菜单3");
        btn33.setType("view");
        btn33.setKey("33");
        btn33.setUrl("https://www.baidu.com");


        /**
         * 微信： mainBtn3底部的三个一级菜单。
         */

        ComplexButton mainBtn1 = new ComplexButton();
        mainBtn1.setType("view");
        mainBtn1.setName("一级菜单1");
        mainBtn1.setUrl("https://www.baidu.com");

        ComplexButton mainBtn2 = new ComplexButton();
        mainBtn2.setType("view");
        mainBtn2.setName("一级菜单2");
        mainBtn2.setUrl("https://www.baidu.com");

        ComplexButton mainBtn3 = new ComplexButton();
        mainBtn3.setName("一级菜单3");
        mainBtn3.setSub_button(new CommonButton[]{btn31, btn32, btn33});


        /**
         * 封装整个菜单
         */
        Menu menu = new Menu();
        menu.setButton(new Button[]{mainBtn1, mainBtn2, mainBtn3});

        return menu;
    }
}


