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

import static io.netty.handler.ssl.ApplicationProtocolUtil.toList;
import static io.netty.util.internal.ObjectUtil.checkNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLHandshakeException;

/**
 * Common base class for {@link JdkApplicationProtocolNegotiator} classes to inherit from.
 *
 * <p>封装协议列表、包装工厂及选择/监听工厂；内置 Fail/NoFail 两种握手失败策略实现。</p>
 */
class JdkBaseApplicationProtocolNegotiator implements JdkApplicationProtocolNegotiator {
    /** 按优先级排序的 ALPN 协议名列表（不可变） */
    private final List<String> protocols;
    private final ProtocolSelectorFactory selectorFactory;
    private final ProtocolSelectionListenerFactory listenerFactory;
    private final SslEngineWrapperFactory wrapperFactory;

    /**
     * Create a new instance.
     * @param wrapperFactory Determines which application protocol will be used by wrapping the SSLEngine in use.
     * @param selectorFactory How the peer selecting the protocol should behave.
     * @param listenerFactory How the peer being notified of the selected protocol should behave.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    JdkBaseApplicationProtocolNegotiator(SslEngineWrapperFactory wrapperFactory,
            ProtocolSelectorFactory selectorFactory, ProtocolSelectionListenerFactory listenerFactory,
            Iterable<String> protocols) {
        this(wrapperFactory, selectorFactory, listenerFactory, toList(protocols));
    }

    /**
     * Create a new instance.
     * @param wrapperFactory Determines which application protocol will be used by wrapping the SSLEngine in use.
     * @param selectorFactory How the peer selecting the protocol should behave.
     * @param listenerFactory How the peer being notified of the selected protocol should behave.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    JdkBaseApplicationProtocolNegotiator(SslEngineWrapperFactory wrapperFactory,
            ProtocolSelectorFactory selectorFactory, ProtocolSelectionListenerFactory listenerFactory,
            String... protocols) {
        this(wrapperFactory, selectorFactory, listenerFactory, toList(protocols));
    }

    /**
     * Create a new instance.
     * @param wrapperFactory Determines which application protocol will be used by wrapping the SSLEngine in use.
     * @param selectorFactory How the peer selecting the protocol should behave.
     * @param listenerFactory How the peer being notified of the selected protocol should behave.
     * @param protocols The order of iteration determines the preference of support for protocols.
     */
    private JdkBaseApplicationProtocolNegotiator(SslEngineWrapperFactory wrapperFactory,
            ProtocolSelectorFactory selectorFactory, ProtocolSelectionListenerFactory listenerFactory,
            List<String> protocols) {
        this.wrapperFactory = checkNotNull(wrapperFactory, "wrapperFactory");
        this.selectorFactory = checkNotNull(selectorFactory, "selectorFactory");
        this.listenerFactory = checkNotNull(listenerFactory, "listenerFactory");
        this.protocols = Collections.unmodifiableList(checkNotNull(protocols, "protocols"));
    }

    @Override
    public List<String> protocols() {
        return protocols;
    }

    @Override
    public ProtocolSelectorFactory protocolSelectorFactory() {
        return selectorFactory;
    }

    @Override
    public ProtocolSelectionListenerFactory protocolListenerFactory() {
        return listenerFactory;
    }

    @Override
    public SslEngineWrapperFactory wrapperFactory() {
        return wrapperFactory;
    }

    /** 无匹配协议时抛出 {@link SSLHandshakeException} 的选择器工厂 */
    static final ProtocolSelectorFactory FAIL_SELECTOR_FACTORY = new ProtocolSelectorFactory() {
        @Override
        public ProtocolSelector newSelector(SSLEngine engine, Set<String> supportedProtocols) {
            return new FailProtocolSelector((JdkSslEngine) engine, supportedProtocols);
        }
    };

