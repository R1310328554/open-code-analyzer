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
package org.apache.rocketmq.client.consumer;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.NamespaceUtil;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * Pull 消费者定时调度服务（遗留实现，建议改用 {@link DefaultLitePullConsumer} 主动拉取）。
 */
public class MQPullConsumerScheduleService {
    /** 日志记录器。 */
    private final Logger log = LoggerFactory.getLogger(MQPullConsumerScheduleService.class);
    /** 内部队列变更监听器。 */
    private final MessageQueueListener messageQueueListener = new MessageQueueListenerImpl();
    /** 队列到拉取任务的映射表。 */
    private final ConcurrentMap<MessageQueue, PullTaskImpl> taskTable =
        new ConcurrentHashMap<>();
    /** 底层 Pull 消费者实例。 */
    private DefaultMQPullConsumer defaultMQPullConsumer;
    /** 拉取线程池大小，默认 20。 */
    private int pullThreadNums = 20;
    /** Topic 到拉取回调的映射。 */
    private ConcurrentMap<String /* topic */, PullTaskCallback> callbackTable =
        new ConcurrentHashMap<>();
    /** 调度拉取任务的线程池。 */
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    /** 按消费组创建调度服务（集群模式）。 */
    public MQPullConsumerScheduleService(final String consumerGroup) {
        this.defaultMQPullConsumer = new DefaultMQPullConsumer(consumerGroup);
        this.defaultMQPullConsumer.setMessageModel(MessageModel.CLUSTERING);
    }

    /** 带 RPC Hook 的构造器。 */
    public MQPullConsumerScheduleService(final String consumerGroup, final RPCHook rpcHook) {
        this.defaultMQPullConsumer = new DefaultMQPullConsumer(consumerGroup, rpcHook);
        this.defaultMQPullConsumer.setMessageModel(MessageModel.CLUSTERING);
    }

    /** 同步 Topic 的拉取任务：移除已下线队列，为新队列创建任务。 */
    public void putTask(String topic, Set<MessageQueue> mqNewSet) {
        Iterator<Entry<MessageQueue, PullTaskImpl>> it = this.taskTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<MessageQueue, PullTaskImpl> next = it.next();
            if (next.getKey().getTopic().equals(topic)) {
                if (!mqNewSet.contains(next.getKey())) {
                    next.getValue().setCancelled(true);
                    it.remove();
                }
            }
        }

