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

import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLEngine;

import static io.netty.handler.ssl.ApplicationProtocolUtil.toList;
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.ObjectUtil.checkNonEmpty;

/**
 * Provides an {@link SSLEngine} agnostic way to configure a {@link ApplicationProtocolNegotiator}.
 *
 * <p>与具体 {@link SSLEngine} 实现无关的 ALPN/NPN 配置：协议列表、协商机制及双方匹配失败时的行为。</p>
 */
public final class ApplicationProtocolConfig {

    /**
     * The configuration that disables application protocol negotiation.
     *
     * <p>禁用应用层协议协商的单例配置。</p>
     */
    public static final ApplicationProtocolConfig DISABLED = new ApplicationProtocolConfig();

    /** 本端支持的协议名列表，顺序表示优先级。 */
    private final List<String> supportedProtocols;
    /** 使用的协商扩展：NPN、ALPN 或两者。 */
    private final Protocol protocol;
    /** 选择协议一方在无匹配时的行为。 */
    private final SelectorFailureBehavior selectorBehavior;
    /** 被通知已选协议一方在无匹配时的行为。 */
    private final SelectedListenerFailureBehavior selectedBehavior;

    /**
     * Create a new instance.
     * @param protocol The application protocol functionality to use.
     * @param selectorBehavior How the peer selecting the protocol should behave.
     * @param selectedBehavior How the peer being notified of the selected protocol should behave.
     * @param supportedProtocols The order of iteration determines the preference of support for protocols.
     *
     * <p>从可迭代协议集合构造配置。</p>
     */
    public ApplicationProtocolConfig(Protocol protocol, SelectorFailureBehavior selectorBehavior,
            SelectedListenerFailureBehavior selectedBehavior, Iterable<String> supportedProtocols) {
        this(protocol, selectorBehavior, selectedBehavior, toList(supportedProtocols));
    }

    /**
     * Create a new instance.
     * @param protocol The application protocol functionality to use.
     * @param selectorBehavior How the peer selecting the protocol should behave.
     * @param selectedBehavior How the peer being notified of the selected protocol should behave.
     * @param supportedProtocols The order of iteration determines the preference of support for protocols.
     *
     * <p>从可变参数协议名构造配置。</p>
     */
    public ApplicationProtocolConfig(Protocol protocol, SelectorFailureBehavior selectorBehavior,
            SelectedListenerFailureBehavior selectedBehavior, String... supportedProtocols) {
        this(protocol, selectorBehavior, selectedBehavior, toList(supportedProtocols));
    }

    /**
     * Create a new instance.
     * @param protocol The application protocol functionality to use.
     * @param selectorBehavior How the peer selecting the protocol should behave.
     * @param selectedBehavior How the peer being notified of the selected protocol should behave.
     * @param supportedProtocols The order of iteration determines the preference of support for protocols.
     */
    private ApplicationProtocolConfig(
            Protocol protocol, SelectorFailureBehavior selectorBehavior,
            SelectedListenerFailureBehavior selectedBehavior, List<String> supportedProtocols) {
        this.supportedProtocols = Collections.unmodifiableList(checkNotNull(supportedProtocols, "supportedProtocols"));
        this.protocol = checkNotNull(protocol, "protocol");
        this.selectorBehavior = checkNotNull(selectorBehavior, "selectorBehavior");
        this.selectedBehavior = checkNotNull(selectedBehavior, "selectedBehavior");

        if (protocol == Protocol.NONE) {
            throw new IllegalArgumentException("protocol (" + Protocol.NONE + ") must not be " + Protocol.NONE + '.');
        }
        checkNonEmpty(supportedProtocols, "supportedProtocols");
    }

    /**
     * A special constructor that is used to instantiate {@link #DISABLED}.
     *
     * <p>仅供 {@link #DISABLED} 使用的私有构造，协议为 NONE、列表为空。</p>
     */
    private ApplicationProtocolConfig() {
        supportedProtocols = Collections.emptyList();
        protocol = Protocol.NONE;
        selectorBehavior = SelectorFailureBehavior.CHOOSE_MY_LAST_PROTOCOL;
        selectedBehavior = SelectedListenerFailureBehavior.ACCEPT;
    }

