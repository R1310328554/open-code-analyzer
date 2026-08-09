/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

/*
 * The code was inspired by the similarly named JCTools class:
 * https://github.com/JCTools/JCTools/blob/master/jctools-core/src/main/java/org/jctools/queues/atomic
 */

package io.reactivex.rxjava4.operators;

import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.internal.util.Pow2;

/**
 * SPSC 链表式数组队列：消费慢于生产时可分配新数组岛并链接。
 * 末槽 HAS_NEXT 指向下一岛；支持原子双元素 offer。
 *
 * @param <T> 元素类型
 * @since 3.1.1
 */
public final class SpscLinkedArrayQueue<T> implements SimplePlainQueue<T> {
    static final int MAX_LOOK_AHEAD_STEP = Integer.getInteger("jctools.spsc.max.lookahead.step", 4096);
    final AtomicLong producerIndex = new AtomicLong();

    int producerLookAheadStep;
    long producerLookAhead;

    final int producerMask;

    AtomicReferenceArray<Object> producerBuffer;
    final int consumerMask;
    AtomicReferenceArray<Object> consumerBuffer;
    final AtomicLong consumerIndex = new AtomicLong();

    private static final Object HAS_NEXT = new Object();

    /**
     * @param bufferSize 每个数组岛的最大元素数（向上取 2 的幂，至少 8）
     */
    public SpscLinkedArrayQueue(final int bufferSize) {
        int p2capacity = Pow2.roundToPowerOfTwo(Math.max(8, bufferSize));
        int mask = p2capacity - 1;
        AtomicReferenceArray<Object> buffer = new AtomicReferenceArray<>(p2capacity + 1);
        producerBuffer = buffer;
        producerMask = mask;
        adjustLookAheadStep(p2capacity);
        consumerBuffer = buffer;
        consumerMask = mask;
        producerLookAhead = mask - 1; // we know it's all empty to start with
        soProducerIndex(0L);
    }

    /** {@inheritDoc} 仅单生产者线程安全。 */
    /** look-ahead 或 resize 新岛后写入；仅单生产者。 */
    @Override
    public boolean offer(final T e) {
        if (null == e) {
            throw new NullPointerException("Null is not a valid element");
        }
        // local load of field to avoid repeated loads after volatile reads
        final AtomicReferenceArray<Object> buffer = producerBuffer;
        final long index = lpProducerIndex();
        final int mask = producerMask;
        final int offset = calcWrappedOffset(index, mask);
        if (index < producerLookAhead) {
            return writeToQueue(buffer, e, index, offset);
        } else {
            final int lookAheadStep = producerLookAheadStep;
            // go around the buffer or resize if full (unless we hit max capacity)
            int lookAheadElementOffset = calcWrappedOffset(index + lookAheadStep, mask);
            if (null == lvElement(buffer, lookAheadElementOffset)) { // LoadLoad
                producerLookAhead = index + lookAheadStep - 1; // joy, there's plenty of room
                return writeToQueue(buffer, e, index, offset);
            } else if (null == lvElement(buffer, calcWrappedOffset(index + 1, mask))) { // buffer is not full
                return writeToQueue(buffer, e, index, offset);
            } else {
                resize(buffer, index, offset, e, mask); // add a buffer and link old to new
                return true;
            }
        }
    }

    private boolean writeToQueue(final AtomicReferenceArray<Object> buffer, final T e, final long index, final int offset) {
        soElement(buffer, offset, e); // StoreStore
        soProducerIndex(index + 1); // this ensures atomic write of long on 32bit platforms
        return true;
    }

    private void resize(final AtomicReferenceArray<Object> oldBuffer, final long currIndex, final int offset, final T e,
            final long mask) {
        final int capacity = oldBuffer.length();
        final AtomicReferenceArray<Object> newBuffer = new AtomicReferenceArray<>(capacity);
        producerBuffer = newBuffer;
        producerLookAhead = currIndex + mask - 1;
        soElement(newBuffer, offset, e); // StoreStore
        soNext(oldBuffer, newBuffer);
        soElement(oldBuffer, offset, HAS_NEXT); // new buffer is visible after element is
                                                                 // inserted
        soProducerIndex(currIndex + 1); // this ensures correctness on 32bit platforms
    }

    private void soNext(AtomicReferenceArray<Object> curr, AtomicReferenceArray<Object> next) {
        soElement(curr, calcDirectOffset(curr.length() - 1), next);
    }

