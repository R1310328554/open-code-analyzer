/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.saml;

/**
 * SAML 联邦用户主标识类型：决定 IdP 如何将用户映射到本地账户。
 * <p>SUBJECT 使用 NameID；ATTRIBUTE 使用指定属性；FRIENDLY_ATTRIBUTE 使用友好名属性。</p>
 */
public enum SamlPrincipalType {

    /** 以 SAML Subject/NameID 作为主标识 */
    SUBJECT,
    /** 以 SAML 属性（按属性名）作为主标识 */
    ATTRIBUTE,
    /** 以 SAML 友好名属性作为主标识 */
    FRIENDLY_ATTRIBUTE;

    /**
     * 按名称解析枚举值，无效时返回默认值。
     * @param name 枚举名字符串
     * @param defaultValue 解析失败时的默认值
     * @return 匹配的 {@link SamlPrincipalType}
     */
        if (name == null) {
            return defaultValue;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ex) {
            return defaultValue;
        }
    }

}
