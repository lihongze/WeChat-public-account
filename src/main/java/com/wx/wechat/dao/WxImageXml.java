package com.wx.wechat.dao;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@Data
@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class WxImageXml {
    @XmlJavaTypeAdapter(CDataAdapter.class)
    @XmlElement(name = "ToUserName")
    private String ToUserName;
    @XmlJavaTypeAdapter(CDataAdapter.class)
    @XmlElement(name = "FromUserName")
    private String FromUserName;
    @XmlElement(name = "CreateTime")
    private String CreateTime;
    @XmlJavaTypeAdapter(CDataAdapter.class)
    @XmlElement(name = "MsgType")
    private String MsgType;
    @XmlElement(name = "Image")
    private WxImage wxImage;

}
