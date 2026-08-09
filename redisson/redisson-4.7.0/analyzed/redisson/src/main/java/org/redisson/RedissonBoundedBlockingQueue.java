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

import org.redisson.api.Entry;
import org.redisson.api.RBoundedBlockingQueue;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.api.queue.QueueMoveElementsArgs;
import org.redisson.connection.decoder.ListDrainToDecoder;
import org.redisson.misc.CompletableFutureWrapper;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 有界分布式阻塞队列 {@link RBoundedBlockingQueue}（已废弃）。
 * <p>通过 {@link RedissonQueueSemaphore} 限制容量，委托 {@link RedissonBlockingQueue}
 * 实现阻塞入队/出队。
 *
 * @author Nikita Koksharov
 */
@Deprecated
public class RedissonBoundedBlockingQueue<V> extends RedissonQueue<V> implements RBoundedBlockingQueue<V> {

    /** 底层阻塞队列实现。 */
    private final RedissonBlockingQueue<V> blockingQueue;
    /** 控制队列容量的分布式信号量。 */
    private final RedissonQueueSemaphore semaphore;
    private final String channelName;
    private final String semaphoreName;

    protected RedissonBoundedBlockingQueue(CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {
        super(commandExecutor, name, redisson);
        blockingQueue = new RedissonBlockingQueue<>(commandExecutor, name, redisson);
        semaphoreName = getSemaphoreName(getRawName());
        semaphore = new RedissonQueueSemaphore(commandExecutor, semaphoreName, getServiceManager().getCfg().getCodec());
        channelName = RedissonSemaphore.getChannelName(semaphore.getRawName());
    }

    protected RedissonBoundedBlockingQueue(Codec codec, CommandAsyncExecutor commandExecutor, String name, RedissonClient redisson) {
        super(codec, commandExecutor, name, redisson);
        blockingQueue = new RedissonBlockingQueue<>(commandExecutor, name, redisson);
        semaphoreName = getSemaphoreName(getRawName());
        semaphore = new RedissonQueueSemaphore(commandExecutor, semaphoreName, codec);
        channelName = RedissonSemaphore.getChannelName(semaphore.getRawName());
    }
    
    /** 获取 SemaphoreName。 */
    private String getSemaphoreName(String name) {
        return prefixName("redisson_bqs", name);
    }
    
    /** 异步执行 add。 */
    @Override
    public RFuture<Boolean> addAsync(V e) {
        RFuture<Boolean> future = offerAsync(e);
        CompletionStage<Boolean> f = future.handle((res, ex) -> {
            if (ex != null) {
                throw new CompletionException(ex);
            }

            if (!res) {
                throw new CompletionException(new IllegalStateException("Queue is full"));
            }
            return true;
        });
        return new CompletableFutureWrapper<>(f);
    }

    /** 异步执行 put。 */
    @Override
    public RFuture<Void> putAsync(V e) {
        RedissonQueueSemaphore semaphore = createSemaphore(e);
        return semaphore.acquireAsync();
    }

    /** 有界阻塞队列 createSemaphore 操作。 */
    private RedissonQueueSemaphore createSemaphore(V e) {
        RedissonQueueSemaphore semaphore = new RedissonQueueSemaphore(commandExecutor, semaphoreName, getCodec());
        semaphore.setQueueName(getRawName());
        semaphore.setValue(e);
        return semaphore;
    }
    
    /** 写入键值（Map/Cache）。 */
    @Override
    public void put(V e) throws InterruptedException {
        RedissonQueueSemaphore semaphore = createSemaphore(e);
        semaphore.acquire();
    }
    
    /** 异步执行 offer。 */
    @Override
    public RFuture<Boolean> offerAsync(V e) {
        RedissonQueueSemaphore semaphore = createSemaphore(e);
        return semaphore.tryAcquireAsync();
    }

    /** 尝试入队（有界队列可能阻塞或失败）。 */
    @Override
    public boolean offer(V e, long timeout, TimeUnit unit) throws InterruptedException {
        RedissonQueueSemaphore semaphore = createSemaphore(e);
        return semaphore.tryAcquire(timeout, unit);
    }
    
    /** 异步执行 offer。 */
    @Override
    public RFuture<Boolean> offerAsync(V e, long timeout, TimeUnit unit) {
        RedissonQueueSemaphore semaphore = createSemaphore(e);
        return semaphore.tryAcquireAsync(timeout, unit);
    }

    /** 异步执行 take。 */
    @Override
    public RFuture<V> takeAsync() {
        RFuture<V> takeFuture = blockingQueue.takeAsync();
        return wrapTakeFuture(takeFuture);
    }

    /** 有界阻塞队列 wrapTakeFuture 操作。 */
    private <V> RFuture<V> wrapTakeFuture(RFuture<V> takeFuture) {
        CompletionStage<V> f = takeFuture.thenCompose(res -> {
            if (res == null) {
                return CompletableFuture.completedFuture(null);
            }
            return createSemaphore(null).releaseAsync().handle((r, ex) -> res);
        });
        f.whenComplete((r, e) -> {
            if (f.toCompletableFuture().isCancelled()) {
                takeFuture.cancel(false);
            }
        });
        return new CompletableFutureWrapper<>(f);
    }

    /** 异步执行 remove。 */
    @Override
    public RFuture<Boolean> removeAsync(Object o) {
        return removeAllAsync(Collections.singleton(o));
    }
    
    /** 异步执行 removeAll。 */
    @Override
    public RFuture<Boolean> removeAllAsync(Collection<?> c) {
        if (c.isEmpty()) {
            return new CompletableFutureWrapper<>(false);
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
                        "local count = 0; " +
                        "for i = 1, #ARGV, 1 do "
                            + "if redis.call('lrem', KEYS[1], 0, ARGV[i]) == 1 then "
                            + "count = count + 1; "
                            + "end; "
                        +"end; "
                        + "if count > 0 then "
                            + "local value = redis.call('incrby', KEYS[2], count); "
                            + "redis.call('publish', KEYS[3], value); "
                            + "return 1;"
                        + "end;"
                       + "return 0 ",
                       Arrays.asList(getRawName(), semaphore.getRawName(), channelName), encode(c).toArray());
    }
    
    /** 异步执行 poll。 */
    @Override
    public RFuture<V> pollAsync() {
        return commandExecutor.evalWriteNoRetryAsync(getRawName(), codec, RedisCommands.EVAL_OBJECT,
                "local res = redis.call('lpop', KEYS[1]);"
                + "if res ~= false then " +
                    "local value = redis.call('incrby', KEYS[2], ARGV[1]); " +
                    "redis.call('publish', KEYS[3], value); "
                + "end;"
                + "return res;",
                Arrays.asList(getRawName(), semaphore.getRawName(), channelName), 1);
    }
    
    /*
     * (non-Javadoc)
     * @see java.util.concurrent.BlockingQueue#take()
     */
    /** 阻塞直到可取到元素。 */
    @Override
    public V take() throws InterruptedException {
        return commandExecutor.getInterrupted(takeAsync());
    }

    /** 异步执行 poll。 */
    @Override
    public RFuture<V> pollAsync(long timeout, TimeUnit unit) {
        RFuture<V> takeFuture = blockingQueue.pollAsync(timeout, unit);
        return wrapTakeFuture(takeFuture);
    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.BlockingQueue#poll(long, java.util.concurrent.TimeUnit)
     */
    /** 出队队首元素。 */
    @Override
    public V poll(long timeout, TimeUnit unit) throws InterruptedException {
        return commandExecutor.getInterrupted(pollAsync(timeout, unit));
    }

    /*
     * (non-Javadoc)
     * @see org.redisson.core.RBlockingQueue#pollFromAny(long, java.util.concurrent.TimeUnit, java.lang.String[])
     */
    /** 有界阻塞队列 pollFromAny 操作。 */
    @Override
    public V pollFromAny(long timeout, TimeUnit unit, String... queueNames) throws InterruptedException {
        return commandExecutor.getInterrupted(pollFromAnyAsync(timeout, unit, queueNames));
    }

    /*
     * (non-Javadoc)
     * @see org.redisson.core.RBlockingQueueAsync#pollFromAnyAsync(long, java.util.concurrent.TimeUnit, java.lang.String[])
     */
    /** 异步执行 pollFromAny。 */
    @Override
    public RFuture<V> pollFromAnyAsync(long timeout, TimeUnit unit, String... queueNames) {
        RFuture<V> takeFuture = blockingQueue.pollFromAnyAsync(timeout, unit, queueNames);
        return wrapTakeFuture(takeFuture);
    }

    /** 有界阻塞队列 pollFromAnyWithName 操作。 */
    @Override
    public Entry<String, V> pollFromAnyWithName(Duration timeout, String... queueNames) throws InterruptedException {
        return commandExecutor.getInterrupted(pollFromAnyWithNameAsync(timeout, queueNames));
    }

    /** 异步执行 pollFromAnyWithName。 */
    @Override
    public RFuture<Entry<String, V>> pollFromAnyWithNameAsync(Duration timeout, String... queueNames) {
        RFuture<Entry<String, V>> takeFuture = blockingQueue.pollFromAnyWithNameAsync(timeout, queueNames);
        return wrapTakeFuture(takeFuture);
    }

    /** 有界阻塞队列 pollLastFromAnyWithName 操作。 */
    @Override
    public Entry<String, V> pollLastFromAnyWithName(Duration timeout, String... queueNames) throws InterruptedException {
        return commandExecutor.getInterrupted(pollLastFromAnyWithNameAsync(timeout, queueNames));
    }

    /** 异步执行 pollLastFromAnyWithName。 */
    @Override
    public RFuture<Entry<String, V>> pollLastFromAnyWithNameAsync(Duration timeout, String... queueNames) {
        RFuture<Entry<String, V>> takeFuture = blockingQueue.pollLastFromAnyWithNameAsync(timeout, queueNames);
        return wrapTakeFuture(takeFuture);
    }

    /** 有界阻塞队列 pollFirstFromAny 操作。 */
    @Override
    public Map<String, List<V>> pollFirstFromAny(Duration duration, int count, String... queueNames) {
        return get(pollFirstFromAnyAsync(duration, count, queueNames));
    }

    /** 有界阻塞队列 pollLastFromAny 操作。 */
    @Override
    public Map<String, List<V>> pollLastFromAny(Duration duration, int count, String... queueNames) {
        return get(pollLastFromAnyAsync(duration, count, queueNames));
    }

    /** 异步执行 pollFirstFromAny。 */
    @Override
    public RFuture<Map<String, List<V>>> pollFirstFromAnyAsync(Duration duration, int count, String... queueNames) {
        RFuture<Map<String, List<V>>> future = blockingQueue.pollFirstFromAnyAsync(duration, count, queueNames);
        return wrapTakeFuture(future);
    }

    /** 异步执行 pollLastFromAny。 */
    @Override
    public RFuture<Map<String, List<V>>> pollLastFromAnyAsync(Duration duration, int count, String... queueNames) {
        RFuture<Map<String, List<V>>> future = blockingQueue.pollLastFromAnyAsync(duration, count, queueNames);
        return wrapTakeFuture(future);
    }

    /** 有界阻塞队列 takeLastAndOfferFirstTo 操作。 */
    @Override
    public V takeLastAndOfferFirstTo(String queueName) throws InterruptedException {
        return commandExecutor.getInterrupted(takeLastAndOfferFirstToAsync(queueName));
    }

    /** 有界阻塞队列 subscribeOnElements 操作。 */
    @Override
    public int subscribeOnElements(Consumer<V> consumer) {
        return getServiceManager().getElementsSubscribeService()
                .subscribeOnElements(this::takeAsync, consumer);
    }

    /** 有界阻塞队列 subscribeOnElements 操作。 */
    @Override
    public int subscribeOnElements(Function<V, CompletionStage<Void>> consumer) {
        return getServiceManager().getElementsSubscribeService()
                .subscribeOnElements(this::takeAsync, consumer);
    }

    /** 有界阻塞队列 unsubscribe 操作。 */
    @Override
    public void unsubscribe(int listenerId) {
        getServiceManager().getElementsSubscribeService().unsubscribe(listenerId);
    }

    /** 异步执行 takeLastAndOfferFirstTo。 */
    @Override
    public RFuture<V> takeLastAndOfferFirstToAsync(String queueName) {
        return pollLastAndOfferFirstToAsync(queueName, 0, TimeUnit.SECONDS);
    }
    
    /** 异步执行 pollLastAndOfferFirstTo。 */
    @Override
    public RFuture<V> pollLastAndOfferFirstToAsync(String queueName, long timeout, TimeUnit unit) {
        RFuture<V> takeFuture = blockingQueue.pollLastAndOfferFirstToAsync(queueName, timeout, unit);
        return wrapTakeFuture(takeFuture);
    }

    /** 有界阻塞队列 pollLastAndOfferFirstTo 操作。 */
    @Override
    public V pollLastAndOfferFirstTo(String queueName, long timeout, TimeUnit unit) throws InterruptedException {
        return commandExecutor.getInterrupted(pollLastAndOfferFirstToAsync(queueName, timeout, unit));
    }

    /** 将键移动到指定数据库。 */
    @Override
    public List<V> move(QueueMoveElementsArgs args) {
        return get(moveAsync(args));
    }

    /** 异步执行 move。 */
    @Override
    public RFuture<List<V>> moveAsync(QueueMoveElementsArgs args) {
        return wrapMoveFuture(super.moveAsync(args));
    }

    /** 将键移动到指定数据库。 */
    @Override
    public List<V> move(Duration timeout, QueueMoveElementsArgs args) {
        return get(moveAsync(timeout, args));
    }

    /** 异步执行 move。 */
    @Override
    public RFuture<List<V>> moveAsync(Duration timeout, QueueMoveElementsArgs args) {
        return wrapMoveFuture(blockingQueue.moveAsync(timeout, args));
    }

    /** 有界阻塞队列 wrapMoveFuture 操作。 */
    private RFuture<List<V>> wrapMoveFuture(RFuture<List<V>> moveFuture) {
        CompletionStage<List<V>> f = moveFuture.thenCompose(res -> {
            if (res == null || res.isEmpty()) {
                return CompletableFuture.completedFuture(res);
            }
            return createSemaphore(null).releaseAsync(res.size()).handle((r, ex) -> res);
        });
        f.whenComplete((r, e) -> {
            if (f.toCompletableFuture().isCancelled()) {
                moveFuture.cancel(false);
            }
        });
        return new CompletableFutureWrapper<>(f);
    }

    /** 有界阻塞队列 remainingCapacity 操作。 */
    @Override
    public int remainingCapacity() {
        return createSemaphore(null).availablePermits();
    }

    /** 有界阻塞队列 drainTo 操作。 */
    @Override
    public int drainTo(Collection<? super V> c) {
        return get(drainToAsync(c));
    }

    /** 异步执行 drainTo。 */
    @Override
    public RFuture<Integer> drainToAsync(Collection<? super V> c) {
        if (c == null) {
            throw new NullPointerException();
        }

        return commandExecutor.evalWriteAsync(getRawName(), codec, new RedisCommand<Object>("EVAL", new ListDrainToDecoder(c)),
              "local vals = redis.call('lrange', KEYS[1], 0, -1); " +
              "redis.call('del', KEYS[1]); " +
              "if #vals > 0 then "
              + "local value = redis.call('incrby', KEYS[2], #vals); " +
                "redis.call('publish', KEYS[3], value); "
            + "end; " +
              "return vals", 
              Arrays.asList(getRawName(), semaphore.getRawName(), channelName));
    }
    
    /** 有界阻塞队列 drainTo 操作。 */
    @Override
    public int drainTo(Collection<? super V> c, int maxElements) {
        return get(drainToAsync(c, maxElements));
    }

    /** 异步执行 drainTo。 */
    @Override
    public RFuture<Integer> drainToAsync(Collection<? super V> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (maxElements <= 0) {
            return new CompletableFutureWrapper<>(0);
        }
        
        return commandExecutor.evalWriteAsync(getRawName(), codec, new RedisCommand<Object>("EVAL", new ListDrainToDecoder(c)),
                "local elemNum = math.min(ARGV[1], redis.call('llen', KEYS[1])) - 1;" +
                        "local vals = redis.call('lrange', KEYS[1], 0, elemNum); " +
                        "redis.call('ltrim', KEYS[1], elemNum + 1, -1); " +
                        "if #vals > 0 then "
                        + "local value = redis.call('incrby', KEYS[2], #vals); " +
                          "redis.call('publish', KEYS[3], value); "
                      + "end; " +
                        "return vals",
                        Arrays.asList(getRawName(), semaphore.getRawName(), channelName), maxElements);
    }
    
    /** 异步执行 trySetCapacity。 */
    @Override
    public RFuture<Boolean> trySetCapacityAsync(int capacity) {
        return commandExecutor.evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "local value = redis.call('get', KEYS[1]); " +
                "if (value == false) then "
                    + "redis.call('set', KEYS[1], ARGV[1]); "
                    + "redis.call('publish', KEYS[2], ARGV[1]); "
                    + "return 1;"
                + "end;"
                + "return 0;",
                Arrays.asList(semaphore.getRawName(), channelName), capacity);
    }
    
    /** 有界阻塞队列 trySetCapacity 操作。 */
    @Override
    public boolean trySetCapacity(int capacity) {
        return get(trySetCapacityAsync(capacity));
    }
    
    /** 清空全部元素。 */
    @Override
    public void clear() {
        get(commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
              "local len = redis.call('llen', KEYS[1]); " +
              "if len > 0 then "
              + "redis.call('del', KEYS[1]); "
              + "local value = redis.call('incrby', KEYS[2], len); " +
                "redis.call('publish', KEYS[3], value); "
            + "end; ", 
              Arrays.asList(getRawName(), semaphore.getRawName(), channelName)));

    }
    
