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

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.dom.saml.v2.protocol.ExtensionsType;
import org.keycloak.dom.saml.v2.protocol.LogoutRequestType;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.processing.api.saml.v2.request.SAML2Request;
import org.keycloak.saml.processing.core.saml.v2.util.XMLTimeUtil;

import org.w3c.dom.Document;

/**
 * SAML 2.0 单点登出请求（LogoutRequest）构建器。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SAML2LogoutRequestBuilder implements SamlProtocolExtensionsAwareBuilder<SAML2LogoutRequestBuilder> {
    /** 待登出用户 NameID。 */
    protected NameIDType nameId;
    /** IdP 会话索引（SessionIndex）。 */
    protected String sessionIndex;
    /** 请求有效时长（秒），映射到 NotOnOrAfter。 */
    protected long assertionExpiration;
    /** 请求目标 URL（IdP SLO 端点）。 */
    protected String destination;
    /** 请求 Issuer（SP 实体 ID）。 */
    protected NameIDType issuer;
    /** 协议扩展节点生成器列表。 */
    protected final List<NodeGenerator> extensions = new LinkedList<>();

    /**
     * 设置 Destination。
     *
     * @param destination IdP 登出端点 URL
     * @return 当前构建器
     */
    public SAML2LogoutRequestBuilder destination(String destination) {
        this.destination = destination;
        return this;
    }

    /**
     * 设置 Issuer。
     *
     * @param issuer SP 标识
     * @return 当前构建器
     */
    public SAML2LogoutRequestBuilder issuer(NameIDType issuer) {
        this.issuer = issuer;
        return this;
    }

    /**
     * 设置 Issuer（字符串形式）。
     *
     * @param issuer SP 实体 ID
     * @return 当前构建器
     */
    public SAML2LogoutRequestBuilder issuer(String issuer) {
        return issuer(SAML2NameIDBuilder.value(issuer).build());
    }

    /** {@inheritDoc} */
    @Override
    public SAML2LogoutRequestBuilder addExtension(NodeGenerator extension) {
        this.extensions.add(extension);
        return this;
    }

    /**
     * 设置请求有效时长（秒）。
     * 参见 SAML Core 规范 2.5.1.2 NotOnOrAfter。
     *
     * @param assertionExpiration 有效秒数
     * @return 当前构建器
     */
    public SAML2LogoutRequestBuilder assertionExpiration(int assertionExpiration) {
        this.assertionExpiration = assertionExpiration;
        return this;
    }

    /**
     * 设置用户主体（已弃用，请使用 {@link #nameId(org.keycloak.dom.saml.v2.assertion.NameIDType)}）。
     *
     * @param userPrincipal 用户标识值
     * @param userPrincipalFormat NameID 格式 URI
     * @return 当前构建器
     * @deprecated 请改用 {@link #nameId(org.keycloak.dom.saml.v2.assertion.NameIDType)}
     */
    @Deprecated
    public SAML2LogoutRequestBuilder userPrincipal(String userPrincipal, String userPrincipalFormat) {
        NameIDType nid = new NameIDType();
        nid.setValue(userPrincipal);
        if (userPrincipalFormat != null) {
            nid.setFormat(URI.create(userPrincipalFormat));
        }
        
        return nameId(nid);
    }

    /**
     * 设置登出用户的 NameID。
     *
     * @param nameId 用户 NameID
     * @return 当前构建器
     */
    public SAML2LogoutRequestBuilder nameId(NameIDType nameId) {
        this.nameId = nameId;
        return this;
    }

    /**
     * 设置 IdP 会话索引。
     *
     * @param index SessionIndex 值
     * @return 当前构建器
     */
    public SAML2LogoutRequestBuilder sessionIndex(String index) {
        this.sessionIndex = index;
        return this;
    }

    /**
     * 构建 LogoutRequest 并序列化为 DOM 文档。
     *
     * @return W3C DOM 文档
     */
    public Document buildDocument() throws ProcessingException, ConfigurationException, ParsingException {
        Document document = SAML2Request.convert(createLogoutRequest());
        return document;
    }

    /**
     * 组装 {@link LogoutRequestType} 模型对象。
     *
     * @return 登出请求对象
     */
    public LogoutRequestType createLogoutRequest() throws ConfigurationException {
        LogoutRequestType lort = SAML2Request.createLogoutRequest(issuer);

        lort.setNameID(nameId);
        lort.setIssuer(issuer);

        if (sessionIndex != null) lort.addSessionIndex(sessionIndex);


        if (assertionExpiration > 0) lort.setNotOnOrAfter(XMLTimeUtil.add(lort.getIssueInstant(), assertionExpiration * 1000));
        if (destination != null) {
            lort.setDestination(URI.create(destination));
        }

        if (! this.extensions.isEmpty()) {
            ExtensionsType extensionsType = new ExtensionsType();
            for (NodeGenerator extension : this.extensions) {
                extensionsType.addExtension(extension);
            }
            lort.setExtensions(extensionsType);
        }

        return lort;
    }
}
