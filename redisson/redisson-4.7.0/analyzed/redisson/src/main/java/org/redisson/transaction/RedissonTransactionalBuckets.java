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
package org.redisson.transaction;

import org.redisson.RedissonBuckets;
import org.redisson.RedissonKeys;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RFuture;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.bucket.SetParams;
import org.redisson.api.bucket.SetArgs;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.transaction.operation.TransactionalOperation;
import org.redisson.transaction.operation.bucket.BucketSetOperation;
import org.redisson.transaction.operation.bucket.BucketsSetIfAllKeysAbsentOperation;
import org.redisson.transaction.operation.bucket.BucketsSetIfAllKeysExistOperation;
import org.redisson.transaction.operation.bucket.BucketsTrySetOperation;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * 事务内批量 {@link org.redisson.api.RBuckets} 实现。
 * <p>
 * 按 Redis 键名维护 {@link #state}；多键写操作通过 {@link RedissonMultiLock}
 * 一次性加锁，支持 trySet、setIfAllKeysExist/Absent 等条件批量写入。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonTransactionalBuckets extends RedissonBuckets {

    /** 键已删除哨兵。 */
    static final Object NULL = new Object();
    
    /** 多锁等待超时。 */
    private final long timeout;
    /** 事务是否已结束。 */
    private final AtomicBoolean executed;
    /** 操作列表。 */
    private final List<TransactionalOperation> operations;
    /** 键名 → 本地值或 {@link #NULL}。 */
    private final Map<String, Object> state = new HashMap<>();
    /** 事务 ID。 */
    private final String transactionId;
    
    public RedissonTransactionalBuckets(CommandAsyncExecutor commandExecutor, 
            long timeout, List<TransactionalOperation> operations, AtomicBoolean executed, String transactionId) {
        super(commandExecutor);
        
        this.timeout = timeout;
        this.operations = operations;
        this.executed = executed;
        this.transactionId = transactionId;
    }

    public RedissonTransactionalBuckets(Codec codec, CommandAsyncExecutor commandExecutor, 
            long timeout, List<TransactionalOperation> operations, AtomicBoolean executed, String transactionId) {
        super(codec, commandExecutor);
        
        this.timeout = timeout;
        this.operations = operations;
        this.executed = executed;
        this.transactionId = transactionId;
    }

    @Override
    /** 批量 get：合并本地 state 与 Redis 未缓存键。 */
    public <V> RFuture<Map<String, V>> getAsync(String... keys) {
        checkState();
        
        if (keys.length == 0) {
            return new CompletableFutureWrapper<>(Collections.emptyMap());
        }
        
        Set<String> keysToLoad = new HashSet<>();
        Map<String, V> map = new LinkedHashMap<>();
        for (String key : keys) {
            Object value = state.get(key);
            if (value != null) {
                if (value != NULL) {
                    map.put(key, (V) value);
                }
            } else {
                keysToLoad.add(key);
            }
        }
        
        if (keysToLoad.isEmpty()) {
            return new CompletableFutureWrapper<>(map);
        }
        
        CompletionStage<Map<String, V>> f = super.getAsync(keysToLoad.toArray(new String[0])).thenApply(res -> {
            map.putAll((Map<String, V>) res);
            return map;
        });
        return new CompletableFutureWrapper<>(f);
    }
    
    @Override
    /** 批量 set：对每个键加锁并登记 {@link BucketSetOperation}。 */
    public RFuture<Void> setAsync(Map<String, ?> buckets) {
        checkState();
        
        long currentThreadId = Thread.currentThread().getId();
        return executeLocked(() -> {
            for (Entry<String, ?> entry : buckets.entrySet()) {
                operations.add(new BucketSetOperation<>(entry.getKey(), getLockName(entry.getKey()),
                                        codec, entry.getValue(), transactionId, currentThreadId));
                if (entry.getValue() == null) {
                    state.put(entry.getKey(), NULL);
                } else {
                    state.put(entry.getKey(), entry.getValue());
                }
            }
            return CompletableFuture.completedFuture(null);
        }, buckets.keySet());
    }
    
//    待实现：RKeys.deleteAsync 的事务支持
//
//    public RFuture<Long> deleteAsync(String... keys) {
//        checkState();
//        RPromise<Long> result = new RedissonPromise<>();
//        long threadId = Thread.currentThread().getId();
//        executeLocked(result, new Runnable() {
//            @Override
//            public void run() {
//                AtomicLong counter = new AtomicLong();
//                AtomicLong executions = new AtomicLong(keys.length);
//                for (String key : keys) {
//                    Object st = state.get(key);
//                    if (st != null) {
//                        operations.add(new DeleteOperation(key, getLockName(key), transactionId, threadId));
//                        if (st != NULL) {
//                            state.put(key, NULL);
//                            counter.incrementAndGet();
//                        }
//                        if (executions.decrementAndGet() == 0) {
//                            result.trySuccess(counter.get());
//                        }
//                        continue;
//                    }
//
//                    RedissonKeys ks = new RedissonKeys(commandExecutor);
//                    ks.countExistsAsync(key).onComplete((res, e) -> {
//                        if (e != null) {
//                            result.tryFailure(e);
//                            return;
//                        }
//
//                        if (res > 0) {
//                            operations.add(new DeleteOperation(key, getLockName(key), transactionId, threadId));
//                            state.put(key, NULL);
//                            counter.incrementAndGet();
//                        }
//
//                        if (executions.decrementAndGet() == 0) {
//                            result.trySuccess(counter.get());
//                        }
//                    });
//                }
//            }
//        }, Arrays.asList(keys));
//        return result;
//    }
    
    @Override
    /** 全部键均不存在时才批量写入。 */
    public RFuture<Boolean> trySetAsync(Map<String, ?> buckets) {
        checkState();
        
        return executeLocked(() -> {
            Set<String> keysToSet = new HashSet<>();
            for (String key : buckets.keySet()) {
                Object value = state.get(key);
                if (value != null) {
                    if (value != NULL) {
                        operations.add(new BucketsTrySetOperation(codec, (Map<String, Object>) buckets, transactionId));
                        return CompletableFuture.completedFuture(false);
                    }
                } else {
                    keysToSet.add(key);
                }
            }
            
            if (keysToSet.isEmpty()) {
                operations.add(new BucketsTrySetOperation(codec, (Map<String, Object>) buckets, transactionId));
                state.putAll(buckets);
                return CompletableFuture.completedFuture(true);
            }
            
            RKeys keys = new RedissonKeys(commandExecutor);
            String[] ks = keysToSet.toArray(new String[keysToSet.size()]);
            return keys.countExistsAsync(ks).thenApply(res -> {
                operations.add(new BucketsTrySetOperation(codec, (Map<String, Object>) buckets, transactionId));
                if (res == 0) {
                    state.putAll(buckets);
                    return true;
                } else {
                    return false;
                }
            });
        }, buckets.keySet());
    }

    @Override
    /** 指定键在 Redis 与本地均存在时才批量 set。 */
    public RFuture<Boolean> setIfAllKeysExistAsync(SetArgs args) {
        checkState();

        SetParams pps = (SetParams) args;
        Map<String, ?> buckets = pps.getEntries();

        return executeLocked(() -> {
            Set<String> keysToSet = new HashSet<>();
            for (String key : buckets.keySet()) {
                Object value = state.get(key);
                if (value != null) {
                    if (value == NULL) {
                        operations.add(new BucketsSetIfAllKeysExistOperation(codec, args, transactionId));
                        return CompletableFuture.completedFuture(false);
                    }
                } else {
                    keysToSet.add(key);
                }
            }

            if (keysToSet.isEmpty()) {
                operations.add(new BucketsSetIfAllKeysExistOperation(codec, args, transactionId));
                state.putAll(buckets);
                return CompletableFuture.completedFuture(true);
            }

            RKeys keys = new RedissonKeys(commandExecutor);
            String[] ks = keysToSet.toArray(new String[keysToSet.size()]);
            return keys.countExistsAsync(ks).thenApply(res -> {
                operations.add(new BucketsSetIfAllKeysExistOperation(codec, args, transactionId));
                if (Objects.equals(res, (long) keysToSet.size())) {
                    state.putAll(buckets);
                    return true;
                } else {
                    return false;
                }
            });
        }, buckets.keySet());
    }

    @Override
    /** 全部键 absent 时才批量 set。 */
    public RFuture<Boolean> setIfAllKeysAbsentAsync(SetArgs args) {
        checkState();

        SetParams pps = (SetParams) args;
        Map<String, ?> buckets = pps.getEntries();

        return executeLocked(() -> {
            Set<String> keysToSet = new HashSet<>();
            for (String key : buckets.keySet()) {
                Object value = state.get(key);
                if (value != null) {
                    if (value != NULL) {
                        operations.add(new BucketsSetIfAllKeysAbsentOperation(codec, args, transactionId));
                        return CompletableFuture.completedFuture(false);
                    }
                } else {
                    keysToSet.add(key);
                }
            }

            if (keysToSet.isEmpty()) {
                operations.add(new BucketsSetIfAllKeysAbsentOperation(codec, args, transactionId));
                state.putAll(buckets);
                return CompletableFuture.completedFuture(true);
            }

            RKeys keys = new RedissonKeys(commandExecutor);
            String[] ks = keysToSet.toArray(new String[keysToSet.size()]);
            return keys.countExistsAsync(ks).thenApply(res -> {
                operations.add(new BucketsSetIfAllKeysAbsentOperation(codec, args, transactionId));
                if (res == 0) {
                    state.putAll(buckets);
                    return true;
                } else {
                    return false;
                }
            });
        }, buckets.keySet());
    }
    
    /** 对 keys 集合加 MultiLock 后执行。 */
    protected <R> RFuture<R> executeLocked(Supplier<CompletionStage<R>> runnable, Collection<String> keys) {
        List<RLock> locks = new ArrayList<>(keys.size());
        for (String key : keys) {
            RLock lock = getLock(key);
            locks.add(lock);
        }
        RedissonMultiLock multiLock = new RedissonMultiLock(locks.toArray(new RLock[0]));
        long threadId = Thread.currentThread().getId();
        CompletionStage<R> f = multiLock.lockAsync(timeout, TimeUnit.MILLISECONDS)
                .thenCompose(res -> runnable.get())
                .whenComplete((r, e) -> {
                    if (e != null) {
                        multiLock.unlockAsync(threadId);
                    }
                });
        return new CompletableFutureWrapper<>(f);
    }
    
    private RLock getLock(String name) {
        return new RedissonTransactionalLock(commandExecutor, getLockName(name), transactionId);
    }

    private String getLockName(String name) {
        return name + ":transaction_lock";
    }

    protected void checkState() {
        if (executed.get()) {
            throw new IllegalStateException("Unable to execute operation. Transaction is in finished state!");
        }
    }
    
}
