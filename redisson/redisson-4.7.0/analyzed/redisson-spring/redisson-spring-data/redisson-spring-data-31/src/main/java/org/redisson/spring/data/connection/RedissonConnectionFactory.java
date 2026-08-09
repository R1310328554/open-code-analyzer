/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.spring.data.connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redisson.Redisson;
import org.redisson.RedissonKeys;
import org.redisson.RedissonReactive;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.config.Config;
import org.redisson.connection.SentinelConnectionManager;
import org.redisson.reactive.CommandReactiveService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.redis.ExceptionTranslationStrategy;
import org.springframework.data.redis.PassThroughExceptionTranslationStrategy;
import org.springframework.data.redis.connection.ReactiveRedisClusterConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;

/**
 * 基于 Redisson 的 Spring Data Redis 连接工厂。
 * <p>同时实现阻塞式 {@link RedisConnectionFactory} 与响应式
 * {@link ReactiveRedisConnectionFactory}；集群/Sentinel 模式按配置返回对应连接类型；
 * 异常经 {@link RedissonExceptionConverter} 翻译为 Spring {@link DataAccessException}。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonConnectionFactory implements RedisConnectionFactory, 
                ReactiveRedisConnectionFactory, InitializingBean, DisposableBean {

    private final static Log log = LogFactory.getLog(RedissonConnectionFactory.class);
    
    /** 全局异常翻译策略，供 {@link #translateExceptionIfPossible} 使用。 */
    public static final ExceptionTranslationStrategy EXCEPTION_TRANSLATION = 
                                new PassThroughExceptionTranslationStrategy(new RedissonExceptionConverter());

    private Config config;
    private RedissonClient redisson;
    /** {@code true} 表示工厂内部创建了 {@link RedissonClient}，销毁时需 shutdown。 */
    private boolean hasOwnRedisson;
    private boolean filterOkResponses = false;

    /** 使用 {@link Redisson#create()} 默认配置创建工厂。 */
    /**
     * Creates factory with default Redisson configuration
     */
    public RedissonConnectionFactory() {
        this(Redisson.create());
        hasOwnRedisson = true;
    }
    
    /**
     * Creates factory with defined Redisson instance
     * 
     * @param redisson - Redisson instance
     */
    public RedissonConnectionFactory(RedissonClient redisson) {
        this.redisson = redisson;
    }
    
    /**
     * Creates factory with defined Redisson config
     * 
     * @param config - Redisson config
     */
    public RedissonConnectionFactory(Config config) {
        super();
        this.config = config;
        hasOwnRedisson = true;
    }

    public boolean isFilterOkResponses() {
        return filterOkResponses;
    }

    public void setFilterOkResponses(boolean filterOkResponses) {
        this.filterOkResponses = filterOkResponses;
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        return EXCEPTION_TRANSLATION.translate(ex);
    }

    /** 若持有自建客户端则关闭 Redisson 实例。 */
    @Override
    public void destroy() throws Exception {
        if (hasOwnRedisson) {
            redisson.shutdown();
        }
    }

    /** 若注入了 {@link Config}，在此阶段创建 {@link RedissonClient}。 */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (config != null) {
            redisson = Redisson.create(config);
        }
    }

    /** 按配置返回 {@link RedissonClusterConnection} 或 {@link RedissonConnection}。 */
    @Override
    public RedisConnection getConnection() {
        // 集群配置时使用 Cluster 连接实现。
        if (redisson.getConfig().isClusterConfig()) {
            return new RedissonClusterConnection(redisson, filterOkResponses);
        }
        return new RedissonConnection(redisson, filterOkResponses);
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        if (!redisson.getConfig().isClusterConfig()) {
            throw new InvalidDataAccessResourceUsageException("Redisson is not in Cluster mode");
        }
        return new RedissonClusterConnection(redisson, filterOkResponses);
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return true;
    }

    /** 遍历 Sentinel 节点 PING，返回首个可用的 {@link RedissonSentinelConnection}。 */
    @Override
    public RedisSentinelConnection getSentinelConnection() {
        if (!redisson.getConfig().isSentinelConfig()) {
            throw new InvalidDataAccessResourceUsageException("Redisson is not in Sentinel mode");
        }
        
        SentinelConnectionManager manager = (SentinelConnectionManager)(((Redisson)redisson).getCommandExecutor().getConnectionManager());
        for (RedisClient client : manager.getSentinels()) {
            org.redisson.client.RedisConnection connection = null;
            try {
                connection = client.connect();
                String res = connection.sync(RedisCommands.PING);
                // 首个响应 PONG 的 Sentinel 用于 Spring Data 管理命令。
                if ("pong".equalsIgnoreCase(res)) {
                    return new RedissonSentinelConnection(connection);
                }
            } catch (Exception e) {
                log.warn("Can't connect to " + client, e);
                if (connection != null) {
                    connection.closeAsync();
                }
            }
        }
        
        throw new InvalidDataAccessResourceUsageException("Sentinels are offline");
    }

    /** 返回单机或集群响应式 Redis 连接。 */
    @Override
    public ReactiveRedisConnection getReactiveConnection() {
        if (redisson.getConfig().isClusterConfig()) {
            return new RedissonReactiveRedisClusterConnection(((RedissonReactive)redisson.reactive()).getCommandExecutor());
        }

        return new RedissonReactiveRedisConnection(((RedissonReactive)redisson.reactive()).getCommandExecutor());
    }

    /** 非集群模式调用时抛出 {@link InvalidDataAccessResourceUsageException}。 */
    @Override
    public ReactiveRedisClusterConnection getReactiveClusterConnection() {
        if (!redisson.getConfig().isClusterConfig()) {
            throw new InvalidDataAccessResourceUsageException("Redisson is not in Cluster mode");
        }

        return new RedissonReactiveRedisClusterConnection(((RedissonReactive)redisson.reactive()).getCommandExecutor());
    }

}