    /** 无匹配协议时返回 null、不强制失败握手的选择器工厂 */
    static final ProtocolSelectorFactory NO_FAIL_SELECTOR_FACTORY = new ProtocolSelectorFactory() {
        @Override
        public ProtocolSelector newSelector(SSLEngine engine, Set<String> supportedProtocols) {
            return new NoFailProtocolSelector((JdkSslEngine) engine, supportedProtocols);
        }
    };

    static final ProtocolSelectionListenerFactory FAIL_SELECTION_LISTENER_FACTORY =
            new ProtocolSelectionListenerFactory() {
        @Override
        public ProtocolSelectionListener newListener(SSLEngine engine, List<String> supportedProtocols) {
            return new FailProtocolSelectionListener((JdkSslEngine) engine, supportedProtocols);
        }
    };

    static final ProtocolSelectionListenerFactory NO_FAIL_SELECTION_LISTENER_FACTORY =
            new ProtocolSelectionListenerFactory() {
        @Override
        public ProtocolSelectionListener newListener(SSLEngine engine, List<String> supportedProtocols) {
            return new NoFailProtocolSelectionListener((JdkSslEngine) engine, supportedProtocols);
        }
    };

    static class NoFailProtocolSelector implements ProtocolSelector {
        private final JdkSslEngine engineWrapper;
        /** 本地支持的协议集合，按配置优先级遍历 */
        private final Set<String> supportedProtocols;

        NoFailProtocolSelector(JdkSslEngine engineWrapper, Set<String> supportedProtocols) {
            this.engineWrapper = engineWrapper;
            this.supportedProtocols = supportedProtocols;
        }

        @Override
        public void unsupported() {
            // 对端不支持 ALPN/NPN 时清空协商结果
            engineWrapper.setNegotiatedApplicationProtocol(null);
        }

        @Override
        public String select(List<String> protocols) throws Exception {
            // 按本地优先级在 advertised 列表中找第一个交集
            for (String p : supportedProtocols) {
                if (protocols.contains(p)) {
                    engineWrapper.setNegotiatedApplicationProtocol(p);
                    return p;
                }
            }
            return noSelectMatchFound();
        }

        public String noSelectMatchFound() throws Exception {
            engineWrapper.setNegotiatedApplicationProtocol(null);
            return null;
        }
    }

    private static final class FailProtocolSelector extends NoFailProtocolSelector {
        FailProtocolSelector(JdkSslEngine engineWrapper, Set<String> supportedProtocols) {
            super(engineWrapper, supportedProtocols);
        }

        @Override
        public String noSelectMatchFound() throws Exception {
            throw new SSLHandshakeException("Selected protocol is not supported");
        }
    }

    private static class NoFailProtocolSelectionListener implements ProtocolSelectionListener {
        private final JdkSslEngine engineWrapper;
        private final List<String> supportedProtocols;

        NoFailProtocolSelectionListener(JdkSslEngine engineWrapper, List<String> supportedProtocols) {
            this.engineWrapper = engineWrapper;
            this.supportedProtocols = supportedProtocols;
        }

        @Override
        public void unsupported() {
            engineWrapper.setNegotiatedApplicationProtocol(null);
        }

        @Override
        public void selected(String protocol) throws Exception {
            if (supportedProtocols.contains(protocol)) {
                engineWrapper.setNegotiatedApplicationProtocol(protocol);
            } else {
                noSelectedMatchFound(protocol);
            }
        }

        protected void noSelectedMatchFound(String protocol) throws Exception {
            // NoFail 子类默认忽略对端选了本地不支持的协议
        }
    }

    private static final class FailProtocolSelectionListener extends NoFailProtocolSelectionListener {
        FailProtocolSelectionListener(JdkSslEngine engineWrapper, List<String> supportedProtocols) {
            super(engineWrapper, supportedProtocols);
        }

        @Override
        protected void noSelectedMatchFound(String protocol) throws Exception {
            throw new SSLHandshakeException("No compatible protocols found");
        }
    }
}
