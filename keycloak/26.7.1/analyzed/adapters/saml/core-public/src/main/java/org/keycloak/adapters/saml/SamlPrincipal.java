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

package org.keycloak.adapters.saml;

import java.io.Serializable;
import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.NameIDType;

import org.w3c.dom.Document;

/**
 * SAML 认证成功后代表当前用户的主体（Principal）。
 *
 * <p>封装 SAML 断言中的 NameID、属性集及完整断言对象，供容器安全上下文与
 * 应用层读取用户身份与角色信息。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SamlPrincipal implements Serializable, Principal {

    /** 默认角色属性名 */
    public static final String DEFAULT_ROLE_ATTRIBUTE_NAME = "Roles";

    /** 按属性名索引的多值属性映射 */
    private MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>();
    /** 按友好名（FriendlyName）索引的多值属性映射 */
    private MultivaluedHashMap<String, String> friendlyAttributes = new MultivaluedHashMap<>();
    /** 主体显示名称 */
    private String name;
    /** SAML Subject 标识符 */
    private String samlSubject;
    /** NameID 格式 URI */
    private String nameIDFormat;
    /** 解析后的 SAML 断言对象 */
    private AssertionType assertion;
    /** 保留原始语法的断言 DOM 文档（可选） */
    private Document assertionDocument;

    public SamlPrincipal(AssertionType assertion, String name, String samlSubject, String nameIDFormat, MultivaluedHashMap<String, String> attributes, MultivaluedHashMap<String, String> friendlyAttributes) {
        this(assertion, null, name, samlSubject, nameIDFormat, attributes, friendlyAttributes);
    }

    public SamlPrincipal(AssertionType assertion, Document assertionDocument, String name, String samlSubject, String nameIDFormat, MultivaluedHashMap<String, String> attributes, MultivaluedHashMap<String, String> friendlyAttributes) {
        this.name = name;
        this.attributes = attributes;
        this.friendlyAttributes = friendlyAttributes;
        this.samlSubject = samlSubject;
        this.nameIDFormat = nameIDFormat;
        this.assertion = assertion;
        this.assertionDocument = assertionDocument;
    }

    public SamlPrincipal() {
    }

    /**
     * 获取完整的 SAML 断言对象。
     *
     * @return 断言对象
     */
    public AssertionType getAssertion() {
        return assertion;
    }

    /**
     * 获取断言中 SAML Subject 的标识符。
     *
     * @return Subject 字符串
     */
    public String getSamlSubject() {
        return samlSubject;
    }

    /**
     * 获取 Subject NameID 的格式 URI。
     *
     * @return NameID 格式
     */
    public String getNameIDFormat() {
        return nameIDFormat;
    }

    /**
     * 获取 Subject 的 {@link NameIDType} 对象。
     *
     * <p>若断言中已包含 NameID 则直接返回；否则根据 {@link #samlSubject}
     * 与 {@link #nameIDFormat} 构造合成 NameID。</p>
     *
     * @return NameID 对象
     */
    public NameIDType getNameID() {
        if (assertion != null
          && assertion.getSubject() != null
          && assertion.getSubject().getSubType() != null
          && assertion.getSubject().getSubType().getBaseID() instanceof NameIDType) {
            return (NameIDType) assertion.getSubject().getSubType().getBaseID();
        }

        NameIDType res = new NameIDType();
        res.setValue(getSamlSubject());
        if (getNameIDFormat() != null) {
            res.setFormat(URI.create(getNameIDFormat()));
        }
        return res;
    }

    /**
     * 以 DOM 格式获取断言元素，保留原始 XML 语法。
     *
     * <p>仅当配置项 <em>keepDOMAssertion</em> 为 true 时可用。</p>
     *
     * @return 断言 DOM 文档，未保留时为 null
     */
    public Document getAssertionDocument() {
        return assertionDocument;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 按属性名获取属性值列表。
     *
     * @param name 属性名
     * @return 属性值列表（不可变，不存在时返回空列表）
     */
    public List<String> getAttributes(String name) {
        List<String> list = attributes.get(name);
        if (list != null) {
            return Collections.unmodifiableList(list);
        } else {
            return Collections.emptyList();
        }

    }

    /**
     * 获取本主体关联的全部属性映射。
     *
     * @return 属性名到值列表的不可变映射
     */
    public Map<String, List<String>> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * 按友好名（FriendlyName）获取属性值列表。
     *
     * @param friendlyName 属性友好名
     * @return 属性值列表（不可变，不存在时返回空列表）
     */
    public List<String> getFriendlyAttributes(String friendlyName) {
        List<String> list = friendlyAttributes.get(friendlyName);
        if (list != null) {
            return Collections.unmodifiableList(list);
        } else {
            return Collections.emptyList();
        }

    }

    /**
     * 按属性名获取第一个属性值。
     *
     * @param name 属性名
     * @return 第一个属性值，不存在时为 null
     */
    public String getAttribute(String name) {
        return attributes.getFirst(name);
    }

    /**
     * 按友好名获取第一个属性值。
     *
     * @param friendlyName 属性友好名
     * @return 第一个属性值，不存在时为 null
     */
    public String getFriendlyAttribute(String friendlyName) {
        return friendlyAttributes.getFirst(friendlyName);
    }

    /**
     * 获取断言中所有属性名集合。
     *
     * @return 不可变的属性名集合
     */
    public Set<String> getAttributeNames() {
        return Collections.unmodifiableSet(attributes.keySet());

    }

    /**
     * 获取断言中所有友好属性名集合。
     *
     * @return 不可变的友好名集合
     */
    public Set<String> getFriendlyNames() {
        return Collections.unmodifiableSet(friendlyAttributes.keySet());

    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;

        if (!(other instanceof SamlPrincipal))
            return false;

        SamlPrincipal otherPrincipal = (SamlPrincipal) other;

        return (this.name != null ? this.name.equals(otherPrincipal.name) : otherPrincipal.name == null) &&
                (this.samlSubject != null ? this.samlSubject.equals(otherPrincipal.samlSubject) : otherPrincipal.samlSubject == null) &&
                (this.nameIDFormat != null ? this.nameIDFormat.equals(otherPrincipal.nameIDFormat) : otherPrincipal.nameIDFormat == null) &&
                (this.attributes != null ? this.attributes.equals(otherPrincipal.attributes) : otherPrincipal.attributes == null) &&
                (this.friendlyAttributes != null ? this.friendlyAttributes.equals(otherPrincipal.friendlyAttributes) : otherPrincipal.friendlyAttributes == null);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.name == null ? 0 : this.name.hashCode());
        result = prime * result + (this.samlSubject == null ? 0 : this.samlSubject.hashCode());
        result = prime * result + (this.nameIDFormat == null ? 0 : this.nameIDFormat.hashCode());
        result = prime * result + (this.attributes == null ? 0 : this.attributes.hashCode());
        result = prime * result + (this.friendlyAttributes == null ? 0 : this.friendlyAttributes.hashCode());
        return result;
    }
}
