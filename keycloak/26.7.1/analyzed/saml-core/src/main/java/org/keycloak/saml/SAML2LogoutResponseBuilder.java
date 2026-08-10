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
import org.keycloak.dom.saml.v2.protocol.StatusCodeType;
import org.keycloak.dom.saml.v2.protocol.StatusResponseType;
import org.keycloak.dom.saml.v2.protocol.StatusType;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.processing.api.saml.v2.response.SAML2Response;
import org.keycloak.saml.processing.core.saml.v2.common.IDGenerator;
import org.keycloak.saml.processing.core.saml.v2.util.XMLTimeUtil;

import org.w3c.dom.Document;

/**
 * SAML 2.0 登出响应（LogoutResponse）构建器，向 SP 返回成功状态。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SAML2LogoutResponseBuilder implements SamlProtocolExtensionsAwareBuilder<SAML2LogoutResponseBuilder> {

    /** 对应的 LogoutRequest ID（InResponseTo）。 */
    protected String logoutRequestID;
    /** 响应目标 URL。 */
    protected String destination;
    /** 响应 Issuer（IdP 实体 ID）。 */
    protected NameIDType issuer;
    /** 协议扩展节点生成器列表。 */
    protected final List<NodeGenerator> extensions = new LinkedList<>();

    /**
     * 设置 InResponseTo（原 LogoutRequest ID）。
     *
     * @param logoutRequestID 登出请求 ID
     * @return 当前构建器
     */
    public SAML2LogoutResponseBuilder logoutRequestID(String logoutRequestID) {
        this.logoutRequestID = logoutRequestID;
        return this;
    }

    /**
     * 设置 Destination。
     *
     * @param destination SP 登出响应接收 URL
     * @return 当前构建器
     */
    public SAML2LogoutResponseBuilder destination(String destination) {
        this.destination = destination;
        return this;
    }

    /**
     * 设置 Issuer。
     *
     * @param issuer IdP 标识
     * @return 当前构建器
     */
    public SAML2LogoutResponseBuilder issuer(NameIDType issuer) {
        this.issuer = issuer;
        return this;
    }

    /**
     * 设置 Issuer（字符串形式）。
     *
     * @param issuer IdP 实体 ID
     * @return 当前构建器
     */
    public SAML2LogoutResponseBuilder issuer(String issuer) {
        return issuer(SAML2NameIDBuilder.value(issuer).build());
    }

    /** {@inheritDoc} */
    @Override
    public SAML2LogoutResponseBuilder addExtension(NodeGenerator extension) {
        this.extensions.add(extension);
        return this;
    }

    /**
     * 构建 {@link StatusResponseType} 模型（状态码为 Success）。
     *
     * @return 登出响应对象
     */
    public StatusResponseType buildModel() throws ConfigurationException {
        StatusResponseType statusResponse = new StatusResponseType(IDGenerator.create("ID_"), XMLTimeUtil.getIssueInstant());

        // 设置成功状态
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(JBossSAMLURIConstants.STATUS_SUCCESS.getUri());
        statusType.setStatusCode(statusCodeType);

        statusResponse.setStatus(statusType);
        statusResponse.setInResponseTo(logoutRequestID);
        statusResponse.setIssuer(issuer);
        statusResponse.setDestination(destination);

        if (! this.extensions.isEmpty()) {
            ExtensionsType extensionsType = new ExtensionsType();
            for (NodeGenerator extension : this.extensions) {
                extensionsType.addExtension(extension);
            }
            statusResponse.setExtensions(extensionsType);
        }

        return statusResponse;
    }

    /**
     * 构建并序列化为 DOM 文档。
     *
     * @return W3C DOM 文档
     * @throws ProcessingException 序列化失败
     */
    public Document buildDocument() throws ProcessingException {
        Document samlResponse = null;
        try {
            StatusResponseType statusResponse = buildModel();

            SAML2Response saml2Response = new SAML2Response();
            samlResponse = saml2Response.convert(statusResponse);
        } catch (ConfigurationException e) {
            throw new ProcessingException(e);
        } catch (ParsingException e) {
            throw new ProcessingException(e);
        }
        return samlResponse;

    }


}
