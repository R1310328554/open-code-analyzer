/*
 * Copyright 2021 The Netty Project
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

import io.netty.internal.tcnative.SSLSession;
import io.netty.internal.tcnative.SSLSessionCache;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import io.netty.util.ResourceLeakTracker;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.SystemPropertyUtil;

import javax.security.cert.X509Certificate;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link SSLSessionCache} implementation for our native SSL implementation.
 *
 * <p>Netty OpenSSL 的外部 {@link SSLSessionCache}：用 {@link LinkedHashMap} 按插入顺序缓存
 * {@code SSL_SESSION*}，配合 tcnative 回调实现会话复用、过期驱逐与容量上限（默认读
 * {@code javax.net.ssl.sessionCacheSize}）。</p>
 */
class OpenSslSessionCache implements SSLSessionCache {
    private static final OpenSslInternalSession[] EMPTY_SESSIONS = new OpenSslInternalSession[0];

    private static final int DEFAULT_CACHE_SIZE;
    static {
        // 与 JDK SSLSessionContext 使用同一系统属性，便于切换实现
        int cacheSize = SystemPropertyUtil.getInt("javax.net.ssl.sessionCacheSize", 20480);
        if (cacheSize >= 0) {
            DEFAULT_CACHE_SIZE = cacheSize;
        } else {
            DEFAULT_CACHE_SIZE = 20480;
        }
    }
    /** SSL* -> {@link ReferenceCountedOpenSslEngine} 映射，native 回调时定位 Java 引擎。 */
    private final OpenSslEngineMap engines;

