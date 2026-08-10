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

package io.netty.util.internal;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The internal data structure that stores the thread-local variables for Netty and all {@link FastThreadLocal}s.
 * Note that this class is for internal use only and is subject to change at any time.  Use {@link FastThreadLocal}
 * unless you know what you are doing.
 *
 * <p>Netty 与所有 {@link FastThreadLocal} 的线程本地存储后端。
 * {@link FastThreadLocalThread} 将 map 挂在线程上；普通线程走慢路径 {@link ThreadLocal}。
 * 含索引表、StringBuilder/编解码器缓存、ArrayList 复用等。</p>
 */
public final class InternalThreadLocalMap extends UnpaddedInternalThreadLocalMap {
    /** 非 FastThreadLocalThread 使用的 ThreadLocal 后备。 */
    private static final ThreadLocal<InternalThreadLocalMap> slowThreadLocalMap =
            new ThreadLocal<InternalThreadLocalMap>();
    /** 为每个 FastThreadLocal 分配唯一索引。 */
    private static final AtomicInteger nextIndex = new AtomicInteger();
    // Internal use only.
    /** 记录待 remove 的 FastThreadLocal 集合的索引槽位。 */
    public static final int VARIABLES_TO_REMOVE_INDEX = nextVariableIndex();

    private static final int DEFAULT_ARRAY_LIST_INITIAL_CAPACITY = 8;
    private static final int ARRAY_LIST_CAPACITY_EXPAND_THRESHOLD = 1 << 30;
    // Reference: https://hg.openjdk.java.net/jdk8/jdk8/jdk/file/tip/src/share/classes/java/util/ArrayList.java#l229
    private static final int ARRAY_LIST_CAPACITY_MAX_SIZE = Integer.MAX_VALUE - 8;

    private static final int HANDLER_SHARABLE_CACHE_INITIAL_CAPACITY = 4;
    private static final int INDEXED_VARIABLE_TABLE_INITIAL_SIZE = 32;

    private static final int STRING_BUILDER_INITIAL_SIZE;
    private static final int STRING_BUILDER_MAX_SIZE;

    private static final InternalLogger logger;
    /** Internal use only. 索引槽未设置时的哨兵值。 */
    public static final Object UNSET = new Object();

    /** Used by {@link FastThreadLocal} 按索引存取的变量表。 */
    private Object[] indexedVariables;

    // Core thread-locals
    /** Future 监听器嵌套深度（防重入）。 */
    private int futureListenerStackDepth;
    /** LocalChannel 读栈深度。 */
    private int localChannelReaderStackDepth;
    /** Handler @Sharable 判定缓存。 */
    private Map<Class<?>, Boolean> handlerSharableCache;
    /** TypeParameterMatcher.get 缓存。 */
    private Map<Class<?>, TypeParameterMatcher> typeParameterMatcherGetCache;
    /** TypeParameterMatcher.find 缓存（按方法名）。 */
    private Map<Class<?>, Map<String, TypeParameterMatcher>> typeParameterMatcherFindCache;

    // String-related thread-locals
    /** 线程本地 StringBuilder 复用。 */
    private StringBuilder stringBuilder;
    private Map<Charset, CharsetEncoder> charsetEncoderCache;
    private Map<Charset, CharsetDecoder> charsetDecoderCache;

    // ArrayList-related thread-locals
    /** 线程本地 ArrayList 复用。 */
    private ArrayList<Object> arrayList;

    /** 已弃用：ObjectCleaner 相关标志位。 */
    private BitSet cleanerFlags;

    /** @deprecated These padding fields will be removed in the future. 伪共享填充字段。 */
    public long rp1, rp2, rp3, rp4, rp5, rp6, rp7, rp8;

