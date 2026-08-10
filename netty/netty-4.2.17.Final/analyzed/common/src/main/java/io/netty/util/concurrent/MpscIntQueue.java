/*
 * Copyright 2025 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util.concurrent;

import io.netty.util.internal.MathUtil;
import io.netty.util.internal.ObjectUtil;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * A multi-producer (concurrent and thread-safe {@code offer} and {@code fill}),
 * single-consumer (single-threaded {@code poll} and {@code drain}) queue of primitive integers.
 *
 * <p>多生产者、单消费者（MPSC）的原始 int 队列：{@code offer}/{@code fill} 可并发调用，
 * {@code poll}/{@code drain} 须单线程。使用 {@code emptyValue} 表示槽位为空，不可作为有效元素入队。</p>
 */
public interface MpscIntQueue {
    /**
     * Create a new queue instance of the given size.
     * <p>
     * Note: the size of the queue may be rounded up to nearest power-of-2.
     *
     * @param size The required fixed size of the queue.
     * @param emptyValue The special value that the queue should use to signal the "empty" case.
     * This value will be returned from {@link #poll()} when the queue is empty,
     * and giving this value to {@link #offer(int)} will cause an exception to be thrown.
     * @return The queue instance.
     *
     * <p>创建固定容量队列；容量会向上取整为 2 的幂。{@code emptyValue} 用于标记空槽，{@link #poll()} 空队列时返回该值。</p>
     */
    static MpscIntQueue create(int size, int emptyValue) {
        return new MpscAtomicIntegerArrayQueue(size, emptyValue);
    }

    /**
     * Offer the given value to the queue. This will throw an exception if the given value is the "empty" value.
     * @param value The value to add to the queue.
     * @return {@code true} if the value was added to the queue,
     * or {@code false} if the value could not be added because the queue is full.
     *
     * <p>入队一个 int；队列满时返回 {@code false}，不可 offer {@code emptyValue}。</p>
     */
    boolean offer(int value);

    /**
     * Remove and return the next value from the queue, or return the "empty" value if the queue is empty.
     * @return The next value or the "empty" value.
     *
     * <p>出队；空队列返回 {@code emptyValue}。仅单消费者线程调用。</p>
     */
    int poll();

    /**
     * Remove up to the given limit of elements from the queue, and pass them to the consumer in order.
     * @param limit The maximum number of elements to dequeue.
     * @param consumer The consumer to pass the removed elements to.
     * @return The actual number of elements removed.
     *
     * <p>批量出队并依次交给 {@code consumer}，返回实际出队数量。</p>
     */
    int drain(int limit, IntConsumer consumer);

    /**
     * Add up to the given limit of elements to this queue, from the given supplier.
     * @param limit The maximum number of elements to enqueue.
     * @param supplier The supplier to obtain the elements from.
     * @return The actual number of elements added.
     *
     * <p>从 {@code supplier} 批量入队，最多 {@code limit} 个，返回实际入队数。</p>
     */
    int fill(int limit, IntSupplier supplier);

    /**
     * Peek at all available elements and compute a reduction.
     * The elements are not removed, and the iteration is weakly consistent.
     * @param limit The maximum number of elements to process.
     * @param initial The initial value to the reduction operation.
     * @param op The reduction operation, taking a prior result and an element, and producing a new result.
     * @return The last result of the reduction operation.
     *
     * <p>弱一致地窥视最多 {@code limit} 个元素并归约，不移除元素。默认实现直接返回 {@code initial}。</p>
     */
    default int weakPeekReduce(int limit, int initial, IntBinaryOperator op) {
        // There's no safe way to implement this method in terms of the other operations.
        // Take the "weak" definition to the extreme and just return the initial value.
        return initial;
    }

    /**
     * Query if the queue is empty or not.
     * <p>
     * This method is inherently racy and the result may be out of date by the time the method returns.
     * @return {@code true} if the queue was observed to be empty, otherwise {@code false.
     *
     * <p>是否为空（弱一致，结果可能已过时）。</p>
     */
    boolean isEmpty();

