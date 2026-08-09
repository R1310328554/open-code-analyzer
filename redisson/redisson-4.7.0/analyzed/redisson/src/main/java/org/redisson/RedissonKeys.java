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

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletionStage;
import org.redisson.api.*;
import org.redisson.api.listener.FlushListener;
import org.redisson.api.listener.NewObjectListener;
import org.redisson.api.listener.SetObjectListener;
import org.redisson.api.options.KeysScanOptions;
import org.redisson.api.options.KeysScanParams;
import org.redisson.api.keys.MigrateArgs;
import org.redisson.api.keys.MigrateParams;
import org.redisson.client.RedisClient;
import org.redisson.client.RedisException;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.client.protocol.decoder.ListMultiDecoder2;
import org.redisson.client.protocol.decoder.ListScanResult;
import org.redisson.client.protocol.decoder.ListScanResultReplayDecoder;
import org.redisson.client.protocol.decoder.ObjectListReplayDecoder;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.command.CommandBatchService;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.iterator.BaseAsyncIterator;
import org.redisson.iterator.RedissonBaseIterator;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.misc.CompositeAsyncIterator;
import org.redisson.misc.CompositeIterable;
import org.redisson.pubsub.PublishSubscribeService;
import org.redisson.reactive.CommandReactiveBatchService;
import org.redisson.rx.CommandRxBatchService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Redis 键空间管理 {@link RKeys} 实现。
 * <p>封装 KEYS/SCAN、UNLINK、DUMP/RESTORE、MIGRATE 及跨库 MOVE 等键级操作。
 *
 * @author Nikita Koksharov
 */
public final class RedissonKeys implements RKeys {

    private final CommandAsyncExecutor commandExecutor;

    public RedissonKeys(CommandAsyncExecutor commandExecutor) {
        super();
        this.commandExecutor = commandExecutor;
    }

    /** 键空间 CommandExecutor 操作。 */
    public CommandAsyncExecutor getCommandExecutor() {
        return commandExecutor;
    }

    /** 键空间 ConnectionManager 操作。 */
    public ConnectionManager getConnectionManager() {
        return commandExecutor.getConnectionManager();
    }

    /** 键空间 Type 操作。 */
    @Override
    public RType getType(String key) {
        return commandExecutor.get(getTypeAsync(key));
    }

    /** 异步获取 Type 或执行 Type 操作。 */
    @Override
    public RFuture<RType> getTypeAsync(String key) {
        return commandExecutor.readAsync(map(key), RedisCommands.TYPE, map(key));
    }

    /** 键空间 Slot 操作。 */
    @Override
    public int getSlot(String key) {
        return commandExecutor.get(getSlotAsync(key));
    }

    /** 异步获取 Slot 或执行 Slot 操作。 */
    @Override
    public RFuture<Integer> getSlotAsync(String key) {
        return commandExecutor.readAsync(null, RedisCommands.KEYSLOT, map(key));
    }

    /** 按模式匹配返回键集合。 */
    @Override
    public Iterable<String> getKeysByPattern(String pattern) {
        return getKeysByPattern(pattern, 10);
    }

    private final RedisCommand<ListScanResult<String>> scan = new RedisCommand<ListScanResult<String>>("SCAN", new ListMultiDecoder2(
            new ListScanResultReplayDecoder() {
                @Override
                public ListScanResult<Object> decode(List<Object> parts, State state) {
                    return new ListScanResult<>((String) parts.get(0), (List<Object>) (Object) unmap((List<String>) parts.get(1)));
                }
            }, new ObjectListReplayDecoder<String>()));

    private final RedisCommand<ListScanResult<Object>> binaryScan = new RedisCommand<ListScanResult<Object>>("SCAN", new ListMultiDecoder2(
            new ListScanResultReplayDecoder() {
                @Override
                public ListScanResult<Object> decode(List<Object> parts, State state) {
                    return new ListScanResult<>((String) parts.get(0), (List<Object>) parts.get(1));
                }
            }, new ObjectListReplayDecoder<String>()));

    /** 按模式匹配返回键集合。 */
    @Override
    public Iterable<String> getKeysByPattern(String pattern, int count) {
        return getKeys(KeysScanOptions.defaults().pattern(pattern).chunkSize(count));
    }

