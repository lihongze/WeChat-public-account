package com.wx.wechat.dao;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class WxImage {
    @XmlJavaTypeAdapter(CDataAdapter.class)
    @XmlElement(name = "MediaId")
    private String MediaId;
}