    /**
     * Query the number of elements currently in the queue.
     * <p>
     * This method is inherently racy and the result may be out of date by the time the method returns.
     * @return An estimate of the number of elements observed in the queue.
     *
     * <p>当前元素个数估计值（弱一致）。</p>
     */
    int size();

    /**
     * This implementation is based on MpscAtomicUnpaddedArrayQueue from JCTools.
     *
     * <p>基于 JCTools MpscAtomicUnpaddedArrayQueue 的 {@link AtomicIntegerArray} 实现。</p>
     */
    final class MpscAtomicIntegerArrayQueue extends AtomicIntegerArray implements MpscIntQueue {
        private static final long serialVersionUID = 8740338425124821455L;
        private static final AtomicLongFieldUpdater<MpscAtomicIntegerArrayQueue> PRODUCER_INDEX =
                AtomicLongFieldUpdater.newUpdater(MpscAtomicIntegerArrayQueue.class, "producerIndex");
        private static final AtomicLongFieldUpdater<MpscAtomicIntegerArrayQueue> PRODUCER_LIMIT =
                AtomicLongFieldUpdater.newUpdater(MpscAtomicIntegerArrayQueue.class, "producerLimit");
        private static final AtomicLongFieldUpdater<MpscAtomicIntegerArrayQueue> CONSUMER_INDEX =
                AtomicLongFieldUpdater.newUpdater(MpscAtomicIntegerArrayQueue.class, "consumerIndex");
        /** 容量掩码（capacity - 1），用于 index & mask 定位槽位。 */
        private final int mask;
        /** 表示空槽的特殊值。 */
        private final int emptyValue;
        /** 生产者已 claim 的下一槽位索引（仅递增，实际写入可能略滞后）。 */
        private volatile long producerIndex;
        /** 生产者可写入的上界缓存，减少读取 consumerIndex 的频率。 */
        private volatile long producerLimit;
        /** 消费者下一待读槽位索引。 */
        private volatile long consumerIndex;

        public MpscAtomicIntegerArrayQueue(int capacity, int emptyValue) {
            super(MathUtil.safeFindNextPositivePowerOfTwo(capacity));
            if (emptyValue != 0) {
                this.emptyValue = emptyValue;
                int end = length() - 1;
                for (int i = 0; i < end; i++) {
                    lazySet(i, emptyValue);
                }
                getAndSet(end, emptyValue); // 'getAndSet' acts as a full barrier, giving us initialization safety.
            } else {
                this.emptyValue = 0;
            }
            mask = length() - 1;
        }

        @Override
        public boolean offer(int value) {
            if (value == emptyValue) {
                throw new IllegalArgumentException("Cannot offer the \"empty\" value: " + emptyValue);
            }
            // use a cached view on consumer index (potentially updated in loop)
            final int mask = this.mask;
            long producerLimit = this.producerLimit;
            long pIndex;
            do {
                pIndex = producerIndex;
                if (pIndex >= producerLimit) {
                    final long cIndex = consumerIndex;
                    producerLimit = cIndex + mask + 1;
                    if (pIndex >= producerLimit) {
                        // FULL :(
                        return false;
                    } else {
                        // update producer limit to the next index that we must recheck the consumer index
                        // this is racy, but the race is benign
                        PRODUCER_LIMIT.lazySet(this, producerLimit);
                    }
                }
            } while (!PRODUCER_INDEX.compareAndSet(this, pIndex, pIndex + 1));
            /*
             * NOTE: the new producer index value is made visible BEFORE the element in the array. If we relied on
             * the index visibility to poll() we would need to handle the case where the element is not visible.
             */
            // Won CAS, move on to storing — 先发布 index 再写入元素
            final int offset = (int) (pIndex & mask);
            lazySet(offset, value);
            // AWESOME :)
            return true;
        }

