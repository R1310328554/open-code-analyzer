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
package io.netty.util;

import io.netty.util.internal.RefCnt;

/**
 * Abstract base class for classes wants to implement {@link ReferenceCounted}.
 *
 * <p>实现 {@link ReferenceCounted} 的抽象基类，引用计数委托给 {@link RefCnt}。
 * 计数归零时调用子类 {@link #deallocate()} 释放资源。</p>
 */
public abstract class AbstractReferenceCounted implements ReferenceCounted {

    /** 底层引用计数存储（volatile + 原子操作）。 */
    private final RefCnt refCnt = new RefCnt();

    @Override
    public int refCnt() {
        return RefCnt.refCnt(refCnt);
    }

    /**
     * An unsafe operation intended for use by a subclass that sets the reference count of the object directly
     *
     * <p>供子类直接设置引用计数，跳过 retain/release 语义；误用会导致泄漏或重复释放。</p>
     */
    protected void setRefCnt(int refCnt) {
        RefCnt.setRefCnt(this.refCnt, refCnt);
    }

    /** 引用计数加 1 并返回 {@code this}。 */
    @Override
    public ReferenceCounted retain() {
        RefCnt.retain(refCnt);
        return this;
    }

    /** 引用计数增加 {@code increment} 并返回 {@code this}。 */
    @Override
    public ReferenceCounted retain(int increment) {
        RefCnt.retain(refCnt, increment);
        return this;
    }

    /** 记录访问点（默认无 hint）；子类可覆盖以支持泄漏检测。 */
    @Override
    public ReferenceCounted touch() {
        return touch(null);
    }

    /** 引用计数减 1；归零时触发 {@link #deallocate()}。 */
    @Override
    public boolean release() {
        return handleRelease(RefCnt.release(refCnt));
    }

    /** 引用计数减少 {@code decrement}；归零时触发 {@link #deallocate()}。 */
    @Override
    public boolean release(int decrement) {
        return handleRelease(RefCnt.release(refCnt, decrement));
    }

    /** 若 release 成功则调用 {@link #deallocate()}。 */
    private boolean handleRelease(boolean result) {
        if (result) {
            deallocate();
        }
        return result;
    }

    /**
     * Called once {@link #refCnt()} is equals 0.
     *
     * <p>引用计数变为 0 时由框架调用一次，子类在此释放缓冲区、连接等资源。</p>
     */
    protected abstract void deallocate();
}
