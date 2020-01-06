package com.wx.wechat.temp.wxTempImpl;

import com.wx.wechat.temp.WxTempMsg;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;

import java.util.Arrays;
import java.util.List;

import static com.wx.wechat.constant.Constants.COLOR_BLUE;

public class WxTempMsgTest implements WxTempMsg {
    private List<WxMpTemplateData> tempData;

    public WxTempMsgTest(String keyword1, String keyword2, String keyword3) {
        this.tempData = Arrays.asList(
                new WxMpTemplateData("first", "您好，模板消息first"),
                new WxMpTemplateData("keyword1", keyword1, COLOR_BLUE),
                new WxMpTemplateData("keyword2", keyword2, COLOR_BLUE),
                new WxMpTemplateData("keyword3", keyword3, COLOR_BLUE),
                new WxMpTemplateData("remark", "您好，模板消息remark")
        );
    }

    @Override
    public String getMsgName() {
        return "模板名称";
    }

    @Override
    public String getTempMsgId() {
        return "此处配置模板id";
    }

    @Override
    public List<WxMpTemplateData> getTempData() {
        return tempData;
    }

    @Override
    public Integer getSendTimes() {
        return 30;
    }

    @Override
    public Integer getPriority() {
        return 1;
    }

    @Override
    public String getUrl() {
        return "此处为模板配置链接";
    }
}
