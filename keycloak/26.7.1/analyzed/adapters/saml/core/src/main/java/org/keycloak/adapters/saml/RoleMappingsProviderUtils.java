/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.adapters.saml;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import org.keycloak.adapters.saml.config.SP;
import org.keycloak.adapters.saml.config.parsers.ResourceLoader;

import org.jboss.logging.Logger;

/**
 * 角色映射提供者的实例化与配置工具类。
 *
 * <p>通过 Java {@link ServiceLoader} 发现 {@link RoleMappingsProvider} 实现，
 * 按 {@code keycloak-saml.xml} 中配置的 id 选择并初始化。</p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class RoleMappingsProviderUtils {

    private static final Logger logger = Logger.getLogger(RoleMappingsProviderUtils.class);

    /**
     * 加载可用的 {@link RoleMappingsProvider} 实现，选择 id 与
     * {@code keycloak-saml.xml} 配置匹配的提供者并完成初始化。
     *
     * <p>若 SP 未配置角色映射提供者则返回 {@code null}。</p>
     *
     * @param deployment 正在构建的 {@link SamlDeployment} 引用
     * @param loader 允许从 SP 应用 WAR 加载资源的 {@link ResourceLoader}
     * @param providerConfig {@code keycloak-saml.xml} 中的提供者配置；无配置属性时可为空 Properties
     * @return 已实例化并初始化的 {@link RoleMappingsProvider}，未配置时返回 {@code null}
     */
    public static RoleMappingsProvider bootstrapRoleMappingsProvider(final SamlDeployment deployment, final ResourceLoader loader, final SP.RoleMappingsProviderConfig providerConfig) {
        String providerId;
        if (providerConfig == null || providerConfig.getId() == null) {
            return null;
        } else {
            providerId = providerConfig.getId();
        }

        // 加载所有角色映射提供者，查找与配置 id 匹配的实现
        Map<String, RoleMappingsProvider> roleMappingsProviders = new HashMap<>();
        loadProviders(roleMappingsProviders, RoleMappingsProviderUtils.class.getClassLoader());
        loadProviders(roleMappingsProviders, Thread.currentThread().getContextClassLoader());

        RoleMappingsProvider provider = roleMappingsProviders.get(providerId);
        if (provider == null) {
            throw new RuntimeException("Couldn't find RoleMappingsProvider implementation class with id: " + providerId +
                    ". Loaded role mappings providers: " + roleMappingsProviders.keySet());
        }

        provider.init(deployment, loader, providerConfig != null ? providerConfig.getConfiguration() : new Properties());
        return provider;
    }

    /**
     * 使用指定 {@link ClassLoader} 通过 ServiceLoader 加载 {@link RoleMappingsProvider} 实现。
     *
     * @param providers 按 id 存储已加载提供者的映射
     * @param classLoader 用于加载实现类的类加载器
     */
    private static void loadProviders(Map<String, RoleMappingsProvider> providers, ClassLoader classLoader) {
        for (RoleMappingsProvider provider : ServiceLoader.load(RoleMappingsProvider.class, classLoader)) {
            logger.debugf("Loaded RoleMappingsProvider %s", provider.getId());
            providers.put(provider.getId(), provider);
        }
    }
}
