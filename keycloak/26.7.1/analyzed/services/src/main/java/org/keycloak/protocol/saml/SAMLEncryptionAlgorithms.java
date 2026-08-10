/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.crypto.Algorithm;

import org.apache.xml.security.encryption.XMLCipher;

/**
 * SAML/XML 加密算法映射枚举。
 * <p>关联 Keycloak {@link Algorithm} 标识与 Apache XMLSec {@link XMLCipher} URI，确保密钥仅用于其生成时指定的算法。</p>
 */
public enum SAMLEncryptionAlgorithms {
    /** RSA-OAEP（含 MGF1 与 OAEP 1.1 变体） */
    RSA_OAEP(Algorithm.RSA_OAEP, XMLCipher.RSA_OAEP, XMLCipher.RSA_OAEP_11),
    /** RSA PKCS#1 v1.5 */
    RSA1_5(Algorithm.RSA1_5, XMLCipher.RSA_v1dot5);

    private final String[] xmlEncIdentifier;
    private final String keycloakIdentifier;
    private static final Map<String, SAMLEncryptionAlgorithms> forKeycloakIdentifier;
    private static final Map<String, SAMLEncryptionAlgorithms> forXMLEncIdentifier;

    static {
        Map<String, SAMLEncryptionAlgorithms> forKeycloakIdentifierTmp = new HashMap<>();
        Map<String, SAMLEncryptionAlgorithms> forXMLEncIdentifierTmp = new HashMap<>();
        for (SAMLEncryptionAlgorithms alg: values()) {
            forKeycloakIdentifierTmp.put(alg.getKeycloakIdentifier(), alg);
            for (String xmlAlg : alg.getXmlEncIdentifiers()) {
                forXMLEncIdentifierTmp.put(xmlAlg, alg);
            }
        }
        forKeycloakIdentifier = Collections.unmodifiableMap(forKeycloakIdentifierTmp);
        forXMLEncIdentifier = Collections.unmodifiableMap(forXMLEncIdentifierTmp);
    }

    SAMLEncryptionAlgorithms(String keycloakIdentifier, String... xmlEncIdentifier) {
        assert xmlEncIdentifier.length > 0 : "xmlEncIdentifier should contain at least one identifier";
        this.xmlEncIdentifier = xmlEncIdentifier;
        this.keycloakIdentifier = keycloakIdentifier;
    }

    /**
     * 获取所有 XML 加密算法 URI。
     * @return xmlenc 标识符数组（至少一个）
     */
    public String[] getXmlEncIdentifiers() {
        return xmlEncIdentifier;
    }

    /**
     * 获取 Keycloak 内部算法标识。
     * @return Keycloak 算法名
     */
    public String getKeycloakIdentifier() {
        return keycloakIdentifier;
    }

    /**
     * 按 XML 加密 URI 查找枚举值。
     * @param xmlEncIdentifier xmlenc 算法 URI
     * @return 对应枚举或 null
     */
    public static SAMLEncryptionAlgorithms forXMLEncIdentifier(String xmlEncIdentifier) {
        return forXMLEncIdentifier.get(xmlEncIdentifier);
    }

    /**
     * 按 Keycloak 算法标识查找枚举值。
     * @param keycloakIdentifier Keycloak 算法名
     * @return 对应枚举或 null
     */
    public static SAMLEncryptionAlgorithms forKeycloakIdentifier(String keycloakIdentifier) {
        return forKeycloakIdentifier.get(keycloakIdentifier);
    }
}
