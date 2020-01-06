package com.wx.wechat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WeChatAccountConfig {
    @Value("${wechat.appId}")
    private String APP_ID;

    @Value("${wechat.appSecret}")
    private String APP_SECRET;

    @Value("${wechat.token}")
    private String WX_TOKEN;

    public String getWX_TOKEN() {
        return WX_TOKEN;
    }

    public String getAPP_ID() {
        return APP_ID;
    }

    public String getAPP_SECRET() {
        return APP_SECRET;
    }
}


