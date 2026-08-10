/*
 * Copyright 2018 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.ssl;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.security.cert.Certificate;
import java.util.Map;

/**
 * {@link SSLSession} that is specific to our native implementation.
 *
 * <p>扩展 {@link OpenSslSession} 的包内接口：握手前清理敏感数据、从会话缓存填充元数据、
 * 动态扩大应用缓冲区、握手完成时写入 peer 证书等 native 实现细节。</p>
 */
interface OpenSslInternalSession extends OpenSslSession {

    /**
     * Called on a handshake session before being exposed to a {@link javax.net.ssl.TrustManager}.
     * Session data must be cleared by this call.
     * <p>握手会话暴露给 {@link javax.net.ssl.TrustManager} 前调用，须清空临时会话数据以防泄露。</p>
     */
    void prepareHandshake();

    /**
     * Return the {@link OpenSslSessionId} that can be used to identify this session.
     * <p>返回可用于缓存查找的 {@link OpenSslSessionId}。</p>
     */
    OpenSslSessionId sessionId();

    /**
     * Set the local certificate chain that is used. It is not expected that this array will be changed at all
     * and so its ok to not copy the array.
     * <p>设置本地证书链引用（不拷贝数组，调用方须保证不变性）。</p>
     */
    void setLocalCertificate(Certificate[] localCertificate);

    /**
     * Set the details for the session which might come from a cache.
     * <p>从 {@link OpenSslSessionCache} 命中时批量写入创建时间、最后访问时间、会话 ID 与键值存储。</p>
     *
     * @param creationTime the time at which the session was created.
     * @param lastAccessedTime the time at which the session was last accessed via the session infrastructure (cache).
     * @param id the {@link OpenSslSessionId}
     * @param keyValueStorage the key value store. See {@link #keyValueStorage()}.
     */
    void setSessionDetails(long creationTime, long lastAccessedTime, OpenSslSessionId id,
                           Map<String, Object> keyValueStorage);

    /**
     * Return the underlying {@link Map} that is used by the following methods:
     *
     * <ul>
     *     <li>{@link #putValue(String, Object)}</li>
     *     <li>{@link #removeValue(String)}</li>
     *     <li>{@link #getValue(String)}</li>
     *     <li> {@link #getValueNames()}</li>
     * </ul>
     *
     * The {@link Map} must be thread-safe!
     * <p>供 {@code putValue}/{@code getValue} 等 SSLSession API 使用的线程安全属性表。</p>
     *
     * @return storage
     */
    Map<String, Object> keyValueStorage();

    /**
     * Set the last access time which will be returned by {@link #getLastAccessedTime()}.
     * <p>更新最后访问时间（会话复用或缓存命中时）。</p>
     *
     * @param time the time
     */
    void setLastAccessedTime(long time);

    /**
     * Expand (or increase) the value returned by {@link #getApplicationBufferSize()} if necessary.
     * <p>
     * This is only called in a synchronized block, so no need to use atomic operations.
     * <p>当单次解密明文超过当前应用缓冲区大小时扩容；仅在同步块内调用。</p>
     * @param packetLengthDataOnly The packet size which exceeds the current {@link #getApplicationBufferSize()}.
     */
    void tryExpandApplicationBufferSize(int packetLengthDataOnly);

    /**
     * Called once the handshake has completed.
     * <p>握手成功结束时写入 master secret id、协商套件/协议、对端证书链及超时等最终会话状态。</p>
     */
    void handshakeFinished(byte[] id, String cipher, String protocol, byte[] peerCertificate,
                           byte[][] peerCertificateChain, long creationTime, long timeout) throws SSLException;
}
