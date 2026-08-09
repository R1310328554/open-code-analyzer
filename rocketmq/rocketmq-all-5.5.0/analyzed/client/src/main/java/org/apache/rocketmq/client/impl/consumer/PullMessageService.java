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
package org.apache.rocketmq.client.impl.consumer;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.message.MessageRequestMode;
import org.apache.rocketmq.common.utils.ThreadUtils;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 拉取消息后台服务：从请求队列取出 {@link PullRequest}/{@link PopRequest} 并分派给对应 Push 消费者执行。
 */
public class PullMessageService extends ServiceThread {
    private final Logger logger = LoggerFactory.getLogger(PullMessageService.class);
    /** pull/pop 请求阻塞队列。 */
    private final LinkedBlockingQueue<MessageRequest> messageRequestQueue = new LinkedBlockingQueue<>();

    /** 关联的客户端实例。 */
    private final MQClientInstance mQClientFactory;
    private final ScheduledExecutorService scheduledExecutorService = Executors
        .newSingleThreadScheduledExecutor(new ThreadFactoryImpl("PullMessageServiceScheduledThread"));

    /** 构造拉取服务。 */
    public PullMessageService(MQClientInstance mQClientFactory) {
        this.mQClientFactory = mQClientFactory;
    }

    /** 延迟提交 pull 请求。 */
    public void executePullRequestLater(final PullRequest pullRequest, final long timeDelay) {
        if (!isStopped()) {
            this.scheduledExecutorService.schedule(new Runnable() {
                @Override
                /** 主循环：从队列取请求并按模式分派 pull 或 pop。 */
    public void run() {
                    PullMessageService.this.executePullRequestImmediately(pullRequest);
                }
            }, timeDelay, TimeUnit.MILLISECONDS);
        } else {
            logger.warn("PullMessageServiceScheduledThread has shutdown");
        }
    }

    /** 立即提交 pull 请求到队列。 */
    public void executePullRequestImmediately(final PullRequest pullRequest) {
        try {
            this.messageRequestQueue.put(pullRequest);
        } catch (InterruptedException e) {
            logger.error("executePullRequestImmediately pullRequestQueue.put", e);
        }
    }

    /** 延迟提交 POP 请求。 */
    public void executePopPullRequestLater(final PopRequest popRequest, final long timeDelay) {
        if (!isStopped()) {
            this.scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    PullMessageService.this.executePopPullRequestImmediately(popRequest);
                }
            }, timeDelay, TimeUnit.MILLISECONDS);
        } else {
            logger.warn("PullMessageServiceScheduledThread has shutdown");
        }
    }

    /** 立即提交 POP 请求到队列。 */
    public void executePopPullRequestImmediately(final PopRequest popRequest) {
        try {
            this.messageRequestQueue.put(popRequest);
        } catch (InterruptedException e) {
            logger.error("executePullRequestImmediately pullRequestQueue.put", e);
        }
    }

    /** 延迟执行调度任务。 */
    public void executeTaskLater(final Runnable r, final long timeDelay) {
        if (!isStopped()) {
            this.scheduledExecutorService.schedule(r, timeDelay, TimeUnit.MILLISECONDS);
        } else {
            logger.warn("PullMessageServiceScheduledThread has shutdown");
        }
    }

    /** 立即执行调度任务。 */
    public void executeTask(final Runnable r) {
        if (!isStopped()) {
            this.scheduledExecutorService.execute(r);
        } else {
            logger.warn("PullMessageServiceScheduledThread has shutdown");
        }
    }

    /** 返回延迟调度线程池。 */
    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    /** 根据 pull 请求查找消费者并执行 pull。 */
    private void pullMessage(final PullRequest pullRequest) {
        final MQConsumerInner consumer = this.mQClientFactory.selectConsumer(pullRequest.getConsumerGroup());
        if (consumer != null) {
            DefaultMQPushConsumerImpl impl = (DefaultMQPushConsumerImpl) consumer;
            impl.pullMessage(pullRequest);
        } else {
            logger.warn("No matched consumer for the PullRequest {}, drop it", pullRequest);
        }
    }

    /** 根据 POP 请求查找消费者并执行 pop。 */
    private void popMessage(final PopRequest popRequest) {
        final MQConsumerInner consumer = this.mQClientFactory.selectConsumer(popRequest.getConsumerGroup());
        if (consumer != null) {
            DefaultMQPushConsumerImpl impl = (DefaultMQPushConsumerImpl) consumer;
            impl.popMessage(popRequest);
        } else {
            logger.warn("No matched consumer for the PopRequest {}, drop it", popRequest);
        }
    }

    @Override
    public void run() {
        logger.info(this.getServiceName() + " service started");

        while (!this.isStopped()) {
            try {
                MessageRequest messageRequest = this.messageRequestQueue.take();
                if (messageRequest.getMessageRequestMode() == MessageRequestMode.POP) {
                    this.popMessage((PopRequest) messageRequest);
                } else {
                    this.pullMessage((PullRequest) messageRequest);
                }
            } catch (InterruptedException ignored) {
            } catch (Throwable e) {
                logger.error("Pull Message Service Run Method exception", e);
            }
        }

        logger.info(this.getServiceName() + " service end");
    }

    @Override
    /** 关闭服务并优雅停止调度线程池。 */
    public void shutdown(boolean interrupt) {
        super.shutdown(interrupt);
        ThreadUtils.shutdownGracefully(this.scheduledExecutorService, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public String getServiceName() {
        return PullMessageService.class.getSimpleName();
    }

}
