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
package org.keycloak.saml.processing.core.saml.v2.holders;

/**
 * 保存 SAML HTTP POST 绑定目标地址及消息的容器。
 * <p>用于 HTTP-POST 或 Artifact 绑定中将 SAML 消息投递到 SP 端点。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 24, 2009
 */
public class DestinationInfoHolder {

    /** POST 目标 URL。 */
    private String destination;
    /** Base64 编码的 SAML 消息。 */
    private String samlMessage;
    /** RelayState 参数值。 */
    private String relayState;

    /**
     * 构造 POST 投递信息容器。
     *
     * @param destination POST 请求的目标 URL
     * @param samlMessage SAML 消息（通常为 Base64 编码）
     * @param relayState RelayState 回传参数
     */
    public DestinationInfoHolder(String destination, String samlMessage, String relayState) {
        this.destination = destination;
        this.samlMessage = samlMessage;
        this.relayState = relayState;
    }

    /** 返回 POST 目标 URL。 */
    public String getDestination() {
        return destination;
    }

    /** 返回 SAML 消息内容。 */
    public String getSamlMessage() {
        return samlMessage;
    }

    /** 返回 RelayState 值。 */
    public String getRelayState() {
        return relayState;
    }
}
