/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.integration.jaxrs;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.ApplicationPath;

import org.keycloak.config.BootstrapAdminOptions;
import org.keycloak.config.ServerOptions;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.quarkus.runtime.Environment;
import org.keycloak.quarkus.runtime.KeycloakMain;
import org.keycloak.quarkus.runtime.configuration.Configuration;
import org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider;
import org.keycloak.quarkus.runtime.configuration.PropertyMappingInterceptor;
import org.keycloak.quarkus.runtime.integration.QuarkusKeycloakSessionFactory;
import org.keycloak.quarkus.runtime.storage.database.jpa.QuarkusJpaConnectionProviderFactory;
import org.keycloak.services.DefaultKeycloakSessionFactory;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.ApplianceBootstrap;
import org.keycloak.services.resources.KeycloakApplication;
import org.keycloak.utils.StringUtil;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.ShutdownDelayInitiatedEvent;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.Blocking;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import static org.keycloak.common.util.Environment.isDevMode;
import static org.keycloak.common.util.Environment.isNonServerMode;
import static org.keycloak.quarkus.runtime.Environment.hasEarlyExitLaunchMode;

/**
 * Quarkus 环境下的 Keycloak JAX-RS 应用入口：
 * 监听 Quarkus 生命周期事件，管理异步启动、会话工厂与临时管理员引导。
 */
@ApplicationPath("/")
@Blocking
public class QuarkusKeycloakApplication extends KeycloakApplication {

    private static final String KEYCLOAK_ADMIN_ENV_VAR = "KEYCLOAK_ADMIN";
    private static final String KEYCLOAK_ADMIN_PASSWORD_ENV_VAR = "KEYCLOAK_ADMIN_PASSWORD";

    private static final Logger logger = Logger.getLogger(QuarkusKeycloakApplication.class);

    /** 异步启动任务；同步启动时为 null。 */
    private CompletableFuture<Void> bootstrapFuture;

    /** {@inheritDoc} 从 Quarkus {@link Environment} 读取数据目录。 */
    @Override
    protected String getDataDir() {
        return Environment.getDataDir().orElseGet(() -> {
            logger.warnf("%s is not set", Environment.KC_HOME_DIR);
            return null;
        });
    }

    /** Quarkus 启动事件：按配置选择同步或异步执行 Keycloak 引导。 */
    void onStartupEvent(@Observes StartupEvent event) {
        var asyncBootstrap = Configuration.getOptionalKcValue(ServerOptions.SERVER_ASYNC_BOOTSTRAP)
                .map(Boolean::parseBoolean)
                .orElse(Boolean.TRUE);
        // 开发模式、非 server 模式或早期退出模式下跳过异步引导
        if (isDevMode() || isNonServerMode() || hasEarlyExitLaunchMode() || !asyncBootstrap) {
            startup();
        } else {
            ManagedExecutor executor = Arc.container().instance(ManagedExecutor.class).get();
            bootstrapFuture = CompletableFuture.runAsync(this::startup, executor).exceptionally(cause -> {
                KeycloakMain.asyncExit(1, cause);
                return null;
            });
        }
    }

    /** 返回异步引导 Future，供外部等待启动完成。 */
    public Optional<CompletableFuture<Void>> getBootstrapFuture() {
        return Optional.ofNullable(bootstrapFuture);
    }

    /** Quarkus 关闭事件：触发 Keycloak 关停逻辑。 */
    void onShutdownEvent(@Observes ShutdownEvent event) {
        shutdown();
    }

    /** 延迟关闭开始事件：在优雅停机窗口内清理资源。 */
    void onShutdownDelayInitiatedEvent(@Observes ShutdownDelayInitiatedEvent event) {
        shutdownDelayInitiated();
    }

    /** {@inheritDoc} 从 CDI 容器获取 Quarkus 会话工厂。 */
    @Override
    public DefaultKeycloakSessionFactory createSessionFactory() {
        return Arc.container().instance(QuarkusKeycloakSessionFactory.class).get();
    }

    /** {@inheritDoc} 根据配置或环境变量创建临时 master 管理员用户/服务账号。 */
    @Override
    protected void createTemporaryAdmin(KeycloakSession session) {
        var adminUsername = getOption(BootstrapAdminOptions.USERNAME.getKey(), KEYCLOAK_ADMIN_ENV_VAR);
        var adminPassword = getOption(BootstrapAdminOptions.PASSWORD.getKey(), KEYCLOAK_ADMIN_PASSWORD_ENV_VAR);

        var clientId = Configuration.getOptionalKcValue(BootstrapAdminOptions.CLIENT_ID.getKey()).orElse(null);
        var clientSecret = Configuration.getOptionalKcValue(BootstrapAdminOptions.CLIENT_SECRET.getKey()).orElse(null);

        try {
            //Integer expiration = Configuration.getOptionalKcValue(BootstrapAdminOptions.EXPIRATION.getKey()).map(Integer::valueOf).orElse(null);
            if (StringUtil.isNotBlank(adminPassword) && !createTemporaryMasterRealmAdminUser(adminUsername, adminPassword, /*expiration,*/ session)) {
                throw new RuntimeException("Aborting startup and the creation of the master realm, because the temporary admin user account could not be created.");
            }
            if (StringUtil.isNotBlank(clientSecret) && !createTemporaryMasterRealmAdminService(clientId, clientSecret, /*expiration,*/ session)) {
                throw new RuntimeException("Aborting startup and the creation of the master realm, because the temporary admin service account could not be created.");
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid admin expiration value provided. An integer is expected.", e);
        }
    }

    /** {@inheritDoc} 使用 JPA 迁移事务超时作为全局事务超时。 */
    @Override
    protected int getTransactionTimeout(DefaultKeycloakSessionFactory sessionFactory) {
        return ((QuarkusJpaConnectionProviderFactory) sessionFactory.getProviderFactory(JpaConnectionProvider.class)).getMigrationTransactionTimeout();
    }

    /**
     * 读取配置项，若未设置则回退到环境变量（并记录弃用警告）。
     *
     * @param option  Keycloak 配置键
     * @param envVar  兼容旧版的环境变量名
     */
    private String getOption(String option, String envVar) {
        PropertyMappingInterceptor.disable(); // 禁用默认属性映射拦截
        try {
            return Configuration.getOptionalKcValue(option).orElseGet(() -> {
                String value = System.getenv(envVar);
                if (value != null) {
                    ServicesLogger.LOGGER.usingDeprecatedEnvironmentVariable(envVar, Configuration.toEnvVarFormat(MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX + option));
                }
                return value;
            });
        } finally {
            PropertyMappingInterceptor.enable();
        }
    }

    /** 在 master realm 创建临时管理员用户。 */
    public boolean createTemporaryMasterRealmAdminUser(String adminUserName, String adminPassword, /*Integer adminExpiration,*/ KeycloakSession session) {
        return new ApplianceBootstrap(session).createMasterRealmAdminUser(adminUserName, adminPassword, true /*, adminExpiration*/, false);
    }

    /** 在 master realm 创建临时管理员服务客户端。 */
    public boolean createTemporaryMasterRealmAdminService(String clientId, String clientSecret, /*Integer adminExpiration,*/ KeycloakSession session) {
        return new ApplianceBootstrap(session).createTemporaryMasterRealmAdminService(clientId, clientSecret /*, adminExpiration*/);
    }

}
