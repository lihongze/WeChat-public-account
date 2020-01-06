package com.wx.wechat.Utils;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 自定义JsonValueProcessor
 * 比如我们要控制JSON序列化过程中的Date对象的格式化，以及数值的格式化，JsonValueProcessor是最好的选择。
 */
public class JsonDateValueProcessor implements JsonValueProcessor {
    private String pattern = "yyyy-MM-dd HH:mm:ss";

    @Override
    public Object processArrayValue(Object value, JsonConfig config) {
        return process(value);
    }

    @Override
    public Object processObjectValue(String key, Object value, JsonConfig config) {
        return process(value);
    }

    private Object process(Object value) {
        if (value instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.UK);
            return sdf.format(value);
        }
        return value == null ? "" : value.toString();
    }
}