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

import java.util.List;
import java.util.Properties;

import org.redisson.client.RedisClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.reactive.CommandReactiveExecutor;
import org.springframework.data.redis.connection.ReactiveClusterServerCommands;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.connection.convert.StringToRedisClientInfoConverter;
import org.springframework.data.redis.core.types.RedisClientInfo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 集群模式下 Spring Data Redis 响应式 Server 命令实现。
 * <p>继承 {@link RedissonReactiveServerCommands} 并实现 {@link ReactiveClusterServerCommands}；
支持对单个 {@link RedisClusterNode} 执行 BGSAVE、INFO、CONFIG 等管理命令。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonReactiveClusterServerCommands extends RedissonReactiveServerCommands implements ReactiveClusterServerCommands {

    /** 注入响应式命令执行器。 */
    RedissonReactiveClusterServerCommands(CommandReactiveExecutor executorService) {
        super(executorService);
    }

    /** 在指定节点触发 BGREWRITEAOF。 */
    @Override
    public Mono<String> bgReWriteAof(RedisClusterNode node) {
        return execute(node, BGREWRITEAOF);
    }

    @Override
    public Mono<String> bgSave(RedisClusterNode node) {
        return execute(node, BGSAVE);
    }

    @Override
    public Mono<Long> lastSave(RedisClusterNode node) {
        return execute(node, RedisCommands.LASTSAVE);
    }

    @Override
    public Mono<String> save(RedisClusterNode node) {
        return execute(node, SAVE);
    }

    @Override
    public Mono<Long> dbSize(RedisClusterNode node) {
        return execute(node, RedisCommands.DBSIZE);
    }
    
    private static final RedisStrictCommand<String> FLUSHDB = new RedisStrictCommand<String>("FLUSHDB");

    /** 在指定节点执行 FLUSHDB。 */
    @Override
    public Mono<String> flushDb(RedisClusterNode node) {
        return execute(node, FLUSHDB);
    }
    
    private static final RedisStrictCommand<String> FLUSHALL = new RedisStrictCommand<String>("FLUSHALL");

    /** 在指定节点执行 FLUSHALL。 */
    @Override
    public Mono<String> flushAll(RedisClusterNode node) {
        return execute(node, FLUSHALL);
    }

    /** 在指定节点执行 FLUSHDB，{@link RedisServerCommands.FlushOption#ASYNC} 时追加 ASYNC 参数。 */
    @Override
    public Mono<String> flushDb(RedisClusterNode node, RedisServerCommands.FlushOption option) {
        if (option == RedisServerCommands.FlushOption.ASYNC) {
            return execute(node, FLUSHDB, option.toString());
        }
        return execute(node, FLUSHDB);
    }

    /** 在指定节点执行 FLUSHALL，{@link RedisServerCommands.FlushOption#ASYNC} 时追加 ASYNC 参数。 */
    @Override
    public Mono<String> flushAll(RedisClusterNode node, RedisServerCommands.FlushOption option) {
        if (option == RedisServerCommands.FlushOption.ASYNC) {
            return execute(node, FLUSHALL, option.toString());
        }
        return execute(node, FLUSHALL);
    }

    @Override
    public Mono<Properties> info() {
        return read(null, StringCodec.INSTANCE, INFO_DEFAULT);
    }

    @Override
    public Mono<Properties> info(String section) {
        return read(null, StringCodec.INSTANCE, INFO, section);
    }


    /** 读取指定节点的 INFO 默认段。 */
    @Override
    public Mono<Properties> info(RedisClusterNode node) {
        return execute(node, INFO_DEFAULT);
    }

    @Override
    public Mono<Properties> info(RedisClusterNode node, String section) {
        return execute(node, INFO, section);
    }

    @Override
    public Mono<Properties> getConfig(RedisClusterNode node, String pattern) {
        return execute(node, CONFIG_GET, pattern);
    }

    @Override
    public Mono<String> setConfig(RedisClusterNode node, String param, String value) {
        return execute(node, CONFIG_SET, param, value);
    }

    @Override
    public Mono<String> resetConfigStats(RedisClusterNode node) {
        return execute(node, CONFIG_RESETSTAT);
    }

    @Override
    public Mono<Long> time(RedisClusterNode node) {
        return execute(node, TIME);
    }

    private static final StringToRedisClientInfoConverter CONVERTER = new StringToRedisClientInfoConverter();
    
    /** 获取指定节点的 CLIENT LIST 并转换为 {@link RedisClientInfo} 流。 */
    @Override
    public Flux<RedisClientInfo> getClientList(RedisClusterNode node) {
        RedisClient entry = getEntry(node);
        Mono<List<String>> m = executorService.reactive(() -> {
            return executorService.readAsync(entry, StringCodec.INSTANCE, RedisCommands.CLIENT_LIST);
        });
        return m.flatMapMany(s -> Flux.fromIterable(CONVERTER.convert(s.toArray(new String[s.size()]))));
    }

}
