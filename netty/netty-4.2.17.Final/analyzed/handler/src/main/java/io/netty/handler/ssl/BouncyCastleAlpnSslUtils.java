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

import io.netty.handler.ssl.util.BouncyCastleUtil;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.SecureRandom;
import java.util.List;
import java.util.function.BiFunction;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import static io.netty.handler.ssl.SslUtils.getSSLContext;

/**
 * 通过反射调用 Bouncy Castle JSSE 的 ALPN 扩展 API（{@code setApplicationProtocols}、
 * {@code getApplicationProtocol}、{@code BCApplicationProtocolSelector} 等）。
 *
 * <p>静态块探测 BC TLS 是否可用并缓存 {@link Method}；不可用时 {@link #isAlpnSupported()} 为 false。
 * Java 9+ 在 BC 设置后还会同步调用 {@link JdkAlpnSslUtils} 以兼容双栈行为。</p>
 */
final class BouncyCastleAlpnSslUtils {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(BouncyCastleAlpnSslUtils.class);
    /** SSLParameters.setApplicationProtocols(String[]) */
    private static final Method SET_APPLICATION_PROTOCOLS;
    /** SSLEngine.getApplicationProtocol() */
    private static final Method GET_APPLICATION_PROTOCOL;
    /** SSLEngine.getHandshakeApplicationProtocol() */
    private static final Method GET_HANDSHAKE_APPLICATION_PROTOCOL;
    /** SSLEngine.setBCHandshakeApplicationProtocolSelector(BCApplicationProtocolSelector) */
    private static final Method SET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR;
    /** SSLEngine.getBCHandshakeApplicationProtocolSelector() */
    private static final Method GET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR;
    /** org.bouncycastle.jsse.BCApplicationProtocolSelector 接口类 */
    private static final Class<?> BC_APPLICATION_PROTOCOL_SELECTOR;
    /** BCApplicationProtocolSelector.select(Object, List) */
    private static final Method BC_APPLICATION_PROTOCOL_SELECTOR_SELECT;
    /** 静态初始化是否成功探测到 BC ALPN API */
    private static final boolean SUPPORTED;

