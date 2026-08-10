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

package org.keycloak.spi.infinispan.impl.embedded;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.common.util.DurationConverter;
import org.keycloak.config.CachingOptions;
import org.keycloak.config.OptionsUtil;
import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.sessions.infinispan.InfinispanUserSessionProviderFactory;
import org.keycloak.models.sessions.infinispan.entities.LoginFailureEntity;
import org.keycloak.models.sessions.infinispan.entities.RemoteAuthenticatedClientSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.RemoteUserSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.RootAuthenticationSessionEntity;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.util.TimeQuantity;
import org.infinispan.configuration.cache.AbstractStoreConfiguration;
import org.infinispan.configuration.cache.BackupConfiguration;
import org.infinispan.configuration.cache.BackupFailurePolicy;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.ExpirationConfiguration;
import org.infinispan.configuration.cache.HashConfiguration;
import org.infinispan.configuration.cache.HashConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.lookup.EmbeddedTransactionManagerLookup;
import org.jboss.logging.Logger;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.ACTION_TOKEN_CACHE;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.ALL_CACHES_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.AUTHENTICATION_SESSIONS_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.AUTHORIZATION_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.AUTHORIZATION_REVISIONS_CACHE_DEFAULT_MAX;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.AUTHORIZATION_REVISIONS_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.CLIENT_SESSION_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.CLUSTERED_CACHE_NAMES;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.CLUSTERED_CACHE_NUM_OWNERS;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.CLUSTERED_MAX_COUNT_CACHES;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.CRL_CACHE_DEFAULT_MAX;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.CRL_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.KEYS_CACHE_DEFAULT_MAX;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.KEYS_CACHE_MAX_IDLE_SECONDS;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.KEYS_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.LOCAL_CACHE_NAMES;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.LOCAL_MAX_COUNT_CACHES;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.LOGIN_FAILURE_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.OFFLINE_CLIENT_SESSION_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.OFFLINE_USER_SESSION_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.REALM_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.REALM_REVISIONS_CACHE_DEFAULT_MAX;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.REALM_REVISIONS_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.SESSIONS_CACHE_DEFAULT_MAX;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.USER_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.USER_REVISIONS_CACHE_DEFAULT_MAX;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.USER_REVISIONS_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.USER_SESSION_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.WORK_CACHE_NAME;

/**
 * Infinispan 缓存配置工具类。
 * <p>
 * 根据 SPI 配置选项设置各缓存的 max-count、owners、lifespan 等参数，并提供嵌入式与远程缓存的默认配置模板。
 */
public final class CacheConfigurator {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private static final String MAX_COUNT_SUFFIX = "MaxCount";
    private static final String LIFESPAN_SUFFIX = "Lifespan";
    private static final String OWNER_SUFFIX = "Owners";
    private static final int STATE_TRANSFER_CHUNK_SIZE = 16;
    private static final int MIN_NUM_OWNERS_REMOTE_CACHE = 2;
    private static final long DEFAULT_LIFESPAN = Duration.ofHours(1).toMillis();
    private static final int DISABLED_LIFESPAN = -1;

    private CacheConfigurator() {
    }

    /**
     * 根据 Keycloak 配置初始化本地缓存（realm、user、authorization 等）。
     *
     * @param keycloakConfig Keycloak SPI 配置作用域。
     * @param holder         待写入缓存定义的 {@link ConfigurationBuilderHolder}。
     * @throws IllegalStateException 若某缓存未在 holder 中定义。
     */
    public static void configureLocalCaches(Config.Scope keycloakConfig, ConfigurationBuilderHolder holder) {
        logger.debug("Configuring embedded local caches");
        // 配置除 revision 缓存外的本地 max-count 缓存
        configureCacheMaxCount(keycloakConfig, holder, Arrays.stream(LOCAL_MAX_COUNT_CACHES));
        // 配置 revision 缓存（容量为对应主缓存的两倍）
        configureRevisionCache(holder, REALM_CACHE_NAME, REALM_REVISIONS_CACHE_NAME, REALM_REVISIONS_CACHE_DEFAULT_MAX);
        configureRevisionCache(holder, USER_CACHE_NAME, USER_REVISIONS_CACHE_NAME, USER_REVISIONS_CACHE_DEFAULT_MAX);
        configureRevisionCache(holder, AUTHORIZATION_CACHE_NAME, AUTHORIZATION_REVISIONS_CACHE_NAME, AUTHORIZATION_REVISIONS_CACHE_DEFAULT_MAX);
        // 校验所有本地缓存均已定义
        checkCachesExist(holder, Arrays.stream(LOCAL_CACHE_NAMES));
    }

