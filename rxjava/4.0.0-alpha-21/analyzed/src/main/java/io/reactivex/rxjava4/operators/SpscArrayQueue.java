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

import java.io.Serial;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.internal.util.Pow2;

/**
 * 预分配缓冲区的单生产者单消费者（SPSC）无锁队列（源自 JCTools）。
 * 融合 Fast Flow 与 BQueue 的 offer 优化，容量满时 offer 返回 false。
 * wait-free；容量向上取 2 的幂。
 *
 * @param <E> 元素类型
 * @since 3.1.1
 */
public final class SpscArrayQueue<E> extends AtomicReferenceArray<E> implements SimplePlainQueue<E> {
    @Serial
    private static final long serialVersionUID = -1296597691183856449L;
    private static final Integer MAX_LOOK_AHEAD_STEP = Integer.getInteger("jctools.spsc.max.lookahead.step", 4096);
    final int mask;
    final AtomicLong producerIndex;
    long producerLookAhead;
    final AtomicLong consumerIndex;
    final int lookAheadStep;

    /**
     * @param capacity 最大元素数（向上取 2 的幂作为数组长度）
     */
    public SpscArrayQueue(int capacity) {
        super(Pow2.roundToPowerOfTwo(capacity));
        this.mask = length() - 1;
        this.producerIndex = new AtomicLong();
        this.consumerIndex = new AtomicLong();
        lookAheadStep = Math.min(capacity / 4, MAX_LOOK_AHEAD_STEP);
    }

    /** look-ahead 探测空槽；满则 false；lazySet 更新 producerIndex。 */
    @Override
    public boolean offer(E e) {
        if (null == e) {
            throw new NullPointerException("Null is not a valid element");
        }
        // local load of field to avoid repeated loads after volatile reads
        final int mask = this.mask;
        final long index = producerIndex.get();
        final int offset = calcElementOffset(index, mask);
        if (index >= producerLookAhead) {
            int step = lookAheadStep;
            if (null == lvElement(calcElementOffset(index + step, mask))) { // LoadLoad
                producerLookAhead = index + step;
            } else if (null != lvElement(offset)) {
                return false;
            }
        }
        soElement(offset, e); // StoreStore
        soProducerIndex(index + 1); // ordered store -> atomic and ordered for size()
        return true;
    }

    /** 连续两次 offer（FIXME：非原子双入队）。 */
    @Override
    public boolean offer(E v1, E v2) {
        // FIXME
        return offer(v1) && offer(v2);
    }

    /** 读槽位非 null 则推进 consumerIndex 并清空槽。 */
    @Nullable
    @Override
    public E poll() {
        final long index = consumerIndex.get();
        final int offset = calcElementOffset(index);
        // local load of field to avoid repeated loads after volatile reads
        final E e = lvElement(offset); // LoadLoad
        if (null == e) {
            return null;
        }
        soConsumerIndex(index + 1); // ordered store -> atomic and ordered for size()
        soElement(offset, null); // StoreStore
        return e;
    }

    /** producerIndex == consumerIndex。 */
    @Override
    public boolean isEmpty() {
        return producerIndex.get() == consumerIndex.get();
    }

    void soProducerIndex(long newIndex) {
        producerIndex.lazySet(newIndex);
    }

    void soConsumerIndex(long newIndex) {
        consumerIndex.lazySet(newIndex);
    }

    /** 循环 poll 直至空（poll 弱保证需配合 isEmpty）。 */
    @Override
    public void clear() {
        // we have to test isEmpty because of the weaker poll() guarantee
        while (poll() != null || !isEmpty()) { } // NOPMD
    }

    int calcElementOffset(long index, int mask) {
        return (int)index & mask;
    }

    int calcElementOffset(long index) {
        return (int)index & mask;
    }

    void soElement(int offset, E value) {
        lazySet(offset, value);
    }

    E lvElement(int offset) {
        return get(offset);
    }
}