    /** 按模式匹配返回键集合。 */
    public <T> Iterable<T> getKeysByPattern(RedisCommand<?> command, String pattern, int limit, int count, RType type) {
        List<Iterable<T>> iterables = new ArrayList<>();
        for (MasterSlaveEntry entry : commandExecutor.getConnectionManager().getEntrySet()) {
            Iterable<T> iterable = () -> createKeysIterator(StringCodec.INSTANCE, entry, command, pattern, count, type);
            iterables.add(iterable);
        }
        return new CompositeIterable<T>(iterables, limit);
    }

    /** 键空间 KeysWithLimit 操作。 */
    @Override
    public Iterable<String> getKeysWithLimit(int limit) {
        return getKeysWithLimit(null, limit);
    }

    /** 键空间 KeysWithLimit 操作。 */
    @Override
    public Iterable<String> getKeysWithLimit(String pattern, int limit) {
        return getKeys(KeysScanOptions.defaults().pattern(pattern).limit(limit));
    }

    /** 返回全部键（慎用）。 */
    @Override
    public Iterable<String> getKeys() {
        return getKeys(KeysScanOptions.defaults());
    }

    /** 异步获取 Keys 或执行 Keys 操作。 */
    @Override
    public AsyncIterator<String> getKeysAsync() {
        return getKeysAsync(KeysScanOptions.defaults());
    }

    /** 返回全部键（慎用）。 */
    @Override
    public Iterable<String> getKeys(KeysScanOptions options) {
        KeysScanParams params = (KeysScanParams) options;
        return getKeysByPattern(scan, params.getPattern(), params.getLimit(), params.getChunkSize(), params.getType());
    }

    /** 异步获取 Keys 或执行 Keys 操作。 */
    @Override
    public AsyncIterator<String> getKeysAsync(KeysScanOptions options) {
        KeysScanParams params = (KeysScanParams) options;
        List<AsyncIterator<String>> asyncIterators = new ArrayList<>();
        for (MasterSlaveEntry entry : commandExecutor.getConnectionManager().getEntrySet()) {
            AsyncIterator<String> asyncIterator = new BaseAsyncIterator<String, Object>() {
                @Override
                protected RFuture<ScanResult<Object>> iterator(RedisClient client, String nextItPos) {
                    return scanIteratorAsync(StringCodec.INSTANCE, client, entry, scan, nextItPos, params.getPattern(), params.getChunkSize(), params.getType());
                }
            };
            asyncIterators.add(asyncIterator);

        }
        return new CompositeAsyncIterator<>(asyncIterators, params.getLimit());
    }

    /** 返回全部键（慎用）。 */
    @Override
    public Iterable<String> getKeys(int count) {
        return getKeysByPattern(null, count);
    }

    /** 异步 SCAN 迭代器。 */
    private RFuture<ScanResult<Object>> scanIteratorAsync(Codec codec, RedisClient client, MasterSlaveEntry entry, RedisCommand<?> command,
                                                          String startPos, String pattern, int count, RType type) {
        List<Object> args = new ArrayList<>();
        args.add(startPos);
        if (pattern != null) {
            pattern = map(pattern);
            args.add("MATCH");
            args.add(pattern);
        }
        if (count > 0) {
            args.add("COUNT");
            args.add(count);
        }
        if (type != null) {
            args.add("TYPE");
            args.add(type.getValue());
        }

        return commandExecutor.readAsync(client, entry, codec, command, args.toArray());
    }

    /** 异步 SCAN 迭代器。 */
    public RFuture<ScanResult<Object>> scanIteratorAsync(RedisClient client, MasterSlaveEntry entry,
                                                         String startPos, String pattern, int count, RType type) {
        return scanIteratorAsync(StringCodec.INSTANCE, client, entry, scan, startPos, pattern, count, type);
    }

    /** 键管理 createKeysIterator 操作。 */
    private <T> Iterator<T> createKeysIterator(Codec codec, MasterSlaveEntry entry, RedisCommand<?> command,
                                               String pattern, int count, RType type) {
        return new RedissonBaseIterator<T>() {

            @Override
            protected ScanResult<Object> iterator(RedisClient client, String nextIterPos) {
                return commandExecutor
                        .get(scanIteratorAsync(codec, client, entry, command, nextIterPos, pattern, count, type));
            }

            @Override
            protected void remove(Object value) {
                RedissonKeys.this.delete((String) value);
            }

        };
    }