    /**
     * 为 holder 中缺失的缓存填充默认配置；若用户已自定义部分缓存且未开启 configMutate 则告警。
     *
     * @param holder     缓存配置 holder。
     * @param warnMutate 是否在检测到用户自定义配置时输出弃用警告。
     */
    public static void applyDefaultConfiguration(ConfigurationBuilderHolder holder, boolean warnMutate) {
        var configs = holder.getNamedConfigurationBuilders();
        boolean userProvidedConfig = false;
        boolean clustered = isClustered(holder);
        for (var name : ALL_CACHES_NAME) {
            var config = configs.get(name);
            if (config == null) {
                configs.put(name, getCacheConfiguration(name, clustered));
            } else if (!userProvidedConfig) {
                userProvidedConfig = true;
            }
        }
        if (warnMutate && userProvidedConfig) {
            logger.warnf("Modifying the default cache configuration in the config file without setting %s=true is deprecated.", CachingOptions.CACHE_CONFIG_MUTATE_PROPERTY);
        }
    }

    /**
     * Verifies that all the {@code caches} are defined in the {@code holder}.
     *
     * @param holder The {@link ConfigurationBuilderHolder} where the caches are configured.
     * @param caches The {@link Stream} containing the names of the caches to check.
     * @throws IllegalStateException if one or more Infinispan caches from the provided {@code caches} stream are not
     *                               defined in the {@code holder}. This could indicate a missing or incorrect
     *                               configuration for those specific caches.
     */
    public static void checkCachesExist(ConfigurationBuilderHolder holder, Stream<String> caches) {
        for (var it = caches.iterator(); it.hasNext(); ) {
            var cache = it.next();
            var builder = holder.getNamedConfigurationBuilders().get(cache);
            if (builder == null) {
                throw cacheNotFound(cache);
            }
        }
    }

    /**
     * Validates that the "work" cache is present in the {@code holder} and has a valid configuration.
     *
     * @param holder The {@link ConfigurationBuilderHolder} where the caches are configured.
     * @throws IllegalStateException if the "work" cache is not found in the holder.
     * @throws RuntimeException      if the "work" cache has an invalid configuration. This could include an incorrect
     *                               settings that would prevent the cache from functioning correctly.
     */
    public static void validateWorkCacheConfiguration(ConfigurationBuilderHolder holder) {
        logger.debugf("Validating %s cache configuration", WORK_CACHE_NAME);
        var cacheBuilder = holder.getNamedConfigurationBuilders().get(WORK_CACHE_NAME);
        if (cacheBuilder == null) {
            throw cacheNotFound(WORK_CACHE_NAME);
        }
        if (!isClustered(holder)) {
            // 非集群模式（开发模式？）跳过 work 缓存校验
            return;
        }
        var cacheMode = cacheBuilder.clustering().cacheMode();
        if (!cacheMode.isReplicated()) {
            throw new RuntimeException("Unable to start Keycloak. '%s' cache must be replicated but is %s".formatted(WORK_CACHE_NAME, cacheMode.friendlyCacheModeString().toLowerCase()));
        }
    }

    /**
     * Removes clustered caches from the {@code holder}.
     *
     * @param holder The {@link ConfigurationBuilderHolder} where the caches are configured.
     */
    public static void removeClusteredCaches(ConfigurationBuilderHolder holder) {
        logger.debug("Removing clustered caches");
        Arrays.stream(CLUSTERED_CACHE_NAMES).forEach(holder.getNamedConfigurationBuilders()::remove);
    }

