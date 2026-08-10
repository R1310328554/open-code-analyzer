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

package org.keycloak.saml;

import java.util.LinkedList;
import java.util.List;

import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.dom.saml.v2.protocol.ExtensionsType;
import org.keycloak.dom.saml.v2.protocol.ResponseType;
import org.keycloak.dom.saml.v2.protocol.StatusResponseType;
import org.keycloak.dom.saml.v2.protocol.StatusType;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.processing.api.saml.v2.response.SAML2Response;
import org.keycloak.saml.processing.core.saml.v2.common.IDGenerator;
import org.keycloak.saml.processing.core.saml.v2.factories.JBossSAMLAuthnResponseFactory;
import org.keycloak.saml.processing.core.saml.v2.util.XMLTimeUtil;

import org.w3c.dom.Document;

/**
 * SAML 2.0 错误响应（StatusResponse）构建器，用于向 SP 返回失败状态码与消息。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SAML2ErrorResponseBuilder implements SamlProtocolExtensionsAwareBuilder<SAML2ErrorResponseBuilder> {

    /** SAML 状态码 URI（如 {@code urn:oasis:names:tc:SAML:2.0:status:Responder}）。 */
    protected String status;
    /** 可选的状态描述消息。 */
    protected String statusMessage;
    /** 响应目标 URL（SP 端点）。 */
    protected String destination;
    /** 响应 Issuer（通常为 IdP 实体 ID）。 */
    protected NameIDType issuer;
    /** 对应的原始请求 ID（InResponseTo）。 */
    protected String inResponseTo;
    /** 协议扩展节点生成器列表。 */
    protected final List<NodeGenerator> extensions = new LinkedList<>();

    /**
     * 设置 SAML 状态码。
     *
     * @param status 状态码 URI
     * @return 当前构建器
     */
    public SAML2ErrorResponseBuilder status(String status) {
        this.status = status;
        return this;
    }

    /**
     * 设置状态描述消息。
     *
     * @param statusMessage 人类可读的错误说明
     * @return 当前构建器
     */
    public SAML2ErrorResponseBuilder statusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    /**
     * 设置响应目标地址。
     *
     * @param destination SP 接收端点 URL
     * @return 当前构建器
     */
    public SAML2ErrorResponseBuilder destination(String destination) {
        this.destination = destination;
        return this;
    }

    /**
     * 设置 Issuer。
     *
     * @param issuer IdP 标识
     * @return 当前构建器
     */
    public SAML2ErrorResponseBuilder issuer(NameIDType issuer) {
        this.issuer = issuer;
        return this;
    }

    /**
     * 设置 Issuer（字符串形式）。
     *
     * @param issuer IdP 实体 ID
     * @return 当前构建器
     */
    public SAML2ErrorResponseBuilder issuer(String issuer) {
        return issuer(SAML2NameIDBuilder.value(issuer).build());
    }

    /**
     * 设置 InResponseTo，关联原始请求 ID。
     *
     * @param inResponseTo 原 AuthnRequest 的 ID
     * @return 当前构建器
     */
    public SAML2ErrorResponseBuilder inResponseTo(String inResponseTo) {
        this.inResponseTo = inResponseTo;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public SAML2ErrorResponseBuilder addExtension(NodeGenerator extension) {
        this.extensions.add(extension);
        return this;
    }

    /**
     * 构建并序列化为 SAML 错误响应 XML 文档。
     *
     * @return W3C DOM 文档
     * @throws ProcessingException 序列化或配置失败时抛出
     */
    public Document buildDocument() throws ProcessingException {

        try {
            StatusResponseType statusResponse = new ResponseType(IDGenerator.create("ID_"), XMLTimeUtil.getIssueInstant());
            statusResponse.setInResponseTo(inResponseTo);

            StatusType statusType = JBossSAMLAuthnResponseFactory.createStatusTypeForResponder(status);
            statusType.setStatusMessage(statusMessage);
            statusResponse.setStatus(statusType);
            statusResponse.setIssuer(issuer);
            statusResponse.setDestination(destination);

            if (! this.extensions.isEmpty()) {
                ExtensionsType extensionsType = new ExtensionsType();
                for (NodeGenerator extension : this.extensions) {
                    extensionsType.addExtension(extension);
                }
                statusResponse.setExtensions(extensionsType);
            }

            SAML2Response saml2Response = new SAML2Response();
            return saml2Response.convert(statusResponse);
        } catch (ConfigurationException e) {
            throw new ProcessingException(e);
        } catch (ParsingException e) {
            throw new ProcessingException(e);
        }

    }


}
