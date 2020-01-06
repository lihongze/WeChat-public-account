package com.wx.wechat.constant;

/**
 * 枚举类
 */
public enum HttpStatusEnum {

    OK(200, "success"),
    Fail(500, "fail"),

    //微信相关
    USER_NOT_SINGIN(88000,"该用户未绑定微信号"),
    USER_HAS_SINGIN(88001,"已成功绑定");

    private int code;
    private String description;

    HttpStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static String getDescription(int code) {
        for (HttpStatusEnum httpStatusEnum : HttpStatusEnum.values()) {
            if (httpStatusEnum.code == code) {
                return httpStatusEnum.description;
            }
        }
        return "";
    }

    public static HttpStatusEnum getEnumByCode(int code) {
        for (HttpStatusEnum httpStatusEnum : HttpStatusEnum.values()) {
            if (httpStatusEnum.getCode() == code) {
                return httpStatusEnum;
            }
        }
        return null;
    }
}
