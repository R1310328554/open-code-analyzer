/*
 * Copyright 2014 The Netty Project
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

import io.netty.internal.tcnative.SSL;
import io.netty.internal.tcnative.SSLContext;
import io.netty.internal.tcnative.SessionTicketKey;
import io.netty.util.internal.ObjectUtil;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;

/**
 * OpenSSL specific {@link SSLSessionContext} implementation.
 *
 * <p>OpenSSL {@link SSLSessionContext} 实现：挂接 {@link OpenSslSessionCache}、配置会话超时/容量、
 * TLS session ticket 密钥，并在 {@link #destroy()} 时释放 {@link OpenSslKeyMaterialProvider}。</p>
 */
public abstract class OpenSslSessionContext implements SSLSessionContext {

    /** 本上下文 native 会话统计（SSL_CTX_sess_*）。 */
    private final OpenSslSessionStats stats;

    // The OpenSslKeyMaterialProvider is not really used by the OpenSslSessionContext but only be stored here
    // to make it easier to destroy it later because the ReferenceCountedOpenSslContext will hold a reference
    // to OpenSslSessionContext.
    /** 随 SessionContext 一并销毁的密钥材料提供者（此处仅持有引用）。 */
    private final OpenSslKeyMaterialProvider provider;

    /** 持有 native ctx 指针的 OpenSSL 上下文，防止 GC 提前释放 native 内存。 */
    final ReferenceCountedOpenSslContext context;

    /** 外部会话缓存实现，通过 tcnative 回调与 OpenSSL 交互。 */
    private final OpenSslSessionCache sessionCache;
    /** 服务端/客户端会话缓存模式位掩码（{@link SSL#SSL_SESS_CACHE_SERVER} 等）。 */
    private final long mask;

    // IMPORTANT: We take the OpenSslContext and not just the long (which points the native instance) to prevent
    //            the GC to collect OpenSslContext as this would also free the pointer and so could result in a
    //            segfault when the user calls any of the methods here that try to pass the pointer down to the native
    //            level.
    OpenSslSessionContext(ReferenceCountedOpenSslContext context, OpenSslKeyMaterialProvider provider, long mask,
                          OpenSslSessionCache cache) {
        this.context = context;
        this.provider = provider;
        this.mask = mask;
        stats = new OpenSslSessionStats(context);
        sessionCache = cache;
        SSLContext.setSSLSessionCache(context.ctx, cache);
    }

    /** 是否配置了 KeyManager（即 provider 非 null）。 */
    final boolean useKeyManager() {
        return provider != null;
    }

    @Override
    public void setSessionCacheSize(int size) {
        ObjectUtil.checkPositiveOrZero(size, "size");
        sessionCache.setSessionCacheSize(size);
    }

    @Override
    public int getSessionCacheSize() {
        return sessionCache.getSessionCacheSize();
    }

    @Override
    public void setSessionTimeout(int seconds) {
        ObjectUtil.checkPositiveOrZero(seconds, "seconds");

        Lock writerLock = context.ctxLock.writeLock();
        writerLock.lock();
        try {
            SSLContext.setSessionCacheTimeout(context.ctx, seconds);
            sessionCache.setSessionTimeout(seconds);
        } finally {
            writerLock.unlock();
        }
    }

    @Override
    public int getSessionTimeout() {
        return sessionCache.getSessionTimeout();
    }

    @Override
    public SSLSession getSession(byte[] bytes) {
        return sessionCache.getSession(new OpenSslSessionId(bytes));
    }

    @Override
    public Enumeration<byte[]> getIds() {
        return new Enumeration<byte[]>() {
            private final Iterator<OpenSslSessionId> ids = sessionCache.getIds().iterator();
            @Override
            public boolean hasMoreElements() {
                return ids.hasNext();
            }

            @Override
            public byte[] nextElement() {
                return ids.next().cloneBytes();
            }
        };
    }

