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

package org.apache.rocketmq.proxy.service.receipt;

import com.google.common.base.Stopwatch;
import io.netty.channel.Channel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.broker.client.ClientChannelInfo;
import org.apache.rocketmq.broker.client.ConsumerGroupEvent;
import org.apache.rocketmq.broker.client.ConsumerIdsChangeListener;
import org.apache.rocketmq.broker.client.ConsumerManager;
import org.apache.rocketmq.client.consumer.AckResult;
import org.apache.rocketmq.client.consumer.AckStatus;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.consumer.ReceiptHandle;
import org.apache.rocketmq.common.state.StateEventListener;
import org.apache.rocketmq.common.thread.ThreadPoolMonitor;
import org.apache.rocketmq.common.utils.AbstractStartAndShutdown;
import org.apache.rocketmq.common.utils.ConcurrentHashMapUtils;
import org.apache.rocketmq.common.utils.ExceptionUtils;
import org.apache.rocketmq.common.utils.StartAndShutdown;
import org.apache.rocketmq.common.utils.ThreadUtils;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.proxy.common.MessageReceiptHandle;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.common.ProxyException;
import org.apache.rocketmq.proxy.common.ProxyExceptionCode;
import org.apache.rocketmq.proxy.common.ReceiptHandleGroup;
import org.apache.rocketmq.proxy.common.ReceiptHandleGroupKey;
import org.apache.rocketmq.proxy.common.RenewEvent;
import org.apache.rocketmq.proxy.common.RenewStrategyPolicy;
import org.apache.rocketmq.proxy.common.channel.ChannelHelper;
import org.apache.rocketmq.proxy.config.ConfigurationManager;
import org.apache.rocketmq.proxy.config.ProxyConfig;
import org.apache.rocketmq.proxy.service.metadata.MetadataService;
import org.apache.rocketmq.remoting.protocol.subscription.RetryPolicy;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;

/**
 * 默认回执句柄管理器：维护消费回执、定时续期与客户端离线清理。
 */
