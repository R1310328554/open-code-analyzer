/*
 * Copyright 2013 The Netty Project
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

import java.util.Arrays;

/**
 * {@link DefaultPromise} 内部使用的可变长度监听器容器。
 *
 * <p>当 Promise 上注册的监听器超过 1 个时，由单个 {@link GenericFutureListener} 升级为该数组结构；
 * 支持动态扩容、移除，并单独维护渐进式监听器（{@link GenericProgressiveFutureListener}）计数，
 * 以便 {@link DefaultPromise#notifyProgressiveListeners} 快速筛选。</p>
 */
final class DefaultFutureListeners {

    /** 监听器数组，容量按需翻倍扩容。 */
    private GenericFutureListener<? extends Future<?>>[] listeners;
    /** 当前有效监听器数量。 */
    private int size;
    /** 渐进式监听器数量，用于 progressive 通知路径优化。 */
    private int progressiveSize; // the number of progressive listeners

    /**
     * 以两个监听器初始化容器（由 {@link DefaultPromise} 在第二个监听器加入时创建）。
     */
    @SuppressWarnings("unchecked")
    DefaultFutureListeners(
            GenericFutureListener<? extends Future<?>> first, GenericFutureListener<? extends Future<?>> second) {
        listeners = new GenericFutureListener[2];
        listeners[0] = first;
        listeners[1] = second;
        size = 2;
        if (first instanceof GenericProgressiveFutureListener) {
            progressiveSize ++;
        }
        if (second instanceof GenericProgressiveFutureListener) {
            progressiveSize ++;
        }
    }

    /**
     * 追加监听器；数组满时容量翻倍。
     */
    public void add(GenericFutureListener<? extends Future<?>> l) {
        GenericFutureListener<? extends Future<?>>[] listeners = this.listeners;
        final int size = this.size;
        if (size == listeners.length) {
            this.listeners = listeners = Arrays.copyOf(listeners, size << 1);
        }
        listeners[size] = l;
        this.size = size + 1;

        if (l instanceof GenericProgressiveFutureListener) {
            progressiveSize ++;
        }
    }

    /**
     * 移除首个匹配的监听器（数组元素前移，尾部置 null）。
     */
    public void remove(GenericFutureListener<? extends Future<?>> l) {
        final GenericFutureListener<? extends Future<?>>[] listeners = this.listeners;
        int size = this.size;
        for (int i = 0; i < size; i ++) {
            if (listeners[i] == l) {
                int listenersToMove = size - i - 1;
                if (listenersToMove > 0) {
                    System.arraycopy(listeners, i + 1, listeners, i, listenersToMove);
                }
                listeners[-- size] = null;
                this.size = size;

                if (l instanceof GenericProgressiveFutureListener) {
                    progressiveSize --;
                }
                return;
            }
        }
    }

    /** 返回底层监听器数组（长度可能大于 {@link #size()}）。 */
    public GenericFutureListener<? extends Future<?>>[] listeners() {
        return listeners;
    }

    /** 当前监听器个数。 */
    public int size() {
        return size;
    }

    /** 渐进式监听器个数。 */
    public int progressiveSize() {
        return progressiveSize;
    }
}
