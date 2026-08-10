/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.jgroups.certificates;

import java.io.File;
import java.time.Duration;
import java.util.Set;

import org.keycloak.Config;
import org.keycloak.config.CachingOptions;
import org.keycloak.config.Option;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.Provider;
import org.keycloak.spi.infinispan.JGroupsCertificateProvider;
import org.keycloak.spi.infinispan.JGroupsCertificateProviderFactory;
import org.keycloak.storage.configuration.ServerConfigStorageProvider;

/**
 * {@link JGroupsCertificateProvider} 的默认工厂实现。
 * <p>
 * 根据配置选择具体实现：未激活时返回 DISABLED；配置了 keystore/truststore 时使用
 * {@link FileJGroupsCertificateProvider}，否则使用 {@link DatabaseJGroupsCertificateProvider}。
 *
 * @see DatabaseJGroupsCertificateProvider
 * @see FileJGroupsCertificateProvider
 */
public class DefaultJGroupsCertificateProviderFactory implements JGroupsCertificateProviderFactory {

    /** SPI 提供者 ID。 */
    public static final String PROVIDER_ID = "default";

    // for metadata compatibility
    /** 元数据兼容用的启用标志键名。 */
    public static final String ENABLED = "enabled";

    // config
    /** 是否激活 JGroups mTLS 证书管理。 */
    public static final String ACTIVATED = "activated";
    /** 证书轮换周期（天）配置键。 */
    private static final String ROTATION = "rotation";
    /** 密钥库文件路径配置键。 */
    private static final String KEYSTORE_PATH = "keystoreFile";
    /** 密钥库密码配置键。 */
    private static final String KEYSTORE_PASSWORD = "keystorePassword";
    /** 信任库文件路径配置键。 */
    private static final String TRUSTSTORE_PATH = "truststoreFile";
    /** 信任库密码配置键。 */
    private static final String TRUSTSTORE_PASSWORD = "truststorePassword";

    // shared state
    /** 单例证书提供者实例（延迟初始化）。 */
    private volatile JGroupsCertificateProvider provider;
    /** 工厂初始化时保存的配置作用域。 */
    private volatile Config.Scope configuration;

    @Override
    public JGroupsCertificateProvider create(KeycloakSession session) {
        if (provider == null) {
            postInit(session.getKeycloakSessionFactory());
        }
        return provider;
    }

    @Override
    public void init(Config.Scope config) {
        this.configuration = config;
    }

    @Override
    public synchronized void postInit(KeycloakSessionFactory factory) {
        if (provider != null) {
            return;
        }
        provider = createProvider(factory);
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
        return Set.of(ServerConfigStorageProvider.class);
    }

    /** 根据配置创建文件或数据库证书提供者。 */
    private JGroupsCertificateProvider createProvider(KeycloakSessionFactory factory) {
        if (!configuration.getBoolean(ACTIVATED, Boolean.FALSE)) {
            return JGroupsCertificateProvider.DISABLED;
        }
        if (isKeystoreOrTruststoreConfigured()) {
            return FileJGroupsCertificateProvider.create(
                  requireConfigurationAndFile(KEYSTORE_PATH, CachingOptions.CACHE_EMBEDDED_MTLS_KEYSTORE),
                  requireConfiguration(KEYSTORE_PASSWORD, CachingOptions.CACHE_EMBEDDED_MTLS_KEYSTORE_PASSWORD),
                  requireConfigurationAndFile(TRUSTSTORE_PATH, CachingOptions.CACHE_EMBEDDED_MTLS_TRUSTSTORE),
                  requireConfiguration(TRUSTSTORE_PASSWORD, CachingOptions.CACHE_EMBEDDED_MTLS_TRUSTSTORE_PASSWORD)
            );
        }
        return DatabaseJGroupsCertificateProvider.create(factory, Duration.ofDays(requireRotationInDays()));
    }

    /** 判断是否配置了密钥库或信任库文件路径。 */
    private boolean isKeystoreOrTruststoreConfigured() {
        return configuration.get(KEYSTORE_PATH) != null || configuration.get(TRUSTSTORE_PATH) != null;
    }

    private long requireRotationInDays() {
        var value = configuration.getLong(ROTATION);
        if (value == null) {
            throw new RuntimeException("Property '%s' required but not specified.".formatted(CachingOptions.CACHE_EMBEDDED_MTLS_ROTATION.getKey()));
        }
        return value;
    }

    private String requireConfigurationAndFile(String key, Option<?> option) {
        var value = requireConfiguration(key, option);
        if (!new File(value).exists()) {
            throw new RuntimeException("Property '%s' file '%s' does not exist.".formatted(key, value));
        }
        return value;
    }

    private String requireConfiguration(String key, Option<?> option) {
        var value = configuration.get(key);
        if (value == null) {
            throw new RuntimeException("Property '%s' required but not specified".formatted(option.getKey()));
        }
        return value;
    }

}
