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

package org.keycloak.loginfailures.jpa;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.config.MetricsOptions;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.expiration.jpa.ExpirationHelper;
import org.keycloak.expiration.jpa.ExpirationTask;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserLoginFailureProviderFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

import org.jboss.logging.Logger;

/**
 * JPA 登录失败 Provider 工厂：创建 {@link JpaUserLoginFailureProvider} 并在 {@link #postInit} 注册过期清理任务。
 * <p>
 * 仅在 {@link Profile.Feature#STATELESS} 特性启用时可用（无 Infinispan 会话存储时的持久化路径）。
 * 过期任务按 realm 协调，使用 {@link LoginFailureExpirationAction} 删除超出
 * realm {@code maxDeltaTimeSeconds} 的失败记录。
 */
public class JpaUserLoginFailureProviderFactory implements UserLoginFailureProviderFactory<JpaUserLoginFailureProvider>, EnvironmentDependentProviderFactory, ServerInfoAwareProviderFactory {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());
    private static final String PROVIDER_ID = "jpa";
    private static final String METRICS_KEY = "metricsEnabled";

    /** 过期清理定时器间隔（秒）。 */
    private int expirationTaskIntervalSeconds;
    /** 单次清理事务超时（秒）。 */
    private int expirationTaskTimeoutSeconds;
    /** 每批最多删除条数。 */
    private int expirationTaskMaxRemoval;
    /** 是否向 Micrometer 上报过期任务指标。 */
    private boolean metricsEnabled;

    @Override
    public JpaUserLoginFailureProvider create(KeycloakSession session) {
        return new JpaUserLoginFailureProvider(session);
    }

    /** 从 SPI 配置读取过期任务参数与 metrics 开关。 */
    @Override
    public void init(Config.Scope config) {
        metricsEnabled = config.getBoolean(METRICS_KEY, config.root().getBoolean(MetricsOptions.METRICS_ENABLED.getKey(), Boolean.FALSE));
        expirationTaskIntervalSeconds = ExpirationHelper.getExpirationTaskInterval(config, logger);
        expirationTaskTimeoutSeconds = ExpirationHelper.getExpirationTaskTimeout(config, logger);
        expirationTaskMaxRemoval = ExpirationHelper.getExpirationTaskMaxRemoval(config, logger);
    }

    /** 构建并调度 realm 感知的过期清理任务（entityId = login-failure）。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        ExpirationTask.builder()
                .withEntityId("login-failure")
                .withAction(LoginFailureExpirationAction.INSTANCE)
                .withFactory(factory)
                .withExecutor(ExpirationHelper.expirationExecutor(factory))
                .withMaxRemoval(expirationTaskMaxRemoval)
                .withMetrics(metricsEnabled)
                .withRealmExpiration(true)
                .withTimeout(expirationTaskTimeoutSeconds, TimeUnit.SECONDS)
                .withInterval(expirationTaskIntervalSeconds, TimeUnit.SECONDS)
                .build()
                .schedule();
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        var builder = ProviderConfigurationBuilder.create();
        ExpirationHelper.addConfiguration(builder, "login failure");
        builder.property()
                .name(METRICS_KEY)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .helpText("If metrics is enabled for this provider (expiration metrics). If not set, uses '" + MetricsOptions.METRICS_ENABLED.getKey() + "' option value.")
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
        return Set.copyOf(deps);
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        // 无状态部署模式下使用 JPA 表存储登录失败，替代 Infinispan 缓存
        return Profile.isFeatureEnabled(Profile.Feature.STATELESS);
    }
}