    static {
        STRING_BUILDER_INITIAL_SIZE =
                SystemPropertyUtil.getInt("io.netty.threadLocalMap.stringBuilder.initialSize", 1024);
        STRING_BUILDER_MAX_SIZE =
                SystemPropertyUtil.getInt("io.netty.threadLocalMap.stringBuilder.maxSize", 1024 * 4);

        // Ensure the InternalLogger is initialized as last field in this class as InternalThreadLocalMap might be used
        // by the InternalLogger itself. For this its important that all the other static fields are correctly
        // initialized.
        //
        // See https://github.com/netty/netty/issues/12931.
        // logger 必须最后初始化，避免静态初始化顺序问题
        logger = InternalLoggerFactory.getInstance(InternalThreadLocalMap.class);
        logger.debug("-Dio.netty.threadLocalMap.stringBuilder.initialSize: {}", STRING_BUILDER_INITIAL_SIZE);
        logger.debug("-Dio.netty.threadLocalMap.stringBuilder.maxSize: {}", STRING_BUILDER_MAX_SIZE);
    }

    /** 若当前线程已绑定 map 则返回，否则 null（不创建）。 */
    public static InternalThreadLocalMap getIfSet() {
        Thread thread = Thread.currentThread();
        if (thread instanceof FastThreadLocalThread) {
            return ((FastThreadLocalThread) thread).threadLocalMap();
        }
        return slowThreadLocalMap.get();
    }

    /** 获取或懒创建当前线程的 InternalThreadLocalMap。 */
    public static InternalThreadLocalMap get() {
        Thread thread = Thread.currentThread();
        if (thread instanceof FastThreadLocalThread) {
            return fastGet((FastThreadLocalThread) thread);
        } else {
            return slowGet();
        }
    }

    /** FastThreadLocalThread 快路径：map 为空则新建并挂到线程。 */
    private static InternalThreadLocalMap fastGet(FastThreadLocalThread thread) {
        InternalThreadLocalMap threadLocalMap = thread.threadLocalMap();
        if (threadLocalMap == null) {
            thread.setThreadLocalMap(threadLocalMap = new InternalThreadLocalMap());
        }
        return threadLocalMap;
    }

    /** 普通 Thread 慢路径。 */
    private static InternalThreadLocalMap slowGet() {
        InternalThreadLocalMap ret = slowThreadLocalMap.get();
        if (ret == null) {
            ret = new InternalThreadLocalMap();
            slowThreadLocalMap.set(ret);
        }
        return ret;
    }

    /** 解除当前线程与 map 的绑定。 */
    public static void remove() {
        Thread thread = Thread.currentThread();
        if (thread instanceof FastThreadLocalThread) {
            ((FastThreadLocalThread) thread).setThreadLocalMap(null);
        } else {
            slowThreadLocalMap.remove();
        }
    }

    /** 仅清除 slow ThreadLocal（测试/特殊场景）。 */
    public static void destroy() {
        slowThreadLocalMap.remove();
    }

    /** 分配下一个 FastThreadLocal 索引；过多时抛 IllegalStateException。 */
    public static int nextVariableIndex() {
        int index = nextIndex.getAndIncrement();
        if (index >= ARRAY_LIST_CAPACITY_MAX_SIZE || index < 0) {
            nextIndex.set(ARRAY_LIST_CAPACITY_MAX_SIZE);
            throw new IllegalStateException("too many thread-local indexed variables");
        }
        return index;
    }

    /** 返回已分配的最大索引（nextIndex - 1）。 */
    public static int lastVariableIndex() {
        return nextIndex.get() - 1;
    }

    private InternalThreadLocalMap() {
        indexedVariables = newIndexedVariableTable();
    }

    /** 创建初始索引表，全部填 UNSET。 */
    private static Object[] newIndexedVariableTable() {
        Object[] array = new Object[INDEXED_VARIABLE_TABLE_INITIAL_SIZE];
        Arrays.fill(array, UNSET);
        return array;
    }

