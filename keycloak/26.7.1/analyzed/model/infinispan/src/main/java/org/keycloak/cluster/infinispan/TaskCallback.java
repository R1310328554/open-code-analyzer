/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.cluster.infinispan;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;

/**
 * 异步集群任务的回调句柄：协调任务提交、完成通知与结果 Future。
 * <p>
 * 当 {@link InfinispanClusterProvider#executeIfNotExecutedAsync} 检测到任务已在其他节点执行时，
 * 调用方通过此对象等待任务完成并获取执行结果。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TaskCallback {

    protected static final Logger logger = Logger.getLogger(TaskCallback.class);

    /** 等待 Future 可用的最长毫秒数。 */
    static final int LATCH_TIMEOUT_MS = 10000;

    /** 任务是否成功完成。 */
    private volatile boolean success;

    /** 包装后的异步执行 Future。 */
    private volatile Future<Boolean> future;

    /** 任务完成信号（缓存条目被移除时触发）。 */
    private final CountDownLatch taskCompletedLatch = new CountDownLatch(1);
    /** Future 已设置信号（避免调用方在 Future 赋值前读取）。 */
    private final CountDownLatch futureAvailableLatch = new CountDownLatch(1);


    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    /** 设置 Future 并释放等待线程。 */
    public void setFuture(Future<Boolean> future) {
        this.future = future;
        this.futureAvailableLatch.countDown();
    }

    /** 阻塞等待 Future 可用后返回。 */
    public Future<Boolean> getFuture() {
        try {
            this.futureAvailableLatch.await(LATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            logger.error("Interrupted thread!");
            Thread.currentThread().interrupt();
        }

        return future;
    }


    /** 返回任务完成门闩，供等待方在缓存锁释放时继续执行。 */
    public CountDownLatch getTaskCompletedLatch() {
        return taskCompletedLatch;
    }
}