    /**
     * Configures the maximum number of entries for the specified caches, bounding them to this limit and preventing
     * excessive memory usage.
     *
     * @param keycloakConfig The Keycloak configuration, which provides the maximum entry counts for the caches.
     * @param holder         The {@link ConfigurationBuilderHolder} where the caches are configured.
     * @param caches         The {@link Stream} containing the names of the caches to configure with a maximum count.
     * @throws IllegalStateException if an Infinispan cache from the provided {@code caches} stream is not defined in
     *                               the {@code holder}. This could indicate a missing or incorrect configuration.
     */
    public static void configureCacheMaxCount(Config.Scope keycloakConfig, ConfigurationBuilderHolder holder, Stream<String> caches) {
        for (var it = caches.iterator(); it.hasNext(); ) {
            var name = it.next();
            var builder = holder.getNamedConfigurationBuilders().get(name);
            if (builder == null) {
                throw cacheNotFound(name);
            }
            var maxCount = keycloakConfig.getLong(maxCountConfigKey(name));
            if (maxCount != null) {
                if (maxCount < 0) {
                    // 禁止用户将已有默认 max-count 的缓存设为无界
                    maxCount = builder.memory().maxCount();
                    if (maxCount > -1)
                        logger.infof("Ignoring unbounded max-count for cache '%s', reverting to default max of %d entries.", name, maxCount);
                } else {
                    logger.debugf("Overwriting max-count for cache '%s' to %s entries", name, maxCount);
                }
                builder.memory().maxCount(maxCount);
            }
        }
    }

    /**
     * 启用持久化用户会话时配置会话相关缓存：限制内存、owners=1、禁用 state-transfer。
     *
     * @param keycloakConfig Keycloak 配置。
     * @param holder         缓存配置 holder。
     */
    public static void configureSessionsCachesForPersistentSessions(Config.Scope keycloakConfig, ConfigurationBuilderHolder holder) {
        logger.debug("Configuring session cache (persistent user sessions)");
        var sessionCaches = Set.of(USER_SESSION_CACHE_NAME, CLIENT_SESSION_CACHE_NAME, OFFLINE_USER_SESSION_CACHE_NAME, OFFLINE_CLIENT_SESSION_CACHE_NAME);
        for (var name : CLUSTERED_MAX_COUNT_CACHES) {
            var builder = holder.getNamedConfigurationBuilders().get(name);
            if (builder == null) {
                throw cacheNotFound(name);
            }
            setMemoryMaxCount(keycloakConfig, name, builder);
            if (builder.memory().maxCount() == -1) {
                logger.infof("Persistent user sessions enabled and no memory limit found in configuration. Setting max entries for %s to %d entries.", name, SESSIONS_CACHE_DEFAULT_MAX);
                builder.memory().maxCount(SESSIONS_CACHE_DEFAULT_MAX);
            }
            /* 持久化会话下 owners 须设为 1，避免备份节点持有与主节点不一致的条目：
             主节点按本地 maxCount 驱逐时不会通知备份；computeIfPresent 也不会转发到备份，导致数据陈旧。
             owners=1 即无备份，与持久化存储配合保证一致性。 */
            builder.clustering().hash().numOwners(1);
            if (sessionCaches.contains(name)) {
                configureSessionExpirationReaper(builder);
                // 禁用 state-transfer，降低新节点加入开销
                builder.clustering().stateTransfer().fetchInMemoryState(false);
            }
        }
    }

