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

import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.RuntimePackageDependency;

import static org.keycloak.subsystem.adapter.saml.extension.KeycloakDependencyProcessor.KEYCLOAK_JBOSS_CORE_ADAPTER;

/**
 * {@code subsystem=keycloak-saml} 根资源定义。
 *
 * <p>注册子系统级 describe 操作，并声明部署运行时所需的
 * {@link KeycloakDependencyProcessor#KEYCLOAK_JBOSS_CORE_ADAPTER} 附加模块包依赖。</p>
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2013 Red Hat Inc.
 */
public class KeycloakSubsystemDefinition extends SimpleResourceDefinition {

    /** 单例子系统资源定义。 */
    static final KeycloakSubsystemDefinition INSTANCE = new KeycloakSubsystemDefinition();

    private KeycloakSubsystemDefinition() {
        super(KeycloakSamlExtension.SUBSYSTEM_PATH,
                KeycloakSamlExtension.getResourceDescriptionResolver("subsystem"),
                KeycloakSubsystemAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE
        );
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION, GenericSubsystemDescribeHandler.INSTANCE);
    }

    @Override
    public void registerAdditionalRuntimePackages(ManagementResourceRegistration resourceRegistration) {
        // 部署单元需要此模块，但 JBoss Modules 依赖链中未显式引用
        resourceRegistration.registerAdditionalRuntimePackages(
                RuntimePackageDependency.required(KEYCLOAK_JBOSS_CORE_ADAPTER));
    }
}