    /** 更新键的最后访问时间。 */
    @Override
    public long touch(String... names) {
        return commandExecutor.get(touchAsync(names));
    }

    /** 异步执行 touch。 */
    @Override
    public RFuture<Long> touchAsync(String... names) {
        if (names.length == 0) {
            return new CompletableFutureWrapper<>(0L);
        }

        return commandExecutor.writeBatchedAsync(null, RedisCommands.TOUCH_LONG, new LongSlotCallback(), map(names));
    }

    /** 统计存在的键数量。 */
    @Override
    public long countExists(String... names) {
        return commandExecutor.get(countExistsAsync(names));
    }

    /** 异步执行 countExists。 */
    @Override
    public RFuture<Long> countExistsAsync(String... names) {
        if (names.length == 0) {
            return new CompletableFutureWrapper<>(0L);
        }

        return commandExecutor.readBatchedAsync(StringCodec.INSTANCE, RedisCommands.EXISTS_LONG, new LongSlotCallback(), map(names));
    }

    /** 键管理 randomKey 操作。 */
    @Override
    public String randomKey() {
        return commandExecutor.get(randomKeyAsync());
    }

    private final RedisStrictCommand<String> randomKey = new RedisStrictCommand<String>("RANDOMKEY", obj -> {
        if (obj == null) {
            return null;
        }
        return unmap((String) obj);
    });

    /** 异步执行 randomKey。 */
    @Override
    public RFuture<String> randomKeyAsync() {
        return commandExecutor.readRandomAsync(StringCodec.INSTANCE, randomKey);
    }

    /** 键管理 deleteByPattern 操作。 */
    @Override
    public long deleteByPattern(String pattern) {
        return commandExecutor.get(deleteByPatternAsync(pattern));
    }

    /** 异步执行 deleteByPattern。 */
    @Override
    public RFuture<Long> deleteByPatternAsync(String pattern) {
        return eraseByPatternAsync(RedisCommands.DEL, pattern);
    }

    /** 键管理 unlinkByPattern 操作。 */
    @Override
    public long unlinkByPattern(String pattern) {
        return commandExecutor.get(unlinkByPatternAsync(pattern));
    }

    /** 异步执行 unlinkByPattern。 */
    @Override
    public RFuture<Long> unlinkByPatternAsync(String pattern) {
        return eraseByPatternAsync(RedisCommands.UNLINK, pattern);
    }

    /** 异步执行 eraseByPattern。 */
    private RFuture<Long> eraseByPatternAsync(RedisStrictCommand command, String pattern) {
        Function<Object[], Long> delegate = keys -> (Long) commandExecutor.get(commandExecutor.writeBatchedAsync(null, command, new LongSlotCallback(), keys));

        if (commandExecutor instanceof CommandBatchService
                || commandExecutor instanceof CommandReactiveBatchService
                || commandExecutor instanceof CommandRxBatchService) {
            if (commandExecutor.getServiceManager().getCfg().isClusterConfig()) {
                throw new IllegalStateException("This method doesn't work in batch for Redis cluster mode. For Redis cluster execute it as non-batch method");
            }

            return commandExecutor.evalWriteAsync((String) null, null, RedisCommands.EVAL_LONG,
                    "local keys = redis.call('keys', ARGV[1]) "
                            + "local n = 0 "
                            + "for i=1, #keys,5000 do "
                            + "n = n + redis.call(ARGV[2], unpack(keys, i, math.min(i+4999, table.getn(keys)))) "
                            + "end "
                            + "return n;", Collections.emptyList(), pattern, command.getName());
        }

        int batchSize = 500;
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        for (MasterSlaveEntry entry : commandExecutor.getConnectionManager().getEntrySet()) {
            CompletableFuture<Long> future = new CompletableFuture<>();
            futures.add(future);
            commandExecutor.getServiceManager().getExecutor().execute(() -> {
                long count = 0;
                try {
                    Iterator<Object> keysIterator = createKeysIterator(ByteArrayCodec.INSTANCE, entry, binaryScan, pattern, batchSize, null);
                    List<Object> keys = new ArrayList<>();
                    while (keysIterator.hasNext()) {
                        Object key = keysIterator.next();
                        keys.add(key);

                        if (keys.size() % batchSize == 0) {
                            count += delegate.apply(keys.toArray());
                            keys.clear();
                        }
                    }

                    if (!keys.isEmpty()) {
                        count += delegate.apply(keys.toArray());
                        keys.clear();
                    }

                    future.complete(count);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }

            });
        }

        CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        CompletableFuture<Long> res = future.handle((r, e) -> {
            long cc = futures.stream()
                    .filter(f -> f.isDone())
                    .mapToLong(f -> f.getNow(0L))
                    .sum();
            if (e != null) {
                if (cc > 0) {
                    RedisException ex = new RedisException(
                            cc + " keys have been deleted. But one or more nodes has an error", e);
                    throw new CompletionException(ex);
                } else {
                    throw new CompletionException(e);
                }
            }

            return cc;
        });
        return new CompletableFutureWrapper<>(res);
    }

