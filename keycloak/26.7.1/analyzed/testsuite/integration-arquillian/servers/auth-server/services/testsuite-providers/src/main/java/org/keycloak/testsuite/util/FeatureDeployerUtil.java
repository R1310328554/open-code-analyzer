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
 *
 */

package org.keycloak.testsuite.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.provider.DefaultProviderLoader;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.KeycloakDeploymentInfo;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.ProviderManager;
import org.keycloak.provider.Spi;
import org.keycloak.services.DefaultKeycloakSession;
import org.keycloak.services.DefaultKeycloakSessionFactory;
import org.keycloak.services.resources.KeycloakApplication;

import org.jboss.logging.Logger;

/**
 * 特性部署工具：在特性开关变更后动态重新加载 {@link EnvironmentDependentProviderFactory} 实例。
 * 用于集成测试中启用/禁用 Profile 特性时热部署或卸载相关提供者工厂。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FeatureDeployerUtil {

    /** 特性变更前已启用的工厂快照，按特性索引。 */
    private final static Map<Profile.Feature, Map<ProviderFactory, Spi>> initializer = new ConcurrentHashMap<>();

    /** 按特性缓存的 ProviderManager 部署器。 */
    private final static Map<Profile.Feature, ProviderManager> deployersCache = new ConcurrentHashMap<>();

    private static final Logger logger = Logger.getLogger(FeatureDeployerUtil.class);

    public static void initBeforeChangeFeature(Profile.Feature feature) {
        if (deployersCache.containsKey(feature)) {
            return;
        }

        // 计算特性变更前已启用的提供者工厂
        Map<ProviderFactory, Spi>  factoriesBefore = loadEnabledEnvironmentFactories();
        initializer.put(feature, factoriesBefore);
    }

    public static void deployFactoriesAfterFeatureEnabled(Profile.Feature feature) {
        ProviderManager manager = deployersCache.get(feature);
        if (manager == null) {
            // 确定特性启用后新增的工厂，创建部署器并缓存
            Map<ProviderFactory, Spi> factoriesBeforeEnable = initializer.remove(feature);
            Map<ProviderFactory, Spi> factoriesAfterEnable = loadEnabledEnvironmentFactories();
            Map<ProviderFactory, Spi>  factories = getFactoriesDependentOnFeature(factoriesBeforeEnable, factoriesAfterEnable);

            logger.infof("New factories when enabling feature '%s': %s", feature, factories.keySet());

            KeycloakDeploymentInfo di = createDeploymentInfo(factories);

            manager = new ProviderManager(di, FeatureDeployerUtil.class.getClassLoader(), Collections.singleton(new TestsuiteProviderLoader(di)));
            deployersCache.put(feature, manager);
        }
        deploy(manager);
    }

    public static void undeployFactoriesAfterFeatureDisabled(Profile.Feature feature) {
        ProviderManager manager = deployersCache.get(feature);
        if (manager == null) {
            // 用于默认启用的特性被禁用的场景：确定需卸载的工厂并创建部署器
            Map<ProviderFactory, Spi> factoriesBeforeDisable = initializer.remove(feature);
            Map<ProviderFactory, Spi> factoriesAfterDisable = loadEnabledEnvironmentFactories();
            Map<ProviderFactory, Spi>  factories = getFactoriesDependentOnFeature(factoriesAfterDisable, factoriesBeforeDisable);

            KeycloakDeploymentInfo di = createDeploymentInfo(factories);

            manager = new ProviderManager(di, FeatureDeployerUtil.class.getClassLoader());
            loadFactories(manager);
            deployersCache.put(feature, manager);
        }
        undeploy(manager);
    }

    private static Map<ProviderFactory, Spi> getFactoriesDependentOnFeature(Map<ProviderFactory, Spi> factoriesDisabled, Map<ProviderFactory, Spi> factoriesEnabled) {
        Set<Class<? extends ProviderFactory>> disabledFactoriesClasses = factoriesDisabled.keySet().stream()
                .map(ProviderFactory::getClass)
                .collect(Collectors.toSet());

        Set<Class<? extends ProviderFactory>> enabledFactoriesClasses = factoriesEnabled.keySet().stream()
                .map(ProviderFactory::getClass)
                .collect(Collectors.toSet());

        enabledFactoriesClasses.removeAll(disabledFactoriesClasses);

        Map<ProviderFactory, Spi> newFactories = factoriesEnabled.entrySet().stream()
                .filter(entry -> enabledFactoriesClasses.contains(entry.getKey().getClass()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return newFactories;
    }

    private static KeycloakDeploymentInfo createDeploymentInfo(Map<ProviderFactory, Spi> factories) {
        KeycloakDeploymentInfo di = KeycloakDeploymentInfo.create();
        for (Map.Entry<ProviderFactory, Spi> factory : factories.entrySet()) {
            ProviderFactory pf = factory.getKey();
            Class<? extends Spi> spiClass = factory.getValue().getClass();
            di.addProvider(spiClass, pf);
        }
        return di;
    }

    private static Map<ProviderFactory, Spi> loadEnabledEnvironmentFactories() {
        KeycloakDeploymentInfo di = KeycloakDeploymentInfo.create().services();
        ClassLoader classLoader = DefaultKeycloakSession.class.getClassLoader();
        DefaultProviderLoader loader = new DefaultProviderLoader(di, classLoader);

        Map<ProviderFactory, Spi> providerFactories = new HashMap<>();
        for (Spi spi : loader.loadSpis()) {
            Config.Scope scope = Config.scope(spi.getName(), Config.getProvider(spi.getName()));
            List<ProviderFactory> currentFactories = loader.load(spi);
            for (ProviderFactory factory : currentFactories) {
                if (factory instanceof EnvironmentDependentProviderFactory) {
                    if (((EnvironmentDependentProviderFactory) factory).isSupported(scope)) {
                        providerFactories.put(factory, spi);
                    }
                }

            }
        }

        return providerFactories;
    }

    private static void loadFactories(ProviderManager pm) {
        KeycloakDeploymentInfo di = KeycloakDeploymentInfo.create().services();
        ClassLoader classLoader = DefaultKeycloakSession.class.getClassLoader();
        DefaultProviderLoader loader = new DefaultProviderLoader(di, classLoader);
        loader.loadSpis().forEach(pm::load);
    }

    static void deploy(ProviderManager pm) {
        DefaultKeycloakSessionFactory deployer = KeycloakApplication.getSessionFactory();
        if (deployer == null) {
            throw new IllegalStateException("No active KeycloakApplication");
        }
        deployer.deploy(pm);
    }

    static void undeploy(ProviderManager pm) {
        DefaultKeycloakSessionFactory deployer = KeycloakApplication.getSessionFactory();
        if (deployer != null) {
            deployer.undeploy(pm);
        }
    }
}
