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
import org.keycloak.dom.saml.v2.protocol.ArtifactResolveType;
import org.keycloak.dom.saml.v2.protocol.ExtensionsType;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.processing.api.saml.v2.request.SAML2Request;

import org.w3c.dom.Document;

/**
 * SAML 2.0 ArtifactResolve 请求的流式构建器。
 *
 * <p>用于构造 {@link ArtifactResolveType} 及其 DOM 表示，支持设置 artifact、destination、issuer 及扩展元素。</p>
 */
public class SAML2ArtifactResolveRequestBuilder implements SamlProtocolExtensionsAwareBuilder<SAML2ArtifactResolveRequestBuilder> {
    /** Artifact 字符串值。 */
    protected String artifact;
    /** 请求目标 URI。 */
    protected String destination;
    /** 断言 Issuer 的 NameID。 */
    protected NameIDType issuer;
    /** 协议扩展节点生成器列表。 */
    protected final List<NodeGenerator> extensions = new LinkedList<>();

    /**
     * 设置要解析的 SAML artifact。
     *
     * @param artifact artifact 字符串
     * @return 当前构建器
     */
    public SAML2ArtifactResolveRequestBuilder artifact(String artifact) {
        this.artifact = artifact;
        return this;
    }

    /**
     * 设置请求 destination 属性。
     *
     * @param destination 目标 URI 字符串
     * @return 当前构建器
     */
    public SAML2ArtifactResolveRequestBuilder destination(String destination) {
        this.destination = destination;
        return this;
    }

    /**
     * 设置 Issuer 为 {@link NameIDType}。
     *
     * @param issuer Issuer 名称标识
     * @return 当前构建器
     */
    public SAML2ArtifactResolveRequestBuilder issuer(NameIDType issuer) {
        this.issuer = issuer;
        return this;
    }

    /**
     * 以字符串形式设置 Issuer。
     *
     * @param issuer Issuer 字符串
     * @return 当前构建器
     */
    public SAML2ArtifactResolveRequestBuilder issuer(String issuer) {
        return issuer(SAML2NameIDBuilder.value(issuer).build());
    }

    @Override
    public SAML2ArtifactResolveRequestBuilder addExtension(NodeGenerator extension) {
        this.extensions.add(extension);
        return this;
    }

    /**
     * 构建 ArtifactResolve 请求的 DOM 文档。
     *
     * @return SAML ArtifactResolve DOM 文档
     * @throws ProcessingException 处理失败时抛出
     * @throws ConfigurationException 配置错误时抛出
     * @throws ParsingException 解析失败时抛出
     */
    public Document buildDocument() throws ProcessingException, ConfigurationException, ParsingException {
        Document document = SAML2Request.convert(createArtifactResolveRequest());
        return document;
    }

    /**
     * 创建 {@link ArtifactResolveType} 对象模型实例。
     *
     * @return 填充完毕的 ArtifactResolve 类型对象
     * @throws ConfigurationException 配置错误时抛出
     */
    public ArtifactResolveType createArtifactResolveRequest() throws ConfigurationException {
        ArtifactResolveType lort = SAML2Request.createArtifactResolveRequest(issuer);

        lort.setIssuer(issuer);

        if (destination != null) {
            lort.setDestination(URI.create(destination));
        }

        if (artifact != null) {
            lort.setArtifact(artifact);
        }

        if (!this.extensions.isEmpty()) {
            ExtensionsType extensionsType = new ExtensionsType();
            for (NodeGenerator extension : this.extensions) {
                extensionsType.addExtension(extension);
            }
            lort.setExtensions(extensionsType);
        }

        return lort;
    }
}