    /** 统计当前 map 中非空/已设 thread-local 条目数量（近似）。 */
    public int size() {
        int count = 0;

        if (futureListenerStackDepth != 0) {
            count ++;
        }
        if (localChannelReaderStackDepth != 0) {
            count ++;
        }
        if (handlerSharableCache != null) {
            count ++;
        }
        if (typeParameterMatcherGetCache != null) {
            count ++;
        }
        if (typeParameterMatcherFindCache != null) {
            count ++;
        }
        if (stringBuilder != null) {
            count ++;
        }
        if (charsetEncoderCache != null) {
            count ++;
        }
        if (charsetDecoderCache != null) {
            count ++;
        }
        if (arrayList != null) {
            count ++;
        }

        Object v = indexedVariable(VARIABLES_TO_REMOVE_INDEX);
        if (v != null && v != InternalThreadLocalMap.UNSET) {
            @SuppressWarnings("unchecked")
            Set<FastThreadLocal<?>> variablesToRemove = (Set<FastThreadLocal<?>>) v;
            count += variablesToRemove.size();
        }

        return count;
    }

    /**
     * 获取已清空、容量受控的 StringBuilder；过大时 trim 回初始规模。
     */
    public StringBuilder stringBuilder() {
        StringBuilder sb = stringBuilder;
        if (sb == null) {
            return stringBuilder = new StringBuilder(STRING_BUILDER_INITIAL_SIZE);
        }
        if (sb.capacity() > STRING_BUILDER_MAX_SIZE) {
            sb.setLength(STRING_BUILDER_INITIAL_SIZE);
            sb.trimToSize();
        }
        sb.setLength(0);
        return sb;
    }

    /** 按 Charset 缓存 CharsetEncoder（IdentityHashMap 懒创建）。 */
    public Map<Charset, CharsetEncoder> charsetEncoderCache() {
        Map<Charset, CharsetEncoder> cache = charsetEncoderCache;
        if (cache == null) {
            charsetEncoderCache = cache = new IdentityHashMap<>();
        }
        return cache;
    }

    /** 按 Charset 缓存 CharsetDecoder。 */
    public Map<Charset, CharsetDecoder> charsetDecoderCache() {
        Map<Charset, CharsetDecoder> cache = charsetDecoderCache;
        if (cache == null) {
            charsetDecoderCache = cache = new IdentityHashMap<>();
        }
        return cache;
    }

