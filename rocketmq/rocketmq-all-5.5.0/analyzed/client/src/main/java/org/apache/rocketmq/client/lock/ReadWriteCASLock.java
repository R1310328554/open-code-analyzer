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
package org.apache.rocketmq.client.lock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于 CAS 的读写锁：写锁独占，读锁共享；写锁获取前需等待所有读锁释放。
 */
public class ReadWriteCASLock {
    // true 表示可获取写锁；false 表示写锁已被占用
    /** 写锁标志：true 可写，false 写锁被持有。 */
    private final AtomicBoolean writeLock = new AtomicBoolean(true);

    /** 当前读锁持有计数。 */
    private final AtomicInteger readLock = new AtomicInteger(0);

    /** 自旋获取写锁，并等待所有读锁释放。 */
    public void acquireWriteLock() {
        boolean isLock = false;
        do {
            isLock = writeLock.compareAndSet(true, false);
        } while (!isLock);

        do {
            isLock = readLock.get() == 0;
        } while (!isLock);
    }

    /** 释放写锁。 */
    public void releaseWriteLock() {
        this.writeLock.compareAndSet(false, true);
    }

    /** 等待写锁可用后递增读锁计数。 */
    public void acquireReadLock() {
        boolean isLock = false;
        do {
            isLock = writeLock.get();
        } while (!isLock);
        readLock.getAndIncrement();
    }

    /** 递减读锁计数。 */
    public void releaseReadLock() {
        this.readLock.getAndDecrement();
    }

    /** 是否可获取写锁（无读锁且写标志为 true）。 */
    public boolean getWriteLock() {
        return this.writeLock.get() && this.readLock.get() == 0;
    }

    /** 是否可获取读锁（写标志为 true）。 */
    public boolean getReadLock() {
        return this.writeLock.get();
    }

}