    /** 删除 JSON 路径或键。 */
    @Override
    public long delete(String... keys) {
        return commandExecutor.get(deleteAsync(keys));
    }

    /** 删除 JSON 路径或键。 */
    @Override
    public long delete(RObject... objects) {
        return commandExecutor.get(deleteAsync(objects));
    }

    /** 异步 JSON 删除。 */
    @Override
    public RFuture<Long> deleteAsync(RObject... objects) {
        List<String> keys = new ArrayList<>();
        for (RObject obj : objects) {
            keys.add(obj.getName());
        }

        return deleteAsync(keys.toArray(new String[0]));
    }

    /** 异步删除键（UNLINK）。 */
    @Override
    public long unlink(String... keys) {
        return commandExecutor.get(unlinkAsync(keys));
    }

    /** 异步执行 unlink。 */
    @Override
    public RFuture<Long> unlinkAsync(String... keys) {
        if (keys.length == 0) {
            return new CompletableFutureWrapper<>(0L);
        }

        return commandExecutor.writeBatchedAsync(null, RedisCommands.UNLINK, new LongSlotCallback(), map(keys));
    }

    /** 异步 JSON 删除。 */
    @Override
    public RFuture<Long> deleteAsync(String... keys) {
        if (keys.length == 0) {
            return new CompletableFutureWrapper<>(0L);
        }

        return commandExecutor.writeBatchedAsync(null, RedisCommands.DEL, new LongSlotCallback(), map(keys));
    }

    /** 键管理 map 操作。 */
    private String map(String key) {
        return commandExecutor.getServiceManager().getNameMapper().map(key);
    }

    /** 键管理 unmap 操作。 */
    private String unmap(String key) {
        return commandExecutor.getServiceManager().getNameMapper().unmap(key);
    }

    /** 键管理 unmap 操作。 */
    private List<String> unmap(List<String> keys) {
        return keys.stream()
                .map(k -> commandExecutor.getServiceManager().getNameMapper().unmap(k))
                .collect(Collectors.toList());
    }

    /** 键管理 map 操作。 */
    private String[] map(String[] keys) {
        return Arrays.stream(keys)
                .map(k -> commandExecutor.getServiceManager().getNameMapper().map(k))
                .toArray(String[]::new);
    }

    /** 键管理 count 操作。 */
    @Override
    public long count() {
        return commandExecutor.get(countAsync());
    }

    /** 异步执行 count。 */
    @Override
    public RFuture<Long> countAsync() {
        List<CompletableFuture<Long>> futures = commandExecutor.readAllAsync(RedisCommands.DBSIZE);
        CompletableFuture<Void> f = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        CompletableFuture<Long> s = f.thenApply(r -> futures.stream().mapToLong(v -> v.getNow(0L)).sum());
        return new CompletableFutureWrapper<>(s);
    }

    /** 键管理 flushdbParallel 操作。 */
    @Override
    public void flushdbParallel() {
        commandExecutor.get(flushdbParallelAsync());
    }

    /** 异步执行 flushdbParallel。 */
    @Override
    public RFuture<Void> flushdbParallelAsync() {
        return commandExecutor.writeAllVoidAsync(RedisCommands.FLUSHDB_ASYNC);
    }

    /** 键管理 flushallParallel 操作。 */
    @Override
    public void flushallParallel() {
        commandExecutor.get(flushallParallelAsync());
    }

    /** 异步执行 flushallParallel。 */
    @Override
    public RFuture<Void> flushallParallelAsync() {
        return commandExecutor.writeAllVoidAsync(RedisCommands.FLUSHALL_ASYNC);
    }