public class DefaultReceiptHandleManager extends AbstractStartAndShutdown implements ReceiptHandleManager {
    protected final static Logger log = LoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME);
    /** 订阅组元数据查询服务。 */
    protected final MetadataService metadataService;
    /** 消费者组与通道管理器。 */
    protected final ConsumerManager consumerManager;
    /** 按通道与消费者组索引的回执句柄分组表。 */
    protected final ConcurrentMap<ReceiptHandleGroupKey, ReceiptHandleGroup> receiptHandleGroupMap;
    /** 续期/清理事件监听器，触发 Broker 侧 ACK 操作。 */
    protected final StateEventListener<RenewEvent> eventListener;
    /** 续期重试间隔策略。 */
    protected final static RetryPolicy RENEW_POLICY = new RenewStrategyPolicy();
    protected final ScheduledExecutorService scheduledExecutorService =
        ThreadUtils.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("RenewalScheduledThread_"));
    protected final ThreadPoolExecutor renewalWorkerService;
    protected final ThreadPoolExecutor returnHandleGroupWorkerService;

    /** 初始化线程池、消费者注销监听与定时续期任务。 */
    public DefaultReceiptHandleManager(MetadataService metadataService, ConsumerManager consumerManager, StateEventListener<RenewEvent> eventListener) {
        this.metadataService = metadataService;
        this.consumerManager = consumerManager;
        this.eventListener = eventListener;
        ProxyConfig proxyConfig = ConfigurationManager.getProxyConfig();
        this.renewalWorkerService = ThreadPoolMonitor.createAndMonitor(
            proxyConfig.getRenewThreadPoolNums(),
            proxyConfig.getRenewMaxThreadPoolNums(),
            1, TimeUnit.MINUTES,
            "RenewalWorkerThread",
            proxyConfig.getRenewThreadPoolQueueCapacity()
        );
        this.returnHandleGroupWorkerService = ThreadPoolMonitor.createAndMonitor(
            proxyConfig.getReturnHandleGroupThreadPoolNums(),
            proxyConfig.getReturnHandleGroupThreadPoolNums() * 2,
            1, TimeUnit.MINUTES,
            "ReturnHandleGroupWorkerThread",
            proxyConfig.getRenewThreadPoolQueueCapacity()
        );
        consumerManager.appendConsumerIdsChangeListener(new ConsumerIdsChangeListener() {
            @Override
            public void handle(ConsumerGroupEvent event, String group, Object... args) {
                if (ConsumerGroupEvent.CLIENT_UNREGISTER.equals(event)) {
                    if (args == null || args.length < 1) {
                        return;
                    }
                    if (args[0] instanceof ClientChannelInfo) {
                        ClientChannelInfo clientChannelInfo = (ClientChannelInfo) args[0];
                        if (ChannelHelper.isRemote(clientChannelInfo.getChannel())) {
                            // 来自其他 Proxy 同步的远程通道过期时不清理本机连接数据
                            return;
                        }
                        clearGroup(new ReceiptHandleGroupKey(clientChannelInfo.getChannel(), group));
                        log.info("clear handle of this client when client unregister. group:{}, clientChannelInfo:{}", group, clientChannelInfo);
                    }
                }
            }

            @Override
            public void shutdown() {

            }
        });
        this.receiptHandleGroupMap = new ConcurrentHashMap<>();
        this.renewalWorkerService.setRejectedExecutionHandler((r, executor) -> log.warn("add renew task failed. queueSize:{}", executor.getQueue().size()));
        this.appendStartAndShutdown(new StartAndShutdown() {
            @Override
            public void start() throws Exception {
                scheduledExecutorService.scheduleWithFixedDelay(() -> scheduleRenewTask(), 0,
                    ConfigurationManager.getProxyConfig().getRenewSchedulePeriodMillis(), TimeUnit.MILLISECONDS);
            }

            @Override
            public void shutdown() throws Exception {
                scheduledExecutorService.shutdown();
                clearAllHandle();
            }
        });
    }

    /** 将消息回执句柄登记到指定通道与消费者组。 */
    public void addReceiptHandle(ProxyContext context, Channel channel, String group, String msgID, MessageReceiptHandle messageReceiptHandle) {
        ConcurrentHashMapUtils.computeIfAbsent(this.receiptHandleGroupMap, new ReceiptHandleGroupKey(channel, group),
            k -> new ReceiptHandleGroup()).put(msgID, messageReceiptHandle);
    }

    /** ACK 成功后移除对应回执句柄并返回被移除实例。 */
    public MessageReceiptHandle removeReceiptHandle(ProxyContext context, Channel channel, String group, String msgID, String receiptHandle) {
        ReceiptHandleGroup handleGroup = receiptHandleGroupMap.get(new ReceiptHandleGroupKey(channel, group));
        if (handleGroup == null) {
            return null;
        }
        return handleGroup.remove(msgID, receiptHandle);
    }

    /** 统计指定通道与消费者组下未 ACK 消息数量。 */
    public int getUnackedMessageCount(ProxyContext context, Channel channel, String group) {
        ReceiptHandleGroup handleGroup = receiptHandleGroupMap.get(new ReceiptHandleGroupKey(channel, group));
        return handleGroup == null ? 0 : handleGroup.getMsgCount();
    }

    /** 判断消费者通道是否已从 {@link ConsumerManager} 注销。 */
    protected boolean clientIsOffline(ReceiptHandleGroupKey groupKey) {
        return this.consumerManager.findChannel(groupKey.getGroup(), groupKey.getChannel()) == null;
    }

    /** 定时扫描各分组，对即将过期的回执提交续期任务。 */
    protected void scheduleRenewTask() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            ProxyConfig proxyConfig = ConfigurationManager.getProxyConfig();
            for (Map.Entry<ReceiptHandleGroupKey, ReceiptHandleGroup> entry : receiptHandleGroupMap.entrySet()) {
                ReceiptHandleGroupKey key = entry.getKey();
                if (clientIsOffline(key)) {
                    clearGroup(key);
                    continue;
                }

                ReceiptHandleGroup group = entry.getValue();
                group.scan((msgID, handleStr, v) -> {
                    long current = System.currentTimeMillis();
                    ReceiptHandle handle = ReceiptHandle.decode(v.getReceiptHandleStr());
                    if (handle.getNextVisibleTime() - current > proxyConfig.getRenewAheadTimeMillis()) {
                        return;
                    }
                    renewalWorkerService.submit(() -> renewMessage(createContext("RenewMessage"), key, group,
                        msgID, handleStr));
                });
            }
        } catch (Exception e) {
            log.error("unexpect error when schedule renew task", e);
        }

        log.debug("scan for renewal done. cost:{}ms", stopwatch.elapsed().toMillis());
    }

    /** 在分组锁内触发单条消息回执续期。 */
    protected void renewMessage(ProxyContext context, ReceiptHandleGroupKey key, ReceiptHandleGroup group, String msgID, String handleStr) {
        try {
            group.computeIfPresent(msgID, handleStr, messageReceiptHandle -> startRenewMessage(context, key, messageReceiptHandle), 0);
        } catch (Exception e) {
            log.error("error when renew message. msgID:{}, handleStr:{}", msgID, handleStr, e);
        }
    }

    /** 发起续期或停止续期（NACK）并更新回执状态。 */
    protected CompletableFuture<MessageReceiptHandle> startRenewMessage(ProxyContext context, ReceiptHandleGroupKey key, MessageReceiptHandle messageReceiptHandle) {
        CompletableFuture<MessageReceiptHandle> resFuture = new CompletableFuture<>();
        ProxyConfig proxyConfig = ConfigurationManager.getProxyConfig();
        long current = System.currentTimeMillis();
        try {
            if (messageReceiptHandle.getRenewRetryTimes() >= proxyConfig.getMaxRenewRetryTimes()) {
                log.warn("handle has exceed max renewRetryTimes. handle:{}", messageReceiptHandle);
                return CompletableFuture.completedFuture(null);
            }
            if (current - messageReceiptHandle.getConsumeTimestamp() < proxyConfig.getRenewMaxTimeMillis()) {
                CompletableFuture<AckResult> future = new CompletableFuture<>();
                eventListener.fireEvent(new RenewEvent(key, messageReceiptHandle, RENEW_POLICY.nextDelayDuration(messageReceiptHandle.getRenewTimes()), RenewEvent.EventType.RENEW, future));
                future.whenComplete((ackResult, throwable) -> {
                    if (throwable != null) {
                        log.error("error when renew. handle:{}", messageReceiptHandle, throwable);
                        if (renewExceptionNeedRetry(throwable)) {
                            messageReceiptHandle.incrementAndGetRenewRetryTimes();
                            resFuture.complete(messageReceiptHandle);
                        } else {
                            resFuture.complete(null);
                        }
                    } else if (AckStatus.OK.equals(ackResult.getStatus())) {
                        messageReceiptHandle.updateReceiptHandle(ackResult.getExtraInfo());
                        messageReceiptHandle.resetRenewRetryTimes();
                        messageReceiptHandle.incrementRenewTimes();
                        resFuture.complete(messageReceiptHandle);
                    } else {
                        log.error("renew response is not ok. result:{}, handle:{}", ackResult, messageReceiptHandle);
                        resFuture.complete(null);
                    }
                });
            } else {
                SubscriptionGroupConfig subscriptionGroupConfig =
                    metadataService.getSubscriptionGroupConfig(context, messageReceiptHandle.getGroup());
                if (subscriptionGroupConfig == null) {
                    log.error("group's subscriptionGroupConfig is null when renew. handle: {}", messageReceiptHandle);
                    return CompletableFuture.completedFuture(null);
                }
                RetryPolicy retryPolicy = subscriptionGroupConfig.getGroupRetryPolicy().getRetryPolicy();
                CompletableFuture<AckResult> future = new CompletableFuture<>();
                eventListener.fireEvent(new RenewEvent(key, messageReceiptHandle, retryPolicy.nextDelayDuration(messageReceiptHandle.getReconsumeTimes()), RenewEvent.EventType.STOP_RENEW, future));
                future.whenComplete((ackResult, throwable) -> {
                    if (throwable != null) {
                        log.error("error when nack in renew. handle:{}", messageReceiptHandle, throwable);
                    }
                    resFuture.complete(null);
                });
            }
        } catch (Throwable t) {
            log.error("unexpect error when renew message, stop to renew it. handle:{}", messageReceiptHandle, t);
            resFuture.complete(null);
        }
        return resFuture;
    }

    /** 移除分组并异步归还其中全部回执句柄。 */
    protected void clearGroup(ReceiptHandleGroupKey key) {
        if (key == null) {
            return;
        }
        ReceiptHandleGroup handleGroup = receiptHandleGroupMap.remove(key);
        returnHandleGroupWorkerService.submit(() -> returnHandleGroup(key, handleGroup));
    }

    // 不再阻塞等待锁；仅立即处理已加锁句柄，未获锁的留待下次调度
    private void returnHandleGroup(ReceiptHandleGroupKey key, ReceiptHandleGroup handleGroup) {
        if (handleGroup == null || handleGroup.isEmpty()) {
            return;
        }
        ProxyConfig proxyConfig = ConfigurationManager.getProxyConfig();
        handleGroup.scan((msgID, handle, v) -> {
            try {
                handleGroup.computeIfPresent(msgID, handle, messageReceiptHandle -> {
                    CompletableFuture<AckResult> future = new CompletableFuture<>();
                    eventListener.fireEvent(new RenewEvent(key, messageReceiptHandle, proxyConfig.getInvisibleTimeMillisWhenClear(), RenewEvent.EventType.CLEAR_GROUP, future));
                    return CompletableFuture.completedFuture(null);
                }, 0);
            } catch (Exception e) {
                log.error("error when clear handle for group. key:{}", key, e);
            }
        });
        // scheduleRenewTask 将再次触发清理
        if (!handleGroup.isEmpty()) {
            log.warn("The handle cannot be completely cleared, the remaining quantity is {}, key:{}", handleGroup.getHandleNum(), key);
            receiptHandleGroupMap.putIfAbsent(key, handleGroup);
        }
    }

    /** 关闭时清空全部回执句柄分组。 */
    protected void clearAllHandle() {
        log.info("start clear all handle in receiptHandleProcessor");
        Set<ReceiptHandleGroupKey> keySet = receiptHandleGroupMap.keySet();
        for (ReceiptHandleGroupKey key : keySet) {
            clearGroup(key);
        }
        log.info("clear all handle in receiptHandleProcessor done");
    }

    /** 判断续期异常是否可重试（无效 Broker/句柄不重试）。 */
    protected boolean renewExceptionNeedRetry(Throwable t) {
        t = ExceptionUtils.getRealException(t);
        if (t instanceof ProxyException) {
            ProxyException proxyException = (ProxyException) t;
            if (ProxyExceptionCode.INVALID_BROKER_NAME.equals(proxyException.getCode()) ||
                ProxyExceptionCode.INVALID_RECEIPT_HANDLE.equals(proxyException.getCode())) {
                return false;
            }
        }
        return true;
    }

    /** 创建内部调用的 {@link ProxyContext}。 */
    protected ProxyContext createContext(String actionName) {
        return ProxyContext.createForInner(this.getClass().getSimpleName() + actionName);
    }
}
