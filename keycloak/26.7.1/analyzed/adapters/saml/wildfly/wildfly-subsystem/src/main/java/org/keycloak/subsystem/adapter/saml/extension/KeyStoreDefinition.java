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
package org.keycloak.subsystem.adapter.saml.extension;

import java.util.HashMap;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.dmr.ModelType;

/**
 * 密钥库（{@code key-store}）配置的抽象属性定义基类。
 *
 * <p>汇总文件/资源路径、类型、别名及嵌套私钥、证书子配置，
 * 供 {@link KeyDefinition} 与 {@link KeycloakSubsystemParser} 共用。</p>
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
abstract class KeyStoreDefinition {

    /** 类路径资源位置（与 {@link #FILE} 二选一）。 */
    static final SimpleAttributeDefinition RESOURCE =
            new SimpleAttributeDefinitionBuilder(Constants.Model.RESOURCE, ModelType.STRING, true)
                    .setXmlName(Constants.XML.RESOURCE)
                    .build();

    /** 密钥库访问密码。 */
    static final SimpleAttributeDefinition PASSWORD =
            new SimpleAttributeDefinitionBuilder(Constants.Model.PASSWORD, ModelType.STRING, true)
                    .setXmlName(Constants.XML.PASSWORD)
                    .build();

    /** 文件系统上的密钥库路径（与 {@link #RESOURCE} 二选一）。 */
    static final SimpleAttributeDefinition FILE =
            new SimpleAttributeDefinitionBuilder(Constants.Model.FILE, ModelType.STRING, true)
                    .setXmlName(Constants.XML.FILE)
                    .build();

    /** 密钥库类型（如 JKS、PKCS12）。 */
    static final SimpleAttributeDefinition TYPE =
            new SimpleAttributeDefinitionBuilder(Constants.Model.TYPE, ModelType.STRING, true)
                    .setXmlName(Constants.XML.TYPE)
                    .build();

    /** 默认密钥条目别名。 */
    static final SimpleAttributeDefinition ALIAS =
            new SimpleAttributeDefinitionBuilder(Constants.Model.ALIAS, ModelType.STRING, true)
                    .setXmlName(Constants.XML.ALIAS)
                    .build();

    /** 密钥库顶层 XML 属性。 */
    static final SimpleAttributeDefinition[] ATTRIBUTES = {RESOURCE, PASSWORD, FILE, TYPE, ALIAS};
    /** 含嵌套私钥与证书别名的完整属性集。 */
    static final SimpleAttributeDefinition[] ALL_ATTRIBUTES = {RESOURCE, PASSWORD, FILE, TYPE, ALIAS,
            KeyStorePrivateKeyDefinition.PRIVATE_KEY_ALIAS,
            KeyStorePrivateKeyDefinition.PRIVATE_KEY_PASSWORD,
            KeyStoreCertificateDefinition.CERTIFICATE_ALIAS
    };

    /** XML 属性名到定义对象的查找表。 */
    static final HashMap<String, SimpleAttributeDefinition> ATTRIBUTE_MAP = new HashMap<>();

    static {
        for (SimpleAttributeDefinition def : ATTRIBUTES) {
            ATTRIBUTE_MAP.put(def.getXmlName(), def);
        }
    }

    /** 按 XML 属性名查找密钥库字段定义。 */
    static SimpleAttributeDefinition lookup(String xmlName) {
        return ATTRIBUTE_MAP.get(xmlName);
    }
}
