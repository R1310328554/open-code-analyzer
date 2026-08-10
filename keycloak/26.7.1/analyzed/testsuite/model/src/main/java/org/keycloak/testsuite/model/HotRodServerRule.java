package org.keycloak.testsuite.model;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.keycloak.Config;
import org.keycloak.connections.infinispan.InfinispanUtil;
import org.keycloak.marshalling.Marshalling;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.configuration.cache.BackupConfiguration;
import org.infinispan.configuration.cache.BackupFailurePolicy;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.server.hotrod.HotRodServer;
import org.infinispan.server.hotrod.configuration.HotRodServerConfiguration;
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.junit.rules.ExternalResource;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.ACTION_TOKEN_CACHE;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.AUTHENTICATION_SESSIONS_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.CLIENT_SESSION_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.LOGIN_FAILURE_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.OFFLINE_CLIENT_SESSION_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.OFFLINE_USER_SESSION_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.USER_SESSION_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.WORK_CACHE_NAME;

/**
 * JUnit 规则：启动嵌入式 Hot Rod 服务器与远程缓存管理器，供模型层测试使用。
 */
public class HotRodServerRule extends ExternalResource {

    /** Keycloak 会话相关缓存名称列表。 */
    private static final List<String> CACHES_NAME = List.of(
            USER_SESSION_CACHE_NAME, OFFLINE_USER_SESSION_CACHE_NAME, CLIENT_SESSION_CACHE_NAME,
            OFFLINE_CLIENT_SESSION_CACHE_NAME, LOGIN_FAILURE_CACHE_NAME, WORK_CACHE_NAME, ACTION_TOKEN_CACHE,
            AUTHENTICATION_SESSIONS_CACHE_NAME
    );

    /** 第一个 Hot Rod 服务器实例。 */
    protected HotRodServer hotRodServer;

    /** 第二个 Hot Rod 服务器实例（跨站点测试）。 */
    protected HotRodServer hotRodServer2;

    /** Hot Rod 远程缓存客户端。 */
    protected RemoteCacheManager remoteCacheManager;

    /** 第一个嵌入式 Infinispan 缓存管理器。 */
    protected DefaultCacheManager hotRodCacheManager;

    /** 第二个嵌入式 Infinispan 缓存管理器。 */
    protected DefaultCacheManager hotRodCacheManager2;

    /** 测试结束后停止远程缓存管理器。 */
    @Override
    protected void after() {
        if (remoteCacheManager != null) {
            remoteCacheManager.stop();
        }
    }

    /**
     * 启动双节点 Hot Rod 集群并创建 Keycloak 远程缓存。
     *
     * @param config 配置作用域，可指定 {@code async} 缓存模式
     */
    public void createEmbeddedHotRodServer(Config.Scope config) {
        try {
            hotRodCacheManager = new DefaultCacheManager("hotrod/hotrod1.xml");
            hotRodCacheManager2 = new DefaultCacheManager("hotrod/hotrod2.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HotRodServerConfiguration build = new HotRodServerConfigurationBuilder().build();
        hotRodServer = new HotRodServer();
        hotRodServer.start(build, hotRodCacheManager);
        hotRodServer.postStart();

        HotRodServerConfiguration build2 = new HotRodServerConfigurationBuilder().port(11333).build();
        hotRodServer2 = new HotRodServer();
        hotRodServer2.start(build2, hotRodCacheManager2);
        hotRodServer2.postStart();


        // 创建 Hot Rod 远程客户端
        org.infinispan.client.hotrod.configuration.ConfigurationBuilder remoteBuilder = new org.infinispan.client.hotrod.configuration.ConfigurationBuilder();
        Marshalling.configure(remoteBuilder);
        org.infinispan.client.hotrod.configuration.Configuration cfg = remoteBuilder
                .addServers(hotRodServer.getHost() + ":" + hotRodServer.getPort() + ";"
                        + hotRodServer2.getHost() + ":" + hotRodServer2.getPort()).build();
        remoteCacheManager = new RemoteCacheManager(cfg);

        // 创建 Keycloak 所需的远程缓存
        createKeycloakCaches(config.getBoolean("async", false) ? CacheMode.REPL_ASYNC : CacheMode.REPL_SYNC);

        // 在远程缓存中使用 Keycloak 时间服务
        InfinispanUtil.setTimeServiceToKeycloakTime(hotRodCacheManager);
        InfinispanUtil.setTimeServiceToKeycloakTime(hotRodCacheManager2);
    }

    /** 在两个节点上创建 Keycloak 会话缓存并配置跨站点复制。 */
    private void createKeycloakCaches(CacheMode cacheMode) {
        var builder = createCacheConfigurationBuilder();
        builder.clustering().cacheMode(cacheMode);

        // 跨站点备份配置
        builder.sites().addBackup()
                .site("site-1")
                .backupFailurePolicy(BackupFailurePolicy.FAIL)
                .strategy(BackupConfiguration.BackupStrategy.SYNC)
                .replicationTimeout(15000);
        builder.sites().addBackup()
                .site("site-2")
                .backupFailurePolicy(BackupFailurePolicy.FAIL)
                .strategy(BackupConfiguration.BackupStrategy.SYNC)
                .replicationTimeout(15000);

        // 缩短锁超时，测试中可能出现预期死锁
        builder.locking()
                .lockAcquisitionTimeout(1, TimeUnit.SECONDS);

        // 启用事务以在死锁后保持数据一致
        builder.transaction().transactionMode(TransactionMode.TRANSACTIONAL)
                .lockingMode(LockingMode.PESSIMISTIC)
                .useSynchronization(false);

        var config = builder.build();
        var admin1 = hotRodCacheManager.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE);
        var admin2 = hotRodCacheManager2.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE);
        for (String c: CACHES_NAME) {
            admin1.getOrCreateCache(c, config);
            admin2.getOrCreateCache(c, config);
        }
    }

    /** 创建使用 PROTOSTREAM 编码的缓存配置构建器。 */
    public static ConfigurationBuilder createCacheConfigurationBuilder() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.encoding().mediaType(MediaType.APPLICATION_PROTOSTREAM);
        return builder;
    }

    /** 返回 Hot Rod 远程缓存管理器。 */
    public RemoteCacheManager getRemoteCacheManager() {
        return remoteCacheManager;
    }

    /** 返回第一个 Hot Rod 服务器。 */
    public HotRodServer getHotRodServer() {
        return hotRodServer;
    }

    /** 返回第二个 Hot Rod 服务器。 */
    public HotRodServer getHotRodServer2() {
        return hotRodServer2;
    }

    /** 返回第一个嵌入式缓存管理器。 */
    public DefaultCacheManager getHotRodCacheManager() {
        return hotRodCacheManager;
    }

    /** 返回第二个嵌入式缓存管理器。 */
    public DefaultCacheManager getHotRodCacheManager2() {
        return hotRodCacheManager2;
    }

    /** 以流形式返回两个嵌入式缓存管理器。 */
    public Stream<DefaultCacheManager> streamCacheManagers() {
        return Stream.of(hotRodCacheManager, hotRodCacheManager2);
    }
}
