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

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.redisson.api.RFuture;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.connection.ClientConnectionsEntry;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.reactive.CommandReactiveExecutor;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisClusterNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data Redis 响应式命令实现的公共基类。
 * <p>封装 {@link CommandReactiveExecutor} 上的读写调用、集群节点路由与
 {@link ByteBuffer} 工具方法，供各 {@code RedissonReactive*}Commands 复用。
 *
 * @author Nikita Koksharov
 *
 */
abstract class RedissonBaseReactive {

    /** 响应式 Redis 命令执行器。 */
    final CommandReactiveExecutor executorService;
    
    /** 注入 Redisson 响应式命令执行器。 */
    RedissonBaseReactive(CommandReactiveExecutor executorService) {
        this.executorService = executorService;
    }
    
    /** 将 {@link ByteBuffer} 拷贝为 byte[]，不改变原 buffer 的 position。 */
    public static byte[] toByteArray(ByteBuffer buffer) {
        byte[] dst = new byte[buffer.remaining()];
        int pos = buffer.position();
        buffer.get(dst);
        buffer.position(pos);
        return dst;
    }

    /** 将 {@code Void} 型 Future 映射为固定 {@code "OK"} 字符串响应。 */
    RFuture<String> toStringFuture(RFuture<Void> f) {
        CompletionStage<String> ff = f.thenApply(r -> "OK");
        return new CompletableFutureWrapper<>(ff);
    }
    
    /** 在指定集群节点上执行写命令并包装为 {@link Mono}。 */
    <T> Mono<T> execute(RedisClusterNode node, RedisCommand<T> command, Object... params) {
        RedisClient entry = getEntry(node);
        return executorService.reactive(() -> {
            return executorService.writeAsync(entry, StringCodec.INSTANCE, command, params);
        });
    }

    /** 根据 {@link RedisClusterNode} 地址解析底层 {@link RedisClient} 连接。 */
    protected RedisClient getEntry(RedisClusterNode node) {
        InetSocketAddress addr = new InetSocketAddress(node.getHost(), node.getPort());
        MasterSlaveEntry entry = executorService.getConnectionManager().getEntry(addr);
        ClientConnectionsEntry e = entry.getEntry(addr);
        return e.getClient();
    }

    /** 将 Publisher 命令流按序 {@code concatMap} 展开为响应 {@link Flux}。 */
    <V, T> Flux<T> execute(Publisher<V> commands, Function<V, Publisher<T>> mapper) {
        Flux<V> s = Flux.from(commands);
        return s.concatMap(mapper);
    }
    
    /** 按 key 路由写命令；异常映射为 {@link RedisSystemException}。 */
    <T> Mono<T> write(byte[] key, Codec codec, RedisCommand<?> command, Object... params) {
        Mono<T> f = executorService.reactive(() -> {
            return executorService.writeAsync(key, codec, command, params);
        });
        return f.onErrorMap(e -> new RedisSystemException(e.getMessage(), e));
    }
    
    /** 按 key 路由读命令；异常映射为 {@link RedisSystemException}。 */
    <T> Mono<T> read(byte[] key, Codec codec, RedisCommand<?> command, Object... params) {
        Mono<T> f = executorService.reactive(() -> {
            return executorService.readAsync(key, codec, command, params);
        });
        return f.onErrorMap(e -> new RedisSystemException(e.getMessage(), e));
    }

    
}