    /**
     * 未启用持久化用户会话时配置会话缓存：在线会话不设内存上限，离线会话保留 max-count。
     *
     * @param keycloakConfig Keycloak 配置。
     * @param holder         缓存配置 holder。
     */
    public static void configureSessionsCachesForVolatileSessions(Config.Scope keycloakConfig, ConfigurationBuilderHolder holder) {
        logger.debug("Configuring session cache (volatile user sessions)");
        for (var name : Arrays.asList(USER_SESSION_CACHE_NAME, CLIENT_SESSION_CACHE_NAME)) {
            var builder = holder.getNamedConfigurationBuilders().get(name);
            if (builder == null) {
                throw cacheNotFound(name);
            }

            setMemoryMaxCount(keycloakConfig, name, builder);
            if (builder.memory().maxCount() != -1) {
                logger.infof("Persistent user sessions disabled and memory limit is set. Ignoring cache limits to avoid losing sessions for cache %s.", name);
                builder.memory().maxCount(-1);
            }
            if (builder.clustering().hash().attributes().attribute(HashConfiguration.NUM_OWNERS).get() == 1 &&
                  builder.persistence().stores().stream().noneMatch(p -> p.attributes().attribute(AbstractStoreConfiguration.SHARED).get())
            ) {
                logger.infof("Persistent user sessions disabled with number of owners set to default value 1 for cache %s and no shared persistence store configured. Setting num_owners=2 to avoid data loss.", name);
                builder.clustering().hash().numOwners(2);
            }
            configureSessionExpirationReaper(builder);
        }

        for (var name : Arrays.asList(OFFLINE_USER_SESSION_CACHE_NAME, OFFLINE_CLIENT_SESSION_CACHE_NAME)) {
            var builder = holder.getNamedConfigurationBuilders().get(name);
            if (builder == null) {
                throw cacheNotFound(name);
            }

            setMemoryMaxCount(keycloakConfig, name, builder);
            if (builder.memory().maxCount() == -1) {
                logger.infof("Offline sessions should have a max count set to avoid excessive memory usage. Setting a default cache limit of %d for cache %s.", SESSIONS_CACHE_DEFAULT_MAX, name);
                builder.memory().maxCount(SESSIONS_CACHE_DEFAULT_MAX);
            }
            if (builder.clustering().hash().attributes().attribute(HashConfiguration.NUM_OWNERS).get() != 1 &&
                    builder.persistence().stores().stream().noneMatch(p -> p.attributes().attribute(AbstractStoreConfiguration.SHARED).get())
            ) {
                logger.infof("Setting a memory limit implies to have exactly one owner. Setting num_owners=1 to avoid data loss.", name);
                builder.clustering().hash().numOwners(1);
            }
            configureSessionExpirationReaper(builder);
            // 禁用 state-transfer，降低新节点加入开销
            builder.clustering().stateTransfer().fetchInMemoryState(false);
        }
    }

    /**
     * 为纯内存缓存（actionToken、authenticationSessions、loginFailures）设置最少 2 个 owner，防止单节点崩溃丢数据。
     *
     * @param holder 缓存配置 holder。
     */
    public static void ensureMinimumOwners(ConfigurationBuilderHolder holder) {
        for (var name : Arrays.asList(
                LOGIN_FAILURE_CACHE_NAME,
                AUTHENTICATION_SESSIONS_CACHE_NAME,
                ACTION_TOKEN_CACHE)) {
            var builder = holder.getNamedConfigurationBuilders().get(name);
            if (builder == null) {
                throw cacheNotFound(name);
            }
            var hashConfig = builder.clustering().hash();
            var owners = hashConfig.attributes().attribute(HashConfiguration.NUM_OWNERS).get();
            if (owners < 2) {
                logger.infof("Setting num_owners=2 (configured value is %s) for cache '%s' to prevent data loss.", owners, name);
                hashConfig.numOwners(2);
            }
        }
    }

    /**
     * Configures (and overwrites) the {@link HashConfigurationBuilder#numOwners(int)} based on the SPI configuration
     * input.
     *
     * @param keycloakConfig The Keycloak configuration, which provides the number owners value for the caches.
     * @param holder         The {@link ConfigurationBuilderHolder} where the caches are configured.
     */
    public static void configureNumOwners(Config.Scope keycloakConfig, ConfigurationBuilderHolder holder) {
        for (var name : CLUSTERED_CACHE_NUM_OWNERS) {
            var builder = holder.getNamedConfigurationBuilders().get(name);
            if (builder == null) {
                throw cacheNotFound(name);
            }
            var owners = keycloakConfig.getInt(numOwnerConfigKey(name));
            if (owners != null) {
                builder.clustering().hash().numOwners(owners);
            }
        }
    }

