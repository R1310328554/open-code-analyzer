/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.connections.infinispan.shutdown;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 在 {@link ShutdownCondition} 不再 {@linkplain ShutdownCondition#inProgress() 进行中}
 * 或到达截止时间之前，阻塞关闭线程的 {@link ShutdownListener} 实现。
 * <p>
 * 外部事件（如拓扑变更）应调用 {@link #check()} 以检测条件是否已解除；若条件已清除，
 * 则唤醒等待中的关闭线程并调用 {@link ShutdownCondition#complete()}；若超时先到，
 * 则调用 {@link ShutdownCondition#onTimeout()}。
 */
public class WaitConditionShutdownListener implements ShutdownListener {

    /** 保护条件变量与等待逻辑的互斥锁。 */
    private final ReentrantLock lock;
    /** 拓扑/条件稳定时用于唤醒关闭线程的条件变量。 */
    private final Condition stableCluster;
    /** 决定关闭是否仍需等待的业务条件。 */
    private final ShutdownCondition condition;

    /**
     * @param condition 关闭等待条件，不可为 {@code null}
     */
    public WaitConditionShutdownListener(ShutdownCondition condition) {
        this.condition = Objects.requireNonNull(condition, "condition");
        lock = new ReentrantLock();
        stableCluster = lock.newCondition();
    }

    /** {@inheritDoc} 在锁保护下等待条件稳定或超时，并调用相应的条件回调。 */
    @Override
    public void onShutdown(Instant shutdownTime, Date deadline) {
        try {
            lock.lockInterruptibly();
            try {
                if (awaitUntilStable(deadline)) {
                    condition.complete();
                } else {
                    condition.onTimeout();
                }
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 检查 {@link ShutdownCondition} 是否仍在进行；若已解除则唤醒等待中的关闭线程。
     * <p>
     * 应由外部事件处理器（如拓扑变更监听器）在条件可能发生变化时调用。
     */
    public void check() {
        if (condition.inProgress()) {
            return;
        }
        try {
            lock.lockInterruptibly();
            try {
                stableCluster.signalAll();
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 循环等待直到条件不再进行中，或到达 {@code deadline}。
     *
     * @return {@code true} 表示条件在截止前已稳定；{@code false} 表示超时
     */
    private boolean awaitUntilStable(Date deadline) throws InterruptedException {
        while (condition.inProgress()) {
            if (!stableCluster.awaitUntil(deadline)) {
                return false;
            }
        }
        return true;
    }
}
