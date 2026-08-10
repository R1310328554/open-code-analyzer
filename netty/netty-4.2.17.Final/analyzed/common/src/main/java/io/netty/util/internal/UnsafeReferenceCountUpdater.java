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
package io.netty.util.internal;

import io.netty.util.ReferenceCounted;

/**
 * 基于 Unsafe/PlatformDependent 原子操作的引用计数更新器。
 */
public abstract class UnsafeReferenceCountUpdater<T extends ReferenceCounted> extends ReferenceCountUpdater<T> {

    protected UnsafeReferenceCountUpdater() {
    }

    /** 引用计数字段在对象内的字节偏移。 */
    protected abstract long refCntFieldOffset();

    /** 构造后安全写入初始 raw 引用计数。 */
    @Override
    protected final void safeInitializeRawRefCnt(T refCntObj, int value) {
        PlatformDependent.safeConstructPutInt(refCntObj, refCntFieldOffset(), value);
    }

    /** 原子加减 raw 引用计数。 */
    @Override
    protected final int getAndAddRawRefCnt(T refCntObj, int increment) {
        return PlatformDependent.getAndAddInt(refCntObj, refCntFieldOffset(), increment);
    }

    /** 普通读 raw 引用计数。 */
    @Override
    protected final int getRawRefCnt(T refCnt) {
        return PlatformDependent.getInt(refCnt, refCntFieldOffset());
    }

    /** acquire 语义读 raw 引用计数。 */
    @Override
    protected final int getAcquireRawRefCnt(T refCnt) {
        return PlatformDependent.getVolatileInt(refCnt, refCntFieldOffset());
    }

    /** release 语义写 raw 引用计数。 */
    @Override
    protected final void setReleaseRawRefCnt(T refCnt, int value) {
        PlatformDependent.putOrderedInt(refCnt, refCntFieldOffset(), value);
    }

    /** CAS 更新 raw 引用计数。 */
    @Override
    protected final boolean casRawRefCnt(T refCnt, int expected, int value) {
        return PlatformDependent.compareAndSwapInt(refCnt, refCntFieldOffset(), expected, value);
    }
}