    /**
     * 为远程 Infinispan 集群创建指定缓存的 {@link ConfigurationBuilder} 模板。
     * <p>
     * 返回的 builder 基于 Provider 默认配置，调用方可自由修改后再提交。
     *
     * @param cacheName 缓存名称。
     * @param config    Provider 基础配置作用域，可含缓存级自定义项。
     * @param sites     跨站点复制备份的远程站点名数组；null 或空则禁用跨站点复制。
     * @return 指定缓存的配置 builder；若 cacheName 无对应模板则返回 {@code null}。
     */
    public static ConfigurationBuilder getRemoteCacheConfiguration(String cacheName, Config.Scope config, String[] sites) {
        return switch (cacheName) {
            case CLIENT_SESSION_CACHE_NAME, OFFLINE_CLIENT_SESSION_CACHE_NAME ->
                    remoteCacheConfigurationBuilder(cacheName, config, sites, RemoteAuthenticatedClientSessionEntity.class, InfinispanUserSessionProviderFactory.getExpirationPeriod(TimeUnit.MILLISECONDS));
            case USER_SESSION_CACHE_NAME, OFFLINE_USER_SESSION_CACHE_NAME ->
                    remoteCacheConfigurationBuilder(cacheName, config, sites, RemoteUserSessionEntity.class, InfinispanUserSessionProviderFactory.getExpirationPeriod(TimeUnit.MILLISECONDS));
            case AUTHENTICATION_SESSIONS_CACHE_NAME ->
                    remoteCacheConfigurationBuilder(cacheName, config, sites, RootAuthenticationSessionEntity.class, ExpirationConfiguration.WAKEUP_INTERVAL.getDefaultValue());
            case LOGIN_FAILURE_CACHE_NAME ->
                    remoteCacheConfigurationBuilder(cacheName, config, sites, LoginFailureEntity.class, ExpirationConfiguration.WAKEUP_INTERVAL.getDefaultValue());
            case ACTION_TOKEN_CACHE, WORK_CACHE_NAME -> remoteCacheConfigurationBuilder(cacheName, config, sites, null, ExpirationConfiguration.WAKEUP_INTERVAL.getDefaultValue());
            default -> null;
        };
    }

    /**
     * Configures the entry lifespan for the local caches (realm, user, and authorization).
     * <p>
     * When the {@link Profile.Feature#STATELESS} feature is enabled, the default lifespan is set to
     * {@link #DEFAULT_LIFESPAN} milliseconds; otherwise, entries are immortal by default.
     *
     * @param holder The {@link ConfigurationBuilderHolder} where the caches are configured.
     * @param config The Keycloak configuration, which may provide per-cache lifespan overrides.
     * @throws IllegalStateException if a cache is not defined in the {@code holder}.
     */
    public static void configureLocalCachesExpiration(ConfigurationBuilderHolder holder, Config.Scope config) {
        var defaultLifespan = Profile.isFeatureEnabled(Profile.Feature.STATELESS) ? DEFAULT_LIFESPAN : DISABLED_LIFESPAN;
        Stream.of(AUTHORIZATION_CACHE_NAME, REALM_CACHE_NAME, USER_CACHE_NAME)
                .forEach(name -> setExpiration(holder, config, name, defaultLifespan));
    }

    /**
     * Adds the lifespan configuration properties for the local caches (realm, user, and authorization) to the given
     * provider configuration builder.
     *
     * @param builder The {@link ProviderConfigurationBuilder} to add the properties to.
     */
    public static void addExpirationConfiguration(ProviderConfigurationBuilder builder) {
        Stream.of(AUTHORIZATION_CACHE_NAME, REALM_CACHE_NAME, USER_CACHE_NAME)
                .forEach(name -> builder.property()
                        .name(CacheConfigurator.lifespanConfigKey(name))
                        .helpText("Sets the lifespan of stored objects for cache %s. A zero or negative value makes the entries immortal, i.e., they never expire. %s".formatted(name, OptionsUtil.DURATION_DESCRIPTION))
                        .label("lifespan")
                        .type(ProviderConfigProperty.STRING_TYPE)
                        .add());
    }

    // 私有方法

    private static void configureSessionExpirationReaper(ConfigurationBuilder builder) {
        builder.expiration().enableReaper().wakeUpInterval(InfinispanUserSessionProviderFactory.getExpirationPeriod(TimeUnit.MILLISECONDS));
    }

