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
package io.netty.util.internal;

import static io.netty.util.internal.ObjectUtil.checkPositive;

import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ReferenceCounted;

/**
 * Common logic for {@link ReferenceCounted} implementations
 * @deprecated Instead of extending this class, prefer instead to include a {@link RefCnt} field and delegate to that.
 * This approach has better compatibility with Graal Native Image.
 *
 * <p>{@link ReferenceCounted} 引用计数通用逻辑：偶数 raw 值表示存活引用数，奇数表示已释放。
 * 推荐改用组合 {@link RefCnt} 字段以更好支持 Graal Native Image。</p>
 */
@Deprecated
public abstract class ReferenceCountUpdater<T extends ReferenceCounted> {
    /*
     * 实现说明：
     * int 字段编码：偶数 => 真实引用计数为 (refCnt >>> 1)；奇数 => 已释放（计数 0）。
     */

    protected ReferenceCountUpdater() {
    }

    protected abstract void safeInitializeRawRefCnt(T refCntObj, int value);

    protected abstract int getAndAddRawRefCnt(T refCntObj, int increment);

    protected abstract int getRawRefCnt(T refCnt);

    protected abstract int getAcquireRawRefCnt(T refCnt);

    protected abstract void setReleaseRawRefCnt(T refCnt, int value);

    protected abstract boolean casRawRefCnt(T refCnt, int expected, int value);

    /** 初始 raw 值 2，对应引用计数 1。 */
    public final int initialValue() {
        return 2;
    }

    /** 构造后安全初始化引用计数字段。 */
    public final void setInitialValue(T instance) {
        safeInitializeRawRefCnt(instance, initialValue());
    }

    private static int realRefCnt(int rawCnt) {
        return rawCnt >>> 1;
    }

    /** 带 acquire 语义读取当前引用计数。 */
    public final int refCnt(T instance) {
        return realRefCnt(getAcquireRawRefCnt(instance));
    }

    /** 非 volatile 快速判断是否仍存活（raw==2 或偶数）。 */
    public final boolean isLiveNonVolatile(T instance) {
        final int rawCnt = getRawRefCnt(instance);
        if (rawCnt == 2) {
            return true;
        }
        return (rawCnt & 1) == 0;
    }

    /**
     * An unsafe operation that sets the reference count directly
     *
     * <p>直接设置引用计数，仅供内部/测试使用。</p>
     */
    public final void setRefCnt(T instance, int refCnt) {
        int rawRefCnt = refCnt > 0 ? refCnt << 1 : 1; // overflow OK here
        setReleaseRawRefCnt(instance, rawRefCnt);
    }

    /**
     * Resets the reference count to 1
     *
     * <p>重置为 1；须在无并发访问的静默状态调用。</p>
     */
    public final void resetRefCnt(T instance) {
        // no need of a volatile set, it should happen in a quiescent state
        setReleaseRawRefCnt(instance, initialValue());
    }

    public final T retain(T instance) {
        return retain0(instance, 2);
    }

    public final T retain(T instance, int increment) {
        return retain0(instance, checkPositive(increment, "increment") << 1);
    }

    private T retain0(T instance, int increment) {
        int oldRef = getAndAddRawRefCnt(instance, increment);
        // oldRef & 0x80000001 表示已释放（奇数）或溢出
        // NOTE: we're optimizing for inlined and constant folded increment here -> which will make
        // Integer.MAX_VALUE - increment to be computed at compile time
        if ((oldRef & 0x80000001) != 0 || oldRef > Integer.MAX_VALUE - increment) {
            getAndAddRawRefCnt(instance, -increment);
            throw new IllegalReferenceCountException(0, increment >>> 1);
        }
        return instance;
    }

    public final boolean release(T instance) {
        return release0(instance, 2);
    }

    public final boolean release(T instance, int decrement) {
        return release0(instance, checkPositive(decrement, "decrement") << 1);
    }

    private boolean release0(final T instance, final int decrement) {
        int curr, next;
        do {
            curr = getRawRefCnt(instance);
            if (curr == decrement) {
                next = 1;
            } else {
                if (curr < decrement || (curr & 1) == 1) {
                    throwIllegalRefCountOnRelease(decrement, curr);
                }
                next = curr - decrement;
            }
        } while (!casRawRefCnt(instance, curr, next));
        return (next & 1) == 1;
    }

    private static void throwIllegalRefCountOnRelease(int decrement, int curr) {
        throw new IllegalReferenceCountException(curr >>> 1, -(decrement >>> 1));
    }

    /** 引用计数底层原子更新实现类型。 */
    public enum UpdaterType {
        /** sun.misc.Unsafe 字段偏移 + CAS。 */
        Unsafe,
        /** Java 9+ VarHandle。 */
        VarHandle,
        /** AtomicIntegerFieldUpdater 回退。 */
        Atomic
    }

    /** 根据 Unsafe 偏移与 VarHandle 可用性选择 Updater 类型。 */
    public static <T extends ReferenceCounted> UpdaterType updaterTypeOf(Class<T> clz, String fieldName) {
        long fieldOffset = getUnsafeOffset(clz, fieldName);
        if (fieldOffset >= 0) {
            return UpdaterType.Unsafe;
        }
        if (PlatformDependent.hasVarHandle()) {
            return UpdaterType.VarHandle;
        }
        return UpdaterType.Atomic;
    }

    /** 获取 refCnt 字段的 Unsafe 偏移，不可用时返回 -1。 */
    public static long getUnsafeOffset(Class<? extends ReferenceCounted> clz, String fieldName) {
        try {
            if (PlatformDependent.hasUnsafe()) {
                return PlatformDependent.objectFieldOffset(clz.getDeclaredField(fieldName));
            }
        } catch (Throwable ignore) {
            // fall-back
        }
        return -1;
    }
}
