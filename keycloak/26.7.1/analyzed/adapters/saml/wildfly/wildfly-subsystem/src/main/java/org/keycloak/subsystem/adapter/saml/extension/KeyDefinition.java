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

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.ObjectTypeAttributeDefinition;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;

/**
 * SAML 密钥（{@code key}）资源定义。
 *
 * <p>在 WildFly 管理模型中描述单个密钥条目，支持签名/加密用途、PEM 内联材料
 * 以及嵌套的 {@link KeyStoreDefinition} 配置。</p>
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class KeyDefinition extends SimpleResourceDefinition {

    /** 是否用于 SAML 签名。 */
    static final SimpleAttributeDefinition SIGNING =
            new SimpleAttributeDefinitionBuilder(Constants.Model.SIGNING, ModelType.BOOLEAN, true)
                    .setXmlName(Constants.XML.SIGNING)
                    .build();

    /** 是否用于 SAML 加密。 */
    static final SimpleAttributeDefinition ENCRYPTION =
            new SimpleAttributeDefinitionBuilder(Constants.Model.ENCRYPTION, ModelType.BOOLEAN, true)
                    .setXmlName(Constants.XML.ENCRYPTION)
                    .build();

    /** PEM 编码的私钥内容（XML 子元素）。 */
    static final SimpleAttributeDefinition PRIVATE_KEY_PEM =
            new SimpleAttributeDefinitionBuilder(Constants.Model.PRIVATE_KEY_PEM, ModelType.STRING, true)
                    .setXmlName(Constants.XML.PRIVATE_KEY_PEM)
                    .build();

    /** PEM 编码的公钥内容（XML 子元素）。 */
    static final SimpleAttributeDefinition PUBLIC_KEY_PEM =
            new SimpleAttributeDefinitionBuilder(Constants.Model.PUBLIC_KEY_PEM, ModelType.STRING, true)
                    .setXmlName(Constants.XML.PUBLIC_KEY_PEM)
                    .build();

    /** PEM 编码的证书内容（XML 子元素）。 */
    static final SimpleAttributeDefinition CERTIFICATE_PEM =
            new SimpleAttributeDefinitionBuilder(Constants.Model.CERTIFICATE_PEM, ModelType.STRING, true)
                    .setXmlName(Constants.XML.CERTIFICATE_PEM)
                    .build();

    /** 嵌套密钥库对象类型属性。 */
    static final ObjectTypeAttributeDefinition KEY_STORE =
            ObjectTypeAttributeDefinition.Builder.of(Constants.Model.KEY_STORE,
                    KeyStoreDefinition.ALL_ATTRIBUTES)
                    .setRequired(false)
                    .build();

    /** XML 属性级字段集合。 */
    static final SimpleAttributeDefinition[] ATTRIBUTES = {SIGNING, ENCRYPTION};
    /** XML 子元素级字段集合。 */
    static final SimpleAttributeDefinition[] ELEMENTS = {PRIVATE_KEY_PEM, PUBLIC_KEY_PEM, CERTIFICATE_PEM};
    /** 全部可读写字段（属性 + 元素 + 密钥库）。 */
    static final AttributeDefinition[] ALL_ATTRIBUTES = {SIGNING, ENCRYPTION, PRIVATE_KEY_PEM, PUBLIC_KEY_PEM, CERTIFICATE_PEM, KEY_STORE};

    /** XML 属性名到定义对象的查找表。 */
    static final HashMap<String, SimpleAttributeDefinition> ATTRIBUTE_MAP = new HashMap<>();

    static {
        for (SimpleAttributeDefinition def : ATTRIBUTES) {
            ATTRIBUTE_MAP.put(def.getXmlName(), def);
        }
    }

    /** XML 子元素名到定义对象的查找表。 */
    static final HashMap<String, SimpleAttributeDefinition> ELEMENT_MAP = new HashMap<>();

    static {
        for (SimpleAttributeDefinition def : ELEMENTS) {
            ELEMENT_MAP.put(def.getXmlName(), def);
        }
    }

    /** 单例资源定义实例。 */
    static final KeyDefinition INSTANCE = new KeyDefinition();

    private KeyDefinition() {
        super(PathElement.pathElement(Constants.Model.KEY),
                KeycloakSamlExtension.getResourceDescriptionResolver(Constants.Model.KEY),
                new KeyAddHandler(),
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION, GenericSubsystemDescribeHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);

        final OperationStepHandler writeHandler = new ReloadRequiredWriteAttributeHandler(ALL_ATTRIBUTES);
        for (AttributeDefinition attribute : ALL_ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(attribute, null, writeHandler);
        }
    }

    /** 按 XML 属性名查找字段定义。 */
    static SimpleAttributeDefinition lookup(String xmlName) {
        return ATTRIBUTE_MAP.get(xmlName);
    }

    /** 按 XML 子元素名查找字段定义。 */
    static SimpleAttributeDefinition lookupElement(String xmlName) {
        return ELEMENT_MAP.get(xmlName);
    }
}