    private static ConfigurationBuilder remoteCacheConfigurationBuilder(String name, Config.Scope config, String[] sites, Class<?> indexedEntity, long expirationWakeupPeriodMillis) {
        return remoteCacheConfigurationBuilder(name, config, sites, indexedEntity, TimeQuantity.valueOf(expirationWakeupPeriodMillis));
    }

    private static ConfigurationBuilder remoteCacheConfigurationBuilder(String name, Config.Scope config, String[] sites, Class<?> indexedEntity, TimeQuantity expirationWakeupPeriod) {
        var builder = new ConfigurationBuilder();
        builder.clustering().cacheMode(CacheMode.DIST_SYNC);
        builder.clustering().hash().numOwners(Math.max(MIN_NUM_OWNERS_REMOTE_CACHE, config.getInt(numOwnerConfigKey(name), MIN_NUM_OWNERS_REMOTE_CACHE)));
        builder.clustering().stateTransfer().chunkSize(STATE_TRANSFER_CHUNK_SIZE);
        builder.encoding().mediaType(MediaType.APPLICATION_PROTOSTREAM);
        builder.statistics().enable();
        builder.expiration().enableReaper().wakeUpInterval(expirationWakeupPeriod.longValue());

        if (indexedEntity != null) {
            builder.indexing().enable().addIndexedEntities(Marshalling.protoEntity(indexedEntity));
        }

        if (sites == null || sites.length == 0) {
            return builder;
        }

        // 跨站点复制需要事务以检测死锁并回滚
        builder.transaction()
                .transactionMode(TransactionMode.TRANSACTIONAL)
                .useSynchronization(false)
                .lockingMode(LockingMode.PESSIMISTIC);
        for (var site : sites) {
            builder.sites().addBackup()
                    .site(site)
                    .strategy(BackupConfiguration.BackupStrategy.SYNC)
                    .backupFailurePolicy(BackupFailurePolicy.FAIL)
                    .stateTransfer().chunkSize(STATE_TRANSFER_CHUNK_SIZE);
        }
        return builder;
    }

    private static void configureRevisionCache(ConfigurationBuilderHolder holder, String baseCache, String revisionCache, long defaultMaxEntries) {
        var baseBuilder = holder.getNamedConfigurationBuilders().get(baseCache);
        if (baseBuilder == null) {
            throw cacheNotFound(baseCache);
        }
        var maxCount = baseBuilder.memory().maxCount();
        maxCount = maxCount > 0 ? 2 * maxCount : defaultMaxEntries;
        logger.debugf("Creating revision cache '%s' with max-count %s", revisionCache, maxCount);
        holder.getNamedConfigurationBuilders().put(revisionCache, getRevisionCacheConfig(maxCount));
    }

    private static void setMemoryMaxCount(Config.Scope keycloakConfig, String name, ConfigurationBuilder builder) {
        var maxCount = keycloakConfig.getLong(maxCountConfigKey(name));
        if (maxCount != null) {
            builder.memory().maxCount(maxCount);
        }
    }

    public static String maxCountConfigKey(String name) {
        return name + MAX_COUNT_SUFFIX;
    }

    /**
     * 返回缓存的 SPI lifespan 配置键，例如 {@code "realmLifespan"}。
     *
     * @param name 缓存名称。
     * @return 配置键字符串。
     */
    public static String lifespanConfigKey(String name) {
        return name + LIFESPAN_SUFFIX;
    }

    public static String numOwnerConfigKey(String name) {
        return name + OWNER_SUFFIX;
    }

    private static IllegalStateException cacheNotFound(String cache) {
        return new IllegalStateException("Infinispan cache '%s' not found.".formatted(cache));
    }

    // 缓存默认配置

    public static ConfigurationBuilder getCrlCacheConfig() {
        return getCacheConfiguration(CRL_CACHE_NAME, true);
    }

    public static ConfigurationBuilder getRevisionCacheConfig(long maxEntries) {
        var builder = createCacheConfigurationBuilder();
        builder.simpleCache(false);
        builder.invocationBatching().enable().transaction().transactionMode(TransactionMode.TRANSACTIONAL);

        // 即使在 WildFly/EAP 托管环境也使用 Embedded 事务管理器，避免 Infinispan 参与全局 XA 事务
        builder.transaction().transactionManagerLookup(new EmbeddedTransactionManagerLookup());

        builder.transaction().lockingMode(LockingMode.PESSIMISTIC);
        if (builder.memory().storage().canStoreReferences()) {
            builder.encoding().mediaType(MediaType.APPLICATION_OBJECT_TYPE);
        }

        builder.memory().whenFull(EvictionStrategy.REMOVE).maxCount(maxEntries);

        return builder;
    }

