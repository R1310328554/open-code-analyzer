/*
 * Copyright 2019 The Netty Project
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
package io.netty.handler.codec.http2;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * 带容量上限的 {@link Queue} 装饰器：{@link #offer} 在达到 {@code maxCapacity} 时返回 false 而非扩容。
 * <p>用于 HTTP/2 内部缓冲，防止无界队列耗尽内存。
 */
final class MaxCapacityQueue<E> implements Queue<E> {
    private final Queue<E> queue;
    /** 允许的最大元素个数（含当前已在队列中的元素）。 */
    private final int maxCapacity;

    MaxCapacityQueue(Queue<E> queue, int maxCapacity) {
        this.queue = queue;
        this.maxCapacity = maxCapacity;
    }

    @Override
    public boolean add(E element) {
        if (offer(element)) {
            return true;
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean offer(E element) {
        // 已达上限则拒绝入队（非阻塞）
        if (maxCapacity <= queue.size()) {
            return false;
        }
        return queue.offer(element);
    }

    @Override
    public E remove() {
        return queue.remove();
    }

    @Override
    public E poll() {
        return queue.poll();
    }

    @Override
    public E element() {
        return queue.element();
    }

    @Override
    public E peek() {
        return queue.peek();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return queue.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return queue.iterator();
    }

    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return queue.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return queue.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return queue.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (maxCapacity >= size() + c.size()) {
            return queue.addAll(c);
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return queue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return queue.retainAll(c);
    }

    @Override
    public void clear() {
        queue.clear();
    }
}