    /** 键管理 flushdb 操作。 */
    @Override
    public void flushdb() {
        commandExecutor.get(flushdbAsync());
    }

    /** 异步执行 flushdb。 */
    @Override
    public RFuture<Void> flushdbAsync() {
        return commandExecutor.writeAllVoidAsync(RedisCommands.FLUSHDB);
    }

    /** 键管理 flushall 操作。 */
    @Override
    public void flushall() {
        commandExecutor.get(flushallAsync());
    }

    /** 异步执行 flushall。 */
    @Override
    public RFuture<Void> flushallAsync() {
        return commandExecutor.writeAllVoidAsync(RedisCommands.FLUSHALL);
    }

    /** 条目剩余 TTL（毫秒）。 */
    @Override
    public long remainTimeToLive(String name) {
        return commandExecutor.get(remainTimeToLiveAsync(name));
    }

    /** 异步返回条目 TTL。 */
    @Override
    public RFuture<Long> remainTimeToLiveAsync(String name) {
        return commandExecutor.readAsync(map(name), StringCodec.INSTANCE, RedisCommands.PTTL, map(name));
    }

    /** 重命名键。 */
    @Override
    public void rename(String currentName, String newName) {
        commandExecutor.get(renameAsync(currentName, newName));
    }

    /** 异步执行 rename。 */
    @Override
    public RFuture<Void> renameAsync(String currentName, String newName) {
        return commandExecutor.writeAsync(map(currentName), RedisCommands.RENAME, map(currentName), map(newName));
    }

    /** 键管理 renamenx 操作。 */
    @Override
    public boolean renamenx(String oldName, String newName) {
        return commandExecutor.get(renamenxAsync(oldName, newName));
    }

    /** 异步执行 renamenx。 */
    @Override
    public RFuture<Boolean> renamenxAsync(String oldName, String newName) {
        return commandExecutor.writeAsync(map(oldName), RedisCommands.RENAMENX, map(oldName), map(newName));
    }

    /** 清除条目 TTL。 */
    @Override
    public boolean clearExpire(String name) {
        return commandExecutor.get(clearExpireAsync(name));
    }

    /** 异步清除 TTL。 */
    @Override
    public RFuture<Boolean> clearExpireAsync(String name) {
        return commandExecutor.writeAsync(map(name), StringCodec.INSTANCE, RedisCommands.PERSIST, map(name));
    }

    /** 键管理 expireAt 操作。 */
    @Override
    public boolean expireAt(String name, long timestamp) {
        return commandExecutor.get(expireAtAsync(name, timestamp));
    }

    /** 异步执行 expireAt。 */
    @Override
    public RFuture<Boolean> expireAtAsync(String name, long timestamp) {
        return commandExecutor.writeAsync(map(name), StringCodec.INSTANCE, RedisCommands.PEXPIREAT, map(name), timestamp);
    }

    /** 键管理 expireAt 操作。 */
    @Override
    public long expireAt(Instant instant, String... names) {
        return commandExecutor.get(expireAtAsync(instant, names));
    }

    /** 异步执行 expireAt。 */
    @Override
    public RFuture<Long> expireAtAsync(Instant instant, String... names) {
        return expireAsyncInternal(RedisCommands.PEXPIREAT, instant.toEpochMilli(), names);
    }

    /** 键管理 expire 操作。 */
    @Override
    public boolean expire(String name, long timeToLive, TimeUnit timeUnit) {
        return commandExecutor.get(expireAsync(name, timeToLive, timeUnit));
    }

    /** 异步执行 expire。 */
    @Override
    public RFuture<Boolean> expireAsync(String name, long timeToLive, TimeUnit timeUnit) {
        return commandExecutor.writeAsync(map(name), StringCodec.INSTANCE, RedisCommands.PEXPIRE, map(name),
                timeUnit.toMillis(timeToLive));
    }

    /** 键管理 expire 操作。 */
    @Override
    public long expire(Duration duration, String... names) {
        return commandExecutor.get(expireAsync(duration, names));
    }

    /** 异步执行 expire。 */
    @Override
    public RFuture<Long> expireAsync(Duration duration, String... names) {
        return expireAsyncInternal(RedisCommands.PEXPIRE, duration.toMillis(), names);
    }

