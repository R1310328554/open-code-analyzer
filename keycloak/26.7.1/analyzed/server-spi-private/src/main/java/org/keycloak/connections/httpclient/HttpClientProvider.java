/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.connections.httpclient;

import java.io.IOException;
import java.io.InputStream;

import org.keycloak.provider.Provider;

import org.apache.http.impl.client.CloseableHttpClient;

/**
 * HTTP 客户端 SPI 提供者，封装 Apache HttpClient 及常用 GET/POST 辅助方法。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface HttpClientProvider extends Provider {
    /**
     * 返回可自由使用的 {@code CloseableHttpClient}。
     * <p>
     * <b>调用方不得对返回的 {@code HttpClient} 实例调用 {@code close()}。</b>
     * <p>
     * 关闭客户端由本提供者负责；但通过该客户端创建的对象须由调用方正确关闭。
     * @return 共享 HTTP 客户端实例
     */
    CloseableHttpClient getHttpClient();

    /**
     * 向 URI 发送纯文本 POST 请求。
     *
     * @param uri 目标 URI
     * @param text 请求体文本
     * @return HTTP 响应状态码
     * @throws IOException 网络或 I/O 错误
     */
    public int postText(String uri, String text) throws IOException;

    /**
     * 以字符串形式获取 URL 响应体，按响应头选择字符集解码。
     * <p>二进制数据请使用 {@link #getInputStream(String)}。实现应限制读取量以防 {@link OutOfMemoryError}。</p>
     *
     * @param uri 待请求的 URI
     * @return 响应体字符串
     * @throws IOException 网络错误、无内容或非 2xx 状态码
     */
    String getString(String uri) throws IOException;

    /**
     * 以 {@link InputStream} 获取 URL 响应体，适用于二进制数据。
     * <p>调用方须关闭返回流以防资源泄漏。文本内容请使用 {@link #getString(String)}。</p>
     *
     * @param uri 待请求的 URI
     * @return 响应体输入流
     * @throws IOException 网络错误、无内容或非 2xx 状态码
     */
    InputStream getInputStream(String uri) throws IOException;

    /**
     * 辅助方法，委托 {@link #getInputStream(String)}。
     * <p>调用方须关闭返回流以防资源泄漏。</p>
     *
     * @deprecated 字符串请用 {@link #getString(String)}，二进制请用 {@link #getInputStream(String)}，Keycloak 27 移除。
     *
     * @param uri 待请求的 URI
     * @return 响应体输入流
     * @throws IOException 网络错误、无内容或非 2xx 状态码
     */
    @Deprecated
    default InputStream get(String uri) throws IOException {
        return getInputStream(uri);
    }

    /** 默认最大可消费响应体字节数（10 MB）。 */
    long DEFAULT_MAX_CONSUMED_RESPONSE_SIZE = 10_000_000L;

    /**
     * 获取配置的响应体大小上限。
     *
     * @return 最大字节数
     */
    default long getMaxConsumedResponseSize() {
        return DEFAULT_MAX_CONSUMED_RESPONSE_SIZE;
    }

}
