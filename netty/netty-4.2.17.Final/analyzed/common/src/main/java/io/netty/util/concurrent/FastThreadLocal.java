/*
 * Copyright 2014 The Netty Project
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

import io.netty.util.internal.InternalThreadLocalMap;
import io.netty.util.internal.PlatformDependent;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import static io.netty.util.internal.InternalThreadLocalMap.UNSET;
import static io.netty.util.internal.InternalThreadLocalMap.VARIABLES_TO_REMOVE_INDEX;

/**
 * A special variant of {@link ThreadLocal} that yields higher access performance when accessed from a
 * {@link FastThreadLocalThread}.
 * <p>
 * Internally, a {@link FastThreadLocal} uses a constant index in an array, instead of using hash code and hash table,
 * to look for a variable.  Although seemingly very subtle, it yields slight performance advantage over using a hash
 * table, and it is useful when accessed frequently.
 * </p><p>
 * To take advantage of this thread-local variable, your thread must be a {@link FastThreadLocalThread} or its subtype.
 * By default, all threads created by {@link DefaultThreadFactory} are {@link FastThreadLocalThread} due to this reason.
 * </p><p>
 * Note that the fast path is only possible on threads that extend {@link FastThreadLocalThread}, because it requires
 * a special field to store the necessary state.  An access by any other kind of thread falls back to a regular
 * {@link ThreadLocal}.
 * </p>
 *
 * @param <V> the type of the thread-local variable
 * @see ThreadLocal
 *
 * <p>{@link ThreadLocal} 的高性能变体：在 {@link FastThreadLocalThread} 上通过
 * {@link InternalThreadLocalMap} 的固定数组下标直接访问，避免哈希表。
 * 非 FastThreadLocalThread 回退到普通 ThreadLocal。{@link DefaultThreadFactory} 创建的线程默认可走快路径。</p>
 */
public class FastThreadLocal<V> {

    /**
     * Removes all {@link FastThreadLocal} variables bound to the current thread.  This operation is useful when you
     * are in a container environment, and you don't want to leave the thread local variables in the threads you do not
     * manage.
     *
     * <p>移除当前线程全部 FastThreadLocal 并释放 map；线程池/容器场景防泄漏。</p>
     */
    public static void removeAll() {
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
        if (threadLocalMap == null) {
            return;
        }

        try {
            Object v = threadLocalMap.indexedVariable(VARIABLES_TO_REMOVE_INDEX);
            if (v != null && v != InternalThreadLocalMap.UNSET) {
                @SuppressWarnings("unchecked")
                Set<FastThreadLocal<?>> variablesToRemove = (Set<FastThreadLocal<?>>) v;
                FastThreadLocal<?>[] variablesToRemoveArray =
                        variablesToRemove.toArray(new FastThreadLocal[0]);
                for (FastThreadLocal<?> tlv: variablesToRemoveArray) {
                    tlv.remove(threadLocalMap);
                }
            }
        } finally {
            InternalThreadLocalMap.remove();
        }
    }

    /**
     * Returns the number of thread local variables bound to the current thread.
     *
     * <p>当前线程已绑定的变量个数。</p>
     */
    public static int size() {
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
        if (threadLocalMap == null) {
            return 0;
        } else {
            return threadLocalMap.size();
        }
    }

    /**
     * Destroys the data structure that keeps all {@link FastThreadLocal} variables accessed from
     * non-{@link FastThreadLocalThread}s.  This operation is useful when you are in a container environment, and you
     * do not want to leave the thread local variables in the threads you do not manage.  Call this method when your
     * application is being unloaded from the container.
     *
     * <p>销毁慢路径 ThreadLocal 全局结构。</p>
     */
    public static void destroy() {
        InternalThreadLocalMap.destroy();
    }

