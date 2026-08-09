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

import org.redisson.RedissonMap;
import org.redisson.ScanResult;
import org.redisson.api.*;
import org.redisson.api.mapreduce.RMapReduce;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.transaction.operation.TransactionalOperation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 事务内 {@link org.redisson.api.RMap} 实现：继承 {@link RedissonMap}，
 * 读写经 {@link BaseTransactionalMap} 缓冲，commit 前对其他客户端不可见。
 * <p>
 * 事务已结束（commit/rollback）后调用 {@link #checkState()} 会抛异常；
 * move/migrate/mapReduce/loadAll 及字段级锁等在事务中不支持。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key type
 * @param <V> value type
 */
public class RedissonTransactionalMap<K, V> extends RedissonMap<K, V> {

    /** 事务 Map 核心逻辑：维护本地状态与 {@link TransactionalOperation} 列表。 */
    private final BaseTransactionalMap<K, V> transactionalMap;
    /** 事务是否已结束；为 true 时禁止再执行操作。 */
    private final AtomicBoolean executed;

    /** 基于已有 {@link RMap} 实例包装为事务 Map。 */
    public RedissonTransactionalMap(CommandAsyncExecutor commandExecutor,  
            List<TransactionalOperation> operations, long timeout, AtomicBoolean executed, RMap<K, V> innerMap, String transactionId) {
        super(innerMap.getCodec(), commandExecutor, innerMap.getName(), null, null, null);
        this.executed = executed;
        this.transactionalMap = new BaseTransactionalMap<K, V>(commandExecutor, timeout, operations, innerMap, transactionId);
    }
    
    public RedissonTransactionalMap(CommandAsyncExecutor commandExecutor, String name, 
            List<TransactionalOperation> operations, long timeout, AtomicBoolean executed, String transactionId) {
        super(commandExecutor, name, null, null, null);
        this.executed = executed;
        RedissonMap<K, V> innerMap = new RedissonMap<K, V>(commandExecutor, name, null, null, null);
        this.transactionalMap = new BaseTransactionalMap<K, V>(commandExecutor, timeout, operations, innerMap, transactionId);
    }

    public RedissonTransactionalMap(Codec codec, CommandAsyncExecutor commandExecutor, String name,
            List<TransactionalOperation> operations, long timeout, AtomicBoolean executed, String transactionId) {
        super(codec, commandExecutor, name, null, null, null);
        this.executed = executed;
        RedissonMap<K, V> innerMap = new RedissonMap<K, V>(codec, commandExecutor, name, null, null, null);
        this.transactionalMap = new BaseTransactionalMap<K, V>(commandExecutor, timeout, operations, innerMap, transactionId);
    }
    
    // 过期设置委托 transactionalMap（commit 时落库）
    @Override
    public RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit, String param, String... keys) {
        return transactionalMap.expireAsync(timeToLive, timeUnit, param, keys);
    }
    
    @Override
    protected RFuture<Boolean> expireAtAsync(long timestamp, String param, String... keys) {
        return transactionalMap.expireAtAsync(timestamp, param, keys);
    }

    @Override
    public RFuture<Boolean> clearExpireAsync() {
        return transactionalMap.clearExpireAsync();
    }
    
    // 事务内不支持跨库 MOVE
    @Override
    public RFuture<Boolean> moveAsync(int database) {
        throw new UnsupportedOperationException("move method is not supported in transaction");
    }
    
    @Override
    public RFuture<Void> migrateAsync(String host, int port, int database, long timeout) {
        throw new UnsupportedOperationException("migrate method is not supported in transaction");
    }
    
    @Override
    public <KOut, VOut> RMapReduce<K, V, KOut, VOut> mapReduce() {
        throw new UnsupportedOperationException("mapReduce method is not supported in transaction");
    }
    
    @Override
    public ScanResult<Map.Entry<Object, Object>> scanIterator(String name, RedisClient client,
                                                              String startPos, String pattern, int count) {
        checkState();
        return transactionalMap.scanIterator(name, client, startPos, pattern, count);
    }
    
    // 读操作：校验事务状态后委托
    @Override
    public RFuture<Boolean> containsKeyAsync(Object key) {
        checkState();
        return transactionalMap.containsKeyAsync(key);
    }
    
    @Override
    public RFuture<Boolean> containsValueAsync(Object value) {
        checkState();
        return transactionalMap.containsValueAsync(value);
    }
    
    @Override
    protected RFuture<V> addAndGetOperationAsync(K key, Number value) {
        checkState();
        return transactionalMap.addAndGetOperationAsync(key, value);
    }

    @Override
    protected RFuture<V> putIfExistsOperationAsync(K key, V value) {
        checkState();
        return transactionalMap.putIfExistsOperationAsync(key, value);
    }

    @Override
    protected RFuture<V> putIfAbsentOperationAsync(K key, V value) {
        checkState();
        return transactionalMap.putIfAbsentOperationAsync(key, value);
    }
    
    // 写操作：缓冲至事务操作列表
    @Override
    protected RFuture<V> putOperationAsync(K key, V value) {
        checkState();
        return transactionalMap.putOperationAsync(key, value);
    }

    @Override
    protected RFuture<Boolean> fastPutIfExistsOperationAsync(K key, V value) {
        checkState();
        return transactionalMap.fastPutIfExistsOperationAsync(key, value);
    }

    @Override
    protected RFuture<Boolean> fastPutIfAbsentOperationAsync(K key, V value) {
        checkState();
        return transactionalMap.fastPutIfAbsentOperationAsync(key, value);
    }
    
    @Override
    protected RFuture<Boolean> fastPutOperationAsync(K key, V value) {
        checkState();
        return transactionalMap.fastPutOperationAsync(key, value);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected RFuture<Long> fastRemoveOperationAsync(K... keys) {
        checkState();
        return transactionalMap.fastRemoveOperationAsync(keys);
    }
    
    @Override
    public RFuture<Integer> valueSizeAsync(K key) {
        checkState();
        return transactionalMap.valueSizeAsync(key);
    }
    
    @Override
    public RFuture<V> getOperationAsync(K key) {
        checkState();
        return transactionalMap.getOperationAsync(key);
    }

    @Override
    public RFuture<Set<K>> readAllKeySetAsync() {
        checkState();
        return transactionalMap.readAllKeySetAsync();
    }
    
    @Override
    public RFuture<Set<Entry<K, V>>> readAllEntrySetAsync() {
        checkState();
        return transactionalMap.readAllEntrySetAsync();
    }
    
    @Override
    public RFuture<Collection<V>> readAllValuesAsync() {
        checkState();
        return transactionalMap.readAllValuesAsync();
    }
    
    @Override
    public RFuture<Map<K, V>> readAllMapAsync() {
        checkState();
        return transactionalMap.readAllMapAsync();
    }
    
    @Override
    public RFuture<Map<K, V>> getAllOperationAsync(Set<K> keys) {
        checkState();
        return transactionalMap.getAllOperationAsync(keys);
    }
    
    @Override
    protected RFuture<V> removeOperationAsync(K key) {
        checkState();
        return transactionalMap.removeOperationAsync(key);
    }

    @Override
    public Set<K> keySet(String pattern, int count) {
        checkState();
        return transactionalMap.keySet(pattern, count);
    }

    @Override
    protected RFuture<Boolean> removeOperationAsync(Object key, Object value) {
        checkState();
        return transactionalMap.removeOperationAsync(key, value);
    }
    
    @Override
    protected RFuture<Void> putAllOperationAsync(Map<? extends K, ? extends V> entries) {
        checkState();
        return transactionalMap.putAllOperationAsync(entries);
    }
    
    @Override
    protected RFuture<Boolean> replaceOperationAsync(final K key, final V oldValue, final V newValue) {
        checkState();
        return transactionalMap.replaceOperationAsync(key, oldValue, newValue);
    }

    @Override
    public RFuture<Boolean> touchAsync() {
        checkState();
        return transactionalMap.touchAsync(commandExecutor);
    }
    
    @Override
    public RFuture<Boolean> isExistsAsync() {
        checkState();
        return transactionalMap.isExistsAsync();
    }
    
    @Override
    public RFuture<Boolean> unlinkAsync() {
        return transactionalMap.unlinkAsync(commandExecutor);
    }
    
    @Override
    public RFuture<Boolean> deleteAsync() {
        checkState();
        return transactionalMap.deleteAsync(commandExecutor);
    }
    
    @Override
    protected RFuture<V> replaceOperationAsync(final K key, final V value) {
        checkState();
        return transactionalMap.replaceOperationAsync(key, value);
    }
    
    /** 若事务已 commit/rollback，拒绝后续操作。 */
    protected void checkState() {
        if (executed.get()) {
            throw new IllegalStateException("Unable to execute operation. Transaction is in finished state!");
        }
    }
    
    @Override
    public RFuture<Void> loadAllAsync(boolean replaceExistingValues, int parallelism) {
        throw new UnsupportedOperationException("loadAll method is not supported in transaction");
    }
    
    @Override
    public RFuture<Void> loadAllAsync(Set<? extends K> keys, boolean replaceExistingValues, int parallelism) {
        throw new UnsupportedOperationException("loadAll method is not supported in transaction");
    }
    
    // 事务内不支持字段级分布式锁
    @Override
    public RLock getFairLock(K key) {
        throw new UnsupportedOperationException("getFairLock method is not supported in transaction");
    }
    
    @Override
    public RCountDownLatch getCountDownLatch(K key) {
        throw new UnsupportedOperationException("getCountDownLatch method is not supported in transaction");
    }
    
    @Override
    public RPermitExpirableSemaphore getPermitExpirableSemaphore(K key) {
        throw new UnsupportedOperationException("getPermitExpirableSemaphore method is not supported in transaction");
    }
    
    @Override
    public RSemaphore getSemaphore(K key) {
        throw new UnsupportedOperationException("getSemaphore method is not supported in transaction");
    }
    
    @Override
    public RLock getLock(K key) {
        throw new UnsupportedOperationException("getLock method is not supported in transaction");
    }
    
    @Override
    public RReadWriteLock getReadWriteLock(K key) {
        throw new UnsupportedOperationException("getReadWriteLock method is not supported in transaction");
    }
    
}
