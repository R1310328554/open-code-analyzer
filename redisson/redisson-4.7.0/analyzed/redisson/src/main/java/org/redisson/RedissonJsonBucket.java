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
package org.redisson;

import org.redisson.api.FPHAType;
import org.redisson.api.JsonType;
import org.redisson.api.RFuture;
import org.redisson.api.RJsonBucket;
import org.redisson.api.bucket.*;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.client.protocol.convertor.JsonTypeConvertor;
import org.redisson.client.protocol.convertor.LongNumberConvertor;
import org.redisson.client.protocol.convertor.NumberConvertor;
import org.redisson.client.protocol.decoder.ListFirstObjectDecoder;
import org.redisson.client.protocol.decoder.ObjectListReplayDecoder;
import org.redisson.codec.JsonCodec;
import org.redisson.codec.JsonCodecWrapper;
import org.redisson.command.CommandAsyncExecutor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * JSON 字符串桶 {@link RJsonBucket} 实现。
 * <p>基于 Redis JSON 模块读写结构化文档，支持路径查询与部分更新。
 *
 * @author Nikita Koksharov
 */
public class RedissonJsonBucket<V> extends RedissonExpirable implements RJsonBucket<V> {

    public RedissonJsonBucket(JsonCodec codec, CommandAsyncExecutor connectionManager, String name) {
        super(new JsonCodecWrapper(codec), connectionManager, name);
    }


    /** 返回元素/条目数量。 */
    @Override
    public long size() {
        return get(sizeAsync());
    }

    /** 异步返回数量。 */
    @Override
    public RFuture<Long> sizeAsync() {
        throw new UnsupportedOperationException();
    }

    /** JSON Bucket stringSizeMulti 操作。 */
    @Override
    public List<Long> stringSizeMulti(String path) {
        return get(stringSizeMultiAsync(path));
    }

