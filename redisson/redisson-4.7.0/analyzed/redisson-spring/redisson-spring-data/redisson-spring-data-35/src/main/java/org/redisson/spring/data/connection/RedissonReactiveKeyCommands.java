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

import org.reactivestreams.Publisher;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.client.protocol.convertor.BooleanReplayConvertor;
import org.redisson.client.protocol.convertor.Convertor;
import org.redisson.reactive.CommandReactiveExecutor;
import org.redisson.reactive.RedissonKeysReactive;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.ExpirationOptions;
import org.springframework.data.redis.connection.ReactiveKeyCommands;
import org.springframework.data.redis.connection.ReactiveRedisConnection.*;
import org.springframework.data.redis.connection.ValueEncoding;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Spring Data Redis 响应式 Key 命令实现。
 * <p>封装 EXISTS、TYPE、KEYS、RENAME、DEL、EXPIRE、TTL、MOVE 等通用 key 操作。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonReactiveKeyCommands extends RedissonBaseReactive implements ReactiveKeyCommands {

    /** 注入响应式命令执行器。 */
    public RedissonReactiveKeyCommands(CommandReactiveExecutor executorService) {
        super(executorService);
    }

    /** EXISTS：判断 key 是否存在。 */
    /** EXISTS：判断 key 是否存在。 */
    @Override
    public Flux<BooleanResponse<KeyCommand>> exists(Publisher<KeyCommand> keys) {
        return execute(keys, key -> {

            Assert.notNull(key.getKey(), "Key must not be null!");

            byte[] keyBuf = toByteArray(key.getKey());
            Mono<Boolean> m = read(keyBuf, StringCodec.INSTANCE, RedisCommands.EXISTS, keyBuf);
            return m.map(v -> new BooleanResponse<>(key, v));
        });
    }
    
    private static final RedisStrictCommand<DataType> TYPE = new RedisStrictCommand<DataType>("TYPE", obj -> DataType.fromCode(obj.toString()));

    /** TYPE：返回 key 的 {@link DataType}。 */
    /** TYPE：返回 key 的数据类型。 */
    @Override
    public Flux<CommandResponse<KeyCommand, DataType>> type(Publisher<KeyCommand> keys) {
        return execute(keys, key -> {

            Assert.notNull(key.getKey(), "Key must not be null!");

            byte[] keyBuf = toByteArray(key.getKey());
            Mono<DataType> m = read(keyBuf, StringCodec.INSTANCE, TYPE, keyBuf);
            return m.map(v -> new CommandResponse<>(key, v));
        });
    }
    
    /** TOUCH：更新 key 的最后访问时间。 */
    @Override
    public Flux<NumericResponse<Collection<ByteBuffer>, Long>> touch(Publisher<Collection<ByteBuffer>> keys) {
        return execute(keys, coll -> {

            Assert.notNull(coll, "Collection must not be null!");
            
            Object[] params = coll.stream().map(buf -> toByteArray(buf)).toArray(Object[]::new);

            Mono<Long> m = write(null, StringCodec.INSTANCE, RedisCommands.TOUCH_LONG, params);
            return m.map(v -> new NumericResponse<>(coll, v));
        });
    }

    /** KEYS：按模式匹配返回 key 列表（生产环境慎用）。 */
    /** KEYS：按模式匹配返回 key 集合。 */
    @Override
    public Flux<MultiValueResponse<ByteBuffer, ByteBuffer>> keys(Publisher<ByteBuffer> patterns) {
        return execute(patterns, pattern -> {

            Assert.notNull(pattern, "Pattern must not be null!");

            Mono<List<String>> m = read(null, StringCodec.INSTANCE, RedisCommands.KEYS, toByteArray(pattern));
            return m.map(v -> {
                List<ByteBuffer> values = v.stream().map(t -> ByteBuffer.wrap(t.getBytes())).collect(Collectors.toList());
                return new MultiValueResponse<>(pattern, values);   
            });
        });
    }

    /** SCAN：增量迭代 key 空间。 */
    @Override
    public Flux<ByteBuffer> scan(ScanOptions options) {
        RedissonKeysReactive reactive = new RedissonKeysReactive(executorService);
        if (options.getCount() != null) {
            return reactive.getKeysByPattern(options.getPattern(), options.getCount().intValue()).map(t -> ByteBuffer.wrap(t.getBytes()));
        }
        return reactive.getKeysByPattern(options.getPattern()).map(t -> ByteBuffer.wrap(t.getBytes()));
    }

    /** RANDOMKEY：随机返回一个 key。 */
    /** RANDOMKEY：随机返回一个 key。 */
    @Override
    public Mono<ByteBuffer> randomKey() {
        return executorService.reactive(() -> {
            return executorService.readRandomAsync(ByteArrayCodec.INSTANCE, RedisCommands.RANDOM_KEY);
        });
    }

    static final RedisStrictCommand<String> RENAME = new RedisStrictCommand<String>("RENAME");
    
    /** RENAME：重命名 key。 */
    /** RENAME：重命名 key。 */
    @Override
    public Flux<BooleanResponse<RenameCommand>> rename(Publisher<RenameCommand> commands) {
        return execute(commands, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getNewKey(), "New name must not be null!");

            byte[] keyBuf = toByteArray(command.getKey());
            byte[] newKeyBuf = toByteArray(command.getNewKey());
            Mono<String> m = write(keyBuf, StringCodec.INSTANCE, RENAME, keyBuf, newKeyBuf);
            return m.map(v -> new BooleanResponse<>(command, true));
        });
    }

    /** RENAMENX：仅当新 key 不存在时重命名。 */
    @Override
    public Flux<BooleanResponse<RenameCommand>> renameNX(Publisher<RenameCommand> commands) {
        return execute(commands, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getNewKey(), "New name must not be null!");

            byte[] keyBuf = toByteArray(command.getKey());
            byte[] newKeyBuf = toByteArray(command.getNewKey());
            Mono<Boolean> m = write(keyBuf, StringCodec.INSTANCE, RedisCommands.RENAMENX, keyBuf, newKeyBuf);
            return m.map(v -> new BooleanResponse<>(command, v));
        });
    }

    /** DEL：删除单个 key 并返回删除数量。 */
    /** DEL：删除一个或多个 key。 */
    @Override
    public Flux<NumericResponse<KeyCommand, Long>> del(Publisher<KeyCommand> keys) {
        Flux<KeyCommand> s = Flux.from(keys);
        return s.concatMap(command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");

            byte[] keyBuf = toByteArray(command.getKey());
            Mono<Long> m = write(keyBuf, StringCodec.INSTANCE, RedisCommands.DEL, keyBuf);
            return m.map(v -> new NumericResponse<>(command, v));
        });
    }

    /** 批量 DEL：一次删除多个 key。 */
    /** 批量 DEL：一次删除多个 key。 */
    @Override
    public Flux<NumericResponse<List<ByteBuffer>, Long>> mDel(Publisher<List<ByteBuffer>> keys) {
        return execute(keys, coll -> {

            Assert.notNull(coll, "List must not be null!");
            
            Object[] params = coll.stream().map(buf -> toByteArray(buf)).toArray(Object[]::new);

            Mono<Long> m = write(null, StringCodec.INSTANCE, RedisCommands.DEL, params);
            return m.map(v -> new NumericResponse<>(coll, v));
        });
    }

    /** UNLINK：异步删除 key。 */
    @Override
    public Flux<NumericResponse<KeyCommand, Long>> unlink(Publisher<KeyCommand> keys) {
        return execute(keys, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");

            byte[] keyBuf = toByteArray(command.getKey());
            Mono<Long> m = write(keyBuf, StringCodec.INSTANCE, RedisCommands.UNLINK, keyBuf);
            return m.map(v -> new NumericResponse<>(command, v));
        });
   }

    /** mUnlink：Redis 命令实现。 */
    @Override
    public Flux<NumericResponse<List<ByteBuffer>, Long>> mUnlink(Publisher<List<ByteBuffer>> keys) {
        return execute(keys, coll -> {

            Assert.notNull(coll, "List must not be null!");
            
            Object[] params = coll.stream().map(buf -> toByteArray(buf)).toArray(Object[]::new);

            Mono<Long> m = write(null, StringCodec.INSTANCE, RedisCommands.UNLINK, params);
            return m.map(v -> new NumericResponse<>(coll, v));
        });
    }

    private static final RedisStrictCommand<Boolean> EXPIRE = new RedisStrictCommand<Boolean>("EXPIRE", new BooleanReplayConvertor());
    
    /** EXPIRE：以秒为单位设置 key 过期时间。 */
    /** EXPIRE：以秒为单位设置过期时间。 */
    @Override
    public Flux<BooleanResponse<ExpireCommand>> expire(Publisher<ExpireCommand> commands) {
        return execute(commands, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");

            byte[] keyBuf = toByteArray(command.getKey());
            Mono<Boolean> m = write(keyBuf, StringCodec.INSTANCE, EXPIRE, keyBuf, command.getTimeout().getSeconds());
            return m.map(v -> new BooleanResponse<>(command, v));
        });
    }

    /** PEXPIRE：以毫秒为单位设置过期时间。 */
    @Override
    public Flux<BooleanResponse<ExpireCommand>> pExpire(Publisher<ExpireCommand> commands) {
        return execute(commands, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getTimeout(), "Timeout must not be null!");

            byte[] keyBuf = toByteArray(command.getKey());
            Mono<Boolean> m = write(keyBuf, StringCodec.INSTANCE, RedisCommands.PEXPIRE, keyBuf, command.getTimeout().toMillis());
            return m.map(v -> new BooleanResponse<>(command, v));
        });
    }

    private static final RedisStrictCommand<Boolean> EXPIREAT = new RedisStrictCommand<>("EXPIREAT", new BooleanReplayConvertor());
    
    /** EXPIREAT：按 Unix 秒时间戳设置过期。 */
    @Override
    public Flux<BooleanResponse<ExpireAtCommand>> expireAt(Publisher<ExpireAtCommand> commands) {
        return execute(commands, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");

            byte[] keyBuf = toByteArray(command.getKey());
            Mono<Boolean> m = write(keyBuf, StringCodec.INSTANCE, EXPIREAT, keyBuf, command.getExpireAt().getEpochSecond());
            return m.map(v -> new BooleanResponse<>(command, v));
        });
    }

    /** PEXPIREAT：按 Unix 毫秒时间戳设置过期。 */
    @Override
    public Flux<BooleanResponse<ExpireAtCommand>> pExpireAt(Publisher<ExpireAtCommand> commands) {
        return execute(commands, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");

            byte[] keyBuf = toByteArray(command.getKey());
            Mono<Boolean> m = write(keyBuf, StringCodec.INSTANCE, RedisCommands.PEXPIREAT, keyBuf, command.getExpireAt().toEpochMilli());
            return m.map(v -> new BooleanResponse<>(command, v));
        });
    }

    /** PERSIST：移除 key 的过期时间。 */
    @Override
    public Flux<BooleanResponse<KeyCommand>> persist(Publisher<KeyCommand> commands) {
        return execute(commands, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");

            byte[] keyBuf = toByteArray(command.getKey());
            Mono<Boolean> m = write(keyBuf, StringCodec.INSTANCE, RedisCommands.PERSIST, keyBuf);
            return m.map(v -> new BooleanResponse<>(command, v));
        });
    }
    
    private static final RedisStrictCommand<Long> TTL = new RedisStrictCommand<Long>("TTL");

    /** TTL：返回 key 剩余存活秒数。 */
    /** TTL：返回 key 剩余存活秒数。 */
    @Override
    public Flux<NumericResponse<KeyCommand, Long>> ttl(Publisher<KeyCommand> commands) {
        return execute(commands, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");

            byte[] keyBuf = toByteArray(command.getKey());
            Mono<Long> m = read(keyBuf, StringCodec.INSTANCE, TTL, keyBuf);
            return m.map(v -> new NumericResponse<>(command, v));
        });
    }

    /** PTTL：返回 key 剩余存活毫秒数。 */
    @Override
    public Flux<NumericResponse<KeyCommand, Long>> pTtl(Publisher<KeyCommand> commands) {
        return execute(commands, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");

            byte[] keyBuf = toByteArray(command.getKey());
            Mono<Long> m = read(keyBuf, StringCodec.INSTANCE, RedisCommands.PTTL, keyBuf);
            return m.map(v -> new NumericResponse<>(command, v));
        });
    }

    /** MOVE：将 key 迁移到指定数据库编号。 */
    /** MOVE：将 key 迁移到指定数据库。 */
    @Override
    public Flux<BooleanResponse<MoveCommand>> move(Publisher<MoveCommand> commands) {
        return execute(commands, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getDatabase(), "Database must not be null!");

            byte[] keyBuf = toByteArray(command.getKey());
            Mono<Boolean> m = write(keyBuf, StringCodec.INSTANCE, RedisCommands.MOVE, keyBuf, command.getDatabase());
            return m.map(v -> new BooleanResponse<>(command, v));
        });
    }
    
    private static final RedisStrictCommand<ValueEncoding> OBJECT_ENCODING = new RedisStrictCommand<ValueEncoding>("OBJECT", "ENCODING", obj -> ValueEncoding.of((String) obj));

    /** OBJECT ENCODING：返回 key 内部编码。 */
    @Override
    public Mono<ValueEncoding> encodingOf(ByteBuffer key) {
        Assert.notNull(key, "Key must not be null!");

        byte[] keyBuf = toByteArray(key);
        return read(keyBuf, StringCodec.INSTANCE, OBJECT_ENCODING, keyBuf);
    }

    private static final RedisStrictCommand<Long> OBJECT_IDLETIME = new RedisStrictCommand<Long>("OBJECT", "IDLETIME");
    
    /** OBJECT IDLETIME：返回 key 空闲秒数。 */
    @Override
    public Mono<Duration> idletime(ByteBuffer key) {
        Assert.notNull(key, "Key must not be null!");

        byte[] keyBuf = toByteArray(key);
        Mono<Long> m = read(keyBuf, StringCodec.INSTANCE, OBJECT_IDLETIME, keyBuf);
        return m.map(Duration::ofSeconds);
    }
    
    private static final RedisStrictCommand<Long> OBJECT_REFCOUNT = new RedisStrictCommand<Long>("OBJECT", "REFCOUNT");

    /** OBJECT REFCOUNT：返回 key 引用计数。 */
    @Override
    public Mono<Long> refcount(ByteBuffer key) {
        Assert.notNull(key, "Key must not be null!");

        byte[] keyBuf = toByteArray(key);
        return read(keyBuf, StringCodec.INSTANCE, OBJECT_REFCOUNT, keyBuf);
    }

    /** copy：Redis 命令实现。 */
    @Override
    public Flux<BooleanResponse<CopyCommand>> copy(Publisher<CopyCommand> commands) {
        return execute(commands, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getTarget(), "Target must not be null!");

            List<Object> params = new ArrayList<>();
            byte[] keyBuf = toByteArray(command.getKey());
            params.add(keyBuf);
            byte[] targetBuf = toByteArray(command.getTarget());
            params.add(targetBuf);
            if (command.getDatabase() != null) {
                params.add("DB");
                params.add(command.getDatabase());
            }

            Mono<Boolean> m = write(keyBuf, StringCodec.INSTANCE, RedisCommands.COPY, params.toArray());
            return m.map(v -> new BooleanResponse<>(command, v));
        });
    }

    /** EXISTS：判断 key 是否存在。 */
    @Override
    public Mono<Long> exists(List<ByteBuffer> keys) {
        Assert.notEmpty(keys, "Keys must not be empty!");

        List<Object> args = new ArrayList<>(keys.size());
        for (ByteBuffer key : keys) {
            args.add(toByteArray(key));
        }

        return read((byte[]) args.get(0), LongCodec.INSTANCE, RedisCommands.EXISTS_LONG, args.toArray());
    }

    /** applyExpiration：Redis 命令实现。 */
    @Override
    public Flux<BooleanResponse<ExpireCommand>> applyExpiration(Publisher<ExpireCommand> commands) {
        return execute(commands, command -> {
            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getExpiration(), "Expiration must not be null!");

            byte[] keyBuf = toByteArray(command.getKey());
            List<Object> args = new ArrayList<>();
            args.add(keyBuf);

            Mono<Boolean> result;

            if (command.getExpiration().isPersistent()) {
                result = write(keyBuf, StringCodec.INSTANCE, RedisCommands.PERSIST, keyBuf);
            } else {
                long timestamp = command.getExpiration().getExpirationTime();
                args.add(timestamp);

                if (command.getOptions() != null
                        && command.getOptions().getCondition() != ExpirationOptions.Condition.ALWAYS) {
                    args.add(command.getOptions().getCondition().name());
                }

                if (command.getExpiration().isUnixTimestamp()) {

                    if (command.getExpiration().getTimeUnit() == TimeUnit.MILLISECONDS) {
                        result = write(keyBuf, StringCodec.INSTANCE, RedisCommands.PEXPIREAT, args.toArray());
                    } else {
                        long seconds = TimeUnit.SECONDS.convert(timestamp, command.getExpiration().getTimeUnit());
                        args.set(1, seconds);
                        result = write(keyBuf, StringCodec.INSTANCE, EXPIREAT, args.toArray());
                    }
                } else {
                    if (command.getExpiration().getTimeUnit() == TimeUnit.MILLISECONDS) {
                        result = write(keyBuf, StringCodec.INSTANCE, RedisCommands.PEXPIRE, args.toArray());
                    } else {
                        long seconds = TimeUnit.SECONDS.convert(timestamp, command.getExpiration().getTimeUnit());
                        args.set(1, seconds);
                        result = write(keyBuf, StringCodec.INSTANCE, EXPIRE, args.toArray());
                    }
                }
            }

            return result.map(value -> new BooleanResponse<>(command, value));
        });
    }

}
