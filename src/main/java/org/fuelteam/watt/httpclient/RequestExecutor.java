package org.fuelteam.watt.httpclient;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;

import com.alibaba.fastjson.JSON;

public class RequestExecutor<T extends HttpRequestBase> {

    private T t;

    public RequestExecutor<T> build(Class<T> clazz) {
        try {
            t = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return this;
    }

    public RequestExecutor<T> on(String uri, Map<String, String> params, Map<String, String> headers,
            Map<String, Object> fields) {
        String url = uri;
        if (!isNull(params)) url += buildUrlParams(params);
        t.setURI(URI.create(url));
        if (!isNull(headers)) {
            for (String key : headers.keySet()) {
                t.addHeader(key, headers.get(key));
            }
        }
        if (fields == null || fields.isEmpty()) return this;
        if (t instanceof HttpPost) {
            HttpPost post = (HttpPost) t;
            post.setEntity(new StringEntity(JSON.toJSONString(fields), Consts.UTF_8));
        }
        return this;
    }

    public String string(Cookie[] cookies) throws Exception {
        try (CloseableHttpResponse response = RequestClientBuilder.build().get(cookies).execute(t)) {
            return Utf8ResponseHandler.INSTANCE.handleResponse(response);
        } finally {
            t.releaseConnection();
        }
    }

    public String string() throws Exception {
        try (CloseableHttpResponse response = RequestClientBuilder.build().get(null).execute(t)) {
            return Utf8ResponseHandler.INSTANCE.handleResponse(response);
        } finally {
            t.releaseConnection();
        }
    }

    public InputStream stream(Cookie[] cookies) throws Exception {
        try (CloseableHttpResponse response = RequestClientBuilder.build().get(cookies).execute(t)) {
            return StreamResponseHandler.INSTANCE.handleResponse(response);
        } finally {
            t.releaseConnection();
        }
    }

    public InputStream stream() throws Exception {
        try (CloseableHttpResponse response = RequestClientBuilder.build().get(null).execute(t)) {
            return StreamResponseHandler.INSTANCE.handleResponse(response);
        } finally {
            t.releaseConnection();
        }
    }

    public RequestExecutor<T> timeout(int connectionTimeout, int soTimeout) {
        // connectionTimeout 连接上服务器(握手成功)的时间，默认5000ms，零为一直等待，负数为httpclient的默认设置
        // soTimeout 服务器返回数据时间，默认5000ms
        t.setConfig(RequestConfig.custom().setSocketTimeout(soTimeout).setConnectTimeout(connectionTimeout).build());
        return this;
    }

    private static <K, V> boolean isNull(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    private static String buildUrlParams(Map<String, String> queryParams) {
        if (isNull(queryParams)) return "";
        StringBuffer param = new StringBuffer();
        int i = 0;
        for (String key : queryParams.keySet()) {
            if (i == 0) {
                param.append("?");
            } else {
                param.append("&");
            }
            param.append(key).append("=").append(queryParams.get(key));
            i++;
        }
        return param.toString();
    }
}