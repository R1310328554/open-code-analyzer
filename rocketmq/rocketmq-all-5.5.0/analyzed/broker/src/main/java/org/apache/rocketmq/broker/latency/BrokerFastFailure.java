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
package org.apache.rocketmq.broker.latency;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.future.FutureTaskExt;
import org.apache.rocketmq.common.utils.ThreadUtils;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.netty.RequestTask;
import org.apache.rocketmq.remoting.protocol.RemotingSysResponseCode;

/**
 * Broker 快速失败：监控各 Remoting 线程池队列，在 OS 页缓存繁忙或
 * 排队超时时主动拒绝请求，覆盖 {@link BrokerController#getSendThreadPoolQueue()}、
 * {@link BrokerController#getPullThreadPoolQueue()} 等队列。
 */
public class BrokerFastFailure {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME);
    private final ScheduledExecutorService scheduledExecutorService;
    private final BrokerController brokerController;

    private volatile long jstackTime = System.currentTimeMillis();

    private final List<Pair<BlockingQueue<Runnable>, Supplier<Long>>> cleanExpiredRequestQueueList = new ArrayList<>();

    /** 注册待清理队列并创建定时调度器。 */
    public BrokerFastFailure(final BrokerController brokerController) {
        this.brokerController = brokerController;
        initCleanExpiredRequestQueueList();
        this.scheduledExecutorService = ThreadUtils.newScheduledThreadPool(1,
            new ThreadFactoryImpl("BrokerFastFailureScheduledThread", true,
                brokerController == null ? null : brokerController.getBrokerConfig()));
    }

    /** 初始化 Send/Pull/LitePull/Heartbeat/事务/Ack/Admin 等队列及超时阈值。 */
    private void initCleanExpiredRequestQueueList() {
        cleanExpiredRequestQueueList.add(new Pair<>(this.brokerController.getSendThreadPoolQueue(), () -> this.brokerController.getBrokerConfig().getWaitTimeMillsInSendQueue()));
        cleanExpiredRequestQueueList.add(new Pair<>(this.brokerController.getPullThreadPoolQueue(), () -> this.brokerController.getBrokerConfig().getWaitTimeMillsInPullQueue()));
        cleanExpiredRequestQueueList.add(new Pair<>(this.brokerController.getLitePullThreadPoolQueue(), () -> this.brokerController.getBrokerConfig().getWaitTimeMillsInLitePullQueue()));
        cleanExpiredRequestQueueList.add(new Pair<>(this.brokerController.getHeartbeatThreadPoolQueue(), () -> this.brokerController.getBrokerConfig().getWaitTimeMillsInHeartbeatQueue()));
        cleanExpiredRequestQueueList.add(new Pair<>(this.brokerController.getEndTransactionThreadPoolQueue(), () -> this.brokerController.getBrokerConfig().getWaitTimeMillsInTransactionQueue()));
        cleanExpiredRequestQueueList.add(new Pair<>(this.brokerController.getAckThreadPoolQueue(), () -> this.brokerController.getBrokerConfig().getWaitTimeMillsInAckQueue()));
        cleanExpiredRequestQueueList.add(new Pair<>(this.brokerController.getAdminBrokerThreadPoolQueue(), () -> this.brokerController.getBrokerConfig().getWaitTimeMillsInAdminBrokerQueue()));
    }

    /** 从 {@link FutureTaskExt} 包装中提取 {@link RequestTask}。 */
    public static RequestTask castRunnable(final Runnable runnable) {
        try {
            if (runnable instanceof FutureTaskExt) {
                FutureTaskExt object = (FutureTaskExt) runnable;
                return (RequestTask) object.getRunnable();
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("castRunnable exception, %s", runnable.getClass().getName()), e);
        }

        return null;
    }

    /** 每 10ms 检查是否启用快速失败并清理过期请求。 */
    public void start() {
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (brokerController.getBrokerConfig().isBrokerFastFailureEnable()) {
                    cleanExpiredRequest();
                }
            }
        }, 1000, 10, TimeUnit.MILLISECONDS);
    }

    /** 页缓存繁忙时优先丢弃 Send 队列任务，再逐队列清理超时任务。 */
    private void cleanExpiredRequest() {

        while (this.brokerController.getMessageStore().isOSPageCacheBusy()) {
            try {
                if (!this.brokerController.getSendThreadPoolQueue().isEmpty()) {
                    final Runnable runnable = this.brokerController.getSendThreadPoolQueue().poll(0, TimeUnit.SECONDS);
                    if (null == runnable) {
                        break;
                    }

                    final RequestTask rt = castRunnable(runnable);
                    if (rt != null) {
                        rt.returnResponse(RemotingSysResponseCode.SYSTEM_BUSY, String.format(
                            "[PCBUSY_CLEAN_QUEUE]broker busy, start flow control for a while, period in queue: %sms, "
                                + "size of queue: %d",
                            System.currentTimeMillis() - rt.getCreateTimestamp(),
                            this.brokerController.getSendThreadPoolQueue().size()));
                    }
                } else {
                    break;
                }
            } catch (Throwable ignored) {
            }
        }

        for (Pair<BlockingQueue<Runnable>, Supplier<Long>> pair : cleanExpiredRequestQueueList) {
            cleanExpiredRequestInQueue(pair.getObject1(), pair.getObject2().get());
        }
    }

    /** 从队首移除等待超过阈值的 {@link RequestTask} 并返回 SYSTEM_BUSY。 */
    void cleanExpiredRequestInQueue(final BlockingQueue<Runnable> blockingQueue, final long maxWaitTimeMillsInQueue) {
        while (true) {
            try {
                if (!blockingQueue.isEmpty()) {
                    final Runnable runnable = blockingQueue.peek();
                    if (null == runnable) {
                        break;
                    }
                    final RequestTask rt = castRunnable(runnable);
                    if (rt == null || rt.isStopRun()) {
                        break;
                    }

                    final long behind = System.currentTimeMillis() - rt.getCreateTimestamp();
                    if (behind >= maxWaitTimeMillsInQueue) {
                        if (blockingQueue.remove(runnable)) {
                            rt.setStopRun(true);
                            rt.returnResponse(RemotingSysResponseCode.SYSTEM_BUSY, String.format("[TIMEOUT_CLEAN_QUEUE]broker busy, start flow control for a while, period in queue: %sms, size of queue: %d", behind, blockingQueue.size()));
                            if (System.currentTimeMillis() - jstackTime > 15000) {
                                jstackTime = System.currentTimeMillis();
                                LOGGER.warn("broker jstack \n " + UtilAll.jstack());
                            }
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            } catch (Throwable ignored) {
            }
        }
    }

    /** 动态注册额外待清理队列及超时供应函数。 */
    public synchronized void addCleanExpiredRequestQueue(BlockingQueue<Runnable> cleanExpiredRequestQueue,
        Supplier<Long> maxWaitTimeMillsInQueueSupplier) {
        cleanExpiredRequestQueueList.add(new Pair<>(cleanExpiredRequestQueue, maxWaitTimeMillsInQueueSupplier));
    }

    /** 关闭定时调度器。 */
    public void shutdown() {
        this.scheduledExecutorService.shutdown();
    }
}
