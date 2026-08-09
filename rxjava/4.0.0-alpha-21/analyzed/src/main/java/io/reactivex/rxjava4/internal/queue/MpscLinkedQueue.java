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
 * 实现思路参考 JCTools 同名类：
 * https://github.com/JCTools/JCTools/blob/master/jctools-core/src/main/java/org/jctools/queues/atomic
 */

package io.reactivex.rxjava4.internal.queue;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.operators.SimplePlainQueue;

/**
 * 多生产者、单消费者无界链表队列。
 * @param <T> 元素类型
 */
public final class MpscLinkedQueue<T> implements SimplePlainQueue<T> {
    private final AtomicReference<LinkedQueueNode<T>> producerNode;
    private final AtomicReference<LinkedQueueNode<T>> consumerNode;

    public MpscLinkedQueue() {
        producerNode = new AtomicReference<>();
        consumerNode = new AtomicReference<>();
        LinkedQueueNode<T> node = new LinkedQueueNode<>();
        spConsumerNode(node);
        xchgProducerNode(node); // StoreLoad 保证构造可见性
    }

    /**
     * {@inheritDoc} <br>
     * <p>
     * 实现说明：<br>
     * 允许多线程 offer。<br>
     * 分配新节点并：
     * <ol>
     * <li>与当前 producer 节点原子交换（仅一个生产者“获胜”）
     * <li>将新节点链接到被换出的 producer 节点之后
     * </ol>
     * 每个生产者都会植入新节点并链接旧节点；XCHG 保证无两个生产者取得同一 producer 节点。
     *
     * @see java.util.Queue#offer(java.lang.Object)
     */
    @Override
    public boolean offer(final T e) {
        if (null == e) {
            throw new NullPointerException("Null is not a valid element");
        }
        final LinkedQueueNode<T> nextNode = new LinkedQueueNode<>(e);
        final LinkedQueueNode<T> prevProducerNode = xchgProducerNode(nextNode);
        // 若生产者线程在此被中断，链会断裂直至恢复并完成 prev.next 写入
        prevProducerNode.soNext(nextNode); // StoreStore 发布链接
        return true;
    }

    /**
     * {@inheritDoc} <br>
     * <p>
     * 实现说明：<br>
     * 仅允许单线程 poll。<br>
     * 从 consumerNode 读取下一节点：
     * <ol>
     * <li>为 null 则视为空（可能仍有生产者在链接）
     * <li>非 null 则设为 consumer 节点并返回已取出的值
     * </ol>
     * consumerNode.value 恒为 null（队列哨兵）；禁止 offer null，故任意时刻仅该节点 value 可为 null。
     *
     * @see java.util.Queue#poll()
     */
    @Nullable
    @Override
    public T poll() {
        LinkedQueueNode<T> currConsumerNode = lpConsumerNode(); // 本地加载一次即可
        LinkedQueueNode<T> nextNode = currConsumerNode.lvNext();
        if (nextNode != null) {
            // 取出值后清空节点 value，因 consumer 将持有该节点
            final T nextValue = nextNode.getAndNullValue();
            spConsumerNode(nextNode);
            // 断开前一 consumer 节点链接以利于 GC
            currConsumerNode.soNext(null);
            return nextValue;
        }
        else if (currConsumerNode != lvProducerNode()) {
            // 自旋等待链接完成，此路径非 wait-free
            while ((nextNode = currConsumerNode.lvNext()) == null) { } // NOPMD
            // 已取得下一节点

            // we have to null out the value because we are going to hang on to the node
            final T nextValue = nextNode.getAndNullValue();
            spConsumerNode(nextNode);
            // unlink previous consumer to help gc
            currConsumerNode.soNext(null);
            return nextValue;
        }
        return null;
    }

    @Override
    public boolean offer(T v1, T v2) {
        offer(v1);
        offer(v2);
        return true;
    }

    @Override
    public void clear() {
        while (poll() != null && !isEmpty()) { } // NOPMD
    }
    LinkedQueueNode<T> lvProducerNode() {
        return producerNode.get();
    }
    LinkedQueueNode<T> xchgProducerNode(LinkedQueueNode<T> node) {
        return producerNode.getAndSet(node);
    }
    LinkedQueueNode<T> lvConsumerNode() {
        return consumerNode.get();
    }

    LinkedQueueNode<T> lpConsumerNode() {
        return consumerNode.get();
    }
    void spConsumerNode(LinkedQueueNode<T> node) {
        consumerNode.lazySet(node);
    }

    /**
     * {@inheritDoc} <br>
     * <p>
     * 实现说明：<br>
     * producerNode 与 consumerNode 相同时队列为空。
     * 亦可观察 producerNode.value 是否为 null（仅 consumer 哨兵允许 null 值）。
     */
    @Override
    public boolean isEmpty() {
        return lvConsumerNode() == lvProducerNode();
    }

    static final class LinkedQueueNode<E> extends AtomicReference<LinkedQueueNode<E>> {

        @Serial
        private static final long serialVersionUID = 2404266111789071508L;

        private E value;

        LinkedQueueNode() {
        }

        LinkedQueueNode(E val) {
            spValue(val);
        }
        /**
         * 读取当前 value 并将节点内引用置 null。
         *
         * @return 元素值
         */
        public E getAndNullValue() {
            E temp = lpValue();
            spValue(null);
            return temp;
        }

        public E lpValue() {
            return value;
        }

        public void spValue(E newValue) {
            value = newValue;
        }

        public void soNext(LinkedQueueNode<E> n) {
            lazySet(n);
        }

        public LinkedQueueNode<E> lvNext() {
            return get();
        }
    }
}
