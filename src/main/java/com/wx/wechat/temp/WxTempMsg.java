package com.wx.wechat.temp;

import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;

import java.util.List;

public interface WxTempMsg {
    /**
     *  获取消息名称
     */
    String getMsgName();
    /**
     *  获取微信模板ID
     */
    String getTempMsgId();
    /**
     *  获取微信模板内容
     */
    List<WxMpTemplateData> getTempData();
    /**
     *  获取每小时限制次数
     */
    Integer getSendTimes();
    /**
     *  获取获取消息优先级
     */
    Integer getPriority();
    /**
     *  获取跳转链接
     */
    String getUrl();
}
