/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.store;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 引用计数资源基类：通过 hold/release 管理 MappedFile 等资源的共享与回收。
 */
public abstract class ReferenceResource {
    /** 当前引用计数，初始为 1。 */
    protected final AtomicLong refCount = new AtomicLong(1);
    /** 资源是否仍可用（未 shutdown）。 */
    protected volatile boolean available = true;
    /** 底层 cleanup 是否已完成。 */
    protected volatile boolean cleanupOver = false;
    /** 首次 shutdown 的时间戳，用于强制回收。 */
    private volatile long firstShutdownTimestamp = 0;

    /** 增加引用计数；资源不可用或计数异常时返回 false。 */
    public synchronized boolean hold() {
        if (this.isAvailable()) {
            if (this.refCount.getAndIncrement() > 0) {
                return true;
            } else {
                this.refCount.getAndDecrement();
            }
        }

        return false;
    }

    /** 资源是否可用。 */
    public boolean isAvailable() {
        return this.available;
    }

    /** 标记不可用并尝试释放；超时后强制将引用计数置负以触发 cleanup。 */
    public void shutdown(final long intervalForcibly) {
        if (this.available) {
            this.available = false;
            this.firstShutdownTimestamp = System.currentTimeMillis();
            this.release();
        } else if (this.getRefCount() > 0) {
            if ((System.currentTimeMillis() - this.firstShutdownTimestamp) >= intervalForcibly) {
                this.refCount.set(-1000 - this.getRefCount());
                this.release();
            }
        }
    }

    /** 递减引用计数，归零时调用 cleanup。 */
    public void release() {
        long value = this.refCount.decrementAndGet();
        if (value > 0)
            return;

        synchronized (this) {

            this.cleanupOver = this.cleanup(value);
        }
    }

    /** 返回当前引用计数。 */
    public long getRefCount() {
        return this.refCount.get();
    }

    /** 引用计数归零时的资源清理逻辑，由子类实现。 */
    public abstract boolean cleanup(final long currentRef);

    /** 引用已归零且 cleanup 是否完成。 */
    public boolean isCleanupOver() {
        return this.refCount.get() <= 0 && this.cleanupOver;
    }
}