    /** 键管理 expireAsyncInternal 操作。 */
    private RFuture<Long> expireAsyncInternal(RedisCommand<?> command, long arg, String... names) {
        if (names.length == 0) {
            return new CompletableFutureWrapper<>(0L);
        }

        CommandBatchService executorService = new CommandBatchService(commandExecutor);
        for (String name : names) {
            String key = map(name);
            executorService.writeAsync(key, StringCodec.INSTANCE, command, key, arg);
        }

        CompletionStage<Long> result = executorService.executeAsync().thenApply(r -> {
            long success = 0;
            for (Object response : r.getResponses()) {
                if (response instanceof Boolean && (Boolean) response) {
                    success++;
                }
            }
            return success;
        });

        return new CompletableFutureWrapper<>(result);
    }

    /** 将键迁移到另一 Redis 实例。 */
    @Override
    public void migrate(String name, String host, int port, int database, long timeout) {
        commandExecutor.get(migrateAsync(name, host, port, database, timeout));
    }

    /** 将键迁移到另一 Redis 实例。 */
    @Override
    public void migrate(MigrateArgs migrateArgs) {
        commandExecutor.get(migrateAsync(migrateArgs));
    }

    /** 异步执行 migrate。 */
    @Override
    public RFuture<Void> migrateAsync(String name, String host, int port, int database, long timeout) {
        return commandExecutor.writeAsync(map(name), RedisCommands.MIGRATE, host, port, map(name), database, timeout);
    }

    /** 异步执行 migrate。 */
    @Override
    public RFuture<Void> migrateAsync(MigrateArgs migrateArgs) {
        MigrateParams migrateArgsParams = (MigrateParams) migrateArgs;
        List<Object> params = new ArrayList<>();
        params.add(migrateArgsParams.getHost());
        params.add(migrateArgsParams.getPort());
        params.add("");
        params.add(migrateArgsParams.getDatabase());
        params.add(migrateArgsParams.getTimeout());
        MigrateMode migrateMode = migrateArgsParams.getMode();
        if ((migrateMode.ordinal() & MigrateMode.COPY.ordinal()) != 0) {
            params.add(MigrateMode.COPY.name());
        }
        if ((migrateMode.ordinal() & MigrateMode.REPLACE.ordinal()) != 0) {
            params.add(MigrateMode.REPLACE.name());
        }
        String username = migrateArgsParams.getUsername();
        String password = migrateArgsParams.getPassword();
        if (username != null && !username.isEmpty()) {
            params.add("AUTH2");
            params.add(username);
            params.add(password);
        } else if (password != null && !password.isEmpty()) {
            params.add("AUTH");
            params.add(password);
        }
        String[] keys = migrateArgsParams.getKeys();
        String name = keys[0];
        params.add("KEYS");
        for (String key : keys) {
            params.add(map(key));
        }
        return commandExecutor.writeAsync(map(name), RedisCommands.MIGRATE, params.toArray());
    }



    /** 复制键到目标名称。 */
    @Override
    public void copy(String name, String host, int port, int database, long timeout) {
        commandExecutor.get(copyAsync(name, host, port, database, timeout));
    }

    /** 异步执行 copy。 */
    @Override
    public RFuture<Void> copyAsync(String name, String host, int port, int database, long timeout) {
        return commandExecutor.writeAsync(map(name), RedisCommands.MIGRATE, host, port, map(name), database, timeout, "COPY");
    }

    /** 将键移动到指定数据库。 */
    @Override
    public boolean move(String name, int database) {
        return commandExecutor.get(moveAsync(name, database));
    }

    /** 异步执行 move。 */
    @Override
    public RFuture<Boolean> moveAsync(String name, int database) {
        return commandExecutor.writeAsync(map(name), RedisCommands.MOVE, map(name), database);
    }

    /** 按模式流式迭代键。 */
    @Override
    public Stream<String> getKeysStreamByPattern(String pattern) {
        return toStream(getKeysByPattern(pattern).iterator());
    }

