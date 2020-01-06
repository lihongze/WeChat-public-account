package com.wx.wechat.Utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.DefaultDefaultValueProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static JsonConfig jsonConfig = new JsonConfig();

    static {
        //不序列化设置为transient的字段
        jsonConfig.setIgnoreTransientFields(true);
        jsonConfig.registerDefaultValueProcessor(Object.class, new DefaultDefaultValueProcessor() {
            public Object getDefaultValue(Class type) {
                return "";
            }
        });

        JsonDateValueProcessor jsonValueProcessor = new JsonDateValueProcessor();
        jsonConfig.registerJsonValueProcessor(Date.class, jsonValueProcessor);
    }

    Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    public static JSONObject fromObject(Object object) {
        return JSONObject.fromObject(object, jsonConfig);
    }

    public static JSONObject fromObject(Object object, JsonConfig jsonConfig) {
        return JSONObject.fromObject(object, jsonConfig);
    }

    /**
     * 将List格式化JSONArray
     *
     * @param list
     * @return
     */
    public static JSONArray parseObject(Collection list) {
        JSONArray retArray = new JSONArray();
        for (Object t : list) {
            JSONObject j = JSONObject.fromObject(t, jsonConfig);
            retArray.add(j);
        }
        return retArray;
    }

    public static JSONArray parseObject(List list, JsonConfig jsonConfig) {
        JSONArray retArray = new JSONArray();
        for (Object t : list) {
            JSONObject j = JSONObject.fromObject(t, jsonConfig);
            retArray.add(j);
        }
        return retArray;
    }

    @SuppressWarnings("unchecked")
    public static <T> T parse(String data, TypeReference<T> typeRef) throws IOException {
        return (T) objectMapper.readValue(data, typeRef);
    }

    public static <T> T parseQuietly(String data, TypeReference<T> typeRef) {
        try {
            return parse(data, typeRef);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parse(String data, Class<T> clazz) throws IOException {
        return (T) objectMapper.readValue(data, clazz);
    }

    public static JsonNode parse(String data) throws IOException {
        return objectMapper.readTree(data);
    }

    public static JsonNode parse(byte[] data) throws IOException {
        return objectMapper.readTree(data);
    }

    public static String writeValue(Object value) throws IOException {
        return objectMapper.writeValueAsString(value);
    }

   /* public static boolean checkRequiredParams(Object obj, List<String> requiredParamNames){
        String status = Constants.STATUS_DATAFORMATERROR;
        JSONObject paramJson = JSONObject.fromObject(obj);
        // 检查必填参数
        if (null != requiredParamNames) {
            for (String paramName : requiredParamNames) {
                if (!getParamByKey(paramJson, paramName)) {
                    return false;
                }
            }
        }
        return true;
    }*/

    /**
     * 从JSON里获取指定的对象
     *
     * @param json
     * @param key
     * @return
     */
    public static boolean getParamByKey(JSONObject json, String key) {
        if (null == json || null == key) {
            return false;
        }
        if (!json.containsKey(key)) {
            return false;
        }
        try {
            Object object = json.get(key);
            if (object != null) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 数组字符串转List 待验证
     *
     * @param str
     * @return
     */
    public static List<String> StringToStrList(String str) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, String.class);
        try {
            List<String> strList = objectMapper.readValue(str, javaType);
            return strList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 数组字符串转List 待验证
     *
     * @param str
     * @return
     */
    public static List<Integer> StringToIntList(String str) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, Integer.class);
        try {
            List<Integer> strList = objectMapper.readValue(str, javaType);
            return strList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
