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
package org.keycloak.saml.common.util;

import java.security.cert.X509Certificate;

/**
 * XML KeyInfo 中 KeyName 的生成策略。
 *
 * @author hmlnarik
 */
public enum XmlKeyInfoKeyNameTransformer {
    /** 不写入 KeyName。 */
    NONE            { @Override public String getKeyName(String keyId, X509Certificate certificate) { return null; } },
    /** 使用密钥 ID 作为 KeyName。 */
    KEY_ID          { @Override public String getKeyName(String keyId, X509Certificate certificate) { return keyId; } },
    /** 使用 X509 证书 Subject DN 作为 KeyName。 */
    CERT_SUBJECT    { @Override public String getKeyName(String keyId, X509Certificate certificate) {
                        return certificate == null
                               ? null
                               : (certificate.getSubjectDN() == null
                                  ? null
                                  : certificate.getSubjectDN().getName());
                    } }
    ;

    /** 根据 keyId 与证书计算 KeyName 字符串。 */
    public abstract String getKeyName(String keyId, X509Certificate certificate);

    /** 按枚举名解析策略，非法名称时返回 defaultValue。 */
    public static XmlKeyInfoKeyNameTransformer from(String name, XmlKeyInfoKeyNameTransformer defaultValue) {
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
