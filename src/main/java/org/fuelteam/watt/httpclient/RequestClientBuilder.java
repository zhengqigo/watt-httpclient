package org.fuelteam.watt.httpclient;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.DeflateDecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;

public class RequestClientBuilder {

    private final AtomicBoolean prepared = new AtomicBoolean(false);

    // connectionRequestTimeout 从连接池中获取连接的超时时间，默认3000ms，零为一直等待，负数为httpclient的默认设置
    private int connectionRequestTimeout = 3000;

    // connectionTimeout 连接上服务器(握手成功)的时间，默认5000ms，零为一直等待，负数为httpclient的默认设置
    private int connectionTimeout = 5000;

    // soTimeout 服务器返回数据时间，默认5000ms
    private int soTimeout = 5000;

    // 最大总连接数，默认10
    private int maxTotal = 50;

    // 每路最大链接数，默认10
    private int maxPerRoute = 10;

    // 自定义User Agent
    private String userAgent;

    private Integer retryTimes = 0;

    private SSLConnectionSocketFactory sslConnectionSocketFactory = SSLConnectionSocketFactory.getSocketFactory();

    private PlainConnectionSocketFactory plainConnectionSocketFactory = PlainConnectionSocketFactory.getSocketFactory();

    // 闲置连接监控线程
    private IdleConnectionMonitor idleConnectionMonitor;

    // 持有client对象,仅初始化一次,避免多service实例的时候造成重复初始化的问题
    private CloseableHttpClient closeableHttpClient;

    private CredentialsProvider credentialsProvider;

    private RequestClientBuilder() {/* nothing */}

    public static RequestClientBuilder build() {
        return RequestClientBuilder.SingletonHolder.INSTANCE;
    }

    // 单例模式，持有唯一的CloseableHttpClient，首次调用创建
    private static class SingletonHolder {
        private static final RequestClientBuilder INSTANCE = new RequestClientBuilder();
    }

    public RequestClientBuilder proxy(String host, int port, String username, String passwd) {
        if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(username)) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            AuthScope authScope = new AuthScope(host, port);
            credentialsProvider.setCredentials(authScope, new UsernamePasswordCredentials(username, passwd));
            this.credentialsProvider = credentialsProvider;
        }
        return this;
    }

    public CloseableHttpClient get(Cookie[] cookies) {
        if (!prepared.get()) prepare(cookies);
        return closeableHttpClient;
    }

    // 自定义User Agent
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    private synchronized void prepare(Cookie[] cookies) {
        if (prepared.get()) return;

        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainConnectionSocketFactory).register("https", sslConnectionSocketFactory).build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(maxTotal);
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);

        SocketConfig socketConfig = SocketConfig.copy(SocketConfig.DEFAULT).setSoKeepAlive(true).setTcpNoDelay(true)
                .build();
        connectionManager.setDefaultSocketConfig(socketConfig);

        idleConnectionMonitor = new IdleConnectionMonitor(connectionManager);
        idleConnectionMonitor.setDaemon(true);
        idleConnectionMonitor.start();

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(soTimeout)
                .setConnectTimeout(connectionTimeout).setConnectionRequestTimeout(connectionRequestTimeout).build();

        CookieStore cookieStore = new BasicCookieStore();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                cookieStore.addCookie(cookie);
            }
        }

        HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(connectionManager)
                .setConnectionManagerShared(true).setSSLSocketFactory(buildSSLConnectionSocketFactory())
                .setDefaultRequestConfig(requestConfig).setRetryHandler(retryHandler(retryTimes))
                .setDefaultCookieStore(cookieStore);

        useGzip(httpClientBuilder);

        if (credentialsProvider != null) httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        if (StringUtils.isNotBlank(userAgent)) httpClientBuilder.setUserAgent(userAgent);

        closeableHttpClient = httpClientBuilder.setConnectionManagerShared(true).build();
        prepared.set(true);
    }

    private static void useGzip(HttpClientBuilder httpClientBuilder) {
        httpClientBuilder.addInterceptorFirst((HttpRequestInterceptor) (request, arg1) -> {
            if (!request.containsHeader("Accept-Encoding")) {
                request.addHeader("Accept-Encoding", "gzip,deflate,sdch");
            }
        });
        httpClientBuilder.addInterceptorFirst((HttpResponseInterceptor) (response, arg1) -> {
            Header[] headers = response.getHeaders("Content-Encoding");
            for (Header header : headers) {
                if ("gzip".equals(header.getValue())) {
                    response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                    return;
                } else if ("deflate".equals(header.getValue())) {
                    response.setEntity(new DeflateDecompressingEntity(response.getEntity()));
                    return;
                }
            }
        });
    }

    private HttpRequestRetryHandler retryHandler(int retryTimes) {
        return (IOException exception, int executionCount, HttpContext context) -> {
            if (executionCount >= retryTimes) return false;
            if (exception instanceof InterruptedIOException) return false;
            if (exception instanceof UnknownHostException) return false;
            if (exception instanceof SSLException) return false;
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            return !(request instanceof HttpEntityEnclosingRequest);
        };
    }

    private SSLConnectionSocketFactory buildSSLConnectionSocketFactory() {
        try {
            SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;// 忽略掉对服务器端证书的校验
                }
            }).build();

            return new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}