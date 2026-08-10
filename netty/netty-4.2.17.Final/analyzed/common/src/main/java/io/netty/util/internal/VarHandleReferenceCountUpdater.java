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
import java.lang.invoke.VarHandle;

/**
 * 基于 VarHandle 的引用计数更新器，替代 Unsafe 路径。
 */
public abstract class VarHandleReferenceCountUpdater<T extends ReferenceCounted>
        extends ReferenceCountUpdater<T> {

    protected VarHandleReferenceCountUpdater() {
    }

    /** 引用计数字段对应的 VarHandle。 */
    protected abstract VarHandle varHandle();

    /** VarHandle.set 初始化 raw 引用计数。 */
    @Override
    protected final void safeInitializeRawRefCnt(T refCntObj, int value) {
        varHandle().set(refCntObj, value);
    }

    /** 原子加减 raw 引用计数。 */
    @Override
    protected final int getAndAddRawRefCnt(T refCntObj, int increment) {
        return (int) varHandle().getAndAdd(refCntObj, increment);
    }

    /** 普通读 raw 引用计数。 */
    @Override
    protected final int getRawRefCnt(T refCnt) {
        return (int) varHandle().get(refCnt);
    }

    /** acquire 语义读 raw 引用计数。 */
    @Override
    protected final int getAcquireRawRefCnt(T refCnt) {
        return (int) varHandle().getAcquire(refCnt);
    }

    /** release 语义写 raw 引用计数。 */
    @Override
    protected final void setReleaseRawRefCnt(T refCnt, int value) {
        varHandle().setRelease(refCnt, value);
    }

    /** CAS 更新 raw 引用计数。 */
    @Override
    protected final boolean casRawRefCnt(T refCnt, int expected, int value) {
        return varHandle().compareAndSet(refCnt, expected, value);
    }
}
