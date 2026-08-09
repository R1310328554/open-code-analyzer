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
package org.apache.rocketmq.store.lock;

import org.apache.rocketmq.store.config.MessageStoreConfig;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自适应退避自旋锁实现：按 TPS 与退让次数在 SpinLock 与 ReentrantLock 间动态切换。
 */
public class AdaptiveBackOffSpinLockImpl implements AdaptiveBackOffSpinLock {
    /** 当前生效的锁实现。 */
    private AdaptiveBackOffSpinLock adaptiveLock;
    //state
    /** 切换过程中的全局状态标志。 */
    private AtomicBoolean state = new AtomicBoolean(true);

    // Used to determine the switchover between a mutex lock and a spin lock
    private final static float SWAP_SPIN_LOCK_RATIO = 0.8f;

    // It is used to adjust the spin number K of the escape spin lock
    // When (retreat number / TPS) <= (1 / BASE_SWAP_ADAPTIVE_RATIO * SPIN_LOCK_ADAPTIVE_RATIO), K is decreased
    private final static int SPIN_LOCK_ADAPTIVE_RATIO = 4;

    // It is used to adjust the spin number K of the escape spin lock
    // When (retreat number / TPS) >= (1 / BASE_SWAP_ADAPTIVE_RATIO), K is increased
    private final static int BASE_SWAP_LOCK_RATIO = 320;

    private final static String BACK_OFF_SPIN_LOCK = "SpinLock";

    private final static String REENTRANT_LOCK = "ReentrantLock";

    /** 可选锁实现映射（SpinLock / ReentrantLock）。 */
    private Map<String, AdaptiveBackOffSpinLock> locks;

    /** 双槽 TPS 计数表。 */
    private final List<AtomicInteger> tpsTable;

    /** 双槽竞争线程集合。 */
    private final List<Set<Thread>> threadTable;

    /** 自旋锁切互斥锁的 TPS 临界点。 */
    private int swapCriticalPoint;

    /** 当前持锁线程数。 */
    private AtomicInteger currentThreadNum = new AtomicInteger(0);

    /** 是否启用自动切换。 */
    private AtomicBoolean isOpen = new AtomicBoolean(true);

    /** 初始化两种锁实现与统计表。 */
    public AdaptiveBackOffSpinLockImpl() {
        this.locks = new HashMap<>();
        this.locks.put(REENTRANT_LOCK, new BackOffReentrantLock());
        this.locks.put(BACK_OFF_SPIN_LOCK, new BackOffSpinLock());

        this.threadTable = new ArrayList<>(2);
        this.threadTable.add(ConcurrentHashMap.newKeySet());
        this.threadTable.add(ConcurrentHashMap.newKeySet());

        this.tpsTable = new ArrayList<>(2);
        this.tpsTable.add(new AtomicInteger(0));
        this.tpsTable.add(new AtomicInteger(0));

        adaptiveLock = this.locks.get(BACK_OFF_SPIN_LOCK);
    }

    /** 加锁并记录 TPS/线程统计。 */
    @Override
    public void lock() {
        int slot = LocalTime.now().getSecond() % 2;
        this.threadTable.get(slot).add(Thread.currentThread());
        this.tpsTable.get(slot).getAndIncrement();
        boolean state;
        do {
            state = this.state.get();
        } while (!state);

        currentThreadNum.incrementAndGet();
        this.adaptiveLock.lock();
    }

    /** 解锁并在启用时尝试切换锁类型。 */
    @Override
    public void unlock() {
        this.adaptiveLock.unlock();
        currentThreadNum.decrementAndGet();
        if (isOpen.get()) {
            swap();
        }
    }

    /** 委托当前锁实现更新配置。 */
    @Override
    public void update(MessageStoreConfig messageStoreConfig) {
        this.adaptiveLock.update(messageStoreConfig);
    }

    /** 根据退让次数与 TPS 决定是否切换锁类型或调整自旋次数。 */
    @Override
    public void swap() {
        if (!this.state.get()) {
            return;
        }
        boolean needSwap = false;
        int slot = 1 - LocalTime.now().getSecond() % 2;
        int tps = this.tpsTable.get(slot).get() + 1;
        int threadNum = this.threadTable.get(slot).size();
        this.tpsTable.get(slot).set(-1);
        this.threadTable.get(slot).clear();
        if (tps == 0) {
            return;
        }

        if (this.adaptiveLock instanceof BackOffSpinLock) {
            BackOffSpinLock lock = (BackOffSpinLock) this.adaptiveLock;
            // Avoid frequent adjustment of K, and make a reasonable range through experiments
            // reasonable range : (retreat number / TPS) > (1 / BASE_SWAP_ADAPTIVE_RATIO * SPIN_LOCK_ADAPTIVE_RATIO) &&
            // (retreat number / TPS) < (1 / BASE_SWAP_ADAPTIVE_RATIO)
            if (lock.getNumberOfRetreat(slot) * BASE_SWAP_LOCK_RATIO >= tps) {
                if (lock.isAdapt()) {
                    lock.adapt(true);
                } else {
                    // It is used to switch between mutex lock and spin lock
                    this.swapCriticalPoint = tps * threadNum;
                    needSwap = true;
                }
            } else if (lock.getNumberOfRetreat(slot) * BASE_SWAP_LOCK_RATIO * SPIN_LOCK_ADAPTIVE_RATIO <= tps) {
                lock.adapt(false);
            }
            lock.setNumberOfRetreat(slot, 0);
        } else {
            if (tps * threadNum <= this.swapCriticalPoint * SWAP_SPIN_LOCK_RATIO) {
                needSwap = true;
            }
        }

        if (needSwap) {
            if (this.state.compareAndSet(true, false)) {
                // Ensures that no threads are in contention locks as well as in critical zones
                int currentThreadNum;
                do {
                    currentThreadNum = this.currentThreadNum.get();
                } while (currentThreadNum != 0);

                try {
                    if (this.adaptiveLock instanceof BackOffSpinLock) {
                        this.adaptiveLock = this.locks.get(REENTRANT_LOCK);
                    } else {
                        this.adaptiveLock = this.locks.get(BACK_OFF_SPIN_LOCK);
                        ((BackOffSpinLock) this.adaptiveLock).adapt(false);
                    }
                } catch (Exception e) {
                    //ignore
                } finally {
                    this.state.compareAndSet(false, true);
                }
            }
        }
    }

    /** 返回所有锁实现。 */
    public Collection<AdaptiveBackOffSpinLock> getLocks() {
        return this.locks.values();
    }

    public void setLocks(Map<String, AdaptiveBackOffSpinLock> locks) {
        this.locks = locks;
    }

    /** 返回切换状态标志。 */
    public boolean getState() {
        return this.state.get();
    }

    public void setState(boolean state) {
        this.state.set(state);
    }

    /** 返回当前锁实现。 */
    public AdaptiveBackOffSpinLock getAdaptiveLock() {
        return adaptiveLock;
    }

    public List<AtomicInteger> getTpsTable() {
        return tpsTable;
    }

    public void setSwapCriticalPoint(int swapCriticalPoint) {
        this.swapCriticalPoint = swapCriticalPoint;
    }

    public int getSwapCriticalPoint() {
        return swapCriticalPoint;
    }

    /** 是否启用自动切换。 */
    public boolean isOpen() {
        return this.isOpen.get();
    }

    public void setOpen(boolean open) {
        this.isOpen.set(open);
    }
}