    @SuppressWarnings("unchecked")
    private AtomicReferenceArray<Object> lvNextBufferAndUnlink(AtomicReferenceArray<Object> curr, int nextIndex) {
        int nextOffset = calcDirectOffset(nextIndex);
        AtomicReferenceArray<Object> nextBuffer = (AtomicReferenceArray<Object>)lvElement(curr, nextOffset);
        soElement(curr, nextOffset, null); // Avoid GC nepotism
        return nextBuffer;
    }
    /** {@inheritDoc} 仅单消费者线程安全。 */
    /** 遇 HAS_NEXT 则切换到下一岛再取；仅单消费者。 */
    @Nullable
    @SuppressWarnings("unchecked")
    @Override
    public T poll() {
        // local load of field to avoid repeated loads after volatile reads
        final AtomicReferenceArray<Object> buffer = consumerBuffer;
        final long index = lpConsumerIndex();
        final int mask = consumerMask;
        final int offset = calcWrappedOffset(index, mask);
        final Object e = lvElement(buffer, offset); // LoadLoad
        boolean isNextBuffer = e == HAS_NEXT;
        if (null != e && !isNextBuffer) {
            soElement(buffer, offset, null); // StoreStore
            soConsumerIndex(index + 1); // this ensures correctness on 32bit platforms
            return (T) e;
        } else if (isNextBuffer) {
            return newBufferPoll(lvNextBufferAndUnlink(buffer, mask + 1), index, mask);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private T newBufferPoll(AtomicReferenceArray<Object> nextBuffer, final long index, final int mask) {
        consumerBuffer = nextBuffer;
        final int offsetInNew = calcWrappedOffset(index, mask);
        final T n = (T) lvElement(nextBuffer, offsetInNew); // LoadLoad
        if (null != n) {
            soElement(nextBuffer, offsetInNew, null); // StoreStore
            soConsumerIndex(index + 1); // this ensures correctness on 32bit platforms
        }
        return n;
    }

    /** 窥视队首元素不移除；空则 null。 */
    @SuppressWarnings("unchecked")
    @Nullable
    public T peek() {
        final AtomicReferenceArray<Object> buffer = consumerBuffer;
        final long index = lpConsumerIndex();
        final int mask = consumerMask;
        final int offset = calcWrappedOffset(index, mask);
        final Object e = lvElement(buffer, offset); // LoadLoad
        if (e == HAS_NEXT) {
            return newBufferPeek(lvNextBufferAndUnlink(buffer, mask + 1), index, mask);
        }

        return (T) e;
    }

    @SuppressWarnings("unchecked")
    private T newBufferPeek(AtomicReferenceArray<Object> nextBuffer, final long index, final int mask) {
        consumerBuffer = nextBuffer;
        final int offsetInNew = calcWrappedOffset(index, mask);
        return (T) lvElement(nextBuffer, offsetInNew); // LoadLoad
    }

    @Override
    public void clear() {
        while (poll() != null || !isEmpty()) { } // NOPMD
    }

    /** 估算队列长度（先读 consumer 再读 producer，可能高估）。 */
    public int size() {
        /*
         * It is possible for a thread to be interrupted or reschedule between the read of the producer and
         * consumer indices, therefore protection is required to ensure size is within valid range. In the
         * event of concurrent polls/offers to this method the size is overestimated as we read consumer
         * index BEFORE the producer index.
         */
        long after = lvConsumerIndex();
        while (true) {
            final long before = after;
            final long currentProducerIndex = lvProducerIndex();
            after = lvConsumerIndex();
            if (before == after) {
                return (int) (currentProducerIndex - after);
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return lvProducerIndex() == lvConsumerIndex();
    }

    private void adjustLookAheadStep(int capacity) {
        producerLookAheadStep = Math.min(capacity / 4, MAX_LOOK_AHEAD_STEP);
    }

    private long lvProducerIndex() {
        return producerIndex.get();
    }

    private long lvConsumerIndex() {
        return consumerIndex.get();
    }

    private long lpProducerIndex() {
        return producerIndex.get();
    }

    private long lpConsumerIndex() {
        return consumerIndex.get();
    }

    private void soProducerIndex(long v) {
        producerIndex.lazySet(v);
    }

    private void soConsumerIndex(long v) {
        consumerIndex.lazySet(v);
    }

    private static int calcWrappedOffset(long index, int mask) {
        return calcDirectOffset((int)index & mask);
    }
    private static int calcDirectOffset(int index) {
        return index;
    }
    private static void soElement(AtomicReferenceArray<Object> buffer, int offset, Object e) {
        buffer.lazySet(offset, e);
    }

    private static Object lvElement(AtomicReferenceArray<Object> buffer, int offset) {
        return buffer.get(offset);
    }

    /**
     * 原子入队两个元素；勿与普通 offer 混用。
     * 空间不足时扩容新岛并链接。
     */
    @Override
    public boolean offer(T first, T second) {
        final AtomicReferenceArray<Object> buffer = producerBuffer;
        final long p = lvProducerIndex();
        final int m = producerMask;

        int pi = calcWrappedOffset(p + 2, m);

        if (null == lvElement(buffer, pi)) {
            pi = calcWrappedOffset(p, m);
            soElement(buffer, pi + 1, second);
            soElement(buffer, pi, first);
            soProducerIndex(p + 2);
        } else {
            final int capacity = buffer.length();
            final AtomicReferenceArray<Object> newBuffer = new AtomicReferenceArray<>(capacity);
            producerBuffer = newBuffer;

            pi = calcWrappedOffset(p, m);
            soElement(newBuffer, pi + 1, second); // StoreStore
            soElement(newBuffer, pi, first);
            soNext(buffer, newBuffer);

            soElement(buffer, pi, HAS_NEXT); // new buffer is visible after element is

            soProducerIndex(p + 2); // this ensures correctness on 32bit platforms
        }

        return true;
    }
}
