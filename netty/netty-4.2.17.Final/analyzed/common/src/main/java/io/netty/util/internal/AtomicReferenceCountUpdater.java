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

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * 基于 {@link AtomicIntegerFieldUpdater} 的引用计数更新器抽象基类。
 * <p>子类提供具体 {@link ReferenceCounted} 类型上的 {@code updater()}，本类将
 * {@link ReferenceCountUpdater} 的原始计数操作委托给原子字段更新器。</p>
 *
 * @param <T> 实现 {@link ReferenceCounted} 的对象类型
 */
public abstract class AtomicReferenceCountUpdater<T extends ReferenceCounted> extends ReferenceCountUpdater<T> {

    protected AtomicReferenceCountUpdater() {
    }

    /** 返回绑定到目标类型 int 引用计数字段的 {@link AtomicIntegerFieldUpdater}。 */
    protected abstract AtomicIntegerFieldUpdater<T> updater();

    @Override
    protected final void safeInitializeRawRefCnt(T refCntObj, int value) {
        updater().set(refCntObj, value);
    }

    @Override
    protected final int getAndAddRawRefCnt(T refCntObj, int increment) {
        return updater().getAndAdd(refCntObj, increment);
    }

    @Override
    protected final int getRawRefCnt(T refCnt) {
        return updater().get(refCnt);
    }

    @Override
    protected final int getAcquireRawRefCnt(T refCnt) {
        return updater().get(refCnt);
    }

    @Override
    protected final void setReleaseRawRefCnt(T refCnt, int value) {
        // lazySet 提供 release 语义下的写入，避免不必要的内存屏障开销
        updater().lazySet(refCnt, value);
    }

    @Override
    protected final boolean casRawRefCnt(T refCnt, int expected, int value) {
        return updater().compareAndSet(refCnt, expected, value);
    }
}
