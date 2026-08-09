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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 退避自旋锁：CAS 自旋失败后 Thread.sleep(0) 退让，并自适应调整自旋次数。
 */
public class BackOffSpinLock implements AdaptiveBackOffSpinLock {

    /** 自旋锁 CAS 标志。 */
    private AtomicBoolean putMessageSpinLock = new AtomicBoolean(true);

    /** 当前最优自旋次数 K。 */
    private int optimalDegree;

    private final static int INITIAL_DEGREE = 1000;

    private final static int MAX_OPTIMAL_DEGREE = 10000;

    /** 双槽退让次数统计。 */
    private final List<AtomicInteger> numberOfRetreat;

    /** 初始化自旋次数与退让计数器。 */
    public BackOffSpinLock() {
        this.optimalDegree = INITIAL_DEGREE;

        numberOfRetreat = new ArrayList<>(2);
        numberOfRetreat.add(new AtomicInteger(0));
        numberOfRetreat.add(new AtomicInteger(0));
    }

    /** CAS 自旋获取锁，失败则退让并计数。 */
    @Override
    public void lock() {
        int spinDegree = this.optimalDegree;
        while (true) {
            for (int i = 0; i < spinDegree; i++) {
                if (this.putMessageSpinLock.compareAndSet(true, false)) {
                    return;
                }
            }
            numberOfRetreat.get(LocalTime.now().getSecond() % 2).getAndIncrement();
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /** 释放自旋锁。 */
    @Override
    public void unlock() {
        this.putMessageSpinLock.compareAndSet(false, true);
    }

    /** 从配置更新最优自旋次数。 */
    @Override
    public void update(MessageStoreConfig messageStoreConfig) {
        this.optimalDegree = messageStoreConfig.getSpinLockCollisionRetreatOptimalDegree();
    }

    /** 返回当前自旋次数。 */
    public int getOptimalDegree() {
        return this.optimalDegree;
    }

    public void setOptimalDegree(int optimalDegree) {
        this.optimalDegree = optimalDegree;
    }

    /** 是否仍可继续增大自旋次数。 */
    public boolean isAdapt() {
        return optimalDegree < MAX_OPTIMAL_DEGREE;
    }

    /** 根据竞争情况增大或减小自旋次数。 */
    public synchronized void adapt(boolean isRise) {
        if (isRise) {
            if (optimalDegree * 2 <= MAX_OPTIMAL_DEGREE) {
                optimalDegree *= 2;
            } else {
                if (optimalDegree + INITIAL_DEGREE <= MAX_OPTIMAL_DEGREE) {
                    optimalDegree += INITIAL_DEGREE;
                }
            }
        } else {
            if (optimalDegree >= 2 * INITIAL_DEGREE) {
                optimalDegree -= INITIAL_DEGREE;
            }
        }
    }

    /** 返回指定槽位的退让次数。 */
    public int getNumberOfRetreat(int pos) {
        return numberOfRetreat.get(pos).get();
    }

    public void setNumberOfRetreat(int pos, int size) {
        this.numberOfRetreat.get(pos).set(size);
    }
}