    public static ConfigurationBuilder createCacheConfigurationBuilder() {
        ConfigurationBuilder builder = new ConfigurationBuilder();

        // 强制 application/x-java-object 编码，避免键值不必要的转换（参见 WFLY-14356）
        builder.encoding().mediaType(MediaType.APPLICATION_OBJECT_TYPE);

        // 启用事务时必须禁用 simpleCache
        builder.simpleCache(true);

        return builder;
    }

    /**
     * 返回指定缓存的默认配置。
     * <p>
     * revision 缓存不在此返回，其配置依赖关联主缓存的 max-count。
     */
    public static ConfigurationBuilder getCacheConfiguration(String cacheName, boolean clustered) {
        var builder = new ConfigurationBuilder();
        switch (cacheName) {
            // 分布式缓存
            case CLIENT_SESSION_CACHE_NAME:
            case OFFLINE_CLIENT_SESSION_CACHE_NAME:
                // 按用户会话 ID 分组键
                if (clustered) {
                    builder.clustering().hash().groups()
                            .enabled()
                            .addGrouper(ClientSessionKeyGrouper.INSTANCE);
                }
            case USER_SESSION_CACHE_NAME:
            case OFFLINE_USER_SESSION_CACHE_NAME:
                if (clustered) {
                    builder.clustering().cacheMode(CacheMode.DIST_SYNC).hash().numOwners(1);
                }
                builder.memory().maxCount(SESSIONS_CACHE_DEFAULT_MAX);
                return builder;
            case ACTION_TOKEN_CACHE:
            case AUTHENTICATION_SESSIONS_CACHE_NAME:
            case LOGIN_FAILURE_CACHE_NAME:
                if (clustered) {
                    builder.clustering().cacheMode(CacheMode.DIST_SYNC);
                }
                builder.encoding().mediaType(MediaType.APPLICATION_OBJECT_TYPE);
                return builder;
            // 本地缓存
            case CRL_CACHE_NAME:
                builder.simpleCache(true);
                builder.memory().whenFull(EvictionStrategy.REMOVE).maxCount(CRL_CACHE_DEFAULT_MAX);
                return builder;
            case KEYS_CACHE_NAME:
                builder.simpleCache(true);
                builder.expiration().maxIdle(KEYS_CACHE_MAX_IDLE_SECONDS, TimeUnit.SECONDS);
                builder.memory().whenFull(EvictionStrategy.REMOVE).maxCount(KEYS_CACHE_DEFAULT_MAX);
                return builder;
            case AUTHORIZATION_CACHE_NAME:
            case REALM_CACHE_NAME:
            case USER_CACHE_NAME:
                builder.simpleCache(true);
                builder.memory().whenFull(EvictionStrategy.REMOVE).maxCount(10000);
                return builder;
            // 复制缓存
            case WORK_CACHE_NAME:
                if (clustered) {
                    builder.clustering().cacheMode(CacheMode.REPL_SYNC);
                }
                return builder;
            default:
                return null;
        }
    }

    private static boolean isClustered(ConfigurationBuilderHolder holder) {
        return holder.getGlobalConfigurationBuilder().transport().getTransport() != null;
    }

    private static void setExpiration(ConfigurationBuilderHolder holder, Config.Scope config, String cacheName, long defaultLifespan) {
        var builder = holder.getNamedConfigurationBuilders().get(cacheName);
        if (builder == null) {
            throw cacheNotFound(cacheName);
        }
        var lifespan = Optional.ofNullable(DurationConverter.parseDuration(config.get(lifespanConfigKey(cacheName))))
                .map(Duration::toMillis)
                .map(value -> value <= 0 ? DISABLED_LIFESPAN : value)
                .orElse(defaultLifespan);
        builder.expiration().lifespan(lifespan, TimeUnit.MILLISECONDS);
    }
}
