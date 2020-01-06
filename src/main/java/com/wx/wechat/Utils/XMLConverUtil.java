package com.wx.wechat.Utils;

import com.sun.xml.internal.bind.marshaller.CharacterEscapeHandler;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * XML 数据接收对象转换工具类
 *
 * @author LiYi
 *
 */
@Slf4j
public abstract class XMLConverUtil {

    private static Map<Class<?>, JAXBContext> JAXB_CONTEXT_MAP;

    static {
        JAXB_CONTEXT_MAP = new ConcurrentHashMap<Class<?>, JAXBContext>(256);
    }

    /**
     * 解析微信发来的请求（XML）
     *
     * @param request
     * @return
     * @throws Exception
     */
    public static Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        // 将解析结果存储在HashMap中
        Map<String, String> map = new HashMap<String, String>();

        // 从request中取得输入流
        InputStream inputStream = request.getInputStream();

        // 读取输入流
        SAXReader reader = new SAXReader();
        org.dom4j.Document document = reader.read(inputStream);
        String requestXml = document.asXML();
        String subXml = requestXml.split(">")[0]+">";
        requestXml = requestXml.substring(subXml.length());

        // 得到xml根元素
        org.dom4j.Element root = document.getRootElement();

        // 得到根元素的全部子节点
        List<org.dom4j.Element> elementList = root.elements();

        // 遍历全部子节点
        for (org.dom4j.Element e : elementList)  {
            map.put(e.getName(), e.getText());
        }
        map.put("requestXml", requestXml);
        // 释放资源
        inputStream.close();
        inputStream = null;
        return map;

    }

    /**
     * Object to XML
     *
     * @param object
     *            object
     * @return xml
     */
    public static String convertToXML(Object object) {
        try {
            if (!JAXB_CONTEXT_MAP.containsKey(object.getClass())) {
                JAXB_CONTEXT_MAP.put(object.getClass(), JAXBContext.newInstance(object.getClass()));
            }
            Marshaller marshaller = JAXB_CONTEXT_MAP.get(object.getClass()).createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            // 设置CDATA输出字符
            marshaller.setProperty(CharacterEscapeHandler.class.getName(), new CharacterEscapeHandler() {
                @Override
                public void escape(char[] ac, int i, int j, boolean flag, Writer writer) throws IOException {
                    writer.write(ac, i, j);
                }
            });
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(object, stringWriter);
            return stringWriter.getBuffer().toString();
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

}
