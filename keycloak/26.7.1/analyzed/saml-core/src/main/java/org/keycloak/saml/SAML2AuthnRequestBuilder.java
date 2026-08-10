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
import org.keycloak.dom.saml.v2.assertion.SubjectType;
import org.keycloak.dom.saml.v2.protocol.AuthnRequestType;
import org.keycloak.dom.saml.v2.protocol.ExtensionsType;
import org.keycloak.dom.saml.v2.protocol.RequestedAuthnContextType;
import org.keycloak.saml.processing.api.saml.v2.request.SAML2Request;
import org.keycloak.saml.processing.core.saml.v2.common.IDGenerator;
import org.keycloak.saml.processing.core.saml.v2.util.XMLTimeUtil;

import org.w3c.dom.Document;

/**
 * SAML 2.0 认证请求（AuthnRequest）的流式构建器。
 * <p>支持设置断言消费服务 URL、NameID 策略、RequestedAuthnContext 及协议扩展等。</p>
 *
 * @author pedroigor
 */
public class SAML2AuthnRequestBuilder implements SamlProtocolExtensionsAwareBuilder<SAML2AuthnRequestBuilder> {

    /** 底层 AuthnRequest DOM 模型对象。 */
    private final AuthnRequestType authnRequestType;
    /** IdP 接收请求的目标 URL。 */
    protected String destination;
    /** 请求发起方（SP）的 Issuer。 */
    protected NameIDType issuer;
    /** 待写入 {@code samlp:Extensions} 的扩展节点生成器列表。 */
    protected final List<NodeGenerator> extensions = new LinkedList<>();

    /**
     * 设置请求目标地址（Destination）。
     *
     * @param destination IdP SSO 端点 URL
     * @return 当前构建器
     */
    public SAML2AuthnRequestBuilder destination(String destination) {
        this.destination = destination;
        return this;
    }

    /**
     * 设置 Issuer（NameIDType 形式）。
     *
     * @param issuer 发起方标识
     * @return 当前构建器
     */
    public SAML2AuthnRequestBuilder issuer(NameIDType issuer) {
        this.issuer = issuer;
        return this;
    }

    /**
     * 设置 Issuer（字符串形式，自动包装为 NameID）。
     *
     * @param issuer 发起方实体 ID
     * @return 当前构建器
     */
    public SAML2AuthnRequestBuilder issuer(String issuer) {
        return issuer(SAML2NameIDBuilder.value(issuer).build());
    }

    /** {@inheritDoc} */
    @Override
    public SAML2AuthnRequestBuilder addExtension(NodeGenerator extension) {
        this.extensions.add(extension);
        return this;
    }

    /** 创建带唯一 ID 与 IssueInstant 的空 AuthnRequest。 */
    public SAML2AuthnRequestBuilder() {
        this.authnRequestType = new AuthnRequestType(IDGenerator.create("ID_"), XMLTimeUtil.getIssueInstant());
    }

    /**
     * 设置断言消费服务 URL（字符串形式）。
     *
     * @param assertionConsumerUrl ACS 端点地址
     * @return 当前构建器
     */
    public SAML2AuthnRequestBuilder assertionConsumerUrl(String assertionConsumerUrl) {
        this.authnRequestType.setAssertionConsumerServiceURL(URI.create(assertionConsumerUrl));
        return this;
    }

    /**
     * 设置断言消费服务 URL。
     *
     * @param assertionConsumerUrl ACS 端点 URI
     * @return 当前构建器
     */
    public SAML2AuthnRequestBuilder assertionConsumerUrl(URI assertionConsumerUrl) {
        this.authnRequestType.setAssertionConsumerServiceURL(assertionConsumerUrl);
        return this;
    }

    /**
     * 设置属性消费服务索引（AttributeConsumingServiceIndex）。
     *
     * @param attributeConsumingServiceIndex 元数据中定义的索引
     * @return 当前构建器
     */
    public SAML2AuthnRequestBuilder attributeConsumingServiceIndex(Integer attributeConsumingServiceIndex) {
        this.authnRequestType.setAttributeConsumingServiceIndex(attributeConsumingServiceIndex);
        return this;
    }

