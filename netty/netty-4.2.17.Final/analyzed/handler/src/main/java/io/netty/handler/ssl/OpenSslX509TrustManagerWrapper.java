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

import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Utility which allows to wrap {@link X509TrustManager} implementations with the internal implementation used by
 * {@code SSLContextImpl} that provides extended verification.
 * <p>
 * This is really a "hack" until there is an official API as requested on the in
 * <a href="https://bugs.openjdk.java.net/projects/JDK/issues/JDK-8210843">JDK-8210843</a>.
 *
 * <p>通过反射获取 SunJSSE {@link SSLContextImpl} 内部的 {@link X509ExtendedTrustManager} 包装层，
 * 使 OpenSSL 证书校验路径与 JDK 扩展校验（如 hostname 验证）行为一致；Unsafe 不可用时退化为原样返回。</p>
 */
final class OpenSslX509TrustManagerWrapper {
    private static final InternalLogger LOGGER = InternalLoggerFactory
            .getInstance(OpenSslX509TrustManagerWrapper.class);
    /** 运行时选中的包装策略（默认不包装）。 */
    private static final TrustManagerWrapper WRAPPER;

    /** 无 Unsafe 或未探测到内部包装器时使用。 */
    private static final TrustManagerWrapper DEFAULT = new TrustManagerWrapper() {
        @Override
        public X509TrustManager wrapIfNeeded(X509TrustManager manager) {
            return manager;
        }
    };

    static {
        // 默认不包装；仅在 SunJSSE + Unsafe 可用时替换为 UnsafeTrustManagerWrapper
        TrustManagerWrapper wrapper = DEFAULT;

        Throwable cause = null;
        Throwable unsafeCause = PlatformDependent.getUnsafeUnavailabilityCause();
        if (unsafeCause == null) {
            SSLContext context;
            try {
                context = newSSLContext();
                // 用占位 X509TrustManager init，SunJSSE 会包装为 AbstractTrustManagerWrapper
                //
                // See:
                // - https://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/
                //          cadea780bc76/src/share/classes/sun/security/ssl/SSLContextImpl.java#l127
                context.init(null, new TrustManager[] {
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                                    throws CertificateException {
                                throw new CertificateException();
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                                    throws CertificateException {
                                throw new CertificateException();
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return EmptyArrays.EMPTY_X509_CERTIFICATES;
                            }
                        }
                }, null);
            } catch (Throwable error) {
                context = null;
                cause = error;
            }
            if (cause != null) {
                LOGGER.debug("Unable to access wrapped TrustManager", cause);
            } else {
                final SSLContext finalContext = context;
                Object maybeWrapper = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    @Override
                    public Object run() {
                        try {
                            Field contextSpiField = SSLContext.class.getDeclaredField("contextSpi");
                            final long spiOffset = PlatformDependent.objectFieldOffset(contextSpiField);
                            Object spi = PlatformDependent.getObject(finalContext, spiOffset);
                            if (spi != null) {
                                Class<?> clazz = spi.getClass();

                                // 沿 SPI 继承链查找 trustManager 字段，定位 X509ExtendedTrustManager
                                do {
                                    try {
                                        Field trustManagerField = clazz.getDeclaredField("trustManager");
                                        final long tmOffset = PlatformDependent.objectFieldOffset(trustManagerField);
                                        Object trustManager = PlatformDependent.getObject(spi, tmOffset);
                                        if (trustManager instanceof X509ExtendedTrustManager) {
                                            return new UnsafeTrustManagerWrapper(spiOffset, tmOffset);
                                        }
                                    } catch (NoSuchFieldException ignore) {
                                        // try next
                                    }
                                    clazz = clazz.getSuperclass();
                                } while (clazz != null);
                            }
                            throw new NoSuchFieldException();
                        } catch (NoSuchFieldException | SecurityException e) {
                            return e;
                        }
                    }
                });
                if (maybeWrapper instanceof Throwable) {
                    LOGGER.debug("Unable to access wrapped TrustManager", (Throwable) maybeWrapper);
                } else {
                    wrapper = (TrustManagerWrapper) maybeWrapper;
                }
            }
        } else {
            LOGGER.debug("Unable to access wrapped TrustManager", cause);
        }
        WRAPPER = wrapper;
    }

    /** @return 是否已成功探测到 JDK 内部 TrustManager 包装能力 */
    static boolean isWrappingSupported() {
        return WRAPPER != DEFAULT;
    }

    private OpenSslX509TrustManagerWrapper() { }

    static X509TrustManager wrapIfNeeded(X509TrustManager trustManager) {
        return WRAPPER.wrapIfNeeded(trustManager);
    }

    private interface TrustManagerWrapper {
        X509TrustManager wrapIfNeeded(X509TrustManager manager);
    }

    private static SSLContext newSSLContext() throws NoSuchAlgorithmException, NoSuchProviderException {
        // 显式 SunJSSE，避免其他 Provider 的 SPI 布局不同（见 netty#10374）
        return SSLContext.getInstance("TLS", "SunJSSE");
    }

    /** 非 {@link X509ExtendedTrustManager} 时通过临时 SSLContext 获取 JDK 扩展包装实例。 */
    private static final class UnsafeTrustManagerWrapper implements TrustManagerWrapper {
        private final long spiOffset;
        private final long tmOffset;

        UnsafeTrustManagerWrapper(long spiOffset, long tmOffset) {
            this.spiOffset = spiOffset;
            this.tmOffset = tmOffset;
        }

        @Override
        public X509TrustManager wrapIfNeeded(X509TrustManager manager) {
            if (!(manager instanceof X509ExtendedTrustManager)) {
                try {
                    SSLContext ctx = newSSLContext();
                    ctx.init(null, new TrustManager[] { manager }, null);
                    Object spi = PlatformDependent.getObject(ctx, spiOffset);
                    if (spi != null) {
                        Object tm = PlatformDependent.getObject(spi, tmOffset);
                        if (tm instanceof X509ExtendedTrustManager) {
                            return (X509TrustManager) tm;
                        }
                    }
                } catch (NoSuchAlgorithmException | NoSuchProviderException | KeyManagementException e) {
                    // This should never happen as we did the same in the static block
                    // before.
                    PlatformDependent.throwException(e);
                }
            }
            return manager;
        }
    }
}
