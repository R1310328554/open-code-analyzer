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
package org.apache.rocketmq.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 异步并行关闭辅助类：为多个 {@link Shutdown} 目标各启线程执行 shutdown，并用 CountDownLatch 等待完成。
 */
public class AsyncShutdownHelper {
    /** 标记是否已完成关闭流程。 */
    private final AtomicBoolean shutdown;
    /** 待并行关闭的目标列表。 */
    private final List<Shutdown> targetList;

    /** 等待各关闭线程完成的同步器。 */
    private CountDownLatch countDownLatch;

    public AsyncShutdownHelper() {
        this.targetList = new ArrayList<>();
        this.shutdown = new AtomicBoolean(false);
    }

    /** 注册待关闭目标；若已关闭则忽略。 */
    public void addTarget(Shutdown target) {
        if (shutdown.get()) {
            return;
        }
        targetList.add(target);
    }

    /** 为各目标启动独立线程执行 shutdown。 */
    public AsyncShutdownHelper shutdown() {
        if (shutdown.get()) {
            return this;
        }
        if (targetList.isEmpty()) {
            return this;
        }
        this.countDownLatch = new CountDownLatch(targetList.size());
        for (Shutdown target : targetList) {
            Runnable runnable = () -> {
                try {
                    target.shutdown();
                } catch (Exception ignored) {

                } finally {
                    countDownLatch.countDown();
                }
            };
            new Thread(runnable).start();
        }
        return this;
    }

    /** 在指定超时内等待全部关闭线程完成，并标记关闭状态。 */
    public boolean await(long time, TimeUnit unit) throws InterruptedException {
        if (shutdown.get()) {
            return false;
        }
        try {
            return this.countDownLatch.await(time, unit);
        } finally {
            shutdown.compareAndSet(false, true);
        }
    }
}