    /** 默认初始容量的可复用 ArrayList。 */
    public <E> ArrayList<E> arrayList() {
        return arrayList(DEFAULT_ARRAY_LIST_INITIAL_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    /** 清空并 ensureCapacity 后的 ArrayList，供本线程复用。 */
    public <E> ArrayList<E> arrayList(int minCapacity) {
        ArrayList<E> list = (ArrayList<E>) arrayList;
        if (list == null) {
            arrayList = new ArrayList<>(minCapacity);
            return (ArrayList<E>) arrayList;
        }
        list.clear();
        list.ensureCapacity(minCapacity);
        return list;
    }

    public int futureListenerStackDepth() {
        return futureListenerStackDepth;
    }

    public void setFutureListenerStackDepth(int futureListenerStackDepth) {
        this.futureListenerStackDepth = futureListenerStackDepth;
    }

    /**
     * @deprecated Use {@link java.util.concurrent.ThreadLocalRandom#current()} instead.
     */
    @Deprecated
    public ThreadLocalRandom random() {
        return new ThreadLocalRandom();
    }

    public Map<Class<?>, TypeParameterMatcher> typeParameterMatcherGetCache() {
        Map<Class<?>, TypeParameterMatcher> cache = typeParameterMatcherGetCache;
        if (cache == null) {
            typeParameterMatcherGetCache = cache = new IdentityHashMap<>();
        }
        return cache;
    }

    public Map<Class<?>, Map<String, TypeParameterMatcher>> typeParameterMatcherFindCache() {
        Map<Class<?>, Map<String, TypeParameterMatcher>> cache = typeParameterMatcherFindCache;
        if (cache == null) {
            typeParameterMatcherFindCache = cache = new IdentityHashMap<>();
        }
        return cache;
    }

    @Deprecated
    public IntegerHolder counterHashCode() {
        return new IntegerHolder();
    }

    @Deprecated
    public void setCounterHashCode(IntegerHolder counterHashCode) {
        // No-op.
    }

    /** WeakHashMap 缓存 Handler 是否 @Sharable。 */
    public Map<Class<?>, Boolean> handlerSharableCache() {
        Map<Class<?>, Boolean> cache = handlerSharableCache;
        if (cache == null) {
            // Start with small capacity to keep memory overhead as low as possible.
            handlerSharableCache = cache = new WeakHashMap<>(HANDLER_SHARABLE_CACHE_INITIAL_CAPACITY);
        }
        return cache;
    }

    public int localChannelReaderStackDepth() {
        return localChannelReaderStackDepth;
    }

    public void setLocalChannelReaderStackDepth(int localChannelReaderStackDepth) {
        this.localChannelReaderStackDepth = localChannelReaderStackDepth;
    }

    /** 按索引读取 FastThreadLocal 变量；越界返回 UNSET。 */
    public Object indexedVariable(int index) {
        Object[] lookup = indexedVariables;
        return index < lookup.length? lookup[index] : UNSET;
    }

    /**
     * @return {@code true} if and only if a new thread-local variable has been created
     *
     * <p>写入索引槽；若原值为 UNSET 表示首次创建，返回 true。</p>
     */
    public boolean setIndexedVariable(int index, Object value) {
        return getAndSetIndexedVariable(index, value) == UNSET;
    }

    /**
     * @return {@link InternalThreadLocalMap#UNSET} if and only if a new thread-local variable has been created.
     *
     * <p>设置并返回旧值；扩容时旧值视为 UNSET。</p>
     */
    public Object getAndSetIndexedVariable(int index, Object value) {
        Object[] lookup = indexedVariables;
        if (index < lookup.length) {
            Object oldValue = lookup[index];
            lookup[index] = value;
            return oldValue;
        }
        expandIndexedVariableTableAndSet(index, value);
        return UNSET;
    }

    /** 按 ArrayList 策略扩容索引表并写入。 */
    private void expandIndexedVariableTableAndSet(int index, Object value) {
        Object[] oldArray = indexedVariables;
        final int oldCapacity = oldArray.length;
        int newCapacity;
        if (index < ARRAY_LIST_CAPACITY_EXPAND_THRESHOLD) {
            newCapacity = index;
            newCapacity |= newCapacity >>>  1;
            newCapacity |= newCapacity >>>  2;
            newCapacity |= newCapacity >>>  4;
            newCapacity |= newCapacity >>>  8;
            newCapacity |= newCapacity >>> 16;
            newCapacity ++;
        } else {
            newCapacity = ARRAY_LIST_CAPACITY_MAX_SIZE;
        }

        Object[] newArray = Arrays.copyOf(oldArray, newCapacity);
        Arrays.fill(newArray, oldCapacity, newArray.length, UNSET);
        newArray[index] = value;
        indexedVariables = newArray;
    }

    /** 清除索引槽为 UNSET 并返回原值。 */
    public Object removeIndexedVariable(int index) {
        Object[] lookup = indexedVariables;
        if (index < lookup.length) {
            Object v = lookup[index];
            lookup[index] = UNSET;
            return v;
        } else {
            return UNSET;
        }
    }

    /** 索引槽是否已设置（非 UNSET）。 */
    public boolean isIndexedVariableSet(int index) {
        Object[] lookup = indexedVariables;
        return index < lookup.length && lookup[index] != UNSET;
    }

    @Deprecated
    public boolean isCleanerFlagSet(int index) {
        return cleanerFlags != null && cleanerFlags.get(index);
    }

    @Deprecated
    public void setCleanerFlag(int index) {
        if (cleanerFlags == null) {
            cleanerFlags = new BitSet();
        }
        cleanerFlags.set(index);
    }
}
