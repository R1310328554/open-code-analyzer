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
package io.netty.util.internal;

import io.netty.util.Recycler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectPool.Handle;

/**
 * Some pending write which should be picked up later.
 *
 * <p>待稍后处理的写操作封装，配合 {@link Recycler} 复用，含消息与 {@link Promise}。</p>
 */
public final class PendingWrite {
    /** 对象池：复用 PendingWrite 实例，减少 EventLoop 写路径分配。 */
    private static final Recycler<PendingWrite> RECYCLER =
            new Recycler<PendingWrite>() {
                @Override
                protected PendingWrite newObject(Handle<PendingWrite> handle) {
                    return new PendingWrite(handle);
                }
            };

    /**
     * Create a new empty {@link RecyclableArrayList} instance
     *
     * <p>从池中取出实例并绑定待写消息与完成回调。</p>
     */
    public static PendingWrite newInstance(Object msg, Promise<Void> promise) {
        PendingWrite pending = RECYCLER.get();
        pending.msg = msg;
        pending.promise = promise;
        return pending;
    }

    /** Recycler 句柄，回收时归还对象。 */
    private final Handle<PendingWrite> handle;
    /** 待写出消息，失败时须 {@link ReferenceCountUtil#release}。 */
    private Object msg;
    /** 写完成通知，可为 null。 */
    private Promise<Void> promise;

    private PendingWrite(Handle<PendingWrite> handle) {
        this.handle = handle;
    }

    /**
     * Clear and recycle this instance.
     *
     * <p>清空字段并归还对象池。</p>
     */
    public boolean recycle() {
        msg = null;
        promise = null;
        handle.recycle(this);
        return true;
    }

    /**
     * Fails the underlying {@link Promise} with the given cause and recycle this instance.
     *
     * <p>释放消息引用、标记 Promise 失败并回收。</p>
     */
    public boolean failAndRecycle(Throwable cause) {
        ReferenceCountUtil.release(msg);
        if (promise != null) {
            promise.setFailure(cause);
        }
        return recycle();
    }

    /**
     * Mark the underlying {@link Promise} successfully and recycle this instance.
     *
     * <p>标记 Promise 成功并回收实例。</p>
     */
    public boolean successAndRecycle() {
        if (promise != null) {
            promise.setSuccess(null);
        }
        return recycle();
    }

    /** 返回待写消息。 */
    public Object msg() {
        return msg;
    }

    /** 返回关联的写完成 Promise。 */
    public Promise<Void> promise() {
        return promise;
    }

    /**
     * Recycle this instance and return the {@link Promise}.
     *
     * <p>回收前取出 Promise 供调用方继续使用。</p>
     */
    public Promise<Void> recycleAndGet() {
        Promise<Void> promise = this.promise;
        recycle();
        return promise;
    }
}