    static {
        Method setApplicationProtocols;
        Method getApplicationProtocol;
        Method getHandshakeApplicationProtocol;
        Method setHandshakeApplicationProtocolSelector;
        Method getHandshakeApplicationProtocolSelector;
        Method bcApplicationProtocolSelectorSelect;
        Class<?> bcApplicationProtocolSelector;
        boolean supported;

        try {
            if (!BouncyCastleUtil.isBcTlsAvailable()) {
                throw new IllegalStateException(BouncyCastleUtil.unavailabilityCauseBcTls());
            }
            SSLContext context = getSSLContext(BouncyCastleUtil.getBcProviderJsse(), new SecureRandom());
            SSLEngine engine = context.createSSLEngine();
            Class<? extends SSLEngine> engineClass = engine.getClass();
            // We need to use the class returned by BounceCastleUtil below to access the methods as the engine
            // returned by createSSLEngine might be package-private and so would not allow us to access the methods
            // even thought the interface itself that it implements is public and so the methods are public.
            // See https://github.com/netty/netty/issues/15627
            // 使用 BouncyCastleUtil 返回的公开引擎类解析方法，避免 package-private 实现类无法反射
            final Class<? extends SSLEngine> bcEngineClass = BouncyCastleUtil.getBcSSLEngineClass();
            if (bcEngineClass == null || !bcEngineClass.isAssignableFrom(engineClass)) {
                throw new IllegalStateException("Unexpected engine class: " + engineClass);
            }

            final SSLParameters bcSslParameters = engine.getSSLParameters();
            final Class<?> bCSslParametersClass = bcSslParameters.getClass();
            setApplicationProtocols = AccessController.doPrivileged(new PrivilegedExceptionAction<Method>() {
                @Override
                public Method run() throws Exception {
                    return bCSslParametersClass.getMethod("setApplicationProtocols", String[].class);
                }
            });
            setApplicationProtocols.invoke(bcSslParameters, new Object[]{EmptyArrays.EMPTY_STRINGS});

            getApplicationProtocol = AccessController.doPrivileged(new PrivilegedExceptionAction<Method>() {
                @Override
                public Method run() throws Exception {
                    return bcEngineClass.getMethod("getApplicationProtocol");
                }
            });
            getApplicationProtocol.invoke(engine);

            getHandshakeApplicationProtocol = AccessController.doPrivileged(new PrivilegedExceptionAction<Method>() {
                @Override
                public Method run() throws Exception {
                    return bcEngineClass.getMethod("getHandshakeApplicationProtocol");
                }
            });
            getHandshakeApplicationProtocol.invoke(engine);

            final Class<?> testBCApplicationProtocolSelector = Class.forName(
                    "org.bouncycastle.jsse.BCApplicationProtocolSelector", true, engineClass.getClassLoader());
            bcApplicationProtocolSelector = testBCApplicationProtocolSelector;

            bcApplicationProtocolSelectorSelect = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Method>() {
                        @Override
                        public Method run() throws Exception {
                            return testBCApplicationProtocolSelector.getMethod("select", Object.class, List.class);
                        }
                    });

            setHandshakeApplicationProtocolSelector =
                    AccessController.doPrivileged(new PrivilegedExceptionAction<Method>() {
                        @Override
                        public Method run() throws Exception {
                            return bcEngineClass.getMethod("setBCHandshakeApplicationProtocolSelector",
                                    testBCApplicationProtocolSelector);
                        }
                    });

            getHandshakeApplicationProtocolSelector =
                    AccessController.doPrivileged(new PrivilegedExceptionAction<Method>() {
                        @Override
                        public Method run() throws Exception {
                            return bcEngineClass.getMethod("getBCHandshakeApplicationProtocolSelector");
                        }
                    });
            getHandshakeApplicationProtocolSelector.invoke(engine);
            supported = true;
        } catch (Throwable t) {
            logger.error("Unable to initialize BouncyCastleAlpnSslUtils.", t);
            setApplicationProtocols = null;
            getApplicationProtocol = null;
            getHandshakeApplicationProtocol = null;
            setHandshakeApplicationProtocolSelector = null;
            getHandshakeApplicationProtocolSelector = null;
            bcApplicationProtocolSelectorSelect = null;
            bcApplicationProtocolSelector = null;
            supported = false;
        }
        SET_APPLICATION_PROTOCOLS = setApplicationProtocols;
        GET_APPLICATION_PROTOCOL = getApplicationProtocol;
        GET_HANDSHAKE_APPLICATION_PROTOCOL = getHandshakeApplicationProtocol;
        SET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR = setHandshakeApplicationProtocolSelector;
        GET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR = getHandshakeApplicationProtocolSelector;
        BC_APPLICATION_PROTOCOL_SELECTOR_SELECT = bcApplicationProtocolSelectorSelect;
        BC_APPLICATION_PROTOCOL_SELECTOR = bcApplicationProtocolSelector;
        SUPPORTED = supported;
    }

    private BouncyCastleAlpnSslUtils() {
    }

    /** 读取已协商的应用层协议名。 */
    static String getApplicationProtocol(SSLEngine sslEngine) {
        try {
            return (String) GET_APPLICATION_PROTOCOL.invoke(sslEngine);
        } catch (UnsupportedOperationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * 设置客户端 advertised 协议列表（写入 BC SSLParameters，Java 9+ 同步 JDK ALPN）。
     */
    static void setApplicationProtocols(SSLEngine engine, List<String> supportedProtocols) {
        String[] protocolArray = supportedProtocols.toArray(EmptyArrays.EMPTY_STRINGS);
        try {
            SSLParameters bcSslParameters = engine.getSSLParameters();
            SET_APPLICATION_PROTOCOLS.invoke(bcSslParameters, new Object[]{protocolArray});
            engine.setSSLParameters(bcSslParameters);
        } catch (UnsupportedOperationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        if (PlatformDependent.javaVersion() >= 9) {
            JdkAlpnSslUtils.setApplicationProtocols(engine, supportedProtocols);
        }
    }

    /** 读取握手阶段选定的协议（握手完成前）。 */
    static String getHandshakeApplicationProtocol(SSLEngine sslEngine) {
        try {
            return (String) GET_HANDSHAKE_APPLICATION_PROTOCOL.invoke(sslEngine);
        } catch (UnsupportedOperationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * 将 Netty {@link BiFunction} 选择器适配为 BC {@link BC_APPLICATION_PROTOCOL_SELECTOR} 动态代理并注册到引擎。
     */
    static void setHandshakeApplicationProtocolSelector(
            SSLEngine engine, final BiFunction<SSLEngine, List<String>, String> selector) {
        try {
            Object selectorProxyInstance = Proxy.newProxyInstance(
                    BouncyCastleAlpnSslUtils.class.getClassLoader(),
                    new Class[]{BC_APPLICATION_PROTOCOL_SELECTOR},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if (method.getName().equals("select")) {
                                try {
                                    return selector.apply((SSLEngine) args[0], (List<String>) args[1]);
                                } catch (ClassCastException e) {
                                    throw new RuntimeException("BCApplicationProtocolSelector select method " +
                                            "parameter of invalid type.", e);
                                }
                            } else {
                                throw new UnsupportedOperationException(String.format("Method '%s' not supported.",
                                        method.getName()));
                            }
                        }
                    });

            SET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR.invoke(engine, selectorProxyInstance);
        } catch (UnsupportedOperationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** 读取 BC 选择器并包装为 Netty {@link BiFunction}。 */
    static BiFunction<SSLEngine, List<String>, String> getHandshakeApplicationProtocolSelector(SSLEngine engine) {
        try {
            final Object selector = GET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR.invoke(engine);
            return (sslEngine, strings) -> {
                try {
                    return (String) BC_APPLICATION_PROTOCOL_SELECTOR_SELECT.invoke(selector, sslEngine, strings);
                } catch (Exception e) {
                    throw new RuntimeException("Could not call getHandshakeApplicationProtocolSelector", e);
                }
            };
        } catch (UnsupportedOperationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** 当前 JVM 上 BC ALPN 反射初始化是否成功。 */
    static boolean isAlpnSupported() {
        return SUPPORTED;
    }
}
