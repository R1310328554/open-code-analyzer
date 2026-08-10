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

package org.keycloak.connections.infinispan;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.keycloak.common.util.MultiSiteUtils;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.Provider;

import org.infinispan.Cache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.util.concurrent.BlockingManager;

/**
 * Infinispan 连接提供者 SPI 接口。
 * <p>
 * 定义 Keycloak 使用的全部缓存名称、容量默认值及缓存访问、远程缓存、
 * 节点信息、ProtoStream 迁移、阻塞/调度执行器等核心能力。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface InfinispanConnectionProvider extends Provider {

    /** Realm 元数据本地缓存名。 */
    String REALM_CACHE_NAME = "realms";
    /** Realm 修订版本本地缓存名。 */
    String REALM_REVISIONS_CACHE_NAME = "realmRevisions";
    int REALM_REVISIONS_CACHE_DEFAULT_MAX = 20000;

    /** 用户元数据本地缓存名。 */
    String USER_CACHE_NAME = "users";
    /** 用户修订版本本地缓存名。 */
    String USER_REVISIONS_CACHE_NAME = "userRevisions";
    int USER_REVISIONS_CACHE_DEFAULT_MAX = 100000;

    /** 在线用户会话分布式缓存名。 */
    String USER_SESSION_CACHE_NAME = "sessions";
    /** 在线客户端会话分布式缓存名。 */
    String CLIENT_SESSION_CACHE_NAME = "clientSessions";
    /** 离线用户会话分布式缓存名。 */
    String OFFLINE_USER_SESSION_CACHE_NAME = "offlineSessions";
    /** 离线客户端会话分布式缓存名。 */
    String OFFLINE_CLIENT_SESSION_CACHE_NAME = "offlineClientSessions";
    /** 登录失败记录分布式缓存名。 */
    String LOGIN_FAILURE_CACHE_NAME = "loginFailures";
    /** 认证会话分布式缓存名。 */
    String AUTHENTICATION_SESSIONS_CACHE_NAME = "authenticationSessions";
    /** 集群锁与事件传递的 work 缓存名。 */
    String WORK_CACHE_NAME = "work";
    /** 授权策略本地缓存名。 */
    String AUTHORIZATION_CACHE_NAME = "authorization";
    /** 授权修订版本本地缓存名。 */
    String AUTHORIZATION_REVISIONS_CACHE_NAME = "authorizationRevisions";
    int AUTHORIZATION_REVISIONS_CACHE_DEFAULT_MAX = 20000;
    int SESSIONS_CACHE_DEFAULT_MAX = 10000;

    /** Action Token 分布式缓存名。 */
    String ACTION_TOKEN_CACHE = "actionTokens";
    int ACTION_TOKEN_CACHE_DEFAULT_MAX = -1;
    int ACTION_TOKEN_MAX_IDLE_SECONDS = -1;
    long ACTION_TOKEN_WAKE_UP_INTERVAL_SECONDS = 5 * 60 * 1000L;

    /** 密钥本地缓存名。 */
    String KEYS_CACHE_NAME = "keys";
    int KEYS_CACHE_DEFAULT_MAX = 1000;
    int KEYS_CACHE_MAX_IDLE_SECONDS = 3600;

    /** CRL 证书吊销列表本地缓存名。 */
    String CRL_CACHE_NAME = "crl";
    int CRL_CACHE_DEFAULT_MAX = 1000;

    // Wildfly 上用于标识分布式缓存地址与会话粘滞路由的系统属性
    String JBOSS_NODE_NAME = "jboss.node.name";
    String JGROUPS_UDP_MCAST_ADDR = "jgroups.mcast_addr";
    String JGROUPS_BIND_ADDR = "jgroups.bind.address";

    // TODO Wildfly 中无此属性，需确认对应属性是否存在
    String JBOSS_SITE_NAME = "jboss.site.name";

    String JMX_DOMAIN = "jboss.datagrid-infinispan";

    /** 未配置 jboss.node.name 时节点名前缀。 */
    String NODE_PREFIX = "node_";

    /** 本地（非复制）缓存名称列表。 */
    String[] LOCAL_CACHE_NAMES = {
            REALM_CACHE_NAME,
            REALM_REVISIONS_CACHE_NAME,
            USER_CACHE_NAME,
            USER_REVISIONS_CACHE_NAME,
            AUTHORIZATION_CACHE_NAME,
            AUTHORIZATION_REVISIONS_CACHE_NAME,
            KEYS_CACHE_NAME,
            CRL_CACHE_NAME,
    };

    /** 用户与客户端会话缓存名称（在线 + 离线）。 */
    String[] USER_AND_CLIENT_SESSION_CACHES = {
            USER_SESSION_CACHE_NAME,
            CLIENT_SESSION_CACHE_NAME,
            OFFLINE_USER_SESSION_CACHE_NAME,
            OFFLINE_CLIENT_SESSION_CACHE_NAME,
    };

    /** 可配置为分布式或复制模式的集群缓存名称。 */
    String[] CLUSTERED_CACHE_NAMES = {
            USER_SESSION_CACHE_NAME,
            CLIENT_SESSION_CACHE_NAME,
            OFFLINE_USER_SESSION_CACHE_NAME,
            OFFLINE_CLIENT_SESSION_CACHE_NAME,
            LOGIN_FAILURE_CACHE_NAME,
            AUTHENTICATION_SESSIONS_CACHE_NAME,
            ACTION_TOKEN_CACHE,
            WORK_CACHE_NAME
    };

    String[] ALL_CACHES_NAME = Stream.concat(Arrays.stream(LOCAL_CACHE_NAMES), Arrays.stream(CLUSTERED_CACHE_NAMES)).toArray(String[]::new);

    /** 支持 max-count 配置的本地缓存。 */
    String[] LOCAL_MAX_COUNT_CACHES = new String[]{
            AUTHORIZATION_CACHE_NAME,
            CRL_CACHE_NAME,
            KEYS_CACHE_NAME,
            REALM_CACHE_NAME,
            USER_CACHE_NAME
    };

    /** 支持 max-count 配置的集群缓存。 */
    String[] CLUSTERED_MAX_COUNT_CACHES = new String[]{
            CLIENT_SESSION_CACHE_NAME,
            OFFLINE_USER_SESSION_CACHE_NAME,
            OFFLINE_CLIENT_SESSION_CACHE_NAME,
            USER_SESSION_CACHE_NAME
    };

    /** 支持 numOwners 配置项的集群缓存。 */
    String[] CLUSTERED_CACHE_NUM_OWNERS = new String[]{
            USER_SESSION_CACHE_NAME,
            CLIENT_SESSION_CACHE_NAME,
            LOGIN_FAILURE_CACHE_NAME,
            AUTHENTICATION_SESSIONS_CACHE_NAME,
            ACTION_TOKEN_CACHE,
    };

    /**
     * 等价于 {@link InfinispanConnectionProvider#getCache(String, boolean)} 且 createIfAbsent 为 {@code true}。
     */
    default <K, V> Cache<K, V> getCache(String name) {
        return getCache(name, true);
    }

    /**
     * 按名称获取 Infinispan 缓存实例。
     *
     * @param name           缓存名称
     * @param createIfAbsent 若缓存不存在是否自动创建
     * @return 缓存实例
     * @param <K> 键类型
     * @param <V> 值类型
     */
    <K, V> Cache<K, V> getCache(String name, boolean createIfAbsent);

    /**
     * 获取指定名称的 Hot Rod 远程缓存。
     * 可从嵌入式缓存的 remoteStore 配置获取，或返回安全 Hot Rod 端点对应的缓存。
     */
    <K, V> RemoteCache<K, V> getRemoteCache(String name);

    /**
     * @return 集群拓扑信息
     * @deprecated {@link TopologyInfo} 中的逻辑已不再使用，请改用 {@link #getNodeInfo()}。
     */
    @Deprecated(since = "26.5", forRemoval = true)
    TopologyInfo getTopologyInfo();

    /**
     * @return 本节点信息（节点名、站点名等）
     */
    NodeInfo getNodeInfo();

    /**
     * 将 JBoss Marshalling 编码迁移至 Infinispan ProtoStream。
     *
     * @return 迁移完成的 {@link CompletionStage}
     */
    CompletionStage<Void> migrateToProtoStream();

    /**
     * 返回用于执行阻塞 I/O 任务的执行器。
     * <p>
     * 使用 Infinispan {@link BlockingManager} 的阻塞线程池；启用虚拟线程时为虚拟线程执行器。
     *
     * @param name 用于跟踪日志的执行器名称
     * @return Infinispan 阻塞 {@link Executor}
     */
    default Executor getExecutor(String name) {
        return getBlockingManager().asExecutor(name);
    }

    /**
     * @return Infinispan {@link ScheduledExecutorService}，不可直接执行长时或阻塞操作。
     */
    ScheduledExecutorService getScheduledExecutor();

    /**
     * 语法糖：通过 SessionFactory 获取 {@link RemoteCache}。
     *
     * @see InfinispanConnectionProvider#getRemoteCache(String)
     */
    static <K, V> RemoteCache<K, V> getRemoteCache(KeycloakSessionFactory factory, String cacheName) {
        try (var session = factory.create()) {
            return session.getProvider(InfinispanConnectionProvider.class).getRemoteCache(cacheName);
        }
    }

    /**
     * 返回 Infinispan {@link BlockingManager}，用于 offload 磁盘 I/O 等阻塞操作。
     *
     * @return Infinispan {@link BlockingManager}
     */
    BlockingManager getBlockingManager();

    /**
     * 启用 persistent-user-sessions 时跳过外部 Infinispan 的会话缓存。
     * 持久化会话模式下无需外部会话缓存。
     */
    static Stream<String> skipSessionsCacheIfRequired(Stream<String> caches) {
        if (!MultiSiteUtils.isPersistentSessionsEnabled()) {
            return caches;
        }
        // 启用 persistent-user-sessions 后，外部 Infinispan 不再需要会话缓存
        return caches
                .filter(Predicate.isEqual(USER_SESSION_CACHE_NAME).negate())
                .filter(Predicate.isEqual(OFFLINE_USER_SESSION_CACHE_NAME).negate())
                .filter(Predicate.isEqual(CLIENT_SESSION_CACHE_NAME).negate())
                .filter(Predicate.isEqual(OFFLINE_CLIENT_SESSION_CACHE_NAME).negate());
    }

    /** 返回用户自定义的 ProtoStream 序列化器。 */
    Marshaller getMarshaller();
}