        for (MessageQueue mq : mqNewSet) {
            if (!this.taskTable.containsKey(mq)) {
                PullTaskImpl command = new PullTaskImpl(mq);
                this.taskTable.put(mq, command);
                this.scheduledThreadPoolExecutor.schedule(command, 0, TimeUnit.MILLISECONDS);

            }
        }
    }

    /** 启动调度线程池与底层 Pull 消费者。 */
    public void start() throws MQClientException {
        final String group = this.defaultMQPullConsumer.getConsumerGroup();
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(
            this.pullThreadNums,
            new ThreadFactoryImpl("PullMsgThread-" + group)
        );

        this.defaultMQPullConsumer.setMessageQueueListener(this.messageQueueListener);

        this.defaultMQPullConsumer.start();

        log.info("MQPullConsumerScheduleService start OK, {} {}",
            this.defaultMQPullConsumer.getConsumerGroup(), this.callbackTable);
    }

    /** 注册 Topic 的拉取任务回调。 */
    public void registerPullTaskCallback(final String topic, final PullTaskCallback callback) {
        this.callbackTable.put(NamespaceUtil.wrapNamespace(this.defaultMQPullConsumer.getNamespace(), topic), callback);
        this.defaultMQPullConsumer.registerMessageQueueListener(topic, null);
    }

    /** 关闭线程池与消费者。 */
    public void shutdown() {
        if (this.scheduledThreadPoolExecutor != null) {
            this.scheduledThreadPoolExecutor.shutdown();
        }

        if (this.defaultMQPullConsumer != null) {
            this.defaultMQPullConsumer.shutdown();
        }
    }

    /** 获取回调映射表。 */
    public ConcurrentMap<String, PullTaskCallback> getCallbackTable() {
        return callbackTable;
    }

    /** 设置回调映射表。 */
    public void setCallbackTable(ConcurrentHashMap<String, PullTaskCallback> callbackTable) {
        this.callbackTable = callbackTable;
    }

    /** 获取拉取线程数。 */
    public int getPullThreadNums() {
        return pullThreadNums;
    }

    /** 设置拉取线程数。 */
    public void setPullThreadNums(int pullThreadNums) {
        this.pullThreadNums = pullThreadNums;
    }

    /** 获取底层 Pull 消费者。 */
    public DefaultMQPullConsumer getDefaultMQPullConsumer() {
        return defaultMQPullConsumer;
    }

    /** 设置底层 Pull 消费者。 */
    public void setDefaultMQPullConsumer(DefaultMQPullConsumer defaultMQPullConsumer) {
        this.defaultMQPullConsumer = defaultMQPullConsumer;
    }

    /** 获取消息模式（广播/集群）。 */
    public MessageModel getMessageModel() {
        return this.defaultMQPullConsumer.getMessageModel();
    }

    /** 设置消息模式。 */
    public void setMessageModel(MessageModel messageModel) {
        this.defaultMQPullConsumer.setMessageModel(messageModel);
    }

    /** 队列变更时按消息模式更新拉取任务。 */
    class MessageQueueListenerImpl implements MessageQueueListener {
        @Override
        /** 广播使用全部队列，集群使用分配子集。 */
        public void messageQueueChanged(String topic, Set<MessageQueue> mqAll, Set<MessageQueue> mqDivided) {
            MessageModel messageModel =
                MQPullConsumerScheduleService.this.defaultMQPullConsumer.getMessageModel();
            switch (messageModel) {
                case BROADCASTING:
                    MQPullConsumerScheduleService.this.putTask(topic, mqAll);
                    break;
                case CLUSTERING:
                    MQPullConsumerScheduleService.this.putTask(topic, mqDivided);
                    break;
                default:
                    break;
            }
        }
    }

    /** 单队列周期性拉取任务。 */
    public class PullTaskImpl implements Runnable {
        /** 本任务绑定的队列。 */
        private final MessageQueue messageQueue;
        /** 是否已取消。 */
        private volatile boolean cancelled = false;

        /** 创建指定队列的拉取任务。 */
        public PullTaskImpl(final MessageQueue messageQueue) {
            this.messageQueue = messageQueue;
        }

        @Override
        /** 执行拉取回调并按上下文延迟调度下一次。 */
        public void run() {
            String topic = this.messageQueue.getTopic();
            if (!this.isCancelled()) {
                PullTaskCallback pullTaskCallback =
                    MQPullConsumerScheduleService.this.callbackTable.get(topic);
                if (pullTaskCallback != null) {
                    final PullTaskContext context = new PullTaskContext();
                    context.setPullConsumer(MQPullConsumerScheduleService.this.defaultMQPullConsumer);
                    try {
                        pullTaskCallback.doPullTask(this.messageQueue, context);
                    } catch (Throwable e) {
                        context.setPullNextDelayTimeMillis(1000);
                        log.error("doPullTask Exception", e);
                    }

                    if (!this.isCancelled()) {
                        MQPullConsumerScheduleService.this.scheduledThreadPoolExecutor.schedule(this,
                            context.getPullNextDelayTimeMillis(), TimeUnit.MILLISECONDS);
                    } else {
                        log.warn("The Pull Task is cancelled after doPullTask, {}", messageQueue);
                    }
                } else {
                    log.warn("Pull Task Callback not exist , {}", topic);
                }
            } else {
                log.warn("The Pull Task is cancelled, {}", messageQueue);
            }
        }

        /** 是否已取消。 */
        public boolean isCancelled() {
            return cancelled;
        }

        /** 设置取消标志。 */
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        /** 获取绑定的消息队列。 */
        public MessageQueue getMessageQueue() {
            return messageQueue;
        }
    }
}
