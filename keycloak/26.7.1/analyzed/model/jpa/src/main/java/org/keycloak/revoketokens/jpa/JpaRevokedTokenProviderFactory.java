/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.revoketokens.jpa;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.keycloak.Config;
import org.keycloak.cache.LocalCache;
import org.keycloak.cache.LocalCacheConfiguration;
import org.keycloak.cache.LocalCacheProvider;
import org.keycloak.common.Profile;
import org.keycloak.config.MetricsOptions;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.expiration.jpa.ExpirationHelper;
import org.keycloak.expiration.jpa.ExpirationTask;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RevokedTokenProviderFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

import org.jboss.logging.Logger;

/**
 * {@link JpaRevokedTokenProvider} 工厂（ID {@code jpa}）。
 * <p>
 * 仅在 {@link Profile.Feature#STATELESS} 特性启用时可用；初始化本地缓存并注册
 * {@link RevokedTokenExpirationAction} 定时清理任务。
 */
public class JpaRevokedTokenProviderFactory implements RevokedTokenProviderFactory<JpaRevokedTokenProvider>, EnvironmentDependentProviderFactory, ServerInfoAwareProviderFactory {

    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    /** Provider 标识符。 */
    private static final String PROVIDER_ID = "jpa";

    // 配置项
    /** 本地缓存最大条目数配置键。 */
    private static final String CACHE_MAX_SIZE_KEY = "cacheMaxSize";
    /** 本地缓存默认最大条目数。 */
    private static final int DEFAULT_CACHE_MAX_SIZE = 1000;
    /** 是否启用指标的配置键。 */
    private static final String METRICS_KEY = "metricsEnabled";

    /** 过期清理任务执行间隔（秒）。 */
    private int expirationTaskIntervalSeconds;
    /** 过期清理任务超时（秒）。 */
    private int expirationTaskTimeoutSeconds;
    /** 单次清理最多移除的条目数。 */
    private int expirationTaskMaxRemoval;
    /** 本地缓存最大条目数。 */
    private int cacheMaxSize;
    /** 是否启用过期相关指标。 */
    private boolean metricsEnabled;
    /** 已吊销令牌 ID 的共享本地缓存。 */
    private LocalCache<String, Long> loadingCache;

    @Override
    public JpaRevokedTokenProvider create(KeycloakSession session) {
        return new JpaRevokedTokenProvider(session, loadingCache);
    }

    @Override
    public void init(Config.Scope config) {
        metricsEnabled = config.getBoolean(METRICS_KEY, config.root().getBoolean(MetricsOptions.METRICS_ENABLED.getKey(), Boolean.FALSE));
        expirationTaskIntervalSeconds = ExpirationHelper.getExpirationTaskInterval(config, logger);
        expirationTaskTimeoutSeconds = ExpirationHelper.getExpirationTaskTimeout(config, logger);
        expirationTaskMaxRemoval = ExpirationHelper.getExpirationTaskMaxRemoval(config, logger);
        cacheMaxSize = config.getInt(CACHE_MAX_SIZE_KEY, DEFAULT_CACHE_MAX_SIZE);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        var cacheConfig = LocalCacheConfiguration.<String, Long>builder();
        cacheConfig.name("revokedToken")
                .maxSize(cacheMaxSize)
                .expirationAfterCreate((id, lifespan) -> Duration.ofSeconds(lifespan));
        try (var session = factory.create()) {
            loadingCache = session.getProvider(LocalCacheProvider.class)
                    .create(cacheConfig.build());
        }
        ExpirationTask.builder()
                .withEntityId("revoked-token")
                .withAction(RevokedTokenExpirationAction.INSTANCE)
                .withFactory(factory)
                .withExecutor(ExpirationHelper.expirationExecutor(factory))
                .withMaxRemoval(expirationTaskMaxRemoval)
                .withMetrics(metricsEnabled)
                .withRealmExpiration(false)
                .withTimeout(expirationTaskTimeoutSeconds, TimeUnit.SECONDS)
                .withInterval(expirationTaskIntervalSeconds, TimeUnit.SECONDS)
                .build()
                .schedule();
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        var builder = ProviderConfigurationBuilder.create();
        ExpirationHelper.addConfiguration(builder, "revoked token");
        builder.property()
                .name(CACHE_MAX_SIZE_KEY)
                .type(ProviderConfigProperty.INTEGER_TYPE)
                .helpText("Maximum number of revoked token IDs to keep in the local cache. The cache avoids repeated database lookups for frequently checked tokens.")
                .defaultValue(DEFAULT_CACHE_MAX_SIZE)
                .add();
        builder.property()
                .name(METRICS_KEY)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .helpText("Whether metrics are enabled for this provider (expiration metrics). If not set, uses '" + MetricsOptions.METRICS_ENABLED.getKey() + "' option value.")
                .add();
        return builder.build();
    }

    @Override
    public Map<String, String> getOperationalInfo() {
        var map = new HashMap<String, String>();
        ExpirationHelper.addToOperationalInfo(expirationTaskIntervalSeconds, expirationTaskTimeoutSeconds, expirationTaskMaxRemoval, map);
        map.put(METRICS_KEY, Boolean.toString(metricsEnabled));
        return Map.copyOf(map);
    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Set<Class<? extends Provider>> dependsOn() {
        var deps = new HashSet<>(ExpirationHelper.dependsOn());
        deps.add(JpaConnectionProvider.class);
        deps.add(LocalCacheProvider.class);
        return Set.copyOf(deps);
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.STATELESS);
    }

}
