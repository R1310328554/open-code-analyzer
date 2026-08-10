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

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.util.BouncyCastleUtil;

import javax.net.ssl.SSLEngine;

/**
 * The {@link JdkApplicationProtocolNegotiator} to use if you need ALPN and are using {@link SslProvider#JDK}.
 *
 * <p>JDK 提供程序下的 ALPN 协商器：按运行时能力选用 Conscrypt、BouncyCastle 或 JdkAlpnSslEngine。</p>
 *
 * @deprecated use {@link ApplicationProtocolConfig}.
 */
@Deprecated
public final class JdkAlpnApplicationProtocolNegotiator extends JdkBaseApplicationProtocolNegotiator {
    /** 当前 JVM 是否具备任一 ALPN 实现路径。 */
    private static final boolean AVAILABLE = Conscrypt.isAvailable() ||
                                             JdkAlpnSslUtils.supportsAlpn() ||
            (BouncyCastleUtil.isBcTlsAvailable() && BouncyCastleAlpnSslUtils.isAlpnSupported());

    /** 可用时用 AlpnWrapper 包装引擎，否则 FailureWrapper 在 wrap 时抛错。 */
    private static final SslEngineWrapperFactory ALPN_WRAPPER = AVAILABLE ? new AlpnWrapper() : new FailureWrapper();

    /**
     * Create a new instance.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(Iterable<String> protocols) {
        this(false, protocols);
    }

    /**
     * Create a new instance.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(String... protocols) {
        this(false, protocols);
    }

    /**
     * Create a new instance.
     * @param failIfNoCommonProtocols Fail with a fatal alert if not common protocols are detected.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(boolean failIfNoCommonProtocols, Iterable<String> protocols) {
        this(failIfNoCommonProtocols, failIfNoCommonProtocols, protocols);
    }

    /**
     * Create a new instance.
     * @param failIfNoCommonProtocols Fail with a fatal alert if not common protocols are detected.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(boolean failIfNoCommonProtocols, String... protocols) {
        this(failIfNoCommonProtocols, failIfNoCommonProtocols, protocols);
    }

    /**
     * Create a new instance.
     * @param clientFailIfNoCommonProtocols Client side fail with a fatal alert if not common protocols are detected.
     * @param serverFailIfNoCommonProtocols Server side fail with a fatal alert if not common protocols are detected.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(boolean clientFailIfNoCommonProtocols,
            boolean serverFailIfNoCommonProtocols, Iterable<String> protocols) {
        this(serverFailIfNoCommonProtocols ? FAIL_SELECTOR_FACTORY : NO_FAIL_SELECTOR_FACTORY,
                clientFailIfNoCommonProtocols ? FAIL_SELECTION_LISTENER_FACTORY : NO_FAIL_SELECTION_LISTENER_FACTORY,
                protocols);
    }

    /**
     * Create a new instance.
     * @param clientFailIfNoCommonProtocols Client side fail with a fatal alert if not common protocols are detected.
     * @param serverFailIfNoCommonProtocols Server side fail with a fatal alert if not common protocols are detected.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(boolean clientFailIfNoCommonProtocols,
            boolean serverFailIfNoCommonProtocols, String... protocols) {
        this(serverFailIfNoCommonProtocols ? FAIL_SELECTOR_FACTORY : NO_FAIL_SELECTOR_FACTORY,
                clientFailIfNoCommonProtocols ? FAIL_SELECTION_LISTENER_FACTORY : NO_FAIL_SELECTION_LISTENER_FACTORY,
                protocols);
    }

    /**
     * Create a new instance.
     * @param selectorFactory The factory which provides classes responsible for selecting the protocol.
     * @param listenerFactory The factory which provides to be notified of which protocol was selected.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(ProtocolSelectorFactory selectorFactory,
            ProtocolSelectionListenerFactory listenerFactory, Iterable<String> protocols) {
        super(ALPN_WRAPPER, selectorFactory, listenerFactory, protocols);
    }

    /**
     * Create a new instance.
     * @param selectorFactory The factory which provides classes responsible for selecting the protocol.
     * @param listenerFactory The factory which provides to be notified of which protocol was selected.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    public JdkAlpnApplicationProtocolNegotiator(ProtocolSelectorFactory selectorFactory,
            ProtocolSelectionListenerFactory listenerFactory, String... protocols) {
        super(ALPN_WRAPPER, selectorFactory, listenerFactory, protocols);
    }

    /** ALPN 不可用时包装失败，提示添加 Conscrypt 或升级 JDK。 */
    private static final class FailureWrapper extends AllocatorAwareSslEngineWrapperFactory {
        @Override
        public SSLEngine wrapSslEngine(SSLEngine engine, ByteBufAllocator alloc,
                                       JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer) {
            throw new RuntimeException("ALPN unsupported. Does your JDK version support it?"
                    + " For Conscrypt, add the appropriate Conscrypt JAR to classpath and set the security provider.");
        }
    }

    /** 按引擎类型选择 Conscrypt、BC 或 JDK ALPN 包装实现。 */
    private static final class AlpnWrapper extends AllocatorAwareSslEngineWrapperFactory {
        @Override
        public SSLEngine wrapSslEngine(SSLEngine engine, ByteBufAllocator alloc,
                                       JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer) {
            if (Conscrypt.isEngineSupported(engine)) {
                return isServer ? ConscryptAlpnSslEngine.newServerEngine(engine, alloc, applicationNegotiator)
                        : ConscryptAlpnSslEngine.newClientEngine(engine, alloc, applicationNegotiator);
            }
            if (BouncyCastleUtil.isBcJsseInUse(engine) && BouncyCastleAlpnSslUtils.isAlpnSupported()) {
                return new BouncyCastleAlpnSslEngine(engine, applicationNegotiator, isServer);
            }
            // Java 8u251+ 已 backport ALPN，故用方法存在性检测而非版本号
            // ALPN support was recently backported to Java8 as
            // https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8230977.
            // Because of this lets not do a Java version runtime check but just depend on if the required methods are
            // present
            if (JdkAlpnSslUtils.supportsAlpn()) {
                return new JdkAlpnSslEngine(engine, applicationNegotiator, isServer);
            }
            throw new UnsupportedOperationException("ALPN not supported. Unable to wrap SSLEngine of type '"
                    + engine.getClass().getName() + "')");
        }
    }

    /** 供外部查询 JDK 路径 ALPN 是否可用。 */
    static boolean isAlpnSupported() {
        return AVAILABLE;
    }
}
