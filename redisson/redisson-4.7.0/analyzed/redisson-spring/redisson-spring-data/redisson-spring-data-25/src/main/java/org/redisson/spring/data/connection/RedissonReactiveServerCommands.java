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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RFuture;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.client.protocol.decoder.ObjectDecoder;
import org.redisson.client.protocol.decoder.TimeLongObjectDecoder;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.reactive.CommandReactiveExecutor;
import org.springframework.data.redis.connection.ReactiveServerCommands;
import org.springframework.data.redis.connection.convert.StringToRedisClientInfoConverter;
import org.springframework.data.redis.core.types.RedisClientInfo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data Redis 响应式 Server 命令实现。
 * <p>封装 BGSAVE、SAVE、FLUSHDB/FLUSHALL、INFO、CONFIG、TIME、CLIENT LIST 等管理命令。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonReactiveServerCommands extends RedissonBaseReactive implements ReactiveServerCommands {

    /** 注入响应式命令执行器。 */
    RedissonReactiveServerCommands(CommandReactiveExecutor executorService) {
        super(executorService);
    }

    static final RedisStrictCommand<String> BGREWRITEAOF = new RedisStrictCommand<String>("BGREWRITEAOF");
    
    /** BGREWRITEAOF：异步重写 AOF 文件。 */
    @Override
    public Mono<String> bgReWriteAof() {
        return write(null, StringCodec.INSTANCE, BGREWRITEAOF);
    }

    static final RedisStrictCommand<String> BGSAVE = new RedisStrictCommand<String>("BGSAVE");
    
    /** BGSAVE：后台触发 RDB 快照。 */
    @Override
    public Mono<String> bgSave() {
        return write(null, StringCodec.INSTANCE, BGSAVE);
    }

    @Override
    public Mono<Long> lastSave() {
        return write(null, StringCodec.INSTANCE, RedisCommands.LASTSAVE);
    }

    static final RedisStrictCommand<String> SAVE = new RedisStrictCommand<String>("SAVE");

    @Override
    public Mono<String> save() {
        return write(null, StringCodec.INSTANCE, SAVE);
    }

    /** DBSIZE：汇总所有 master 节点的 key 数量。 */
    @Override
    public Mono<Long> dbSize() {
        return executorService.reactive(() -> {
            List<CompletableFuture<Long>> futures = executorService.readAllAsync(RedisCommands.DBSIZE);
            CompletableFuture<Void> f = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            CompletableFuture<Long> s = f.thenApply(r -> futures.stream().mapToLong(v -> v.getNow(0L)).sum());
            return new CompletableFutureWrapper<>(s);
        });
    }
    
    private static final RedisStrictCommand<String> FLUSHDB = new RedisStrictCommand<String>("FLUSHDB");

    /** FLUSHDB：清空所有节点当前数据库。 */
    @Override
    public Mono<String> flushDb() {
        return executorService.reactive(() -> {
            RFuture<Void> f = executorService.writeAllVoidAsync(FLUSHDB);
            return toStringFuture(f);
        });
    }

    private static final RedisStrictCommand<String> FLUSHALL = new RedisStrictCommand<String>("FLUSHALL");

    @Override
    public Mono<String> flushAll() {
        return executorService.reactive(() -> {
            RFuture<Void> f = executorService.writeAllVoidAsync(FLUSHALL);
            return toStringFuture(f);
        });
    }

    static final RedisStrictCommand<Properties> INFO_DEFAULT = new RedisStrictCommand<Properties>("INFO", "DEFAULT", new ObjectDecoder(new PropertiesDecoder()));
    static final RedisStrictCommand<Properties> INFO = new RedisStrictCommand<Properties>("INFO", new ObjectDecoder(new PropertiesDecoder()));
    
    /** INFO DEFAULT：读取默认段服务器信息。 */
    @Override
    public Mono<Properties> info() {
        return read(null, StringCodec.INSTANCE, INFO_DEFAULT);
    }

    @Override
    public Mono<Properties> info(String section) {
        return read(null, StringCodec.INSTANCE, INFO, section);
    }

    static final RedisStrictCommand<Properties> CONFIG_GET = new RedisStrictCommand<Properties>("CONFIG", "GET", new PropertiesListDecoder());
    
    /** CONFIG GET：按模式读取运行时配置。 */
    @Override
    public Mono<Properties> getConfig(String pattern) {
        return read(null, StringCodec.INSTANCE, CONFIG_GET, pattern);
    }
    
    static final RedisStrictCommand<String> CONFIG_SET = new RedisStrictCommand<String>("CONFIG", "SET");

    @Override
    public Mono<String> setConfig(String param, String value) {
        return write(null, StringCodec.INSTANCE, CONFIG_SET, param, value);
    }
    
    static final RedisStrictCommand<String> CONFIG_RESETSTAT = new RedisStrictCommand<String>("CONFIG", "RESETSTAT");

    @Override
    public Mono<String> resetConfigStats() {
        return write(null, StringCodec.INSTANCE, CONFIG_RESETSTAT);
    }

    static final RedisStrictCommand<Long> TIME = new RedisStrictCommand<Long>("TIME", new TimeLongObjectDecoder());
    
    @Override
    public Mono<Long> time() {
        return read(null, LongCodec.INSTANCE, TIME);
    }

    /** TIME：读取服务器时间并按 {@link TimeUnit} 转换（毫秒基准）。 */
    @Override
    public Mono<Long> time(TimeUnit timeUnit) {
        return read(null, LongCodec.INSTANCE, new RedisStrictCommand<>("TIME", new TimeLongObjectDecoder() {
            @Override
            public Long decode(List<Object> parts, State state) {
                Long time = super.decode(parts, state);
                return timeUnit.convert(time, TimeUnit.MILLISECONDS);
            }
        }));
    }

    @Override
    public Mono<String> killClient(String host, int port) {
        throw new UnsupportedOperationException();
    }

    /** 客户端名称应通过 Redisson {@link Config} 配置，此处不支持。 */
    @Override
    public Mono<String> setClientName(String name) {
        throw new UnsupportedOperationException("Should be defined through Redisson Config object");
    }

    @Override
    public Mono<String> getClientName() {
        throw new UnsupportedOperationException();
    }

    private static final StringToRedisClientInfoConverter CONVERTER = new StringToRedisClientInfoConverter();

    /** CLIENT LIST：解析为 {@link RedisClientInfo} 流。 */
    @Override
    public Flux<RedisClientInfo> getClientList() {
        Mono<List<String>> m = read(null, StringCodec.INSTANCE, RedisCommands.CLIENT_LIST);
        return m.flatMapMany(s -> Flux.fromIterable(CONVERTER.convert(s.toArray(new String[s.size()]))));
    }

}
