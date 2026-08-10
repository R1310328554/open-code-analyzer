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
package org.keycloak.provider;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.services.DefaultKeycloakSessionFactory;

import org.jboss.logging.Logger;

/**
 * Keycloak SPI 提供者管理器。
 * <p>协调多个 {@link ProviderLoader}（默认 classpath、部署内嵌及扩展 classpath），去重加载 {@link Spi} 与 {@link ProviderFactory}，并按 order/内外部优先级解析冲突。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ProviderManager {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(ProviderManager.class);

    /** 当前部署信息 */
    private final KeycloakDeploymentInfo info;
    /** 已注册的提供者加载器链 */
    private List<ProviderLoader> loaders = new LinkedList<ProviderLoader>();
    /** 按 Provider 类型缓存已加载的工厂 */
    private MultivaluedHashMap<Class<? extends Provider>, ProviderFactory> cache = new MultivaluedHashMap<>();


    /**
     * 构造管理器并解析扩展 classpath 资源。
     * @param info 部署信息
     * @param baseClassLoader 基类加载器
     * @param resources 形如 {@code classpath:/path} 的额外加载器配置
     */
    public ProviderManager(KeycloakDeploymentInfo info, ClassLoader baseClassLoader, String... resources) {
        this.info = info;
        List<ProviderLoaderFactory> factories = new LinkedList<ProviderLoaderFactory>();
        for (ProviderLoaderFactory f : ServiceLoader.load(ProviderLoaderFactory.class, getClass().getClassLoader())) {
            factories.add(f);
        }

        logger.debugv("Provider loaders {0}", factories);

        addDefaultLoaders(baseClassLoader);

        if (resources != null) {
            for (String r : resources) {
                String type = r.substring(0, r.indexOf(':'));
                String resource = r.substring(r.indexOf(':') + 1, r.length());

                boolean found = false;
                for (ProviderLoaderFactory f : factories) {
                    if (f.supports(type)) {
                        KeycloakDeploymentInfo resourceInfo = KeycloakDeploymentInfo.create().services();
                        loaders.add(f.create(resourceInfo, baseClassLoader, resource));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new RuntimeException("Provider loader for " + r + " not found");
                }
            }
        }
    }

    /** 构造管理器并附加自定义加载器 @param additionalProviderLoaders 额外 ProviderLoader 集合 */
    public ProviderManager(KeycloakDeploymentInfo info, ClassLoader baseClassLoader, Collection<ProviderLoader> additionalProviderLoaders) {
        this.info = info;
        addDefaultLoaders(baseClassLoader);
        if (additionalProviderLoaders != null) {
            loaders.addAll(additionalProviderLoaders);
        }
    }

    /** 注册默认 classpath 与部署内嵌加载器 @param baseClassLoader 基类加载器 */
    private void addDefaultLoaders(ClassLoader baseClassLoader) {
        loaders.add(new DefaultProviderLoader(info, baseClassLoader));
        loaders.add(new DeploymentProviderLoader(info));
    }

    /** 从所有加载器合并 SPI 并按名称去重 @return SPI 列表 */
    public synchronized List<Spi> loadSpis() {
        // 使用 Map 去重，各加载器类路径可能重叠
        Map<String, Spi> spiMap = new HashMap<>();
        for (ProviderLoader loader : loaders) {
            List<Spi> spis = loader.loadSpis();
            if (spis != null) {
                for (Spi spi : spis) {
                    spiMap.put(spi.getName(), spi);
                }
            }
        }
        return new LinkedList<>(spiMap.values());
    }

    /** 加载并缓存指定 SPI 的全部 ProviderFactory @param spi 目标 SPI @return 工厂列表 */
    public synchronized List<ProviderFactory> load(Spi spi) {
        if (!cache.containsKey(spi.getProviderClass())) {

            Map<String, ProviderFactory> loaded = new HashMap<>();
            for (ProviderLoader loader : loaders) {
                List<ProviderFactory> f = loader.load(spi);
                if (f != null) {
                    for (ProviderFactory pf: f) {
                        String uniqueId = spi.getName() + "-" + pf.getId();
                        if (!loaded.containsKey(uniqueId)) {
                            loaded.put(uniqueId, pf);
                        } else {
                            ProviderFactory currentFactory = loaded.get(uniqueId);
                            ProviderFactory factoryToUse = compareFactories(currentFactory, pf);
                            loaded.put(uniqueId, factoryToUse);

                            logger.debugf("Found multiple provider factories of same provider ID implementing same SPI. SPI is '%s', providerFactory ID '%s'. Factories are '%s' and '%s'. Using provider factory '%s'.",
                                    spi.getName(), pf.getId(), currentFactory.getClass().getName(), pf.getClass().getName(), factoryToUse.getClass().getName());
                        }
                    }
                }
            }

            for (ProviderFactory providerFactory : loaded.values()) {
                cache.add(spi.getProviderClass(), providerFactory);
            }
        }
        List<ProviderFactory> rtn = cache.get(spi.getProviderClass());
        return rtn == null ? Collections.EMPTY_LIST : rtn;
    }

    // 同 providerId 的工厂仅保留一个供 Keycloak 使用
    /** 比较同 ID 的两个工厂：优先 order 高者，其次非内部工厂 @return 应采用的工厂 */
    public ProviderFactory compareFactories(ProviderFactory p1, ProviderFactory p2) {
        if (p1.order() != p2.order()) return (p1.order() > p2.order()) ? p1 : p2;

        // 内部工厂应被自定义工厂覆盖
        if (DefaultKeycloakSessionFactory.isInternal(p1) ^ DefaultKeycloakSessionFactory.isInternal(p2)) {
            return DefaultKeycloakSessionFactory.isInternal(p1) ? p2 : p1;
        }

        return p1;
    }

    /** @return 已加载工厂缓存的副本 */

    /** @return 已加载 ProviderFactory 缓存副本 */
    public synchronized MultivaluedHashMap<Class<? extends Provider>, ProviderFactory> getLoadedFactories() {
        MultivaluedHashMap<Class<? extends Provider>, ProviderFactory> copy = new MultivaluedHashMap<>();
        copy.addAll(cache);
        return copy;
    }

    /** 按 SPI 与 providerId 查找单个工厂 @param providerId 工厂标识 @return 匹配的工厂或 null */
    public synchronized ProviderFactory load(Spi spi, String providerId) {
        for (ProviderFactory f : load(spi)) {
            if (f.getId().equals(providerId)) {
                return f;
            }
        }
        return null;
    }

    /** @return 部署信息 */
    public synchronized KeycloakDeploymentInfo getInfo() {
        return  info;
    }

}
