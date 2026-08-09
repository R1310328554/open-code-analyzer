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


import java.util.concurrent.LinkedBlockingQueue;

import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.store.CommitLog.GroupCommitRequest;

/**
 * 刷盘超时监视线程：异步等待 GroupCommit 完成，超时则唤醒调用方。
 */
public class FlushDiskWatcher extends ServiceThread {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);
    /** 待监视的组提交请求队列。 */
    private final LinkedBlockingQueue<GroupCommitRequest> commitRequests = new LinkedBlockingQueue<>();

    /** 返回服务线程名称。 */
    @Override
    public String getServiceName() {
        return FlushDiskWatcher.class.getSimpleName();
    }

    /** 循环取请求并轮询刷盘 Future，超时则返回 FLUSH_DISK_TIMEOUT。 */
    @Override
    public void run() {
        while (!isStopped()) {
            GroupCommitRequest request = null;
            try {
                request = commitRequests.take();
            } catch (InterruptedException e) {
                log.warn("take flush disk commit request, but interrupted, this may caused by shutdown");
                continue;
            }
            while (!request.future().isDone()) {
                long now = System.nanoTime();
                if (now - request.getDeadLine() >= 0) {
                    request.wakeupCustomer(PutMessageStatus.FLUSH_DISK_TIMEOUT);
                    break;
                }
                // 避免频繁线程切换，此处用 sleep 替代 future.get 轮询，
                long sleepTime = (request.getDeadLine() - now) / 1_000_000;
                sleepTime = Math.min(10, sleepTime);
                if (sleepTime == 0) {
                    request.wakeupCustomer(PutMessageStatus.FLUSH_DISK_TIMEOUT);
                    break;
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    log.warn(
                            "An exception occurred while waiting for flushing disk to complete. this may caused by shutdown");
                    break;
                }
            }
        }
    }

    /** 提交一条刷盘监视请求。 */
    public void add(GroupCommitRequest request) {
        commitRequests.add(request);
    }

    /** 返回待处理请求数量。 */
    public int queueSize() {
        return commitRequests.size();
    }
}