    @SuppressWarnings("unchecked")
    /** 维护 VARIABLES_TO_REMOVE_INDEX 处的 IdentityHashSet，供 removeAll 遍历。 */
    private static void addToVariablesToRemove(InternalThreadLocalMap threadLocalMap, FastThreadLocal<?> variable) {
        Object v = threadLocalMap.indexedVariable(VARIABLES_TO_REMOVE_INDEX);
        Set<FastThreadLocal<?>> variablesToRemove;
        if (v == InternalThreadLocalMap.UNSET || v == null) {
            variablesToRemove = Collections.newSetFromMap(new IdentityHashMap<FastThreadLocal<?>, Boolean>());
            threadLocalMap.setIndexedVariable(VARIABLES_TO_REMOVE_INDEX, variablesToRemove);
        } else {
            variablesToRemove = (Set<FastThreadLocal<?>>) v;
        }

        variablesToRemove.add(variable);
    }

    /** 从 VARIABLES_TO_REMOVE 集合移除本变量。 */
    private static void removeFromVariablesToRemove(
            InternalThreadLocalMap threadLocalMap, FastThreadLocal<?> variable) {

        Object v = threadLocalMap.indexedVariable(VARIABLES_TO_REMOVE_INDEX);

        if (v == InternalThreadLocalMap.UNSET || v == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Set<FastThreadLocal<?>> variablesToRemove = (Set<FastThreadLocal<?>>) v;
        variablesToRemove.remove(variable);
    }

    /** 在 InternalThreadLocalMap 数组中的固定下标。 */
    private final int index;

    /** 构造时向全局分配下一个 variable index。 */
    public FastThreadLocal() {
        index = InternalThreadLocalMap.nextVariableIndex();
    }

    /**
     * Returns the current value for the current thread
     *
     * <p>获取值；未初始化则 lazy init。</p>
     */
    @SuppressWarnings("unchecked")
    public final V get() {
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
        Object v = threadLocalMap.indexedVariable(index);
        if (v != InternalThreadLocalMap.UNSET) {
            return (V) v;
        }

        return initialize(threadLocalMap);
    }

    /**
     * Returns the current value for the current thread if it exists, {@code null} otherwise.
     */
    @SuppressWarnings("unchecked")
    public final V getIfExists() {
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
        if (threadLocalMap != null) {
            Object v = threadLocalMap.indexedVariable(index);
            if (v != InternalThreadLocalMap.UNSET) {
                return (V) v;
            }
        }
        return null;
    }

    /**
     * Returns the current value for the specified thread local map.
     * The specified thread local map must be for the current thread.
     *
     * <p>使用调用方提供的 map（须属当前线程）。</p>
     */
    @SuppressWarnings("unchecked")
    public final V get(InternalThreadLocalMap threadLocalMap) {
        Object v = threadLocalMap.indexedVariable(index);
        if (v != InternalThreadLocalMap.UNSET) {
            return (V) v;
        }

        return initialize(threadLocalMap);
    }

    /** 首次访问：initialValue → setIndexedVariable → 登记 VARIABLES_TO_REMOVE。 */
    private V initialize(InternalThreadLocalMap threadLocalMap) {
        V v = null;
        try {
            v = initialValue();
            if (v == InternalThreadLocalMap.UNSET) {
                throw new IllegalArgumentException("InternalThreadLocalMap.UNSET can not be initial value.");
            }
        } catch (Exception e) {
            PlatformDependent.throwException(e);
        }

        threadLocalMap.setIndexedVariable(index, v);
        addToVariablesToRemove(threadLocalMap, this);
        return v;
    }

    /**
     * Set the value for the current thread.
     *
     * <p>写入当前线程；UNSET 视为 remove。</p>
     */
    public final void set(V value) {
        getAndSet(value);
    }

    /**
     * Set the value for the specified thread local map. The specified thread local map must be for the current thread.
     *
     * <p>向指定 map 写入。</p>
     */
    public final void set(InternalThreadLocalMap threadLocalMap, V value) {
        getAndSet(threadLocalMap, value);
    }

    /**
     * Set the value for the current thread and returns the old value.
     *
     * <p>原子替换并返回旧值。</p>
     */
    public V getAndSet(V value) {
        if (value != InternalThreadLocalMap.UNSET) {
            InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
            return setKnownNotUnset(threadLocalMap, value);
        }
        return removeAndGet(InternalThreadLocalMap.getIfSet());
    }

    /**
     * Set the value for the specified thread local map. The specified thread local map must be for the current thread.
     */
    public V getAndSet(InternalThreadLocalMap threadLocalMap, V value) {
        if (value != InternalThreadLocalMap.UNSET) {
            return setKnownNotUnset(threadLocalMap, value);
        }
        return removeAndGet(threadLocalMap);
    }

    /**
     * @see InternalThreadLocalMap#setIndexedVariable(int, Object).
     *
     * <p>内部：写入已知非 UNSET 的值。</p>
     */
    @SuppressWarnings("unchecked")
    private V setKnownNotUnset(InternalThreadLocalMap threadLocalMap, V value) {
        V old = (V) threadLocalMap.getAndSetIndexedVariable(index, value);
        if (old == UNSET) {
            addToVariablesToRemove(threadLocalMap, this);
            return null;
        }
        return old;
    }

    /**
     * Returns {@code true} if and only if this thread-local variable is set.
     *
     * <p>是否已在当前线程初始化。</p>
     */
    public final boolean isSet() {
        return isSet(InternalThreadLocalMap.getIfSet());
    }

    /**
     * Returns {@code true} if and only if this thread-local variable is set.
     * The specified thread local map must be for the current thread.
     *
     * <p>在指定 map 上检查是否已 set。</p>
     */
    public final boolean isSet(InternalThreadLocalMap threadLocalMap) {
        return threadLocalMap != null && threadLocalMap.isIndexedVariableSet(index);
    }
    /**
     * Sets the value to uninitialized for the specified thread local map and returns the old value.
     * After this, any subsequent call to get() will trigger a new call to initialValue().
     *
     * <p>清除当前线程上的绑定。</p>
     */
    public final void remove() {
        remove(InternalThreadLocalMap.getIfSet());
    }

    /**
     * Sets the value to uninitialized for the specified thread local map.
     * After this, any subsequent call to get() will trigger a new call to initialValue().
     * The specified thread local map must be for the current thread.
     */
    @SuppressWarnings("unchecked")
    public final void remove(InternalThreadLocalMap threadLocalMap) {
        removeAndGet(threadLocalMap);
    }

    /**
     * Sets the value to uninitialized for the specified thread local map.
     * After this, any subsequent call to get() will trigger a new call to initialValue().
     * The specified thread local map must be for the current thread.
     */
    @SuppressWarnings("unchecked")
    /** remove 核心：清 slot、更新集合、调用 onRemoval。 */
    private V removeAndGet(InternalThreadLocalMap threadLocalMap) {
        if (threadLocalMap == null) {
            return null;
        }

        Object v = threadLocalMap.removeIndexedVariable(index);
        if (v != InternalThreadLocalMap.UNSET) {
            removeFromVariablesToRemove(threadLocalMap, this);
            try {
                onRemoval((V) v);
            } catch (Exception e) {
                PlatformDependent.throwException(e);
            }
            return (V) v;
        }
        return null;
    }

    /**
     * Returns the initial value for this thread-local variable.
     */
    protected V initialValue() throws Exception {
        return null;
    }

    /**
     * Invoked when this thread local variable is removed by {@link #remove()}. Be aware that {@link #remove()}
     * is not guaranteed to be called when the `Thread` completes which means you can not depend on this for
     * cleanup of the resources in the case of `Thread` completion.
     *
     * <p>remove 时钩子；线程结束不保证调用。</p>
     */
    protected void onRemoval(@SuppressWarnings("UnusedParameters") V value) throws Exception { }
}