        @Override
        public int poll() {
            final long cIndex = consumerIndex;
            final int offset = (int) (cIndex & mask);
            // If we can't see the next available element we can't poll
            int value = get(offset);
            if (emptyValue == value) {
                /*
                 * NOTE: Queue may not actually be empty in the case of a producer (P1) being interrupted after
                 * winning the CAS on offer but before storing the element in the queue. Other producers may go on
                 * to fill up the queue after this element.
                 */
                if (cIndex != producerIndex) {
                    // 生产者已 claim 但尚未写入，自旋等待
                    do {
                        value = get(offset);
                    } while (emptyValue == value);
                } else {
                    return emptyValue;
                }
            }
            lazySet(offset, emptyValue);
            CONSUMER_INDEX.lazySet(this, cIndex + 1);
            return value;
        }

        @Override
        public int drain(int limit, IntConsumer consumer) {
            Objects.requireNonNull(consumer, "consumer");
            ObjectUtil.checkPositiveOrZero(limit, "limit");
            if (limit == 0) {
                return 0;
            }
            final int mask = this.mask;
            final long cIndex = consumerIndex; // Note: could be weakened to plain-load.
            for (int i = 0; i < limit; i++) {
                final long index = cIndex + i;
                final int offset = (int) (index & mask);
                final int value = get(offset);
                if (emptyValue == value) {
                    return i;
                }
                lazySet(offset, emptyValue); // Note: could be weakened to plain-store.
                // ordered store -> atomic and ordered for size()
                CONSUMER_INDEX.lazySet(this, index + 1);
                consumer.accept(value);
            }
            return limit;
        }

        @Override
        public int fill(int limit, IntSupplier supplier) {
            Objects.requireNonNull(supplier, "supplier");
            ObjectUtil.checkPositiveOrZero(limit, "limit");
            if (limit == 0) {
                return 0;
            }
            final int mask = this.mask;
            final long capacity = mask + 1;
            long producerLimit = this.producerLimit;
            long pIndex;
            int actualLimit;
            do {
                pIndex = producerIndex;
                long available = producerLimit - pIndex;
                if (available <= 0) {
                    final long cIndex = consumerIndex;
                    producerLimit = cIndex + capacity;
                    available = producerLimit - pIndex;
                    if (available <= 0) {
                        // FULL :(
                        return 0;
                    } else {
                        // update producer limit to the next index that we must recheck the consumer index
                        PRODUCER_LIMIT.lazySet(this, producerLimit);
                    }
                }
                actualLimit = Math.min((int) available, limit);
            } while (!PRODUCER_INDEX.compareAndSet(this, pIndex, pIndex + actualLimit));
            // right, now we claimed a few slots and can fill them with goodness
            for (int i = 0; i < actualLimit; i++) {
                // Won CAS, move on to storing
                final int offset = (int) (pIndex + i & mask);
                lazySet(offset, supplier.getAsInt());
            }
            return actualLimit;
        }

        @Override
        public int weakPeekReduce(int limit, int initial, IntBinaryOperator op) {
            Objects.requireNonNull(op, "op");
            ObjectUtil.checkPositiveOrZero(limit, "limit");
            if (limit == 0) {
                return 0;
            }
            int result = initial;

            final int mask = this.mask;
            final long cIndex = consumerIndex; // Note: could be weakened to plain-load.
            for (int i = 0; i < limit; i++) {
                final long index = cIndex + i;
                final int offset = (int) (index & mask);
                final int value = get(offset);
                if (emptyValue == value) {
                    return result;
                }
                // Do not remove the element or advance the consumer index.
                result = op.applyAsInt(result, value);
            }
            return result;
        }

        @Override
        public boolean isEmpty() {
            // Load consumer index before producer index, so our check is conservative.
            long cIndex = consumerIndex;
            long pIndex = producerIndex;
            return cIndex >= pIndex;
        }

        @Override
        public int size() {
            // Loop until we get a consistent read of both the consumer and producer indices.
            long after = consumerIndex;
            long size;
            for (;;) {
                long before = after;
                long pIndex = producerIndex;
                after = consumerIndex;
                if (before == after) {
                    size = pIndex - after;
                    break;
                }
            }
            return size < 0 ? 0 : size > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) size;
        }
    }
}
