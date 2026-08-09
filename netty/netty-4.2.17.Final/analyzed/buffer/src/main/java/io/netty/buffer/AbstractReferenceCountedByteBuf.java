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

package io.netty.buffer;

import io.netty.util.internal.RefCnt;

/**
 * 自带引用计数的 {@link ByteBuf} 抽象实现基类。
 * <p>
 * 使用 {@link io.netty.util.internal.RefCnt} 管理引用计数；
 * 计数归零时调用子类实现的 {@link #deallocate()} 释放底层资源。
 */
public abstract class AbstractReferenceCountedByteBuf extends AbstractByteBuf {

    /** 引用计数器，初始值为 1 */
    private final RefCnt refCnt = new RefCnt();

    protected AbstractReferenceCountedByteBuf(int maxCapacity) {
        super(maxCapacity);
    }

    @Override
    boolean isAccessible() {
        // 非 volatile 读以提升热路径性能；ensureAccessible 本身存在竞态，仅作尽力而为的防护
        return RefCnt.isLiveNonVolatile(refCnt);
    }

    @Override
    public int refCnt() {
        return RefCnt.refCnt(refCnt);
    }

    /**
     * 供子类直接设置引用计数的不安全操作。
     */
    protected final void setRefCnt(int count) {
        RefCnt.setRefCnt(refCnt, count);
    }

    /**
     * 供子类将引用计数重置为 1 的不安全操作（如对象池复用时）。
     */
    protected final void resetRefCnt() {
        RefCnt.resetRefCnt(refCnt);
    }

    @Override
    public ByteBuf retain() {
        RefCnt.retain(refCnt);
        return this;
    }

    @Override
    public ByteBuf retain(int increment) {
        RefCnt.retain(refCnt, increment);
        return this;
    }

    @Override
    public ByteBuf touch() {
        return this;
    }

    @Override
    public ByteBuf touch(Object hint) {
        return this;
    }

    @Override
    public boolean release() {
        return handleRelease(RefCnt.release(refCnt));
    }

    @Override
    public boolean release(int decrement) {
        return handleRelease(RefCnt.release(refCnt, decrement));
    }

    private boolean handleRelease(boolean result) {
        if (result) {
            deallocate();
        }
        return result;
    }

    /**
     * 当 {@link #refCnt()} 降为 0 时由 {@link #release()} 调用，子类在此释放内存。
     */
    protected abstract void deallocate();
}