    /** 键管理 toStream 操作。 */
    protected <T> Stream<T> toStream(Iterator<T> iterator) {
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.NONNULL);
        return StreamSupport.stream(spliterator, false);
    }

    /** 按模式流式迭代键。 */
    @Override
    public Stream<String> getKeysStreamByPattern(String pattern, int count) {
        return toStream(getKeysByPattern(pattern, count).iterator());
    }

    /** 流式迭代键空间。 */
    @Override
    public Stream<String> getKeysStream() {
        return toStream(getKeys().iterator());
    }

    /** 流式迭代键空间。 */
    @Override
    public Stream<String> getKeysStream(KeysScanOptions options) {
        return toStream(getKeys(options).iterator());
    }

    /** 流式迭代键空间。 */
    @Override
    public Stream<String> getKeysStream(int count) {
        return toStream(getKeys(count).iterator());
    }

    /** 键管理 swapdb 操作。 */
    @Override
    public void swapdb(int db1, int db2) {
        commandExecutor.get(swapdbAsync(db1, db2));
    }

    /** 异步执行 swapdb。 */
    @Override
    public RFuture<Void> swapdbAsync(int db1, int db2) {
        return commandExecutor.writeAsync(null, RedisCommands.SWAPDB, db1, db2);
    }

    /** 注册 Map 变更监听器。 */
    @Override
    public int addListener(ObjectListener listener) {
        return commandExecutor.get(addListenerAsync(listener));
    }

    /** 异步执行 addListener。 */
    @Override
    public RFuture<Integer> addListenerAsync(ObjectListener listener) {
        if (listener instanceof NewObjectListener) {
            return addListenerAsync("__keyevent@*:new", (NewObjectListener) listener, NewObjectListener::onNew);
        }
        if (listener instanceof SetObjectListener) {
            return addListenerAsync("__keyevent@*:set", (SetObjectListener) listener, SetObjectListener::onSet);
        }
        if (listener instanceof ExpiredObjectListener) {
            return addListenerAsync("__keyevent@*:expired", (ExpiredObjectListener) listener, ExpiredObjectListener::onExpired);
        }
        if (listener instanceof DeletedObjectListener) {
            return addListenerAsync("__keyevent@*:del", (DeletedObjectListener) listener, DeletedObjectListener::onDeleted);
        }
        if (listener instanceof FlushListener) {
            if (!commandExecutor.getServiceManager().isResp3()) {
                throw new IllegalStateException("`protocol` config setting should be set to RESP3 value");
            }

            PublishSubscribeService subscribeService = commandExecutor.getConnectionManager().getSubscribeService();
            CompletableFuture<Integer> r = subscribeService.subscribe(commandExecutor, (FlushListener) listener);
            return new CompletableFutureWrapper<>(r);
        }
        throw new IllegalArgumentException();
    }

    /** 异步执行 addListener。 */
    private <T extends ObjectListener> RFuture<Integer> addListenerAsync(String name, T listener, BiConsumer<T, String> consumer) {
        RPatternTopic topic = new RedissonPatternTopic(StringCodec.INSTANCE, commandExecutor, name);
        return topic.addListenerAsync(String.class, (pattern, channel, msg) -> {
            consumer.accept(listener, msg);
        });
    }

    /** 移除监听器。 */
    @Override
    public void removeListener(int listenerId) {
        commandExecutor.get(removeListenerAsync(listenerId));
    }

    /** 异步执行 removeListener。 */
    @Override
    public RFuture<Void> removeListenerAsync(int listenerId) {
        PublishSubscribeService subscribeService = commandExecutor.getConnectionManager().getSubscribeService();
        CompletableFuture<Void> f = subscribeService.removeFlushListenerAsync(listenerId);
        f = f.thenCompose(r -> removeListenerAsync(null, listenerId,
                "__keyevent@*:expired", "__keyevent@*:del", "__keyevent@*:set", "__keyevent@*:new"));
        return new CompletableFutureWrapper<>(f);
    }

    /** 异步执行 removeListener。 */
    private RFuture<Void> removeListenerAsync(RFuture<Void> future, int listenerId, String... names) {
        List<CompletableFuture<Void>> futures = new ArrayList<>(names.length + 1);
        if (future != null) {
            futures.add(future.toCompletableFuture());
        }
        for (String name : names) {
            RPatternTopic topic = new RedissonPatternTopic(StringCodec.INSTANCE, commandExecutor, name);
            RFuture<Void> f1 = topic.removeListenerAsync(listenerId);
            futures.add(f1.toCompletableFuture());
        }
        CompletableFuture<Void> f = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        return new CompletableFutureWrapper<>(f);
    }

}
