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
package org.keycloak.adapters.saml.elytron.infinispan;

import java.util.Set;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.servlet.ServletContext;

import org.keycloak.adapters.saml.AdapterConstants;
import org.keycloak.adapters.spi.SessionIdMapper;
import org.keycloak.adapters.spi.SessionIdMapperUpdater;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.persistence.manager.PersistenceManager;
import org.infinispan.persistence.remote.RemoteStore;
import org.jboss.logging.Logger;

/**
 * 基于 Infinispan SSO 缓存为 Elytron SAML 适配器注册分布式会话 ID 映射更新器。
 *
 * <p>从 Servlet 初始化参数解析缓存容器与缓存名，配置复制模式 SSO 缓存，
 * 并挂载 {@link SsoSessionCacheListener} 以同步集群内会话映射。</p>
 *
 * @author hmlnarik
 */
public class InfinispanSessionCacheIdMapperUpdater {

    /** 本类日志记录器。 */
    private static final Logger LOG = Logger.getLogger(InfinispanSessionCacheIdMapperUpdater.class);

    /** Infinispan 缓存容器默认 JNDI 前缀。 */
    public static final String DEFAULT_CACHE_CONTAINER_JNDI_NAME = "java:jboss/infinispan/container";

    /**
     * 为部署注册基于 Infinispan SSO 缓存的 {@link SessionIdMapperUpdater}。
     *
     * <p>若无法解析缓存参数或 JNDI 查找失败，则返回 {@code previousIdMapperUpdater}。</p>
     *
     * @param servletContext           Servlet 上下文
     * @param mapper                   会话 ID 映射器
     * @param previousIdMapperUpdater  回退用的先前更新器
     * @return 分布式缓存更新器或回退更新器
     */
    public static SessionIdMapperUpdater addTokenStoreUpdaters(ServletContext servletContext, SessionIdMapper mapper, SessionIdMapperUpdater previousIdMapperUpdater) {
        String containerName = servletContext.getInitParameter(AdapterConstants.REPLICATION_CONFIG_CONTAINER_PARAM_NAME);
        String cacheName = servletContext.getInitParameter(AdapterConstants.REPLICATION_CONFIG_SSO_CACHE_PARAM_NAME);

        // 以下逻辑参考 JBoss AS 7.2 DistributedCacheManagerFactory 的部署会话缓存命名方式
        String contextPath = servletContext.getContextPath();
        if (contextPath == null || contextPath.isEmpty() || "/".equals(contextPath)) {
            contextPath = "/ROOT";
        }
        String deploymentSessionCacheName = contextPath;

        if (containerName == null || cacheName == null) {
            LOG.warnv("Cannot determine parameters of SSO cache for deployment {0}.", contextPath);

            return previousIdMapperUpdater;
        }

        String cacheContainerLookup = DEFAULT_CACHE_CONTAINER_JNDI_NAME + "/" + containerName;

        try {
            EmbeddedCacheManager cacheManager = (EmbeddedCacheManager) new InitialContext().lookup(cacheContainerLookup);

            Configuration ssoCacheConfiguration = cacheManager.getCacheConfiguration(cacheName);
            if (ssoCacheConfiguration == null) {
                // 回退：以部署 context path 对应缓存配置为模板
                ssoCacheConfiguration = tryDefineCacheConfigurationFromTemplate(cacheManager, containerName, cacheName, deploymentSessionCacheName);

                if (ssoCacheConfiguration == null) {
                    // 回退：以去掉扩展名的缓存名（如 my-app.war → my-app）为模板
                    if (cacheName.lastIndexOf('.') != -1) {
                        String templateName = cacheName.substring(0, cacheName.lastIndexOf('.'));
                        ssoCacheConfiguration = tryDefineCacheConfigurationFromTemplate(cacheManager, containerName, cacheName, templateName);
                    }
                }

                if (ssoCacheConfiguration == null) {
                    // 最终回退：使用缓存容器默认配置
                    LOG.debugv("Using default configuration for SSO cache {0}.{1}.", containerName, cacheName);
                    ssoCacheConfiguration = cacheManager.getDefaultCacheConfiguration();
                    cacheManager.defineConfiguration(cacheName, ssoCacheConfiguration);
                }
            } else {
                LOG.debugv("Using custom configuration of SSO cache {0}.{1}.", containerName, cacheName);
            }

            CacheMode ssoCacheMode = ssoCacheConfiguration.clustering().cacheMode();
            if (ssoCacheMode != CacheMode.REPL_ASYNC && ssoCacheMode != CacheMode.REPL_SYNC) {
                LOG.warnv("SSO cache mode is {0}, it is recommended to use replicated mode instead.", ssoCacheConfiguration.clustering().cacheModeString());
            }

            Cache<String, String[]> ssoCache = cacheManager.getCache(cacheName, true);
            SsoSessionCacheListener listener = new SsoSessionCacheListener(ssoCache, mapper);
            ssoCache.addListener(listener);

            addSsoCacheCrossDcListener(ssoCache, listener);

            LOG.debugv("Added distributed SSO session cache, lookup={0}, cache name={1}", cacheContainerLookup, cacheName);

            return new SsoCacheSessionIdMapperUpdater(ssoCache, previousIdMapperUpdater) {
                @Override
                public void close() {
                    ssoCache.stop();
                }
            };
        } catch (NamingException ex) {
            LOG.warnv("Failed to obtain distributed session cache container, lookup={0}", cacheContainerLookup);
            return previousIdMapperUpdater;
        }
    }

    /**
     * 尝试以已有缓存配置 {@code templateCacheName} 为模板定义 {@code newCacheName}。
     *
     * @param cacheManager       Infinispan 嵌入式缓存管理器
     * @param containerName      容器名称（仅用于日志）
     * @param newCacheName       待定义的新缓存名
     * @param templateCacheName  模板缓存名
     * @return 新定义的配置；模板不存在时返回 null
     */
    private static Configuration tryDefineCacheConfigurationFromTemplate(EmbeddedCacheManager cacheManager, String containerName, String newCacheName, String templateCacheName) {
        Configuration cacheConfiguration = cacheManager.getCacheConfiguration(templateCacheName);
        if (cacheConfiguration != null) {
            LOG.debugv("Using distributed HTTP session cache configuration for SSO cache {0}.{1}, configuration taken from cache {2}",
                    containerName, newCacheName, templateCacheName);
            return cacheManager.defineConfiguration(newCacheName, cacheConfiguration);
        } else {
            // 模板缓存配置不存在
            return null;
        }
    }

    /** 为跨数据中心 RemoteStore 注册 Hot Rod 客户端监听器。 */
    private static void addSsoCacheCrossDcListener(Cache<String, String[]> ssoCache, SsoSessionCacheListener listener) {
        if (ssoCache.getCacheConfiguration().persistence() == null) {
            return;
        }

        Set<RemoteStore> stores = getRemoteStores(ssoCache);
        if (stores == null || stores.isEmpty()) {
            return;
        }

        LOG.infov("Listening for events on remote stores configured for cache {0}", ssoCache.getName());

        for (RemoteStore store : stores) {
            store.getRemoteCache().addClientListener(listener);
        }
    }

    /** 从 Infinispan 缓存组件注册表获取所有 {@link RemoteStore} 持久化存储。 */
    public static Set<RemoteStore> getRemoteStores(Cache<?, ?> ispnCache) {
        return ComponentRegistry.componentOf(ispnCache, PersistenceManager.class).getStores(RemoteStore.class);
    }
}