    /** 异步执行 delete。 */
    @Override
    public RFuture<Boolean> deleteAsync() {
        return deleteAsync(getRawName(), semaphoreName);
    }

    /** 异步执行 copy。 */
    @Override
    public RFuture<Boolean> copyAsync(List<Object> keys, int database, boolean replace) {
        String newName = (String) keys.get(1);
        List<Object> kks = Arrays.asList(getRawName(), semaphoreName,
                                         newName, getSemaphoreName(newName));
        return super.copyAsync(kks, database, replace);
    }

    /** 异步执行 sizeInMemory。 */
    @Override
    public RFuture<Long> sizeInMemoryAsync() {
        List<Object> keys = Arrays.<Object>asList(getRawName(), semaphoreName);
        return super.sizeInMemoryAsync(keys);
    }

    /** 异步执行 expire。 */
    @Override
    public RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit, String param, String... keys) {
        return super.expireAsync(timeToLive, timeUnit, param, getRawName(), semaphoreName);
    }

    /** 异步执行 expireAt。 */
    @Override
    protected RFuture<Boolean> expireAtAsync(long timestamp, String param, String... keys) {
        return super.expireAtAsync(timestamp, param, getRawName(), semaphoreName);
    }

    /** 异步执行 clearExpire。 */
    @Override
    public RFuture<Boolean> clearExpireAsync() {
        return clearExpireAsync(getRawName(), semaphoreName);
    }

    /** 异步执行 addAll。 */
    @Override
    public RFuture<Boolean> addAllAsync(Collection<? extends V> c) {
        if (c.isEmpty()) {
            return new CompletableFutureWrapper<>(false);
        }

        RedissonQueueSemaphore semaphore = new RedissonQueueSemaphore(commandExecutor, semaphoreName, getCodec());
        semaphore.setQueueName(getRawName());
        semaphore.setValues(c);
        return semaphore.tryAcquireAsync();
    }


}