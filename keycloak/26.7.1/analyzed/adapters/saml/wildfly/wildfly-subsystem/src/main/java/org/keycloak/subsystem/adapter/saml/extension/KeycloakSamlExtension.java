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

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;


/**
 * Keycloak SAML WildFly 子系统扩展入口。
 *
 * <p>注册 {@code keycloak-saml} 子系统的 XML 命名空间解析、管理模型层级
 * （secure-deployment → service-provider/identity-provider → key）及 XML 写回器。</p>
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2013 Red Hat Inc.
 */
public class KeycloakSamlExtension implements Extension {

    /** 子系统在 standalone.xml 中的名称。 */
    static final String SUBSYSTEM_NAME = "keycloak-saml";
    /** 1.1 版 XML 命名空间 URI。 */
    private static final String NAMESPACE_1_1 = "urn:jboss:domain:keycloak-saml:1.1";
    /** 1.2 版 XML 命名空间 URI。 */
    private static final String NAMESPACE_1_2 = "urn:jboss:domain:keycloak-saml:1.2";
    /** 1.3 版 XML 命名空间 URI。 */
    private static final String NAMESPACE_1_3 = "urn:jboss:domain:keycloak-saml:1.3";
    /** 1.4 版 XML 命名空间 URI（当前写入版本）。 */
    private static final String NAMESPACE_1_4 = "urn:jboss:domain:keycloak-saml:1.4";

    /** 序列化/反序列化使用的当前命名空间。 */
    static final String CURRENT_NAMESPACE = NAMESPACE_1_4;
    private static final KeycloakSubsystemParser PARSER = new KeycloakSubsystemParser();
    /** 子系统在管理树中的路径元素。 */
    static final PathElement PATH_SUBSYSTEM = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);
    private static final String RESOURCE_NAME = KeycloakSamlExtension.class.getPackage().getName() + ".LocalDescriptions";
    private static final ModelVersion MGMT_API_VERSION = ModelVersion.create(1, 1, 0);
    static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);

    /** 构建带前缀的 i18n 资源描述解析器。 */
    static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String... keyPrefix) {
        StringBuilder prefix = new StringBuilder(SUBSYSTEM_NAME);
        for (String kp : keyPrefix) {
            prefix.append('.').append(kp);
        }
        return new StandardResourceDescriptionResolver(prefix.toString(), RESOURCE_NAME, KeycloakSamlExtension.class.getClassLoader(), true, false);
    }

    /**
     * 注册各版本子系统 XML 命名空间到 {@link KeycloakSubsystemParser} 的映射。
     * {@inheritDoc}
     */
    @Override
    public void initializeParsers(final ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, KeycloakSamlExtension.NAMESPACE_1_1, PARSER);
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, KeycloakSamlExtension.NAMESPACE_1_2, PARSER);
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, KeycloakSamlExtension.NAMESPACE_1_3, PARSER);
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, KeycloakSamlExtension.NAMESPACE_1_4, PARSER);
    }

    /**
     * 注册子系统根资源及 secure-deployment / service-provider / key / identity-provider 子模型。
     * {@inheritDoc}
     */
    @Override
    public void initialize(final ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, MGMT_API_VERSION);

        ManagementResourceRegistration registration = subsystem.registerSubsystemModel(KeycloakSubsystemDefinition.INSTANCE);
        ManagementResourceRegistration secureDeploymentRegistration = registration.registerSubModel(SecureDeploymentDefinition.INSTANCE);
        ManagementResourceRegistration serviceProviderRegistration = secureDeploymentRegistration.registerSubModel(ServiceProviderDefinition.INSTANCE);
        serviceProviderRegistration.registerSubModel(KeyDefinition.INSTANCE);
        ManagementResourceRegistration idpRegistration = serviceProviderRegistration.registerSubModel(IdentityProviderDefinition.INSTANCE);
        idpRegistration.registerSubModel(KeyDefinition.INSTANCE);

        subsystem.registerXMLElementWriter(PARSER);
    }
}
