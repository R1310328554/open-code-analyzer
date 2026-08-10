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

import org.keycloak.dom.saml.v2.protocol.NameIDPolicyType;

/**
 * SAML 2.0 NameIDPolicy 元素构建器，用于 AuthnRequest 中声明期望的 NameID 格式。
 *
 * @author pedroigor
 */
public class SAML2NameIDPolicyBuilder {
    /** 底层 NameIDPolicy 模型对象。 */
    private final NameIDPolicyType policyType;
    /** 是否允许 IdP 创建新标识（AllowCreate）。 */
    private Boolean allowCreate;
    /** SPNameQualifier（可选）。 */
    private String spNameQualifier;

    private SAML2NameIDPolicyBuilder(String format) {
        this.policyType = new NameIDPolicyType();
        this.policyType.setFormat(URI.create(format));
    }

    /**
     * 以指定格式 URI 创建构建器。
     *
     * @param format NameID 格式 URI
     * @return 新构建器实例
     */
    public static SAML2NameIDPolicyBuilder format(String format) {
        return new SAML2NameIDPolicyBuilder(format);
    }

    /**
     * 设置 AllowCreate 标志。
     *
     * @param allowCreate 是否允许创建新 NameID
     * @return 当前构建器
     */
    public SAML2NameIDPolicyBuilder setAllowCreate(Boolean allowCreate) {
        this.allowCreate = allowCreate;
        return this;
    }

    /**
     * 设置 SPNameQualifier。
     *
     * @param spNameQualifier SP 限定符
     * @return 当前构建器
     */
    public SAML2NameIDPolicyBuilder setSPNameQualifier(String spNameQualifier) {
        this.spNameQualifier = spNameQualifier;
        return this;
    }

    /**
     * 构建并返回 {@link NameIDPolicyType}。
     *
     * @return 配置完毕的策略对象
     */
    public NameIDPolicyType build() {
        if (this.allowCreate != null)
            this.policyType.setAllowCreate(this.allowCreate);

        if (this.spNameQualifier != null)
            this.policyType.setSPNameQualifier(this.spNameQualifier);

        return this.policyType;
    }
}