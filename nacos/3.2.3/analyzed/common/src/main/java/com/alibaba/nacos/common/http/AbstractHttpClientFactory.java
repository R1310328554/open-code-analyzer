/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.http;

import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.http.client.NacosAsyncRestTemplate;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.http.client.request.DefaultAsyncHttpClientRequest;
import com.alibaba.nacos.common.http.client.request.JdkHttpClientRequest;
import com.alibaba.nacos.common.tls.SelfHostnameVerifier;
import com.alibaba.nacos.common.tls.TlsFileWatcher;
import com.alibaba.nacos.common.tls.TlsHelper;
import com.alibaba.nacos.common.tls.TlsSystemConfig;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.protocol.RequestContent;
import org.apache.hc.core5.reactor.DefaultConnectingIOReactor;
import org.apache.hc.core5.reactor.IOEventHandler;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * AbstractHttpClientFactory Let the creator only specify the http client config.
 * <p>HTTP 客户端工厂抽象基类：子类仅需实现 {@link #buildHttpClientConfig()} 与 {@link #assignLogger()}，本类负责创建 JDK/Apache 同步与异步 {@link com.alibaba.nacos.common.http.client.NacosRestTemplate} 及 TLS 初始化。</p>
 *
 * @author mai.jh
 */
public abstract class AbstractHttpClientFactory implements HttpClientFactory {
    
    /** 异步 HTTP 客户端工作线程名前缀 */
    private static final String ASYNC_THREAD_NAME = "nacos-http-async-client";
    
    /** I/O Reactor 专用线程名前缀 */
    private static final String ASYNC_IO_REACTOR_NAME = ASYNC_THREAD_NAME + "#I/O Reactor";
    
    @Override
    public NacosRestTemplate createNacosRestTemplate() {
        HttpClientConfig httpClientConfig = buildHttpClientConfig();
        final JdkHttpClientRequest clientRequest = new JdkHttpClientRequest(httpClientConfig);
        
        // 若启用 TLS，加载 SSLContext 并设置主机名校验
        initTls((sslContext, hostnameVerifier) -> {
            clientRequest.setSslContext(loadSslContext());
            clientRequest.replaceSslHostnameVerifier(hostnameVerifier);
        }, filePath -> clientRequest.setSslContext(loadSslContext()));
        
        return new NacosRestTemplate(assignLogger(), clientRequest);
    }
    
    @Override
    public NacosAsyncRestTemplate createNacosAsyncRestTemplate() {
        final IOReactorConfig ioReactorConfig = getIoReactorConfig();
        final HttpClientConfig originalRequestConfig = buildHttpClientConfig();
        final DefaultConnectingIOReactor ioreactor = getIoReactor(ASYNC_IO_REACTOR_NAME);
        final RequestConfig defaultConfig = getRequestConfig();
        final AsyncClientConnectionManager connectionManager =
            getConnectionManager(originalRequestConfig);
        monitorAndExtension(connectionManager);
        
        // issue#12028：升级至 HttpClient 5 异步客户端
        return new NacosAsyncRestTemplate(assignLogger(), new DefaultAsyncHttpClientRequest(
            HttpAsyncClients.custom()
                .addRequestInterceptorLast(new RequestContent(true))
                .setThreadFactory(new NameThreadFactory(ASYNC_THREAD_NAME))
                .setIOReactorConfig(ioReactorConfig)
                // 在工厂层捕获 I/O Reactor 异常，避免 Reactor 被未知网络错误终止
                .setIoReactorExceptionCallback((ex) -> {
                
                })
                .setDefaultRequestConfig(defaultConfig)
                .setUserAgent(originalRequestConfig.getUserAgent())
                .setConnectionManager(connectionManager)
                .build(),
            ioreactor, defaultConfig));
    }
    
    private DefaultConnectingIOReactor getIoReactor(String threadName) {
        return new DefaultConnectingIOReactor(
            (session, ojb) -> new IOEventHandler() {
                
                @Override
                public void connected(IOSession ioSession) throws IOException {
                    
                }
                
                @Override
                public void inputReady(IOSession ioSession, ByteBuffer byteBuffer)
                    throws IOException {
                    
                }
                
                @Override
                public void outputReady(IOSession ioSession) throws IOException {
                    
                }
                
                @Override
                public void timeout(IOSession ioSession, Timeout timeout) throws IOException {
                    
                }
                
                @Override
                public void exception(IOSession ioSession, Exception e) {
                    
                }
                
                @Override
                public void disconnected(IOSession ioSession) {
                    
                }
            },
            getIoReactorConfig(),
            new NameThreadFactory(threadName),
            null,
            // I/O Reactor 层异常回调：IO/运行时异常降级为 WARN
            (ex) -> {
                if (ex instanceof IOException) {
                    assignLogger()
                        .warn("[AsyncClientConnectionManager] handle IOException, ignore it.", ex);
                } else if (ex instanceof RuntimeException) {
                    assignLogger().warn(
                        "[AsyncClientConnectionManager] handle RuntimeException, ignore it.", ex);
                } else {
                    assignLogger().error(
                        "[DefaultConnectingIOReactor] Exception! I/O Reactor error time: {}",
                        System.currentTimeMillis(), ex.getCause());
                }
            },
            null,
            null);
    }
    
    /**
     * create the {@link AsyncClientConnectionManager}, the code mainly from {@link PoolingAsyncClientConnectionManagerBuilder#build()}. we
     * add the {@link Callback} to handle the {@link IOException} and {@link RuntimeException} thrown
     * by the {@link DefaultConnectingIOReactor} when process the event of Network. Using this way
     * to avoid the {@link DefaultConnectingIOReactor} killed by unknown error of network.
     *
     * @param originalRequestConfig request config.
     * @return {@link AsyncClientConnectionManager}.
      * <p>HTTP 客户端工厂基类；详见类说明。</p>
     */
    private AsyncClientConnectionManager getConnectionManager(
        HttpClientConfig originalRequestConfig) {
        try {
            SSLContext sslcontext = SSLContext.getDefault();
            HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();
            TlsStrategy sslStrategy = new DefaultClientTlsStrategy(sslcontext, hostnameVerifier);
            // HttpClient 5：连接管理器不再依赖外部 IOReactor
            return PoolingAsyncClientConnectionManagerBuilder
                // 默认已注册 http 策略，等价于旧版 NoopIOSessionStrategy
                .create()
                // 等价于旧版 Registry.register("https", sslStrategy)
                .setTlsStrategy(sslStrategy)
                // 最大连接总数可在 builder 上配置
                .setMaxConnTotal(originalRequestConfig.getMaxConnTotal())
                // 每路由最大连接数可在 builder 上配置
                .setMaxConnPerRoute(originalRequestConfig.getMaxConnPerRoute())
                .build();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected IOReactorConfig getIoReactorConfig() {
        HttpClientConfig httpClientConfig = buildHttpClientConfig();
        return IOReactorConfig.custom().setIoThreadCount(httpClientConfig.getIoThreadCount())
            .build();
    }
    
    protected RequestConfig getRequestConfig() {
        HttpClientConfig httpClientConfig = buildHttpClientConfig();
        return RequestConfig
            .custom()
            .setConnectTimeout(httpClientConfig.getConTimeOutMillis(), TimeUnit.MILLISECONDS)
            .setResponseTimeout(httpClientConfig.getReadTimeOutMillis(), TimeUnit.MILLISECONDS)
            .setConnectionRequestTimeout(httpClientConfig.getConnectionRequestTimeout(),
                TimeUnit.MILLISECONDS)
            .setContentCompressionEnabled(httpClientConfig.getContentCompressionEnabled())
            .setMaxRedirects(httpClientConfig.getMaxRedirects()).build();
    }
    
    protected void initTls(BiConsumer<SSLContext, HostnameVerifier> initTlsBiFunc,
        TlsFileWatcher.FileChangeListener tlsChangeListener) {
        if (!TlsSystemConfig.tlsEnable) {
            return;
        }
        
        final HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
        final SelfHostnameVerifier selfHostnameVerifier = new SelfHostnameVerifier(hv);
        
        initTlsBiFunc.accept(loadSslContext(), selfHostnameVerifier);
        
        if (tlsChangeListener != null) {
            try {
                TlsFileWatcher.getInstance()
                    .addFileChangeListener(tlsChangeListener,
                        TlsSystemConfig.tlsClientTrustCertPath,
                        TlsSystemConfig.tlsClientKeyPath);
            } catch (IOException e) {
                assignLogger().error("add tls file listener fail", e);
            }
        }
    }
    
    @SuppressWarnings("checkstyle:abbreviationaswordinname")
    protected synchronized SSLContext loadSslContext() {
        try {
            return TlsHelper.buildSslContext(true);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            assignLogger().error("Failed to create SSLContext", e);
        }
        return null;
    }
    
    /**
     * build http client config.
     * <p>由子类提供 HTTP 客户端超时、连接池等配置。</p>
     *
     * @return HttpClientConfig
     */
    protected abstract HttpClientConfig buildHttpClientConfig();
    
    /**
     * assign Logger.
     * <p>由子类指定本工厂使用的 SLF4J 日志记录器。</p>
     *
     * @return Logger
     */
    protected abstract Logger assignLogger();
    
    /**
     * add some monitor and do some extension. default empty implementation, implemented by subclass
     * <p>子类可覆写：对异步连接管理器做监控或扩展，默认空实现。</p>
     */
    protected void monitorAndExtension(AsyncClientConnectionManager connectionManager) {
    }
}
