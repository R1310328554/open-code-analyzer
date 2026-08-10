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

package org.keycloak.adapters.saml.config.parsers;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.adapters.saml.config.Key;
import org.keycloak.adapters.saml.config.Key.KeyStoreConfig;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 {@code <KeyStore>} 元素，配置 Java KeyStore 路径与访问凭据。
 *
 * <p>要求至少指定 file 或 resource 之一；可嵌套 Certificate/PrivateKey 别名配置。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class KeyStoreParser extends AbstractKeycloakSamlAdapterV1Parser<KeyStoreConfig> {

    /** 单例解析器实例。 */
    private static final KeyStoreParser INSTANCE = new KeyStoreParser();

    private KeyStoreParser() {
        super(KeycloakSamlAdapterV1QNames.KEY_STORE);
    }

    /** @return 共享的 {@link KeyStoreParser} 实例 */
    public static KeyStoreParser getInstance() {
        return INSTANCE;
    }

    /** 从属性读取类型、别名、文件/资源路径及密码，并校验路径已配置。 */
    @Override
    protected KeyStoreConfig instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        final KeyStoreConfig keyStore = new Key.KeyStoreConfig();
        keyStore.setType(StaxParserUtil.getAttributeValueRP(element, KeycloakSamlAdapterV1QNames.ATTR_TYPE));
        keyStore.setAlias(StaxParserUtil.getAttributeValueRP(element, KeycloakSamlAdapterV1QNames.ATTR_ALIAS));
        keyStore.setFile(StaxParserUtil.getAttributeValueRP(element, KeycloakSamlAdapterV1QNames.ATTR_FILE));
        keyStore.setResource(StaxParserUtil.getAttributeValueRP(element, KeycloakSamlAdapterV1QNames.ATTR_RESOURCE));
        keyStore.setPassword(StaxParserUtil.getRequiredAttributeValueRP(element, KeycloakSamlAdapterV1QNames.ATTR_PASSWORD));

        if (keyStore.getFile() == null && keyStore.getResource() == null) {
            throw new ParsingException("KeyStore element must have the url or classpath attribute set");
        }

        return keyStore;
    }

    /** 解析 Certificate 或 PrivateKey 子元素，设置条目别名与私钥密码。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, KeyStoreConfig target, KeycloakSamlAdapterV1QNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case CERTIFICATE:
                target.setCertificateAlias(StaxParserUtil.getRequiredAttributeValueRP(elementDetail, KeycloakSamlAdapterV1QNames.ATTR_ALIAS));
                break;

            case PRIVATE_KEY:
                target.setPrivateKeyAlias(StaxParserUtil.getRequiredAttributeValueRP(elementDetail, KeycloakSamlAdapterV1QNames.ATTR_ALIAS));
                target.setPrivateKeyPassword(StaxParserUtil.getRequiredAttributeValueRP(elementDetail, KeycloakSamlAdapterV1QNames.ATTR_PASSWORD));
                break;
        }
    }
}
