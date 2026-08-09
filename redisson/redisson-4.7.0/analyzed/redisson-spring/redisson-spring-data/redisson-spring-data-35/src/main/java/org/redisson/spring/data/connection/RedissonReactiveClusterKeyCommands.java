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

import io.netty.util.CharsetUtil;
import org.reactivestreams.Publisher;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.reactive.CommandReactiveExecutor;
import org.springframework.data.redis.connection.ReactiveClusterKeyCommands;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnection.BooleanResponse;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 集群模式下 Spring Data Redis 响应式 Key 命令实现。
 * <p>继承 {@link RedissonReactiveKeyCommands} 并实现 {@link ReactiveClusterKeyCommands}；
跨 slot 的 RENAME 通过 DUMP/RESTORE 与 TTL 迁移完成。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonReactiveClusterKeyCommands extends RedissonReactiveKeyCommands implements ReactiveClusterKeyCommands {

    /** 注入响应式命令执行器。 */
    public RedissonReactiveClusterKeyCommands(CommandReactiveExecutor executorService) {
        super(executorService);
    }

    /** 在指定集群节点上执行 KEYS 并合并各 master 结果。 */
    @Override
    public Mono<List<ByteBuffer>> keys(RedisClusterNode node, ByteBuffer pattern) {
        Mono<List<String>> m = executorService.reactive(() -> {
            List<CompletableFuture<List<String>>> futures = executorService.readAllAsync(StringCodec.INSTANCE, RedisCommands.KEYS, toByteArray(pattern));
            CompletableFuture<Void> ff = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            CompletableFuture<List<String>> future = ff.thenApply(r -> {
                return futures.stream().flatMap(f -> f.getNow(new ArrayList<>()).stream()).collect(Collectors.toList());
            }).toCompletableFuture();
            return new CompletableFutureWrapper<>(future);
        });
        return m.map(v -> v.stream().map(t -> ByteBuffer.wrap(t.getBytes(CharsetUtil.UTF_8))).collect(Collectors.toList()));
    }

    /** 在指定节点上执行 RANDOMKEY。 */
    @Override
    public Mono<ByteBuffer> randomKey(RedisClusterNode node) {
        RedisClient entry = getEntry(node);
        Mono<byte[]> m = executorService.reactive(() -> {
            return executorService.readRandomAsync(entry, ByteArrayCodec.INSTANCE, RedisCommands.RANDOM_KEY);
        });
        return m.map(v -> ByteBuffer.wrap(v));
    }

    /** 同 slot 走父类 RENAME；跨 slot 时 DUMP+RESTORE 后删除旧 key。 */
    @Override
    public Flux<BooleanResponse<RenameCommand>> rename(Publisher<RenameCommand> commands) {

        return execute(commands, command -> {
            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getNewKey(), "New name must not be null!");

            byte[] keyBuf = toByteArray(command.getKey());
            byte[] newKeyBuf = toByteArray(command.getNewKey());

            // 源 key 与目标 key 在同一 hash slot，可直接 RENAME。
            if (executorService.getConnectionManager().calcSlot(keyBuf) == executorService.getConnectionManager().calcSlot(newKeyBuf)) {
                return super.rename(commands);
            }

            return read(keyBuf, ByteArrayCodec.INSTANCE, RedisCommands.DUMP, keyBuf)
                    .filter(Objects::nonNull)
                    .zipWith(
                            Mono.defer(() -> pTtl(command.getKey())
                                    .filter(Objects::nonNull)
                                    .map(ttl -> Math.max(0, ttl))
                                    .switchIfEmpty(Mono.just(0L))
                            )
                    )
                    .flatMap(valueAndTtl -> {
                        return write(newKeyBuf, StringCodec.INSTANCE, RedisCommands.RESTORE, newKeyBuf, valueAndTtl.getT2(), valueAndTtl.getT1());
                    })
                    .thenReturn(new BooleanResponse<>(command, true))
                    .doOnSuccess((ignored) -> del(command.getKey()));
        });
    }

    /** 跨 slot 时仅当新 key 不存在且 DUMP 成功才 RESTORE 并删除旧 key。 */
    @Override
    public Flux<ReactiveRedisConnection.BooleanResponse<RenameCommand>> renameNX(Publisher<RenameCommand> commands) {
        return execute(commands, command -> {
            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getNewKey(), "New name must not be null!");

            byte[] keyBuf = toByteArray(command.getKey());
            byte[] newKeyBuf = toByteArray(command.getNewKey());

            if (executorService.getConnectionManager().calcSlot(keyBuf) == executorService.getConnectionManager().calcSlot(newKeyBuf)) {
                return super.renameNX(commands);
            }

            return exists(command.getNewKey())
                    .zipWith(read(keyBuf, ByteArrayCodec.INSTANCE, RedisCommands.DUMP, keyBuf))
                    .filter(newKeyExistsAndDump -> !newKeyExistsAndDump.getT1() && Objects.nonNull(newKeyExistsAndDump.getT2()))
                    .map(Tuple2::getT2)
                    .zipWhen(value ->
                            pTtl(command.getKey())
                                    .filter(Objects::nonNull)
                                    .map(ttl -> Math.max(0, ttl))
                                    .switchIfEmpty(Mono.just(0L))

                    )
                    .flatMap(valueAndTtl -> write(newKeyBuf, StringCodec.INSTANCE, RedisCommands.RESTORE, newKeyBuf, valueAndTtl.getT2(), valueAndTtl.getT1())
                            .then(Mono.just(true)))
                    .switchIfEmpty(Mono.just(false))
                    .doOnSuccess(didRename -> {
                        if (didRename) {
                            del(command.getKey());
                        }
                    })
                    .map(didRename -> new BooleanResponse<>(command, didRename));
        });
    }

}