    /**
     * Defines which application level protocol negotiation to use.
     *
     * <p>TLS 握手期间使用的应用层协议协商机制。</p>
     */
    public enum Protocol {
        /** 不启用协商 */
        NONE, NPN, ALPN, NPN_AND_ALPN
    }

    /**
     * Defines the most common behaviors for the peer that selects the application protocol.
     *
     * <p>负责从候选列表中选定协议的一方，在无交集时的策略。</p>
     */
    public enum SelectorFailureBehavior {
        /**
         * If the peer who selects the application protocol doesn't find a match this will result in the failing the
         * handshake with a fatal alert.
         * <p>
         * For example in the case of ALPN this will result in a
         * <a herf="https://tools.ietf.org/html/rfc7301#section-3.2">no_application_protocol(120)</a> alert.
         *
         * <p>无匹配则发送 fatal alert 终止握手（ALPN 为 no_application_protocol）。</p>
         */
        FATAL_ALERT,
        /**
         * If the peer who selects the application protocol doesn't find a match it will pretend no to support
         * the TLS extension by not advertising support for the TLS extension in the handshake. This is used in cases
         * where a "best effort" is desired to talk even if there is no matching protocol.
         *
         * <p>无匹配则假装不支持 ALPN/NPN 扩展，尽力继续普通 TLS。</p>
         */
        NO_ADVERTISE,
        /**
         * If the peer who selects the application protocol doesn't find a match it will just select the last protocol
         * it advertised support for. This is used in cases where a "best effort" is desired to talk even if there
         * is no matching protocol, and the assumption is the "most general" fallback protocol is typically listed last.
         * <p>
         * This may be <a href="https://tools.ietf.org/html/rfc7301#section-3.2">illegal for some RFCs</a> but was
         * observed behavior by some SSL implementations, and is supported for flexibility/compatibility.
         *
         * <p>无匹配则选用本端 advertised 列表最后一项（常见 fallback 如 http/1.1）。</p>
         */
        CHOOSE_MY_LAST_PROTOCOL
    }

    /**
     * Defines the most common behaviors for the peer which is notified of the selected protocol.
     *
     * <p>被告知最终协议的一方，在协议不在本端支持列表中时的策略。</p>
     */
    public enum SelectedListenerFailureBehavior {
        /**
         * If the peer who is notified what protocol was selected determines the selection was not matched, or the peer
         * didn't advertise support for the TLS extension then the handshake will continue and the application protocol
         * is assumed to be accepted.
         *
         * <p>接受对方选择（或未协商），握手继续。</p>
         */
        ACCEPT,
        /**
         * If the peer who is notified what protocol was selected determines the selection was not matched, or the peer
         * didn't advertise support for the TLS extension then the handshake will be failed with a fatal alert.
         *
         * <p>认为选择非法，以 fatal alert 失败握手。</p>
         */
        FATAL_ALERT,
        /**
         * If the peer who is notified what protocol was selected determines the selection was not matched, or the peer
         * didn't advertise support for the TLS extension then the handshake will continue assuming the last protocol
         * supported by this peer is used. This is used in cases where a "best effort" is desired to talk even if there
         * is no matching protocol, and the assumption is the "most general" fallback protocol is typically listed last.
         *
         * <p>握手继续但假定使用本端列表最后一项协议。</p>
         */
        CHOOSE_MY_LAST_PROTOCOL
    }

    /**
     * The application level protocols supported.
     *
     * <p>本端支持的应用协议名不可变列表。</p>
     */
    public List<String> supportedProtocols() {
        return supportedProtocols;
    }

    /**
     * Get which application level protocol negotiation to use.
     *
     * <p>返回 NPN/ALPN 等协商类型。</p>
     */
    public Protocol protocol() {
        return protocol;
    }

    /**
     * Get the desired behavior for the peer who selects the application protocol.
     */
    public SelectorFailureBehavior selectorFailureBehavior() {
        return selectorBehavior;
    }

    /**
     * Get the desired behavior for the peer who is notified of the selected protocol.
     */
    public SelectedListenerFailureBehavior selectedListenerFailureBehavior() {
        return selectedBehavior;
    }
}