    /**
     * 是否强制重新认证（ForceAuthn）。
     *
     * @param forceAuthn {@code true} 表示 IdP 必须重新认证用户
     * @return 当前构建器
     */
    public SAML2AuthnRequestBuilder forceAuthn(boolean forceAuthn) {
        this.authnRequestType.setForceAuthn(forceAuthn);
        return this;
    }

    /**
     * 是否被动认证（IsPassive）：IdP 不得与用户交互。
     *
     * @param isPassive 被动模式标志
     * @return 当前构建器
     */
    public SAML2AuthnRequestBuilder isPassive(boolean isPassive) {
        this.authnRequestType.setIsPassive(isPassive);
        return this;
    }

    /**
     * 设置 NameID 策略。
     *
     * @param nameIDPolicyBuilder NameID 策略构建器
     * @return 当前构建器
     */
    public SAML2AuthnRequestBuilder nameIdPolicy(SAML2NameIDPolicyBuilder nameIDPolicyBuilder) {
        this.authnRequestType.setNameIDPolicy(nameIDPolicyBuilder.build());
        return this;
    }

    /**
     * 设置协议绑定 URI（如 HTTP-POST、HTTP-Redirect）。
     *
     * @param protocolBinding 绑定 URI 字符串
     * @return 当前构建器
     */
    public SAML2AuthnRequestBuilder protocolBinding(String protocolBinding) {
        this.authnRequestType.setProtocolBinding(URI.create(protocolBinding));
        return this;
    }

    /**
     * 设置 Subject 标识（可选，用于 IdP 初始化登录等场景）。
     *
     * @param subject 主体标识值
     * @return 当前构建器
     */
    public SAML2AuthnRequestBuilder subject(String subject) {
        String sanitizedSubject = subject != null ? subject.trim() : null;
        if (sanitizedSubject != null && !sanitizedSubject.isEmpty()) {
            this.authnRequestType.setSubject(createSubject(sanitizedSubject));
        }
        return this;
    }

    /** 根据 NameID 值与当前 NameIDPolicy 格式创建 SubjectType。 */
    private SubjectType createSubject(String value) {
        NameIDType nameId = new NameIDType();
        nameId.setValue(value);
        nameId.setFormat(this.authnRequestType.getNameIDPolicy() != null ? this.authnRequestType.getNameIDPolicy().getFormat() : null);
        SubjectType subject = new SubjectType();
        SubjectType.STSubType subType = new SubjectType.STSubType();
        subType.addBaseID(nameId);
        subject.setSubType(subType);
        return subject;
    }

    /**
     * 设置请求的认证上下文（RequestedAuthnContext）。
     * <p>仅当至少包含一个 ClassRef 或 DeclRef 时才写入该元素。</p>
     *
     * @param requestedAuthnContextBuilder 认证上下文构建器
     * @return 当前构建器
     */
    public SAML2AuthnRequestBuilder requestedAuthnContext(SAML2RequestedAuthnContextBuilder requestedAuthnContextBuilder) {
        RequestedAuthnContextType requestedAuthnContext = requestedAuthnContextBuilder.build();

        // 仅当存在 ClassRef 或 DeclRef 时才输出 RequestedAuthnContext 元素
        if (!requestedAuthnContext.getAuthnContextClassRef().isEmpty() ||
            !requestedAuthnContext.getAuthnContextDeclRef().isEmpty())
            this.authnRequestType.setRequestedAuthnContext(requestedAuthnContext);

        return this;
    }

    /** 将完整 AuthnRequest 序列化为 W3C {@link Document}。 */
    public Document toDocument() {
        try {
            AuthnRequestType authnRequestType = createAuthnRequest();

            return new SAML2Request().convert(authnRequestType);
        } catch (Exception e) {
            throw new RuntimeException("Could not convert " + authnRequestType + " to a document.", e);
        }
    }

    /**
     * 组装并返回 {@link AuthnRequestType} 模型（含 Issuer、Destination 与 Extensions）。
     *
     * @return 填充完毕的认证请求对象
     */
    public AuthnRequestType createAuthnRequest() {
        AuthnRequestType res = this.authnRequestType;

        res.setIssuer(issuer);
        res.setDestination(URI.create(this.destination));

        if (! this.extensions.isEmpty()) {
            ExtensionsType extensionsType = new ExtensionsType();
            for (NodeGenerator extension : this.extensions) {
                extensionsType.addExtension(extension);
            }
            res.setExtensions(extensionsType);
        }

        return res;
    }
}