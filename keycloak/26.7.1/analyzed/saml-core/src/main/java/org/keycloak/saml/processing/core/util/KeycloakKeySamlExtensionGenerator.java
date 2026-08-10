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

package org.keycloak.saml.processing.core.util;

import java.util.Objects;
import javax.xml.stream.XMLStreamWriter;

import org.keycloak.saml.SamlProtocolExtensionsAwareBuilder;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.common.util.StaxUtil;

import org.w3c.dom.Element;

/**
 * Keycloak SAML 协议扩展：消息签名密钥 ID 生成器。
 * <p>在 Extensions 中写入 {@code kckey:KeyInfo} 元素，携带 MessageSigningKeyId 属性。</p>
 *
 * @author hmlnarik
 */
public class KeycloakKeySamlExtensionGenerator implements SamlProtocolExtensionsAwareBuilder.NodeGenerator {

    /** Keycloak 密钥扩展命名空间 URI。 */
    public static final String NS_URI = "urn:keycloak:ext:key:1.0";

    /** 扩展元素命名空间前缀。 */
    public static final String NS_PREFIX = "kckey";

    /** KeyInfo 扩展元素本地名。 */
    public static final String KC_KEY_INFO_ELEMENT_NAME = "KeyInfo";

    /** 消息签名密钥 ID 属性名。 */
    public static final String KEY_ID_ATTRIBUTE_NAME = "MessageSigningKeyId";

    /** 签名密钥标识符。 */
    private final String keyId;

    /**
     * 构造扩展生成器。
     *
     * @param keyId 消息签名密钥 ID
     */
    public KeycloakKeySamlExtensionGenerator(String keyId) {
        this.keyId = keyId;
    }

    /** 将 Keycloak KeyInfo 扩展写入 XML 流。 */
    @Override
    public void write(XMLStreamWriter writer) throws ProcessingException {
        StaxUtil.writeStartElement(writer, NS_PREFIX, KC_KEY_INFO_ELEMENT_NAME, NS_URI);
        StaxUtil.writeNameSpace(writer, NS_PREFIX, NS_URI);
        if (this.keyId != null) {
            StaxUtil.writeAttribute(writer, KEY_ID_ATTRIBUTE_NAME, this.keyId);
        }
        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    /**
     * 校验元素是否为 Keycloak KeyInfo 扩展，并提取 {@code MessageSigningKeyId} 属性值。
     * @param element 待解析的元素
     * @return 非扩展元素或未设置属性时返回 {@code null}，否则返回密钥 ID
     */
    public static String getMessageSigningKeyIdFromElement(Element element) {
        if (Objects.equals(element.getNamespaceURI(), NS_URI) &&
          Objects.equals(element.getLocalName(), KC_KEY_INFO_ELEMENT_NAME) &&
          element.hasAttribute(KEY_ID_ATTRIBUTE_NAME)) {
            return element.getAttribute(KEY_ID_ATTRIBUTE_NAME);
        }

        return null;
    }

}