    /** 会话 ID -> 缓存中的 native 会话；超出容量时在 put 时驱逐最旧条目。 */
    private final Map<OpenSslSessionId, NativeSslSession> sessions =
            new LinkedHashMap<OpenSslSessionId, NativeSslSession>() {

                private static final long serialVersionUID = -7773696788135734448L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<OpenSslSessionId, NativeSslSession> eldest) {
                    int maxSize = maximumCacheSize.get();
                    if (maxSize >= 0 && size() > maxSize) {
                        removeSessionWithId(eldest.getKey());
                    }
                    // 直接在 map 上 remove，LinkedHashMap 的 eldest 机制此处始终返回 false
                    return false;
                }
            };

    /** 缓存条目上限；-1 表示不限制（由 LinkedHashMap eldest 逻辑配合）。 */
    private final AtomicInteger maximumCacheSize = new AtomicInteger(DEFAULT_CACHE_SIZE);

    // Let's use the same default value as OpenSSL does.
    // See https://www.openssl.org/docs/man1.1.1/man3/SSL_get_default_timeout.html
    /** 会话有效时长（秒），默认 300，与 OpenSSL 一致。 */
    private final AtomicInteger sessionTimeout = new AtomicInteger(300);
    /** 新建会话计数，每 255 次触发一次过期清扫（模仿 OpenSSL flush）。 */
    private int sessionCounter;

    OpenSslSessionCache(OpenSslEngineMap engines) {
        this.engines = engines;
    }

    final void setSessionTimeout(int seconds) {
        int oldTimeout = sessionTimeout.getAndSet(seconds);
        if (oldTimeout > seconds) {
            // 缩短超时时清空整个缓存，利用 LinkedHashMap 插入序快速判断是否还有过期项
            clear();
        }
    }

    final int getSessionTimeout() {
        return sessionTimeout.get();
    }

    /**
     * Called once a new {@link OpenSslInternalSession} was created.
     *
     * @param session the new session.
     * @return {@code true} if the session should be cached, {@code false} otherwise.
     *
     * <p>子类可覆盖以过滤不应进入缓存的会话；默认全部缓存。</p>
     */
    protected boolean sessionCreated(NativeSslSession session) {
        return true;
    }

    /**
     * Called once an {@link OpenSslInternalSession} was removed from the cache.
     *
     * @param session the session to remove.
     *
     * <p>会话从缓存移除时的钩子，供子类扩展。</p>
     */
    protected void sessionRemoved(NativeSslSession session) { }

    final void setSessionCacheSize(int size) {
        long oldSize = maximumCacheSize.getAndSet(size);
        if (oldSize > size || size == 0) {
            // 缩小容量或设为 0 时直接清空，实现简单可靠
            clear();
        }
    }

    final int getSessionCacheSize() {
        return maximumCacheSize.get();
    }

    /** 从 LinkedHashMap 头部移除已过期会话（依赖超时缩短时的全量 clear 保证顺序）。 */
    private void expungeInvalidSessions() {
        if (sessions.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<OpenSslSessionId, NativeSslSession>> iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            NativeSslSession session = iterator.next().getValue();
            // LinkedHashMap 按插入顺序遍历，遇到第一个仍有效的会话即可停止
            if (session.isValid(now)) {
                break;
            }
            iterator.remove();

            notifyRemovalAndFree(session);
        }
    }

    @Override
    public boolean sessionCreated(long ssl, long sslSession) {
        ReferenceCountedOpenSslEngine engine = engines.get(ssl);
        if (engine == null) {
            // We couldn't find the engine itself.
            return false;
        }
        OpenSslInternalSession openSslSession = (OpenSslInternalSession) engine.getSession();
        // 创建 native 缓存条目，与引擎上已有 OpenSslInternalSession 共享 keyValueStorage
        NativeSslSession session = new NativeSslSession(sslSession, engine.getPeerHost(), engine.getPeerPort(),
                getSessionTimeout() * 1000L, openSslSession.keyValueStorage());

        openSslSession.setSessionDetails(
                session.creationTime, session.lastAccessedTime, session.sessionId(), session.keyValueStorage);
        synchronized (this) {
            // 模仿 OpenSSL：每 255 个新会话 expunge 一次过期项
            if (++sessionCounter == 255) {
                sessionCounter = 0;
                expungeInvalidSessions();
            }

            if (!sessionCreated(session)) {
                // 子类拒绝缓存：关闭 leak tracker 并返回 false
                session.close();
                return false;
            }
            final NativeSslSession old = sessions.put(session.sessionId(), session);
            if (old != null) {
                notifyRemovalAndFree(old);
            }
        }
        return true;
    }

    @Override
    public final long getSession(long ssl, byte[] sessionId) {
        OpenSslSessionId id = new OpenSslSessionId(sessionId);
        final NativeSslSession session;
        synchronized (this) {
            session = sessions.get(id);
            if (session == null) {
                return -1;
            }

            // 会话无效或 upRef 失败：从缓存移除并返回 -1
            if (!session.isValid() || !session.upRef()) {
                removeSessionWithId(session.sessionId());
                return -1;
            }

            // 此处已通过 SSL_SESSION_up_ref 增加 native 引用计数
            if (session.shouldBeSingleUse()) {
                // 单次使用会话：复用后从缓存移除并 free
                removeSessionWithId(session.sessionId());
            }
        }
        session.setLastAccessedTime(System.currentTimeMillis());
        ReferenceCountedOpenSslEngine engine = engines.get(ssl);
        if (engine != null) {
            OpenSslInternalSession sslSession = (OpenSslInternalSession) engine.getSession();
            sslSession.setSessionDetails(session.getCreationTime(),
                    session.getLastAccessedTime(), session.sessionId(), session.keyValueStorage);
        }

        return session.session();
    }

    /** 客户端子类覆盖：从主机/端口索引的缓存恢复会话；默认服务端 no-op。 */
    boolean setSession(long ssl, OpenSslInternalSession session, String host, int port) {
        // Do nothing by default as this needs special handling for the client side.
       return false;
    }

    /**
     * Remove the session with the given id from the cache
     *
     * <p>按 ID 移除缓存项并 {@code SSL_SESSION_free}。</p>
     */
    final synchronized void removeSessionWithId(OpenSslSessionId id) {
        NativeSslSession sslSession = sessions.remove(id);
        if (sslSession != null) {
            notifyRemovalAndFree(sslSession);
        }
    }

    /**
     * Returns {@code true} if there is a session for the given id in the cache.
     *
     * <p>缓存中是否仍存在该会话 ID（不校验是否仍有效）。</p>
     */
    final synchronized boolean containsSessionWithId(OpenSslSessionId id) {
        return sessions.containsKey(id);
    }

    /** 通知子类并释放 native 会话与 leak tracker。 */
    private void notifyRemovalAndFree(NativeSslSession session) {
        sessionRemoved(session);
        session.free();
    }

    /**
     * Return the {@link OpenSslInternalSession} which is cached for the given id.
     *
     * <p>返回缓存中的会话视图；已过期则移除并返回 null。</p>
     */
    final synchronized OpenSslInternalSession getSession(OpenSslSessionId id) {
        NativeSslSession session = sessions.get(id);
        if (session != null && !session.isValid()) {
            // The session is not valid anymore, let's remove it and just signal back that there is no session
            // with the given ID in the cache anymore. This also takes care of calling SSL_SESSION_free(...)
            removeSessionWithId(session.sessionId());
            return null;
        }
        return session;
    }

    /**
     * Returns a snapshot of the session ids of the current valid sessions.
     *
     * <p>当前仍有效会话的 ID 列表快照。</p>
     */
    final List<OpenSslSessionId> getIds() {
        final OpenSslInternalSession[] sessionsArray;
        synchronized (this) {
            sessionsArray = sessions.values().toArray(EMPTY_SESSIONS);
        }
        List<OpenSslSessionId> ids = new ArrayList<OpenSslSessionId>(sessionsArray.length);
        for (OpenSslInternalSession session: sessionsArray) {
            if (session.isValid()) {
                ids.add(session.sessionId());
            }
        }
        return ids;
    }

    /**
     * Clear the cache and free all cached SSL_SESSION*.
     *
     * <p>清空缓存并对每个条目调用 {@code SSL_SESSION_free}。</p>
     */
    synchronized void clear() {
        Iterator<Map.Entry<OpenSslSessionId, NativeSslSession>> iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            NativeSslSession session = iterator.next().getValue();
            iterator.remove();

            // 通知移除并 free native 会话
            notifyRemovalAndFree(session);
        }
    }

    /**
     * {@link OpenSslInternalSession} implementation which wraps the native SSL_SESSION* while in cache.
     *
     * <p>缓存期内包装 {@code SSL_SESSION*} 的轻量 {@link OpenSslInternalSession}；多数 SSLSession API 不支持，
     * 仅用于 ID、超时、upRef/free 与 leak 检测。</p>
     */
    static final class NativeSslSession implements OpenSslInternalSession {
        static final ResourceLeakDetector<NativeSslSession> LEAK_DETECTOR = ResourceLeakDetectorFactory.instance()
                .newResourceLeakDetector(NativeSslSession.class);
        private final ResourceLeakTracker<NativeSslSession> leakTracker;

        final Map<String, Object> keyValueStorage;

        private final long session;
        private final String peerHost;
        private final int peerPort;
        private final OpenSslSessionId id;
        private final long timeout;
        private final long creationTime = System.currentTimeMillis();
        private volatile long lastAccessedTime = creationTime;
        private volatile boolean valid = true;
        private boolean freed;

        NativeSslSession(long session, String peerHost, int peerPort, long timeout,
                         Map<String, Object> keyValueStorage) {
            this.session = session;
            this.peerHost = peerHost;
            this.peerPort = peerPort;
            this.timeout = timeout;
            this.id = new OpenSslSessionId(io.netty.internal.tcnative.SSLSession.getSessionId(session));
            this.keyValueStorage = keyValueStorage;
            leakTracker = LEAK_DETECTOR.track(this);
        }

        @Override
        public Map<String, Object> keyValueStorage() {
            return keyValueStorage;
        }

        @Override
        public void prepareHandshake() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSessionDetails(long creationTime, long lastAccessedTime,
                                      OpenSslSessionId id, Map<String, Object> keyValueStorage) {
            throw new UnsupportedOperationException();
        }

        boolean shouldBeSingleUse() {
            assert !freed;
            return SSLSession.shouldBeSingleUse(session);
        }

        long session() {
            assert !freed;
            return session;
        }

        boolean upRef() {
            assert !freed;
            return SSLSession.upRef(session);
        }

        synchronized void free() {
            close();
            SSLSession.free(session);
        }

        void close() {
            assert !freed;
            freed = true;
            invalidate();
            if (leakTracker != null) {
                leakTracker.close(this);
            }
        }

        @Override
        public OpenSslSessionId sessionId() {
            return id;
        }

        boolean isValid(long now) {
            return creationTime + timeout >= now && valid;
        }

        @Override
        public void setLocalCertificate(Certificate[] localCertificate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public OpenSslSessionContext getSessionContext() {
            return null;
        }

        @Override
        public void tryExpandApplicationBufferSize(int packetLengthDataOnly) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void handshakeFinished(byte[] id, String cipher, String protocol, byte[] peerCertificate,
                                      byte[][] peerCertificateChain, long creationTime, long timeout) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] getId() {
            return id.cloneBytes();
        }

        @Override
        public long getCreationTime() {
            return creationTime;
        }

        @Override
        public void setLastAccessedTime(long time) {
            lastAccessedTime = time;
        }

        @Override
        public long getLastAccessedTime() {
            return lastAccessedTime;
        }

        @Override
        public void invalidate() {
            valid = false;
        }

        @Override
        public boolean isValid() {
            return isValid(System.currentTimeMillis());
        }

        @Override
        public void putValue(String name, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getValue(String name) {
            return null;
        }

        @Override
        public void removeValue(String name) {
            // NOOP
        }

        @Override
        public String[] getValueNames() {
            return EmptyArrays.EMPTY_STRINGS;
        }

        @Override
        public Certificate[] getPeerCertificates() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasPeerCertificates() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Certificate[] getLocalCertificates() {
            throw new UnsupportedOperationException();
        }

        @Override
        public X509Certificate[] getPeerCertificateChain() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Principal getPeerPrincipal() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Principal getLocalPrincipal() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getCipherSuite() {
            return null;
        }

        @Override
        public String getProtocol() {
            return null;
        }

        @Override
        public String getPeerHost() {
            return peerHost;
        }

        @Override
        public int getPeerPort() {
            return peerPort;
        }

        @Override
        public int getPacketBufferSize() {
            return ReferenceCountedOpenSslEngine.MAX_RECORD_SIZE;
        }

        @Override
        public int getApplicationBufferSize() {
            return ReferenceCountedOpenSslEngine.MAX_PLAINTEXT_LENGTH;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof OpenSslInternalSession)) {
                return false;
            }
            OpenSslInternalSession session1 = (OpenSslInternalSession) o;
            return id.equals(session1.sessionId());
        }
    }
}
