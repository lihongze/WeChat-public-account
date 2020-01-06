package com.wx.wechat.Utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * HttpClient工具类
 *
 */
@Slf4j
public class HttpClientUtil {
    public static CloseableHttpClient createClient() throws Exception {
        TrustStrategy trustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] xc, String msg)
                    throws CertificateException {
                return true;
            }
        };
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(trustStrategy);
        HostnameVerifier hostnameVerifierAllowAll = new HostnameVerifier() {
            @Override
            public boolean verify(String name, SSLSession session) {
                return true;
            }
        };
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                builder.build(), new String[]{"SSLv2Hello", "SSLv3", "TLSv1",
                "TLSv1.1", "TLSv1.2"}, null, hostnameVerifierAllowAll);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)//设置30秒超时
                .setSocketTimeout(5000)//超时设置
                .build();
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setDefaultRequestConfig(requestConfig)
                .build();
        return httpclient;
    }

    public static String doGet(String url, Map<String, String> param) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClientUtil.createClient();
        } catch (Exception e) {
            log.warn("创建httpclient出错！");
            httpClient = HttpClients.createDefault();
        }
        String resultString = "";
        CloseableHttpResponse response = null;
        try {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            if (param != null) {
                for (String key : param.keySet()) {
                    builder.addParameter(key, param.get(key));
                }
            }
            URI uri = builder.build();

            // 创建http GET请求
            HttpGet httpGet = new HttpGet(uri);
            //设置20秒超时
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build();//设置请求和传输超时时间
            httpGet.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            log.warn("Get请求失败", e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException e) {
                log.warn("response关闭失败", e);
            }
        }
        return resultString;
    }

    public static void close(Object httpClient, Object response) {
        try {
            if (response != null) {
                ((CloseableHttpResponse) response).close();
            }
            ((CloseableHttpClient) httpClient).close();
        } catch (IOException e) {
            log.warn("response关闭失败", e);
        }
    }

    public static Map<String, Object> doGet(String url, Map<String, String> param, Map<String, String> headerMap) {
        log.info("url：{}", url);

        Map<String, Object> returnMap = new HashMap();
        // 创建Httpclient对象
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClientUtil.createClient();
        } catch (Exception e) {
            log.error("创建httpclient出错！");
//            httpClient = HttpClients.createDefault();
            return null;
        }
        InputStream inputStreamRes = null;
        CloseableHttpResponse response = null;

        URIBuilder builder = null;
        try {
            builder = new URIBuilder(url);
        } catch (URISyntaxException e) {
            log.info("url后缀没有进行编码");
            String[] split = url.split("\\?");
            log.info("--{}", split);
            if (split.length != 2) return null;
            try {
                url = split[0] + "?" + URLEncoder.encode(split[1], "utf-8");
                log.info("编码后url为:{}", url);
            } catch (UnsupportedEncodingException e1) {
                log.info("url后缀编码错误", e);
                return null;
            }
            try {
                builder = new URIBuilder(url);
            } catch (URISyntaxException e1) {
                log.info("后缀编码后仍报错", e);
                return null;
            }
        }
        try {
//            if (url.contains(""))
//            URL urlObj = new URL(url);
//            URI uriObj = new URI(url);
//            URIBuilder builder = new URIBuilder(url);
//            // 创建uri
////            URIBuilder builder = new URIBuilder(url);
            if (param != null) {
                for (String key : param.keySet()) {
                    builder.addParameter(key, param.get(key));
                }
            }
            URI uri = builder.build();

            // 创建http GET请求
            HttpGet httpGet = new HttpGet(uri);
            if (headerMap != null) {
                for (String key : headerMap.keySet()) {
                    httpGet.setHeader(key, headerMap.get(key));
                }
            }
            //设置20秒超时
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build();//设置请求和传输超时时间
            httpGet.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                inputStreamRes = new ByteArrayInputStream(EntityUtils.toByteArray(entity));

            }
        } catch (Exception e) {
            log.warn("报错图片url为:{}", url);
            log.warn("Get请求失败", e);
            return null;
        }

        returnMap.put("inStream", inputStreamRes);
        returnMap.put("client", httpClient);
        returnMap.put("response", response);
        return returnMap;
    }

    public static String doGet(String url) {
        return doGet(url, null);
    }

    public static String doPost(String url, Map<String, String> param) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClientUtil.createClient();
        } catch (Exception e) {
            log.warn("创建httpclient出错！");
            httpClient = HttpClients.createDefault();
        }
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            // 创建参数列表
            if (param != null) {
                List<NameValuePair> paramList = new ArrayList<>();
                for (String key : param.keySet()) {
                    paramList.add(new BasicNameValuePair(key, param.get(key)));
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            log.warn("Post请求失败", e);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                log.warn("response关闭失败", e);
            }
        }

        return resultString;
    }

    public static String doPost(String url) {
        return doPost(url, null);
    }


    public static String doPostJson(String url, String json,Map<String, String> headerMap) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClientUtil.createClient();
        } catch (Exception e) {
            log.warn("创建httpclient出错！");
            httpClient = HttpClients.createDefault();
        }
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            // 添加header
            Iterator headerIterator = headerMap.entrySet().iterator();
            while (headerIterator.hasNext()) {
                Map.Entry<String, String> elem = (Map.Entry<String, String>) headerIterator.next();
                httpPost.addHeader(elem.getKey(), elem.getValue());
            }

            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);


            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            log.warn("Post请求失败", e);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                log.warn("response关闭失败", e);
            }
        }

        return resultString;
    }

    public static String doPostJson(String url, String json) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClientUtil.createClient();
        } catch (Exception e) {
            log.warn("创建httpclient出错！");
            httpClient = HttpClients.createDefault();
        }
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);


            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            log.warn("Post请求失败", e);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                log.warn("response关闭失败", e);
            }
        }

        return resultString;
    }

    public static boolean doGetCheck(String url) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String resultString = "";
        CloseableHttpResponse response = null;
        try {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);

            URI uri = builder.build();

            // 创建http GET请求
            HttpGet httpGet = new HttpGet(uri);
            //设置20秒超时
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(500).setConnectTimeout(500).build();//设置请求和传输超时时间
            httpGet.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            }
        } catch (Exception e) {
            log.warn("Get请求失败", e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException e) {
                log.warn("response关闭失败", e);
            }
        }
        return false;
    }

    public static String doPost(String url, Map<String, String> headerMap, Map<String, String> contentMap) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClientUtil.createClient();
        } catch (Exception e) {
            log.error("创建httpclient出错！");
            httpClient = HttpClients.createDefault();
        }
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);


            // 添加header
            Iterator headerIterator = headerMap.entrySet().iterator();
            while (headerIterator.hasNext()) {
                Map.Entry<String, String> elem = (Map.Entry<String, String>) headerIterator.next();
                httpPost.addHeader(elem.getKey(), elem.getValue());
            }

            // 添加body
            // 创建参数列表
            if (contentMap != null) {
                List<NameValuePair> paramList = new ArrayList<>();
                for (String key : contentMap.keySet()) {
                    paramList.add(new BasicNameValuePair(key, contentMap.get(key)));
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            log.warn("Post请求失败", e);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                log.warn("response关闭失败", e);
            }
        }

        return resultString;
    }

    public static Map<String, Object> doPostRTMap(String url, Map<String, String> param) {
        Map<String, Object> returnMap = new HashMap<>();
        // 创建Httpclient对象
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClientUtil.createClient();
        } catch (Exception e) {
            log.warn("创建httpclient出错！");
            httpClient = HttpClients.createDefault();
        }
        CloseableHttpResponse response = null;
        InputStream inputStreamRes = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 创建参数列表
            if (param != null) {
                List<NameValuePair> paramList = new ArrayList<>();
                for (String key : param.keySet()) {
                    paramList.add(new BasicNameValuePair(key, param.get(key)));
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }
            // 执行http请求
            response = httpClient.execute(httpPost);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                inputStreamRes = new ByteArrayInputStream(EntityUtils.toByteArray(entity));
            }
        } catch (Exception e) {
            log.warn("Post请求失败", e);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                log.warn("response关闭失败", e);
            }
        }
        returnMap.put("inStream", inputStreamRes);
        returnMap.put("client", httpClient);
        returnMap.put("response", response);
        return returnMap;
    }
}
