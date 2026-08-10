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

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.as.web.common.WarMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.LoginConfigMetaData;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoader;

/**
 * 为 Keycloak SAML 受保护部署向模块规范注入必需的 JBoss 模块依赖。
 *
 * <p>当子系统配置了 {@code secure-deployment}，或 web.xml 声明
 * {@code KEYCLOAK-SAML} 认证方式时，添加核心适配器与公共模块；
 * 平台相关模块由子类 {@link KeycloakDependencyProcessorWildFly} 补充。</p>
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2013 Red Hat Inc.
 */
public abstract class KeycloakDependencyProcessor implements DeploymentUnitProcessor {

    /** JBoss 适配器核心模块标识。 */
    static final String KEYCLOAK_JBOSS_CORE_ADAPTER = "org.keycloak.keycloak-jboss-adapter-core";
    /** SAML 适配器核心实现模块。 */
    static final String KEYCLOAK_CORE_ADAPTER = "org.keycloak.keycloak-saml-adapter-core";
    /** SAML 适配器公共 API 模块。 */
    static final String KEYCLOAK_API_ADAPTER = "org.keycloak.keycloak-saml-adapter-api-public";
    /** Keycloak 公共工具模块。 */
    static final String KEYCLOAK_COMMON = "org.keycloak.keycloak-common";

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        if (Configuration.INSTANCE.getSecureDeployment(deploymentUnit) == null) {
            WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
            if (warMetaData == null) {
                return;
            }
            JBossWebMetaData webMetaData = warMetaData.getMergedJBossWebMetaData();
            if (webMetaData == null) {
                return;
            }
            LoginConfigMetaData loginConfig = webMetaData.getLoginConfig();
            if (loginConfig == null) return;
            if (loginConfig.getAuthMethod() == null) return;
            if (!loginConfig.getAuthMethod().equals("KEYCLOAK-SAML")) return;
        }

         // 下一阶段需识别 Keycloak 部署；非 SAML 部署不注入模块

        final ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
        ModuleLoader moduleLoader = Module.getCallerModuleLoader();
        if (moduleLoader == null) {
            moduleLoader = Module.getSystemModuleLoader();
        }

        addCoreModules(moduleSpecification, moduleLoader);
        addCommonModules(moduleSpecification, moduleLoader);
        addPlatformSpecificModules(phaseContext, moduleSpecification, moduleLoader);
    }

    /** 添加 SAML 适配器核心模块依赖（子类可覆盖以调整导出策略）。 */
    protected void addCoreModules(ModuleSpecification moduleSpecification, ModuleLoader moduleLoader) {
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, KEYCLOAK_CORE_ADAPTER, false, false, false, false));
    }

    /** 添加 JBoss 核心、API 与 common 等共用模块依赖。 */
    private void addCommonModules(ModuleSpecification moduleSpecification, ModuleLoader moduleLoader) {
        // ModuleDependency(ModuleLoader moduleLoader, ModuleIdentifier identifier, boolean optional, boolean export, boolean importServices, boolean userSpecified)
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, KEYCLOAK_JBOSS_CORE_ADAPTER, false, false, false, false));
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, KEYCLOAK_API_ADAPTER, false, false, false, false));
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, KEYCLOAK_COMMON, false, false, false, false));
    }

    /** 由具体 WildFly 版本实现注入 Elytron 等平台模块。 */
    abstract protected void addPlatformSpecificModules(DeploymentPhaseContext phaseContext, ModuleSpecification moduleSpecification, ModuleLoader moduleLoader);

    @Override
    public void undeploy(DeploymentUnit du) {

    }

}
