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
package io.netty.handler.codec.quic;

import io.netty.util.AsciiString;
import io.netty.util.internal.SystemPropertyUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * QUIC 客户端 TLS 会话缓存，行为与 JDK {@code SSLSessionContext} 类似，
 * 支持 0-RTT 等场景下的会话复用。
 */
final class QuicClientSessionCache {

    private static final int DEFAULT_CACHE_SIZE;
    static {
        // 与 JDK 实现共用同一系统属性，便于在两种实现间切换
        int cacheSize = SystemPropertyUtil.getInt("javax.net.ssl.sessionCacheSize", 20480);
        if (cacheSize >= 0) {
            DEFAULT_CACHE_SIZE = cacheSize;
        } else {
            DEFAULT_CACHE_SIZE = 20480;
        }
    }

    private final AtomicInteger maximumCacheSize = new AtomicInteger(DEFAULT_CACHE_SIZE);

    // 默认超时与 OpenSSL 一致（300 秒）
    // 参见 https://www.openssl.org/docs/man1.1.1/man3/SSL_get_default_timeout.html
    private final AtomicInteger sessionTimeout = new AtomicInteger(300);
    private int sessionCounter;

    private final Map<HostPort, SessionHolder> sessions =
            new LinkedHashMap<HostPort, SessionHolder>() {

                private static final long serialVersionUID = -7773696788135734448L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<HostPort, SessionHolder> eldest) {
                    int maxSize = maximumCacheSize.get();
                    return maxSize >= 0 && size() > maxSize;
                }
            };

    void saveSession(@Nullable String host, int port, long creationTime, long timeout, byte[] session,
                     boolean isSingleUse) {
        HostPort hostPort = keyFor(host, port);
        if (hostPort != null) {
            synchronized (sessions) {
                // 模仿 OpenSSL：每保存 255 个新会话后清理一次过期项
                // 参见 https://www.openssl.org/docs/man1.0.2/man3/SSL_CTX_flush_sessions.html
                if (++sessionCounter == 255) {
                    sessionCounter = 0;
                    expungeInvalidSessions();
                }

                sessions.put(hostPort, new SessionHolder(creationTime, timeout, session, isSingleUse));
            }
        }
    }

    /** 仅用于测试：检查指定主机/端口是否存在缓存会话。 */
    boolean hasSession(@Nullable String host, int port) {
        HostPort hostPort = keyFor(host, port);
        if (hostPort != null) {
            synchronized (sessions) {
                return sessions.containsKey(hostPort);
            }
        }
        return false;
    }

    byte @Nullable [] getSession(@Nullable String host, int port) {
        HostPort hostPort = keyFor(host, port);
        if (hostPort != null) {
            SessionHolder sessionHolder;
            synchronized (sessions) {
                sessionHolder = sessions.get(hostPort);
                if (sessionHolder == null) {
                    return null;
                }
                if (sessionHolder.isSingleUse()) {
                    // 单次使用会话，取出后立即移除
                    sessions.remove(hostPort);
                }
            }
            if (sessionHolder.isValid()) {
                return sessionHolder.sessionBytes();
            }
        }
        return null;
    }

    void removeSession(@Nullable String host, int port) {
        HostPort hostPort = keyFor(host, port);
        if (hostPort != null) {
            synchronized (sessions) {
                sessions.remove(hostPort);
            }
        }
    }

    void setSessionTimeout(int seconds) {
        int oldTimeout = sessionTimeout.getAndSet(seconds);
        if (oldTimeout > seconds) {
            // 缩短超时时清空整个缓存，以便利用 LinkedHashMap 顺序快速淘汰无效会话
            clear();
        }
    }

    int getSessionTimeout() {
        return sessionTimeout.get();
    }

    void setSessionCacheSize(int size) {
        long oldSize = maximumCacheSize.getAndSet(size);
        if (oldSize > size || size == 0) {
            // 缩小容量或设为 0 时直接清空缓存，实现简单可靠
            clear();
        }
    }

    int getSessionCacheSize() {
        return maximumCacheSize.get();
    }

    /** 清空缓存并释放所有已缓存的 SSL 会话。 */
    void clear() {
        synchronized (sessions) {
            sessions.clear();
        }
    }

    private void expungeInvalidSessions() {
        assert Thread.holdsLock(sessions);

        if (sessions.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<HostPort, SessionHolder>> iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            SessionHolder sessionHolder = iterator.next().getValue();
            // LinkedHashMap 按插入顺序遍历，遇到首个仍有效的会话即可停止
            // 缩短超时时会整体清空，因此插入顺序与超时顺序一致
            if (sessionHolder.isValid(now)) {
                break;
            }
            iterator.remove();
        }
    }

    @Nullable
    private static HostPort keyFor(@Nullable String host, int port) {
        if (host == null && port < 1) {
            return null;
        }
        return new HostPort(host, port);
    }

    private static final class SessionHolder {
        private final long creationTime;
        private final long timeout;
        private final byte[] sessionBytes;
        private final boolean isSingleUse;

        SessionHolder(long creationTime, long timeout, byte[] session, boolean isSingleUse) {
            this.creationTime = creationTime;
            this.timeout = timeout;
            this.sessionBytes = session;
            this.isSingleUse = isSingleUse;
        }

        boolean isValid() {
            return isValid(System.currentTimeMillis());
        }

        boolean isValid(long current) {
            return current <= creationTime + timeout;
        }

        boolean isSingleUse() {
            return isSingleUse;
        }

        byte[] sessionBytes() {
            return sessionBytes;
        }
    }

    /** 缓存键：主机名与端口的组合。 */
    private static final class HostPort {
        private final int hash;
        private final String host;
        private final int port;

        HostPort(@Nullable String host, int port) {
            this.host = host;
            this.port = port;
            // 计算忽略主机名大小写的 hashCode
            this.hash = 31 * AsciiString.hashCode(host) + port;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof HostPort)) {
                return false;
            }
            HostPort other = (HostPort) obj;
            return port == other.port && host.equalsIgnoreCase(other.host);
        }

        @Override
        public String toString() {
            return "HostPort{" +
                    "host='" + host + '\'' +
                    ", port=" + port +
                    '}';
        }
    }
}
