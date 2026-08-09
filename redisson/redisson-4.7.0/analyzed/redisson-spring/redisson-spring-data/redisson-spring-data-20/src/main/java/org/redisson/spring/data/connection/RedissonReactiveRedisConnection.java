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

import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.reactive.CommandReactiveExecutor;
import org.springframework.data.redis.connection.ReactiveGeoCommands;
import org.springframework.data.redis.connection.ReactiveHashCommands;
import org.springframework.data.redis.connection.ReactiveHyperLogLogCommands;
import org.springframework.data.redis.connection.ReactiveKeyCommands;
import org.springframework.data.redis.connection.ReactiveListCommands;
import org.springframework.data.redis.connection.ReactiveNumberCommands;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveScriptingCommands;
import org.springframework.data.redis.connection.ReactiveServerCommands;
import org.springframework.data.redis.connection.ReactiveSetCommands;
import org.springframework.data.redis.connection.ReactiveStringCommands;
import org.springframework.data.redis.connection.ReactiveZSetCommands;

import reactor.core.publisher.Mono;

/**
 * Spring Data Redis 单机模式响应式连接门面。
 * <p>实现 {@link ReactiveRedisConnection}，按数据类型委托各 {@code RedissonReactive*Commands}。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonReactiveRedisConnection extends RedissonBaseReactive implements ReactiveRedisConnection {

    /** 注入响应式命令执行器。 */
    public RedissonReactiveRedisConnection(CommandReactiveExecutor executorService) {
        super(executorService);
    }
    
    /** 返回 Key 命令实现。 */
    @Override
    public ReactiveKeyCommands keyCommands() {
        return new RedissonReactiveKeyCommands(executorService);
    }

    /** 返回 String 命令实现。 */
    @Override
    public ReactiveStringCommands stringCommands() {
        return new RedissonReactiveStringCommands(executorService);
    }

    @Override
    public ReactiveNumberCommands numberCommands() {
        return new RedissonReactiveNumberCommands(executorService);
    }

    @Override
    public ReactiveListCommands listCommands() {
        return new RedissonReactiveListCommands(executorService);
    }

    @Override
    public ReactiveSetCommands setCommands() {
        return new RedissonReactiveSetCommands(executorService);
    }

    @Override
    public ReactiveZSetCommands zSetCommands() {
        return new RedissonReactiveZSetCommands(executorService);
    }

    @Override
    public ReactiveHashCommands hashCommands() {
        return new RedissonReactiveHashCommands(executorService);
    }

    @Override
    public ReactiveGeoCommands geoCommands() {
        return new RedissonReactiveGeoCommands(executorService);
    }

    @Override
    public ReactiveHyperLogLogCommands hyperLogLogCommands() {
        return new RedissonReactiveHyperLogLogCommands(executorService);
    }

    /** 返回 Lua 脚本命令实现。 */
    @Override
    public ReactiveScriptingCommands scriptingCommands() {
        return new RedissonReactiveScriptingCommands(executorService);
    }

    @Override
    public ReactiveServerCommands serverCommands() {
        return new RedissonReactiveServerCommands(executorService);
    }

    /** PING：检测连接可用性。 */
    @Override
    public Mono<String> ping() {
        return read(null, StringCodec.INSTANCE, RedisCommands.PING);
    }

    /** 响应式连接由工厂统一管理生命周期，此处为空实现。 */
    @Override
    public void close() {
    }

}
