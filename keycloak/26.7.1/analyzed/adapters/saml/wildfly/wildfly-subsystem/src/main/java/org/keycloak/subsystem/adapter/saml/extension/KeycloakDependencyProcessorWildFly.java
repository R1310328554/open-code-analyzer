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

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.modules.ModuleLoader;

import static org.keycloak.subsystem.adapter.saml.extension.Elytron.isElytronEnabled;

/**
 * WildFly 平台的 Keycloak SAML 模块依赖处理器。
 *
 * <p>在核心 SAML 模块之外，根据 Elytron 是否启用注入
 * {@code org.keycloak.keycloak-saml-wildfly-elytron-adapter}；旧版 PicketBox 安全层已不再支持。</p>
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2014 Red Hat Inc.
 */
public class KeycloakDependencyProcessorWildFly extends KeycloakDependencyProcessor {

    /** WildFly Elytron SAML 适配器模块标识。 */
    private static final String KEYCLOAK_ELYTRON_ADAPTER = "org.keycloak.keycloak-saml-wildfly-elytron-adapter";

    @Override
    protected void addCoreModules(ModuleSpecification moduleSpecification, ModuleLoader moduleLoader) {
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, KEYCLOAK_CORE_ADAPTER, false, false, false, false));
    }

    @Override
    protected void addPlatformSpecificModules(DeploymentPhaseContext phaseContext, ModuleSpecification moduleSpecification, ModuleLoader moduleLoader) {
        if (isElytronEnabled(phaseContext)) {
            // Elytron 模式下可选依赖 Elytron 适配器模块
            moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, KEYCLOAK_ELYTRON_ADAPTER, true, false, false, false));
        } else {
            throw new RuntimeException("Legacy WildFly security layer is no longer supported by the Keycloak WildFly adapter");
        }
    }

    /** 通过模块类加载器名称判断是否运行在 Jakarta EE 命名空间。 */
    private boolean isJakarta() {
        ClassLoader classLoader = getClass().getClassLoader();
        String classLoaderName = (classLoader instanceof ModuleClassLoader ? ((ModuleClassLoader) classLoader).getName() : "");
        return classLoaderName.contains("jakarta");
    }
}
