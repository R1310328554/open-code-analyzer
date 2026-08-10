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

import org.keycloak.dom.saml.v2.assertion.NameIDType;

/**
 * SAML 2.0 {@link NameIDType} 的流式构建器。
 */
public class SAML2NameIDBuilder {
    /** 底层 NameID 模型对象。 */
    private final NameIDType nameIdType;
    /** NameID 格式 URI（可选）。 */
    private String format;
    /** NameQualifier（可选）。 */
    private String nameQualifier;
    /** SPNameQualifier（可选）。 */
    private String spNameQualifier;

    private SAML2NameIDBuilder(String value) {
        this.nameIdType = new NameIDType();
        this.nameIdType.setValue(value);
    }

    /**
     * 以给定值创建构建器。
     *
     * @param value NameID 文本值
     * @return 新构建器实例
     */
    public static SAML2NameIDBuilder value(String value) {
        return new SAML2NameIDBuilder(value);
    }

    /**
     * 设置 NameID 格式 URI。
     *
     * @param format 格式字符串
     * @return 当前构建器
     */
    public SAML2NameIDBuilder setFormat(String format) {
        this.format = format;
        return this;
    }

    /**
     * 设置 NameQualifier。
     *
     * @param nameQualifier 限定符
     * @return 当前构建器
     */
    public SAML2NameIDBuilder setNameQualifier(String nameQualifier) {
        this.nameQualifier = nameQualifier;
        return this;
    }

    /**
     * 设置 SPNameQualifier。
     *
     * @param spNameQualifier SP 限定符
     * @return 当前构建器
     */
    public SAML2NameIDBuilder setSPNameQualifier(String spNameQualifier) {
        this.spNameQualifier = spNameQualifier;
        return this;
    }

    /**
     * 构建并返回 {@link NameIDType}。
     *
     * @return 配置完毕的 NameID 对象
     */
    public NameIDType build() {
        if (this.format != null)
            this.nameIdType.setFormat(URI.create(this.format));

        if (this.nameQualifier != null)
            this.nameIdType.setNameQualifier(this.nameQualifier);

        if (this.spNameQualifier != null)
            this.nameIdType.setSPNameQualifier(this.spNameQualifier);

        return this.nameIdType;
    }
}