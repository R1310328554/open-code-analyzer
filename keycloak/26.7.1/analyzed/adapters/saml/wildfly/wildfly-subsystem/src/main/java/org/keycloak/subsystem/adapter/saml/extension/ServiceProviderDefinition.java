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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.AttributeMarshallers;
import org.jboss.as.controller.ListAttributeDefinition;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.PropertiesAttributeDefinition;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.StringListAttributeDefinition;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;

/**
 * SAML 子系统中 SP（服务提供者）资源的 WildFly 管理资源定义。
 *
 * <p>注册 SSL 策略、NameID 格式、登出页、强制认证、被动模式、角色映射等属性，
 * 写操作需触发服务器 reload。</p>
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class ServiceProviderDefinition extends SimpleResourceDefinition {

    /** SSL/TLS 客户端策略名称。 */
    private static final SimpleAttributeDefinition SSL_POLICY =
            new SimpleAttributeDefinitionBuilder(Constants.Model.SSL_POLICY, ModelType.STRING, true)
                    .setXmlName(Constants.XML.SSL_POLICY)
                    .build();

    /** SAML NameID 策略格式（如 emailAddress、persistent 等）。 */
    private static final SimpleAttributeDefinition NAME_ID_POLICY_FORMAT =
            new SimpleAttributeDefinitionBuilder(Constants.Model.NAME_ID_POLICY_FORMAT, ModelType.STRING, true)
                    .setXmlName(Constants.XML.NAME_ID_POLICY_FORMAT)
                    .build();

    /** 本地登出完成后跳转的页面 URL。 */
    private static final SimpleAttributeDefinition LOGOUT_PAGE =
            new SimpleAttributeDefinitionBuilder(Constants.Model.LOGOUT_PAGE, ModelType.STRING, true)
                    .setXmlName(Constants.XML.LOGOUT_PAGE)
                    .build();

    /** 是否在每次 SSO 请求中强制重新认证。 */
    private static final SimpleAttributeDefinition FORCE_AUTHENTICATION =
            new SimpleAttributeDefinitionBuilder(Constants.Model.FORCE_AUTHENTICATION, ModelType.BOOLEAN, true)
                    .setXmlName(Constants.XML.FORCE_AUTHENTICATION)
                    .build();

    /** 解析后是否保留 SAML 断言的 DOM 结构。 */
    private static final SimpleAttributeDefinition KEEP_DOM_ASSERTION =
            new SimpleAttributeDefinitionBuilder(Constants.Model.KEEP_DOM_ASSERTION, ModelType.BOOLEAN, true)
                    .setXmlName(Constants.XML.KEEP_DOM_ASSERTION)
                    .build();

    /** 是否以被动模式发起 SSO（不强制 IdP 显示登录界面）。 */
    private static final SimpleAttributeDefinition IS_PASSIVE =
            new SimpleAttributeDefinitionBuilder(Constants.Model.IS_PASSIVE, ModelType.BOOLEAN, true)
                    .setXmlName(Constants.XML.IS_PASSIVE)
                    .build();

    /** 登录成功后是否禁止更换 HTTP 会话 ID。 */
    private static final SimpleAttributeDefinition TURN_OFF_CHANGE_SESSSION_ID_ON_LOGIN =
            new SimpleAttributeDefinitionBuilder(Constants.Model.TURN_OFF_CHANGE_SESSSION_ID_ON_LOGIN, ModelType.BOOLEAN, true)
                    .setXmlName(Constants.XML.TURN_OFF_CHANGE_SESSSION_ID_ON_LOGIN)
                    .build();

    /** 是否自动检测 Bearer-only 资源（如 REST API）并跳过浏览器重定向。 */
    private static final SimpleAttributeDefinition AUTODETECT_BEARER_ONLY =
            new SimpleAttributeDefinitionBuilder(Constants.Model.AUTODETECT_BEARER_ONLY, ModelType.BOOLEAN, true)
                    .setXmlName(Constants.XML.AUTODETECT_BEARER_ONLY)
                    .setAllowExpression(true)
                    .build();

    /** 主体名称映射策略（如 BY_NAME 或 BY_ATTRIBUTE）。 */
    static final SimpleAttributeDefinition PRINCIPAL_NAME_MAPPING_POLICY =
            new SimpleAttributeDefinitionBuilder(Constants.Model.PRINCIPAL_NAME_MAPPING_POLICY, ModelType.STRING, true)
                    .setXmlName(Constants.XML.PRINCIPAL_NAME_MAPPING_POLICY)
                    .build();

    /** 按 SAML 断言属性映射主体名称时使用的属性名。 */
    static final SimpleAttributeDefinition PRINCIPAL_NAME_MAPPING_ATTRIBUTE_NAME =
            new SimpleAttributeDefinitionBuilder(Constants.Model.PRINCIPAL_NAME_MAPPING_ATTRIBUTE_NAME, ModelType.STRING, true)
                    .setXmlName(Constants.XML.PRINCIPAL_NAME_MAPPING_ATTRIBUTE_NAME)
                    .build();

    /** 从 SAML 断言中提取角色的属性名列表。 */
    static final ListAttributeDefinition ROLE_ATTRIBUTES =
            new StringListAttributeDefinition.Builder(Constants.Model.ROLE_ATTRIBUTES)
                    .setRequired(false)
                    .build();

    /** 角色映射 SPI 提供者的标识符。 */
    static final SimpleAttributeDefinition ROLE_MAPPINGS_PROVIDER_ID =
            new SimpleAttributeDefinitionBuilder(Constants.Model.ROLE_MAPPINGS_PROVIDER_ID, ModelType.STRING, true)
                    .setXmlName(Constants.XML.ID)
                    .build();

    /** 角色映射 SPI 提供者的键值对配置。 */
    static final PropertiesAttributeDefinition ROLE_MAPPINGS_PROVIDER_CONFIG =
            new PropertiesAttributeDefinition.Builder(Constants.Model.ROLE_MAPPINGS_PROVIDER_CONFIG, true)
                    .setAttributeMarshaller(new AttributeMarshallers.PropertiesAttributeMarshaller(null, Constants.XML.PROPERTY, false))
                    .build();

    /** 可直接映射为 XML 属性的 SP 配置项。 */
    static final SimpleAttributeDefinition[] ATTRIBUTES = {SSL_POLICY, NAME_ID_POLICY_FORMAT, LOGOUT_PAGE, FORCE_AUTHENTICATION,
            IS_PASSIVE, TURN_OFF_CHANGE_SESSSION_ID_ON_LOGIN, KEEP_DOM_ASSERTION, AUTODETECT_BEARER_ONLY};

    /** 嵌套元素或复合类型的 SP 配置项。 */
    static final AttributeDefinition[] ELEMENTS = {PRINCIPAL_NAME_MAPPING_POLICY, PRINCIPAL_NAME_MAPPING_ATTRIBUTE_NAME, ROLE_ATTRIBUTES,
            ROLE_MAPPINGS_PROVIDER_ID, ROLE_MAPPINGS_PROVIDER_CONFIG};


    private static final HashMap<String, SimpleAttributeDefinition> ATTRIBUTE_MAP = new HashMap<>();
    private static final HashMap<String, AttributeDefinition> ALL_MAP = new HashMap<>();
    /** 全部可读写的 SP 属性集合（不可变）。 */
    static final Collection<AttributeDefinition> ALL_ATTRIBUTES;

    static {
        for (SimpleAttributeDefinition def : ATTRIBUTES) {
            ATTRIBUTE_MAP.put(def.getXmlName(), def);
        }

        ALL_MAP.putAll(ATTRIBUTE_MAP);
        for (AttributeDefinition def : ELEMENTS) {
            ALL_MAP.put(def.getXmlName(), def);
        }
        ALL_ATTRIBUTES = Collections.unmodifiableCollection(ALL_MAP.values());
    }

    /** 单例资源定义实例。 */
    static final ServiceProviderDefinition INSTANCE = new ServiceProviderDefinition();

    private ServiceProviderDefinition() {
        super(PathElement.pathElement(Constants.Model.SERVICE_PROVIDER),
                KeycloakSamlExtension.getResourceDescriptionResolver(Constants.Model.SERVICE_PROVIDER),
                ServiceProviderAddHandler.INSTANCE,
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

    /** 按 XML 元素/属性名查找对应的简单属性定义。 */
    static SimpleAttributeDefinition lookup(String xmlName) {
        return ATTRIBUTE_MAP.get(xmlName);
    }
}
