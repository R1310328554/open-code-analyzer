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
 * 密钥库内私钥条目的属性定义。
 *
 * <p>定义从 KeyStore 加载私钥所需的别名与解密密码。</p>
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class KeyStorePrivateKeyDefinition {
    /** 私钥在密钥库中的别名。 */
    static final SimpleAttributeDefinition PRIVATE_KEY_ALIAS =
            new SimpleAttributeDefinitionBuilder(Constants.Model.PRIVATE_KEY_ALIAS, ModelType.STRING, true)
                    .setXmlName(Constants.XML.PRIVATE_KEY_ALIAS)
                    .build();

    /** 私钥条目的访问/解密密码。 */
    static final SimpleAttributeDefinition PRIVATE_KEY_PASSWORD =
            new SimpleAttributeDefinitionBuilder(Constants.Model.PRIVATE_KEY_PASSWORD, ModelType.STRING, true)
                    .setXmlName(Constants.XML.PRIVATE_KEY_PASSWORD)
                    .build();

    /** 私钥相关 XML 属性集合。 */
    static final SimpleAttributeDefinition[] ATTRIBUTES = {PRIVATE_KEY_ALIAS, PRIVATE_KEY_PASSWORD};

    /** XML 属性名到定义对象的查找表。 */
    static final HashMap<String, SimpleAttributeDefinition> ATTRIBUTE_MAP = new HashMap<>();

    static {
        for (SimpleAttributeDefinition def : ATTRIBUTES) {
            ATTRIBUTE_MAP.put(def.getXmlName(), def);
        }
    }

    /** 按 XML 属性名查找私钥字段定义。 */
    static SimpleAttributeDefinition lookup(String xmlName) {
        return ATTRIBUTE_MAP.get(xmlName);
    }
}
