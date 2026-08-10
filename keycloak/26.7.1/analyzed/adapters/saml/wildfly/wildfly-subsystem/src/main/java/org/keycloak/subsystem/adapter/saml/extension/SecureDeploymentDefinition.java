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

import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

/**
 * {@code secure-deployment} 管理资源定义。
 *
 * <p>将 WAR 部署单元名称映射为受 SAML 保护的部署配置节点，
 * 其下可挂载 {@link ServiceProviderDefinition} 等服务提供者子资源。</p>
 */
public class SecureDeploymentDefinition extends SimpleResourceDefinition {

    /** 单例资源定义实例。 */
    static final SecureDeploymentDefinition INSTANCE = new SecureDeploymentDefinition();

    private SecureDeploymentDefinition() {
        super(PathElement.pathElement(Constants.Model.SECURE_DEPLOYMENT),
                KeycloakSamlExtension.getResourceDescriptionResolver(Constants.Model.SECURE_DEPLOYMENT),
                SecureDeploymentAddHandler.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION, GenericSubsystemDescribeHandler.INSTANCE);
    }
}
