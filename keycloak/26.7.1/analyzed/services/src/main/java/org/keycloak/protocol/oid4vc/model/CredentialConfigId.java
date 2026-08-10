/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oid4vc.model;

/**
 * 凭证配置标识符值对象。
 * <p>封装 OID4VCI 元数据中 {@code credential_configurations_supported} 的键名，避免与普通字符串混用。</p>
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class CredentialConfigId {
    /** 配置 ID 字符串值。 */
    private final String value;

    /**
     * 从字符串创建值对象；{@code null} 输入返回 {@code null}。
     * @param value 配置 ID 字符串
     * @return 值对象或 {@code null}
     */
    public static CredentialConfigId from(String value) {
        return value == null ? null : new CredentialConfigId(value);
    }

    /** @param value 配置 ID 字符串 */
    public CredentialConfigId(String value) {
        this.value = value;
    }

    /** @return 配置 ID 字符串值 */
    public String getValue() {
        return value;
    }
}
