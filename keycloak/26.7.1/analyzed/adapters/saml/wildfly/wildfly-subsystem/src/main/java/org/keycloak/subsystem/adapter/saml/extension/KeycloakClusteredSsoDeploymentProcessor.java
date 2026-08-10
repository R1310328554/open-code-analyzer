/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.keycloak.adapters.saml.AdapterConstants;

import org.jboss.as.controller.capability.CapabilityServiceSupport;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.common.WarMetaData;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.LoginConfigMetaData;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * 为可分发（集群）SAML 部署配置 Infinispan SSO 缓存复制参数。
 *
 * <p>当 WAR 标记为 distributable 且使用 Keycloak SAML 认证时，
 * 解析会话/SSO 缓存容器与缓存名，写入上下文参数并声明 MSC 服务依赖。</p>
 *
 * @author hmlnarik
 */
public class KeycloakClusteredSsoDeploymentProcessor implements DeploymentUnitProcessor {

    private static final Logger LOG = Logger.getLogger(KeycloakClusteredSsoDeploymentProcessor.class);

    /** 未显式配置 replication-config 时的默认 Web 缓存容器名。 */
    private static final String DEFAULT_CACHE_CONTAINER = "web";
    /** 覆盖 SSO 缓存容器名的上下文参数键。 */
    private static final String SSO_CACHE_CONTAINER_NAME_PARAM_NAME = "keycloak.sessionIdMapperUpdater.infinispan.containerName";
    /** 覆盖 SSO 缓存名的上下文参数键。 */
    private static final String SSO_CACHE_NAME_PARAM_NAME = "keycloak.sessionIdMapperUpdater.infinispan.cacheName";

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        if (isKeycloakSamlAuthMethod(deploymentUnit) && isDistributable(deploymentUnit)) {
            addSamlReplicationConfiguration(deploymentUnit, phaseContext);
        }
    }

    /** 判断 WAR 是否配置了 distributable 或 replication-config（集群会话复制）。 */
    public static boolean isDistributable(final DeploymentUnit deploymentUnit) {
        WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
        if (warMetaData == null) {
            return false;
        }
        JBossWebMetaData webMetaData = warMetaData.getMergedJBossWebMetaData();
        if (webMetaData == null) {
            return false;
        }

        return webMetaData.getDistributable() != null || webMetaData.getReplicationConfig() != null;
    }

    /** 判断部署是否由 Keycloak SAML 子系统或 web.xml 登录配置保护。 */
    public static boolean isKeycloakSamlAuthMethod(final DeploymentUnit deploymentUnit) {
        if (Configuration.INSTANCE.getSecureDeployment(deploymentUnit) != null) {
            return true;
        }

        WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
        if (warMetaData == null) {
            return false;
        }
        JBossWebMetaData webMetaData = warMetaData.getMergedJBossWebMetaData();
        if (webMetaData == null) {
            return false;
        }

        LoginConfigMetaData loginConfig = webMetaData.getLoginConfig();

        return loginConfig != null && Objects.equals(loginConfig.getAuthMethod(), "KEYCLOAK-SAML");
    }

    @Override
    public void undeploy(DeploymentUnit du) {
        
    }

    /** 解析并写入 SSO 复制所需的缓存容器名、缓存名及适配器上下文参数。 */
    private void addSamlReplicationConfiguration(DeploymentUnit deploymentUnit, DeploymentPhaseContext context) {
        WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
        if (warMetaData == null) {
            return;
        }

        JBossWebMetaData webMetaData = warMetaData.getMergedJBossWebMetaData();
        if (webMetaData == null) {
            webMetaData = new JBossWebMetaData();
            warMetaData.setMergedJBossWebMetaData(webMetaData);
        }

        // Find out default names of cache container and cache
        String cacheContainer = DEFAULT_CACHE_CONTAINER;
        String deploymentSessionCacheName =
          (deploymentUnit.getParent() == null
              ? ""
              : deploymentUnit.getParent().getName() + ".")
          + deploymentUnit.getName();

        // 从 jboss-web.xml 的 <replicationConfig> 覆盖默认缓存容器与部署会话缓存名
        if (webMetaData.getReplicationConfig() != null && webMetaData.getReplicationConfig().getCacheName() != null) {
            ServiceName sn = ServiceName.parse(webMetaData.getReplicationConfig().getCacheName());
            cacheContainer = (sn.length() > 1) ? sn.getParent().getSimpleName() : sn.getSimpleName();
            deploymentSessionCacheName = sn.getSimpleName();
        }
        String ssoCacheName = deploymentSessionCacheName + ".ssoCache";

        // 上下文参数可再次覆盖上述解析结果
        List<ParamValueMetaData> contextParams = webMetaData.getContextParams();
        if (contextParams == null) {
            contextParams = new ArrayList<>();
        }
        for (ParamValueMetaData contextParam : contextParams) {
            if (Objects.equals(contextParam.getParamName(), SSO_CACHE_CONTAINER_NAME_PARAM_NAME)) {
                cacheContainer = contextParam.getParamValue();
            } else if (Objects.equals(contextParam.getParamName(), SSO_CACHE_NAME_PARAM_NAME)) {
                ssoCacheName = contextParam.getParamValue();
            }
        }

        LOG.debugv("Determined SSO cache container configuration: container: {0}, cache: {1}", cacheContainer, ssoCacheName);
        addCacheDependency(context, deploymentUnit, cacheContainer, ssoCacheName);

        // 写入适配器读取复制配置所需的上下文参数
        ParamValueMetaData paramContainer = new ParamValueMetaData();
        paramContainer.setParamName(AdapterConstants.REPLICATION_CONFIG_CONTAINER_PARAM_NAME);
        paramContainer.setParamValue(cacheContainer);
        contextParams.add(paramContainer);

        ParamValueMetaData paramSsoCache = new ParamValueMetaData();
        paramSsoCache.setParamName(AdapterConstants.REPLICATION_CONFIG_SSO_CACHE_PARAM_NAME);
        paramSsoCache.setParamValue(ssoCacheName);
        contextParams.add(paramSsoCache);

        webMetaData.setContextParams(contextParams);
    }

    /** 为部署单元声明对 Infinispan SSO 缓存 MSC 服务的运行时依赖。 */
    private void addCacheDependency(DeploymentPhaseContext context, DeploymentUnit deploymentUnit, String cacheContainer, String cacheName) {
        ServiceName wf10CacheContainerServiceName = ServiceName.of("jboss", "infinispan", cacheContainer);
        final ServiceController<?> wf10CacheContainerService = context.getServiceRegistry().getService(wf10CacheContainerServiceName);

        boolean legacy = wf10CacheContainerService != null;
        ServiceTarget st = context.getServiceTarget();

        if (legacy) {
            // WildFly 10 及更早版本的 Infinispan 服务命名
            ServiceName cacheServiceName = wf10CacheContainerServiceName.append(cacheName);
            ServiceController<?> cacheService = context.getServiceRegistry().getService(cacheServiceName);
            if (cacheService != null) {
                st.addDependency(cacheServiceName);
            }
        } else {
            // 新版 WildFly 通过 capability 解析集群 Infinispan 缓存服务
            CapabilityServiceSupport support = deploymentUnit.getAttachment(Attachments.CAPABILITY_SERVICE_SUPPORT);

            ServiceName cacheServiceName = support.getCapabilityServiceName("org.wildfly.clustering.infinispan.cache." + cacheContainer + "." + cacheName);
            ServiceController<?> cacheService = context.getServiceRegistry().getService(cacheServiceName);
            if (cacheService != null) {
                st.addDependency(cacheServiceName);
            }
        }
    }

}
