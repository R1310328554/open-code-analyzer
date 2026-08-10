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

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;

/**
 * {@code keycloak-saml} 子系统添加操作处理器。
 *
 * <p>在子系统安装时注册部署阶段处理器：模块依赖、适配器配置注入及集群 SSO 复制配置。</p>
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2013 Red Hat Inc.
 */
class KeycloakSubsystemAdd extends AbstractBoottimeAddStepHandler {

    /** 单例添加处理器。 */
    static final KeycloakSubsystemAdd INSTANCE = new KeycloakSubsystemAdd();

    @Override
    protected void performBoottime(final OperationContext context, ModelNode operation, final ModelNode model) {
        context.addStep(new AbstractDeploymentChainStep() {
            @Override
            protected void execute(DeploymentProcessorTarget processorTarget) {
                // DEPENDENCIES 阶段：注入 Keycloak 模块
                processorTarget.addDeploymentProcessor(KeycloakSamlExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, 0, chooseDependencyProcessor());
                // POST_MODULE 阶段：写入 SAML 适配器 XML 配置
                processorTarget.addDeploymentProcessor(KeycloakSamlExtension.SUBSYSTEM_NAME,
                        Phase.POST_MODULE, // PHASE
                        Phase.POST_MODULE_VALIDATOR_FACTORY - 1, // PRIORITY
                        chooseConfigDeploymentProcessor());
                // POST_MODULE 阶段：集群 SSO 缓存复制参数
                processorTarget.addDeploymentProcessor(KeycloakSamlExtension.SUBSYSTEM_NAME,
                        Phase.POST_MODULE, // PHASE
                        Phase.POST_MODULE_VALIDATOR_FACTORY - 1, // PRIORITY
                        chooseClusteredSsoDeploymentProcessor());
            }
        }, OperationContext.Stage.RUNTIME);
    }

    /** 选择 WildFly 平台的模块依赖处理器。 */
    private DeploymentUnitProcessor chooseDependencyProcessor() {
        return new KeycloakDependencyProcessorWildFly();
    }

    /** 选择将子系统模型写入 WAR 的部署处理器。 */
    private DeploymentUnitProcessor chooseConfigDeploymentProcessor() {
        return new KeycloakAdapterConfigDeploymentProcessor();
    }

    /** 选择集群 SSO 复制配置处理器。 */
    private DeploymentUnitProcessor chooseClusteredSsoDeploymentProcessor() {
        return new KeycloakClusteredSsoDeploymentProcessor();
    }
}
