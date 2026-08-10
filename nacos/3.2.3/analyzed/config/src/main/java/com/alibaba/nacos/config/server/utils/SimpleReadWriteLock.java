/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.config.server.utils;

/**
 * 最简读写锁：0 无锁，负数写锁，正数读锁计数；加锁与解锁须成对调用。
 * Simplest read-write lock implementation. Requires locking and unlocking must be called in pairs.
 *
 * @author Nacos
 */
public class SimpleReadWriteLock {
    
    /**
     * 锁状态：0 空闲；负数为写锁；正数为读锁持有数。
     * Zero means no lock; Negative Numbers mean write locks; Positive Numbers mean read locks, and the numeric value
     * represents the number of read locks.
     */
    private int status = 0;
    
    /**
     * 尝试获取读锁：写锁占用时失败，否则 status++。
     * Try read lock.
     */
    public synchronized boolean tryReadLock() {
        if (isWriteLocked()) {
            return false;
        } else {
            status++;
            return true;
        }
    }
    
    /**
     * 释放读锁，status 已为 0 时不递减。
     * Release the read lock.
     */
    public synchronized void releaseReadLock() {
        // when status equals 0, it should not decrement to negative numbers
        if (status == 0) {
            return;
        }
        status--;
    }
    
    /**
     * 尝试获取写锁：仅 status==0 时成功并置 -1。
     * Try write lock.
     */
    public synchronized boolean tryWriteLock() {
        if (!isFree()) {
            return false;
        } else {
            status = -1;
            return true;
        }
    }
    
    /** 释放写锁，恢复为 0 */
    public synchronized void releaseWriteLock() {
        status = 0;
    }
    
    /** 是否处于写锁状态 */
    private boolean isWriteLocked() {
        return status < 0;
    }
    
    /** 是否无任何锁 */
    private boolean isFree() {
        return status == 0;
    }
    
}