    /**
     * Sets the SSL session ticket keys of this context.
     *
     * <p>设置 TLS session ticket 加密密钥；可传空数组由 native 库（如 BoringSSL）自动生成与轮换。</p>
     * @deprecated use {@link #setTicketKeys(OpenSslSessionTicketKey...)}.
     */
    @Deprecated
    public void setTicketKeys(byte[] keys) {
        if (keys.length % SessionTicketKey.TICKET_KEY_SIZE != 0) {
            throw new IllegalArgumentException("keys.length % " + SessionTicketKey.TICKET_KEY_SIZE  + " != 0");
        }
        SessionTicketKey[] tickets = new SessionTicketKey[keys.length / SessionTicketKey.TICKET_KEY_SIZE];
        for (int i = 0, a = 0; i < tickets.length; i++) {
            byte[] name = Arrays.copyOfRange(keys, a, SessionTicketKey.NAME_SIZE);
            a += SessionTicketKey.NAME_SIZE;
            byte[] hmacKey = Arrays.copyOfRange(keys, a, SessionTicketKey.HMAC_KEY_SIZE);
            i += SessionTicketKey.HMAC_KEY_SIZE;
            byte[] aesKey = Arrays.copyOfRange(keys, a, SessionTicketKey.AES_KEY_SIZE);
            a += SessionTicketKey.AES_KEY_SIZE;
            tickets[i] = new SessionTicketKey(name, hmacKey, aesKey);
        }
        Lock writerLock = context.ctxLock.writeLock();
        writerLock.lock();
        try {
            SSLContext.clearOptions(context.ctx, SSL.SSL_OP_NO_TICKET);
            SSLContext.setSessionTicketKeys(context.ctx, tickets);
        } finally {
            writerLock.unlock();
        }
    }

    /**
     * Sets the SSL session ticket keys of this context. Depending on the underlying native library you may omit the
     * argument or pass an empty array and so let the native library handle the key generation and rotating for you.
     * If this is supported by the underlying native library should be checked in this case. For example
     * <a href="https://commondatastorage.googleapis.com/chromium-boringssl-docs/ssl.h.html#Session-tickets/">
     *     BoringSSL</a> is known to support this.
     *
     * <p>写入 session ticket 主密钥列表并清除 {@code SSL_OP_NO_TICKET}；空数组时仅启用 ticket 特性。</p>
     */
    public void setTicketKeys(OpenSslSessionTicketKey... keys) {
        ObjectUtil.checkNotNull(keys, "keys");
        SessionTicketKey[] ticketKeys = new SessionTicketKey[keys.length];
        for (int i = 0; i < ticketKeys.length; i++) {
            ticketKeys[i] = keys[i].key;
        }
        Lock writerLock = context.ctxLock.writeLock();
        writerLock.lock();
        try {
            SSLContext.clearOptions(context.ctx, SSL.SSL_OP_NO_TICKET);
            if (ticketKeys.length > 0) {
                SSLContext.setSessionTicketKeys(context.ctx, ticketKeys);
            }
        } finally {
            writerLock.unlock();
        }
    }

    /**
     * Enable or disable caching of SSL sessions.
     *
     * <p>开关 SSL 会话缓存；关闭时清空 {@link OpenSslSessionCache}。</p>
     */
    public void setSessionCacheEnabled(boolean enabled) {
        long mode = enabled ? mask | SSL.SSL_SESS_CACHE_NO_INTERNAL_LOOKUP |
                SSL.SSL_SESS_CACHE_NO_INTERNAL_STORE : SSL.SSL_SESS_CACHE_OFF;
        Lock writerLock = context.ctxLock.writeLock();
        writerLock.lock();
        try {
            SSLContext.setSessionCacheMode(context.ctx, mode);
            if (!enabled) {
                sessionCache.clear();
            }
        } finally {
            writerLock.unlock();
        }
    }

    /**
     * Return {@code true} if caching of SSL sessions is enabled, {@code false} otherwise.
     *
     * <p>当前 {@code SSL_CTX} 是否启用了与本上下文 mask 匹配的会话缓存模式。</p>
     */
    public boolean isSessionCacheEnabled() {
        Lock readerLock = context.ctxLock.readLock();
        readerLock.lock();
        try {
            return (SSLContext.getSessionCacheMode(context.ctx) & mask) != 0;
        } finally {
            readerLock.unlock();
        }
    }

    /**
     * Returns the stats of this context.
     *
     * <p>返回 OpenSSL 会话统计封装 {@link OpenSslSessionStats}。</p>
     */
    public OpenSslSessionStats stats() {
        return stats;
    }

    /**
     * Remove the given {@link OpenSslInternalSession} from the cache, and so not re-use it for new connections.
     *
     * <p>从外部缓存移除指定会话 ID，后续连接不可复用。</p>
     */
    final void removeFromCache(OpenSslSessionId id) {
        sessionCache.removeSessionWithId(id);
    }

    final boolean isInCache(OpenSslSessionId id) {
        return sessionCache.containsSessionWithId(id);
    }

    boolean setSessionFromCache(long ssl, OpenSslInternalSession session, String host, int port) {
        return sessionCache.setSession(ssl, session, host, port);
    }

    /** 销毁 provider 并清空全部缓存的 {@code SSL_SESSION*}。 */
    final void destroy() {
        if (provider != null) {
            provider.destroy();
        }
        sessionCache.clear();
    }
}