    /** 异步执行 stringSizeMulti。 */
    @Override
    public RFuture<List<Long>> stringSizeMultiAsync(String path) {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_STRLEN_LIST, getRawName(), path);
    }

    /** JSON Bucket stringSize 操作。 */
    @Override
    public Long stringSize(String path) {
        return get(stringSizeAsync(path));
    }

    /** 异步执行 stringSize。 */
    @Override
    public RFuture<Long> stringSizeAsync(String path) {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_STRLEN, getRawName(), path);
    }

    /** JSON 路径读取。 */
    @Override
    public V get() {
        return get(getAsync());
    }

    /** 异步 JSON 读取。 */
    @Override
    public RFuture<V> getAsync() {
        if (getServiceManager().isResp3()) {
            return commandExecutor.readAsync(getRawName(), codec, RedisCommands.JSON_GET, getRawName(), ".");
        }
        return commandExecutor.readAsync(getRawName(), codec, RedisCommands.JSON_GET, getRawName());
    }

    /** JSON 路径读取。 */
    @Override
    public <T> T get(JsonCodec codec, String... paths) {
        return get(getAsync(codec, paths));
    }

    /** 异步 JSON 读取。 */
    @Override
    public <T> RFuture<T> getAsync(JsonCodec codec, String... paths) {
        if (getServiceManager().isResp3()) {
            if (paths.length == 0) {
                paths = new String[]{"."};
            }
        }

        List<Object> args = new ArrayList<>();
        args.add(getRawName());
        args.addAll(Arrays.asList(paths));
        return commandExecutor.readAsync(getRawName(), new JsonCodecWrapper(codec), RedisCommands.JSON_GET, args.toArray());
    }

    /** 获取 AndDelete。 */
    @Override
    public V getAndDelete() {
        return get(getAndDeleteAsync());
    }

    /** 异步获取 AndDelete 或执行 AndDelete 操作。 */
    @Override
    public RFuture<V> getAndDeleteAsync() {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
                "local currValue = redis.call('json.get', KEYS[1]); "
                        + "redis.call('del', KEYS[1]); "
                        + "return currValue; ",
                Collections.singletonList(getRawName()));
    }

    /** 设置IfAbsent。 */
    @Override
    public boolean setIfAbsent(V value) {
        return get(setIfAbsentAsync(value));
    }

    /** 设置IfAbsent。 */
    @Override
    public boolean setIfAbsent(V value, Duration duration) {
        return get(setIfAbsentAsync(value, duration));
    }

    /** 设置IfAbsentAsync。 */
    @Override
    public RFuture<Boolean> setIfAbsentAsync(V value) {
        return setIfAbsentAsync("$", value);
    }

    /** 设置IfAbsentAsync。 */
    @Override
    public RFuture<Boolean> setIfAbsentAsync(V value, Duration duration) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
          "local currValue = redis.call('json.set', KEYS[1], '$', ARGV[1], 'NX'); " +
                "if currValue ~= false then " +
                    "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                    "return 1;" +
                "end;" +
                "return 0; ",
        Collections.singletonList(getRawName()), encode(value), duration.toMillis());
    }

    /** JSON Bucket trySet 操作。 */
    @Override
    public boolean trySet(V value) {
        return get(trySetAsync(value));
    }

    /** 异步执行 trySet。 */
    @Override
    public RFuture<Boolean> trySetAsync(V value) {
        return trySetAsync("$", value);
    }

    /** 设置IfAbsent。 */
    @Override
    public boolean setIfAbsent(String path, Object value) {
        return get(setIfAbsentAsync(path, value));
    }

    /** 设置IfAbsentAsync。 */
    @Override
    public RFuture<Boolean> setIfAbsentAsync(String path, Object value) {
        if (value == null) {
            return commandExecutor.readAsync(getRawName(), codec, RedisCommands.NOT_EXISTS, getRawName());
        }

        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.JSON_SET_BOOLEAN, getRawName(), path, encode(value), "NX");
    }

    /** 设置IfAbsent。 */
    @Override
    public boolean setIfAbsent(String path, Object value, FPHAType fphaType) {
        return get(setIfAbsentAsync(path, value, fphaType));
    }

    /** 设置IfAbsentAsync。 */
    @Override
    public RFuture<Boolean> setIfAbsentAsync(String path, Object value, FPHAType fphaType) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.JSON_SET_BOOLEAN, getRawName(), path, encode(value), "NX", "FPHA", fphaType.name());
    }

    /** JSON Bucket trySet 操作。 */
    @Override
    public boolean trySet(String path, Object value) {
        return get(trySetAsync(path, value));
    }

    /** 异步执行 trySet。 */
    @Override
    public RFuture<Boolean> trySetAsync(String path, Object value) {
        if (value == null) {
            return commandExecutor.readAsync(getRawName(), codec, RedisCommands.NOT_EXISTS, getRawName());
        }

        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.JSON_SET_BOOLEAN, getRawName(), path, encode(value), "NX");
    }

    /** JSON Bucket trySet 操作。 */
    @Override
    public boolean trySet(V value, long timeToLive, TimeUnit timeUnit) {
        return get(trySetAsync(value, timeToLive, timeUnit));
    }

    /** 异步执行 trySet。 */
    @Override
    public RFuture<Boolean> trySetAsync(V value, long timeToLive, TimeUnit timeUnit) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                  "local currValue = redis.call('json.set', KEYS[1], '$', ARGV[1], 'NX'); " +
                        "if currValue ~= false then " +
                            "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                            "return 1;" +
                        "end;" +
                        "return 0; ",
                Collections.singletonList(getRawName()), encode(value), timeUnit.toMillis(timeToLive));
    }

    /** 设置IfExists。 */
    @Override
    public boolean setIfExists(V value) {
        return get(setIfExistsAsync(value));
    }

    /** 设置IfExistsAsync。 */
    @Override
    public RFuture<Boolean> setIfExistsAsync(V value) {
        return setIfExistsAsync("$", value);
    }

    /** 设置IfExists。 */
    @Override
    public boolean setIfExists(String path, Object value) {
        return get(setIfExistsAsync(path, value));
    }

    /** 设置IfExistsAsync。 */
    @Override
    public RFuture<Boolean> setIfExistsAsync(String path, Object value) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.JSON_SET_BOOLEAN, getRawName(), path, encode(value), "XX");
    }

    /** 设置IfExists。 */
    @Override
    public boolean setIfExists(String path, Object value, FPHAType fphaType) {
        return get(setIfExistsAsync(path, value, fphaType));
    }

    /** 设置IfExistsAsync。 */
    @Override
    public RFuture<Boolean> setIfExistsAsync(String path, Object value, FPHAType fphaType) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.JSON_SET_BOOLEAN, getRawName(), path, encode(value), "XX", "FPHA", fphaType.name());
    }

    /** 设置IfExists。 */
    @Override
    public boolean setIfExists(V value, long timeToLive, TimeUnit timeUnit) {
        return get(setIfExistsAsync(value, timeToLive, timeUnit));
    }

    /** 设置IfExistsAsync。 */
    @Override
    public RFuture<Boolean> setIfExistsAsync(V value, long timeToLive, TimeUnit timeUnit) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "local currValue = redis.call('json.set', KEYS[1], '$', ARGV[1], 'XX'); " +
                      "if currValue ~= false then " +
                         "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                         "return 1;" +
                      "end;" +
                      "return 0; ",
                Collections.singletonList(getRawName()), encode(value), timeUnit.toMillis(timeToLive));
    }

    /** 设置IfExists。 */
    @Override
    public boolean setIfExists(V value, Duration duration) {
        return get(setIfExistsAsync(value, duration));
    }

    /** 设置IfExistsAsync。 */
    @Override
    public RFuture<Boolean> setIfExistsAsync(V value, Duration duration) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "local currValue = redis.call('json.set', KEYS[1], '$', ARGV[1], 'XX'); " +
                      "if currValue ~= false then " +
                         "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                         "return 1;" +
                      "end;" +
                      "return 0; ",
                Collections.singletonList(getRawName()), encode(value), duration.toMillis());
    }

    /** JSON Bucket compareAndSet 操作。 */
    @Override
    public boolean compareAndSet(V expect, V update) {
        return get(compareAndSetAsync(expect, update));
    }

    /** 异步执行 compareAndSet。 */
    @Override
    public RFuture<Boolean> compareAndSetAsync(V expect, V update) {
        if (expect == null && update == null) {
            return trySetAsync(null);
        }

        if (expect == null) {
            return trySetAsync(update);
        }

        return compareAndSetUpdateAsync("$", expect, update);
    }

    /** JSON Bucket compareAndSet 操作。 */
    @Override
    public boolean compareAndSet(String path, Object expect, Object update) {
        return get(compareAndSetAsync(path, expect, update));
    }

    /** 异步执行 compareAndSet。 */
    @Override
    public RFuture<Boolean> compareAndSetAsync(String path, Object expect, Object update) {
        if (path == null) {
            throw new NullPointerException("path can't be null");
        }

        if (expect == null && update == null) {
            return trySetAsync(path, null);
        }

        if (expect == null) {
            return trySetAsync(path, update);
        }

        if (path.startsWith("$")) {
            expect = Arrays.asList(expect);
        }

        return compareAndSetUpdateAsync(path, expect, update);
    }

    /** 异步执行 compareAndSetUpdate。 */
    protected RFuture<Boolean> compareAndSetUpdateAsync(String path, Object expect, Object update) {
        if (update == null) {
            return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                    "if redis.call('json.get', KEYS[1], ARGV[1]) == ARGV[2] then "
                            + "redis.call('json.del', KEYS[1], ARGV[1]); "
                            + "return 1 "
                        + "else "
                            + "return 0 end;",
                    Collections.singletonList(getRawName()), path, encode(expect));
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                "if redis.call('json.get', KEYS[1], ARGV[1]) == ARGV[2] then "
                        + "redis.call('json.set', KEYS[1], ARGV[1], ARGV[3]); "
                        + "return 1 "
                    + "else "
                        + "return 0 end",
                Collections.singletonList(getRawName()), path, encode(expect), encode(update));
    }

    /** 获取 AndSet。 */
    @Override
    public V getAndSet(V newValue) {
        return get(getAndSetAsync(newValue));
    }

    /** 异步获取 AndSet 或执行 AndSet 操作。 */
    @Override
    public RFuture<V> getAndSetAsync(V newValue) {
        if (newValue == null) {
            return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
                    "local v = redis.call('json.get', KEYS[1]); " +
                          "redis.call('json.del', KEYS[1]); " +
                          "return v",
                    Collections.singletonList(getRawName()));
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
                "local currValue = redis.call('json.get', KEYS[1]); " +
                      "redis.call('json.set', KEYS[1], '$', ARGV[1]); " +
                      "return currValue; ",
                Collections.singletonList(getRawName()), encode(newValue));
    }

    /** 获取 AndSet。 */
    @Override
    public <T> T getAndSet(JsonCodec codec, String path, Object newValue) {
        return get(getAndSetAsync(codec, path, newValue));
    }

    /** 异步获取 AndSet 或执行 AndSet 操作。 */
    @Override
    public <T> RFuture<T> getAndSetAsync(JsonCodec codec, String path, Object newValue) {
        if (newValue == null) {
            return commandExecutor.evalWriteAsync(getRawName(), new JsonCodecWrapper(codec), RedisCommands.EVAL_OBJECT,
                    "local v = redis.call('json.get', KEYS[1], ARGV[1]); " +
                            "redis.call('json.del', KEYS[1]); " +
                            "return v",
                    Collections.singletonList(getRawName()), path);
        }

        return commandExecutor.evalWriteAsync(getRawName(), new JsonCodecWrapper(codec), RedisCommands.EVAL_OBJECT,
                "local currValue = redis.call('json.get', KEYS[1], ARGV[1]); " +
                        "redis.call('json.set', KEYS[1], ARGV[1], ARGV[2]); " +
                        "return currValue; ",
                Collections.singletonList(getRawName()), path, encode(newValue));
    }

    /** 获取 AndSet。 */
    @Override
    public V getAndSet(V value, long timeToLive, TimeUnit timeUnit) {
        return get(getAndSetAsync(value, timeToLive, timeUnit));
    }

    /** 异步获取 AndSet 或执行 AndSet 操作。 */
    @Override
    public RFuture<V> getAndSetAsync(V value, long timeToLive, TimeUnit timeUnit) {
        if (value == null) {
            return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
                    "local v = redis.call('json.get', KEYS[1]); " +
                            "redis.call('json.del', KEYS[1]); " +
                            "return v",
                    Collections.singletonList(getRawName()));
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
                "local currValue = redis.call('json.get', KEYS[1]); " +
                        "redis.call('json.set', KEYS[1], '$', ARGV[1]); " +
                        "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                        "return currValue; ",
                Collections.singletonList(getRawName()), encode(value), timeUnit.toMillis(timeToLive));
    }

    /** 获取 AndSet。 */
    @Override
    public V getAndSet(V value, Duration duration) {
        return get(getAndSetAsync(value, duration));
    }

    /** 异步获取 AndSet 或执行 AndSet 操作。 */
    @Override
    public RFuture<V> getAndSetAsync(V value, Duration duration) {
        if (value == null) {
            return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
                    "local v = redis.call('json.get', KEYS[1]); " +
                            "redis.call('json.del', KEYS[1]); " +
                            "return v",
                    Collections.singletonList(getRawName()));
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
                "local currValue = redis.call('json.get', KEYS[1]); " +
                        "redis.call('json.set', KEYS[1], '$', ARGV[1]); " +
                        "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                        "return currValue; ",
                Collections.singletonList(getRawName()), encode(value), duration.toMillis());
    }

    /** 获取 AndExpire。 */
    @Override
    public V getAndExpire(Duration duration) {
        return get(getAndExpireAsync(duration));
    }

    /** 异步获取 AndExpire 或执行 AndExpire 操作。 */
    @Override
    public RFuture<V> getAndExpireAsync(Duration duration) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
                   "local currValue = redis.call('json.get', KEYS[1]); " +
                        "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                        "return currValue; ",
                Collections.singletonList(getRawName()), duration.toMillis());
    }

    /** 获取 AndExpire。 */
    @Override
    public V getAndExpire(Instant time) {
        return get(getAndExpireAsync(time));
    }

    /** 异步获取 AndExpire 或执行 AndExpire 操作。 */
    @Override
    public RFuture<V> getAndExpireAsync(Instant time) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
                "local currValue = redis.call('json.get', KEYS[1]); " +
                        "redis.call('pexpireat', KEYS[1], ARGV[1]); " +
                        "return currValue; ",
                Collections.singletonList(getRawName()), time.toEpochMilli());
    }

    /** 获取 AndClearExpire。 */
    @Override
    public V getAndClearExpire() {
        return get(getAndClearExpireAsync());
    }

    /** 异步获取 AndClearExpire 或执行 AndClearExpire 操作。 */
    @Override
    public RFuture<V> getAndClearExpireAsync() {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
                        "local currValue = redis.call('json.get', KEYS[1]); " +
                        "redis.call('persist', KEYS[1]); " +
                        "return currValue; ",
                Collections.singletonList(getRawName()));
    }

    /** JSON 路径写入。 */
    @Override
    public void set(V value) {
        get(setAsync(value));
    }

    /** 异步 JSON 写入。 */
    @Override
    public RFuture<Void> setAsync(V value) {
        return setAsync("$", value);
    }

    /** JSON 路径写入。 */
    @Override
    public void set(String path, Object value) {
        get(setAsync(path, value));
    }

    /** 异步 JSON 写入。 */
    @Override
    public RFuture<Void> setAsync(String path, Object value) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.JSON_SET, getRawName(), path, encode(value));
    }

    /** JSON 路径写入。 */
    @Override
    public void set(String path, Object value, FPHAType fphaType) {
        get(setAsync(path, value, fphaType));
    }

    /** 异步 JSON 写入。 */
    @Override
    public RFuture<Void> setAsync(String path, Object value, FPHAType fphaType) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.JSON_SET, getRawName(), path, encode(value), "FPHA", fphaType.name());
    }

    /** JSON 路径写入。 */
    @Override
    public void set(V value, long timeToLive, TimeUnit timeUnit) {
        get(setAsync(value, timeToLive, timeUnit));
    }

    /** 异步 JSON 写入。 */
    @Override
    public RFuture<Void> setAsync(V value, long timeToLive, TimeUnit timeUnit) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_VOID,
                        "redis.call('json.set', KEYS[1], '$', ARGV[1]); " +
                              "redis.call('pexpire', KEYS[1], ARGV[2]); ",
                Collections.singletonList(getRawName()), encode(value), timeUnit.toMillis(timeToLive));
    }

    /** JSON 路径写入。 */
    @Override
    public void set(V value, Duration duration) {
        get(setAsync(value, duration));
    }

    /** 异步 JSON 写入。 */
    @Override
    public RFuture<Void> setAsync(V value, Duration duration) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_VOID,
                        "redis.call('json.set', KEYS[1], '$', ARGV[1]); " +
                              "redis.call('pexpire', KEYS[1], ARGV[2]); ",
                Collections.singletonList(getRawName()), encode(value), duration.toMillis());
    }

    /** 设置AndKeepTTL。 */
    @Override
    public void setAndKeepTTL(V value) {
        get(setAndKeepTTLAsync(value));
    }

    /** 设置AndKeepTTLAsync。 */
    @Override
    public RFuture<Void> setAndKeepTTLAsync(V value) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_VOID,
                "local ttl = redis.call('pttl', KEYS[1]);" +
                      "redis.call('json.set', KEYS[1], '$', ARGV[1]); " +
                      "if ttl > 0 then " +
                        "redis.call('pexpire', KEYS[1], ttl); " +
                      "end;",
                Collections.singletonList(getRawName()), encode(value));
    }

    /** 异步 JSON 删除。 */
    @Override
    public RFuture<Boolean> deleteAsync() {
        return commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.JSON_DEL_BOOLEAN, getRawName());
    }

    /** JSON Bucket stringAppend 操作。 */
    @Override
    public long stringAppend(String path, Object value) {
        return get(stringAppendAsync(path, value));
    }

    /** 异步执行 stringAppend。 */
    @Override
    public RFuture<Long> stringAppendAsync(String path, Object value) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_STRAPPEND, getRawName(), path, encode(value));
    }

    /** JSON Bucket stringAppendMulti 操作。 */
    @Override
    public List<Long> stringAppendMulti(String path, Object value) {
        return get(stringAppendMultiAsync(path, value));
    }

    /** 异步执行 stringAppendMulti。 */
    @Override
    public RFuture<List<Long>> stringAppendMultiAsync(String path, Object value) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_STRAPPEND_LIST, getRawName(), path, encode(value));
    }

    /** JSON 数组追加元素。 */
    @Override
    public long arrayAppend(String path, Object... values) {
        return get(arrayAppendAsync(path, values));
    }

    /** 异步执行 arrayAppend。 */
    @Override
    public RFuture<Long> arrayAppendAsync(String path, Object... values) {
        List<Object> args = new ArrayList<>(values.length + 2);
        args.add(getRawName());
        args.add(path);
        encode(args, Arrays.asList(values));
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_ARRAPPEND, args.toArray());
    }

    /** JSON Bucket arrayAppendMulti 操作。 */
    @Override
    public List<Long> arrayAppendMulti(String path, Object... values) {
        return get(arrayAppendMultiAsync(path, values));
    }

    /** 异步执行 arrayAppendMulti。 */
    @Override
    public RFuture<List<Long>> arrayAppendMultiAsync(String path, Object... values) {
        List<Object> args = new ArrayList<>(values.length + 2);
        args.add(getRawName());
        args.add(path);
        encode(args, Arrays.asList(values));
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_ARRAPPEND_LIST, args.toArray());
    }

    /** JSON Bucket arrayIndex 操作。 */
    @Override
    public long arrayIndex(String path, Object value) {
        return get(arrayIndexAsync(path, value));
    }

    /** 异步执行 arrayIndex。 */
    @Override
    public RFuture<Long> arrayIndexAsync(String path, Object value) {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_ARRINDEX, getRawName(), path, encode(value));
    }

    /** JSON Bucket arrayIndexMulti 操作。 */
    @Override
    public List<Long> arrayIndexMulti(String path, Object value) {
        return get(arrayIndexMultiAsync(path, value));
    }

    /** 异步执行 arrayIndexMulti。 */
    @Override
    public RFuture<List<Long>> arrayIndexMultiAsync(String path, Object value) {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_ARRINDEX_LIST, getRawName(), path, encode(value));
    }

    /** JSON Bucket arrayIndex 操作。 */
    @Override
    public long arrayIndex(String path, Object value, long start, long end) {
        return get(arrayIndexAsync(path, value, start, end));
    }

    /** 异步执行 arrayIndex。 */
    @Override
    public RFuture<Long> arrayIndexAsync(String path, Object value, long start, long end) {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_ARRINDEX, getRawName(), path, encode(value), start, end);
    }

    /** JSON Bucket arrayIndexMulti 操作。 */
    @Override
    public List<Long> arrayIndexMulti(String path, Object value, long start, long end) {
        return get(arrayIndexMultiAsync(path, value, start, end));
    }

    /** 异步执行 arrayIndexMulti。 */
    @Override
    public RFuture<List<Long>> arrayIndexMultiAsync(String path, Object value, long start, long end) {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_ARRINDEX_LIST, getRawName(), path, encode(value), start, end);
    }

    /** JSON 数组插入元素。 */
    @Override
    public long arrayInsert(String path, long index, Object... values) {
        return get(arrayInsertAsync(path, index, values));
    }

    /** 异步执行 arrayInsert。 */
    @Override
    public RFuture<Long> arrayInsertAsync(String path, long index, Object... values) {
        List<Object> args = new ArrayList<>(values.length + 3);
        args.add(getRawName());
        args.add(path);
        args.add(index);
        encode(args, Arrays.asList(values));
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_ARRINSERT, args.toArray());
    }

    /** JSON Bucket arrayInsertMulti 操作。 */
    @Override
    public List<Long> arrayInsertMulti(String path, long index, Object... values) {
        return get(arrayInsertMultiAsync(path, index, values));
    }

    /** 异步执行 arrayInsertMulti。 */
    @Override
    public RFuture<List<Long>> arrayInsertMultiAsync(String path, long index, Object... values) {
        List<Object> args = new ArrayList<>(values.length + 3);
        args.add(getRawName());
        args.add(path);
        args.add(index);
        encode(args, Arrays.asList(values));
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_ARRINSERT_LIST, args.toArray());
    }

    /** JSON Bucket arraySize 操作。 */
    @Override
    public long arraySize(String path) {
        return get(arraySizeAsync(path));
    }

    /** 异步执行 arraySize。 */
    @Override
    public RFuture<Long> arraySizeAsync(String path) {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_ARRLEN, getRawName(), path);
    }

    /** JSON Bucket arraySizeMulti 操作。 */
    @Override
    public List<Long> arraySizeMulti(String path) {
        return get(arraySizeMultiAsync(path));
    }

    /** 异步执行 arraySizeMulti。 */
    @Override
    public RFuture<List<Long>> arraySizeMultiAsync(String path) {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_ARRLEN_LIST, getRawName(), path);
    }

    /** JSON Bucket arrayPollLast 操作。 */
    @Override
    public <T> T arrayPollLast(JsonCodec codec, String path) {
        return get(arrayPollLastAsync(codec, path));
    }

    /** 异步执行 arrayPollLast。 */
    @Override
    public <T> RFuture<T> arrayPollLastAsync(JsonCodec codec, String path) {
        return commandExecutor.writeAsync(getRawName(), new JsonCodecWrapper(codec), RedisCommands.JSON_ARRPOP, getRawName(), path);
    }

    /** JSON Bucket arrayPollLastMulti 操作。 */
    @Override
    public <T> List<T> arrayPollLastMulti(JsonCodec codec, String path) {
        return get(arrayPollLastMultiAsync(codec, path));
    }

    /** 异步执行 arrayPollLastMulti。 */
    @Override
    public <T> RFuture<List<T>> arrayPollLastMultiAsync(JsonCodec codec, String path) {
        return commandExecutor.writeAsync(getRawName(), new JsonCodecWrapper(codec), RedisCommands.JSON_ARRPOP_LIST, getRawName(), path);
    }

    /** JSON Bucket arrayPollFirst 操作。 */
    @Override
    public <T> T arrayPollFirst(JsonCodec codec, String path) {
        return get(arrayPollFirstAsync(codec, path));
    }

    /** 异步执行 arrayPollFirst。 */
    @Override
    public <T> RFuture<T> arrayPollFirstAsync(JsonCodec codec, String path) {
        return commandExecutor.writeAsync(getRawName(), new JsonCodecWrapper(codec), RedisCommands.JSON_ARRPOP, getRawName(), path, 0);
    }

    /** JSON Bucket arrayPollFirstMulti 操作。 */
    @Override
    public <T> List<T> arrayPollFirstMulti(JsonCodec codec, String path) {
        return get(arrayPollFirstMultiAsync(codec, path));
    }

    /** 异步执行 arrayPollFirstMulti。 */
    @Override
    public <T> RFuture<List<T>> arrayPollFirstMultiAsync(JsonCodec codec, String path) {
        return commandExecutor.writeAsync(getRawName(), new JsonCodecWrapper(codec), RedisCommands.JSON_ARRPOP_LIST, getRawName(), path, 0);
    }

    /** JSON Bucket arrayPop 操作。 */
    @Override
    public <T> T arrayPop(JsonCodec codec, String path, long index) {
        return get(arrayPopAsync(codec, path, index));
    }

    /** 异步执行 arrayPop。 */
    @Override
    public <T> RFuture<T> arrayPopAsync(JsonCodec codec, String path, long index) {
        return commandExecutor.writeAsync(getRawName(), new JsonCodecWrapper(codec), RedisCommands.JSON_ARRPOP, getRawName(), path, index);
    }

    /** JSON Bucket arrayPopMulti 操作。 */
    @Override
    public <T> List<T> arrayPopMulti(JsonCodec codec, String path, long index) {
        return get(arrayPopMultiAsync(codec, path, index));
    }

    /** 异步执行 arrayPopMulti。 */
    @Override
    public <T> RFuture<List<T>> arrayPopMultiAsync(JsonCodec codec, String path, long index) {
        return commandExecutor.writeAsync(getRawName(), new JsonCodecWrapper(codec), RedisCommands.JSON_ARRPOP_LIST, getRawName(), path, index);
    }

    /** JSON Bucket arrayTrim 操作。 */
    @Override
    public long arrayTrim(String path, long start, long end) {
        return get(arrayTrimAsync(path, start, end));
    }

    /** 异步执行 arrayTrim。 */
    @Override
    public RFuture<Long> arrayTrimAsync(String path, long start, long end) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_ARRTRIM, getRawName(), path, start, end);
    }

    /** JSON Bucket arrayTrimMulti 操作。 */
    @Override
    public List<Long> arrayTrimMulti(String path, long start, long end) {
        return get(arrayTrimMultiAsync(path, start, end));
    }

    /** 异步执行 arrayTrimMulti。 */
    @Override
    public RFuture<List<Long>> arrayTrimMultiAsync(String path, long start, long end) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_ARRTRIM_LIST, getRawName(), path, start, end);
    }

    /** 清空全部条目。 */
    @Override
    public long clear() {
        return get(clearAsync());
    }

    /** 异步清空。 */
    @Override
    public RFuture<Long> clearAsync() {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_CLEAR, getRawName());
    }

    /** 清空全部条目。 */
    @Override
    public long clear(String path) {
        return get(clearAsync(path));
    }

    /** 异步清空。 */
    @Override
    public RFuture<Long> clearAsync(String path) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_CLEAR, getRawName(), path);
    }

    /** JSON Bucket incrementAndGet 操作。 */
    @Override
    public <T extends Number> T incrementAndGet(String path, T delta) {
        return get(incrementAndGetAsync(path, delta));
    }

    /** 异步执行 incrementAndGet。 */
    @Override
    public <T extends Number> RFuture<T> incrementAndGetAsync(String path, T delta) {
        RedisCommand command;
        if (getServiceManager().isResp3()) {
            command = new RedisCommand<>("JSON.NUMINCRBY", new ListFirstObjectDecoder(), new LongNumberConvertor(delta.getClass()));
        } else {
            command = new RedisCommand<>("JSON.NUMINCRBY", new NumberConvertor(delta.getClass()));
        }
        return commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE, command,
                                            getRawName(), path, new BigDecimal(delta.toString()).toPlainString());
    }

    /** JSON Bucket incrementAndGetMulti 操作。 */
    @Override
    public <T extends Number> List<T> incrementAndGetMulti(String path, T delta) {
        return get(incrementAndGetMultiAsync(path, delta));
    }

    /** 异步执行 incrementAndGetMulti。 */
    @Override
    public <T extends Number> RFuture<List<T>> incrementAndGetMultiAsync(String path, T delta) {
        return commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE, new RedisCommand("JSON.NUMINCRBY",
                                                                        new ObjectListReplayDecoder(), new NumberConvertor(delta.getClass())),
                                            getRawName(), path, new BigDecimal(delta.toString()).toPlainString());
    }

    /** JSON Bucket countKeys 操作。 */
    @Override
    public long countKeys() {
        return get(countKeysAsync());
    }

    /** 异步执行 countKeys。 */
    @Override
    public RFuture<Long> countKeysAsync() {
        RedisStrictCommand command = RedisCommands.JSON_OBJLEN;
        if (getServiceManager().isResp3()) {
            command = new RedisStrictCommand("JSON.OBJLEN", new ListFirstObjectDecoder());
        }

        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, command, getRawName());
    }

    /** JSON Bucket countKeys 操作。 */
    @Override
    public long countKeys(String path) {
        return get(countKeysAsync(path));
    }

    /** 异步执行 countKeys。 */
    @Override
    public RFuture<Long> countKeysAsync(String path) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_OBJLEN, getRawName(), path);
    }

    /** JSON Bucket countKeysMulti 操作。 */
    @Override
    public List<Long> countKeysMulti(String path) {
        return get(countKeysMultiAsync(path));
    }

    /** 异步执行 countKeysMulti。 */
    @Override
    public RFuture<List<Long>> countKeysMultiAsync(String path) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_OBJLEN_LIST, getRawName(), path);
    }

    /** 返回全部键（慎用）。 */
    @Override
    public List<String> getKeys() {
        return get(getKeysAsync());
    }

    /** 异步获取 Keys 或执行 Keys 操作。 */
    @Override
    public RFuture<List<String>> getKeysAsync() {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_OBJKEYS, getRawName());
    }

    /** 返回全部键（慎用）。 */
    @Override
    public List<String> getKeys(String path) {
        return get(getKeysAsync(path));
    }

    /** 异步获取 Keys 或执行 Keys 操作。 */
    @Override
    public RFuture<List<String>> getKeysAsync(String path) {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_OBJKEYS, getRawName(), path);
    }

    /** 获取 KeysMulti。 */
    @Override
    public List<List<String>> getKeysMulti(String path) {
        return get(getKeysMultiAsync(path));
    }

    /** 异步获取 KeysMulti 或执行 KeysMulti 操作。 */
    @Override
    public RFuture<List<List<String>>> getKeysMultiAsync(String path) {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_OBJKEYS_LIST, getRawName(), path);
    }

    /** JSON 布尔取反。 */
    @Override
    public boolean toggle(String path) {
        return get(toggleAsync(path));
    }

    /** 异步执行 toggle。 */
    @Override
    public RFuture<Boolean> toggleAsync(String path) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_TOGGLE, getRawName(), path);
    }

    /** JSON Bucket toggleMulti 操作。 */
    @Override
    public List<Boolean> toggleMulti(String path) {
        return get(toggleMultiAsync(path));
    }

    /** 异步执行 toggleMulti。 */
    @Override
    public RFuture<List<Boolean>> toggleMultiAsync(String path) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.JSON_TOGGLE_LIST, getRawName(), path);
    }

    /** 获取 Type。 */
    @Override
    public JsonType getType() {
        return get(getTypeAsync());
    }

    /** 异步获取 Type 或执行 Type 操作。 */
    @Override
    public RFuture<JsonType> getTypeAsync() {
        RedisCommand command = RedisCommands.JSON_TYPE;
        if (getServiceManager().isResp3()) {
            command = new RedisCommand("JSON.TYPE", new ListFirstObjectDecoder(), new JsonTypeConvertor());
        }

        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE, command, getRawName());
    }

    /** 获取 Type。 */
    @Override
    public JsonType getType(String path) {
        return get(getTypeAsync(path));
    }

    /** 异步获取 Type 或执行 Type 操作。 */
    @Override
    public RFuture<JsonType> getTypeAsync(String path) {
        RedisCommand command = RedisCommands.JSON_TYPE;
        if (getServiceManager().isResp3()) {
            command = new RedisCommand("JSON.TYPE", new ListFirstObjectDecoder(), new JsonTypeConvertor());
        }

        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE, command, getRawName(), path);
    }

    /** 删除 JSON 路径或键。 */
    @Override
    public long delete(String path) {
        return get(deleteAsync(path));
    }

    /** 异步 JSON 删除。 */
    @Override
    public RFuture<Long> deleteAsync(String path) {
        return commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.JSON_DEL_LONG, getRawName(), path);
    }

    /** 按 remapping 函数合并值。 */
    @Override
    public void merge(String path, Object value) {
        get(mergeAsync(path, value));
    }

    /** 异步执行 merge。 */
    @Override
    public RFuture<Void> mergeAsync(String path, Object value) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.JSON_MERGE, getRawName(), path, encode(value));
    }

    /** JSON Bucket findCommon 操作。 */
    @Override
    public V findCommon(String name) {
        return get(findCommonAsync(name));
    }

    /** 异步执行 findCommon。 */
    @Override
    public RFuture<V> findCommonAsync(String name) {
        throw new UnsupportedOperationException();
    }

    /** JSON Bucket findCommonLength 操作。 */
    @Override
    public long findCommonLength(String name) {
        return get(findCommonLengthAsync(name));
    }

    /** 异步执行 findCommonLength。 */
    @Override
    public RFuture<Long> findCommonLengthAsync(String name) {
        throw new UnsupportedOperationException();
    }

    /** 获取 Digest。 */
    @Override
    public String getDigest() {
        throw new UnsupportedOperationException();
    }

    /** 异步获取 Digest 或执行 Digest 操作。 */
    @Override
    public RFuture<String> getDigestAsync() {
        throw new UnsupportedOperationException();
    }

    /** JSON Bucket compareAndSet 操作。 */
    @Override
    public boolean compareAndSet(CompareAndSetArgs<V> args) {
        return get(compareAndSetAsync(args));
    }

    /** 异步执行 compareAndSet。 */
    @Override
    public RFuture<Boolean> compareAndSetAsync(CompareAndSetArgs<V> args) {
        CompareAndSetParams<V> params = (CompareAndSetParams<V>) args;

        Objects.requireNonNull(params.getNewValue(), "New value is null");

        ConditionType conditionType = params.getConditionType();

        switch (conditionType) {
            case EXPECTED:
                return compareAndSetAsync(params, params.getExpectedValue(), "E");
            case UNEXPECTED:
                return compareAndSetAsync(params, params.getUnexpectedValue(), "U");
            case EXPECTED_DIGEST:
                throw new IllegalStateException("Digest is unsupported for JSON type");
            case UNEXPECTED_DIGEST:
                throw new IllegalStateException("Digest is unsupported for JSON type");
            default:
                throw new IllegalStateException("Unknown condition type: " + conditionType);
        }
    }

    /** 异步执行 compareAndSet。 */
    private RFuture<Boolean> compareAndSetAsync(CompareAndSetParams<V> args, V value, String cond) {
        List<Object> params = new ArrayList<>();
        if (value == null) {
            cond += "N";
        }
        params.add(cond);
        params.add(encode(value));
        params.add(encode(args.getNewValue()));
        if (args.getTimeToLive() != null) {
            params.add("pexpire");
            params.add(args.getTimeToLive().toMillis());
        } else if (args.getExpireAt() != null) {
            params.add("pexpireat");
            params.add(args.getExpireAt().toEpochMilli());
        } else {
            params.add("");
        }

        return commandExecutor.evalWriteAsync(getName(), codec, RedisCommands.EVAL_BOOLEAN,
                  "local cv = redis.call('get', KEYS[1]) " +
                        "if (ARGV[1] == 'E' and cv == ARGV[2]) " +
                            "or (ARGV[1] == 'EN' and cv == false) " +
                            "or (ARGV[1] == 'UN' and cv ~= false) " +
                            "or (ARGV[1] == 'U' and cv ~= ARGV[2]) then " +

                            "redis.call('set', KEYS[1], ARGV[3]) " +

                            "if #ARGV[4] > 0 then " +
                                "redis.call(ARGV[4], KEYS[1], ARGV[5]) " +
                            "end " +
                            "return 1 " +
                        "end " +
                        "return 0 ",
                Collections.singletonList(getName()),
                params.toArray());
    }

    /** JSON Bucket compareAndDelete 操作。 */
    @Override
    public boolean compareAndDelete(CompareAndDeleteArgs<V> args) {
        return get(compareAndDeleteAsync(args));
    }

    /** 异步执行 compareAndDelete。 */
    @Override
    public RFuture<Boolean> compareAndDeleteAsync(CompareAndDeleteArgs<V> args) {
        CompareAndDeleteParams<V> params = (CompareAndDeleteParams<V>) args;

        ConditionType conditionType = params.getConditionType();

        switch (conditionType) {
            case EXPECTED:
                return compareAndDeleteExpectedAsync(params.getValue());
            case UNEXPECTED:
                return compareAndDeleteUnexpectedAsync(params.getValue());
            case EXPECTED_DIGEST:
                throw new IllegalStateException("Digest is unsupported for JSON type");
            case UNEXPECTED_DIGEST:
                throw new IllegalStateException("Digest is unsupported for JSON type");
            default:
                throw new IllegalArgumentException("Unknown mode: " + params.getConditionType());
        }
    }

    /** 异步执行 compareAndDeleteExpected。 */
    private RFuture<Boolean> compareAndDeleteExpectedAsync(V expected) {
        String script =
                "local currValue = redis.call('get', KEYS[1]) " +
                        "if currValue == ARGV[1] then " +
                        "redis.call('del', KEYS[1]) " +
                        "return 1 " +
                        "end " +
                        "return 0 ";
        return commandExecutor.evalWriteAsync(getName(), codec, RedisCommands.EVAL_BOOLEAN,
                script,
                Collections.singletonList(getName()), encode(expected));
    }

    /** 异步执行 compareAndDeleteUnexpected。 */
    private RFuture<Boolean> compareAndDeleteUnexpectedAsync(V unexpected) {
        String script =
                "local currValue = redis.call('get', KEYS[1]) " +
                        "if currValue ~= false and currValue ~= ARGV[1] then " +
                        "redis.call('del', KEYS[1]) " +
                        "return 1 " +
                        "end " +
                        "return 0 ";
        return commandExecutor.evalWriteAsync(getName(), codec, RedisCommands.EVAL_BOOLEAN,
                script,
                Collections.singletonList(getName()), encode(unexpected));
    }

}
