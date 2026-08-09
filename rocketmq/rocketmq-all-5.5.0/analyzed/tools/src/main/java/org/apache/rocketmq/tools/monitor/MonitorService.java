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

package org.apache.rocketmq.tools.monitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.RandomUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.KeyBuilder;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.topic.TopicValidator;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;
import org.apache.rocketmq.remoting.protocol.body.Connection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.topic.OffsetMovedEvent;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;

/**
 * RocketMQ 集群监控主服务。
 * <p>定时拉取重试 Topic 消费进度、消费者运行态，并订阅系统 Topic 捕获偏移量迁移事件。
 * <p>可通过 {@link #main(String[])} 独立进程启动。
 */
public class MonitorService {
    /** 服务日志记录器。 */
    private final Logger logger = LoggerFactory.getLogger(MonitorService.class);
    /** 单线程调度器，驱动周期性监控任务。 */
    private final ScheduledExecutorService scheduledExecutorService = Executors
        .newSingleThreadScheduledExecutor(new ThreadFactoryImpl("MonitorService"));

    /** 监控配置（NameServer、轮询间隔等）。 */
    private final MonitorConfig monitorConfig;

    /** 监控事件回调监听器。 */
    private final MonitorListener monitorListener;

    /** 管理端扩展，用于查询 Topic、消费统计与运行态。 */
    private final DefaultMQAdminExt defaultMQAdminExt;
    /** 拉取消费者，用于计算队列最大延迟。 */
    private final DefaultMQPullConsumer defaultMQPullConsumer = new DefaultMQPullConsumer(
        MixAll.TOOLS_CONSUMER_GROUP);
    /** Push 消费者，订阅系统偏移量迁移事件 Topic。 */
    private final DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer(
        MixAll.MONITOR_CONSUMER_GROUP);

    /**
     * 构造监控服务并初始化 Admin/Pull/Push 客户端。
     * @param monitorConfig 运行配置
     * @param monitorListener 事件回调
     * @param rpcHook RPC 钩子（可为 null）
     */
    public MonitorService(MonitorConfig monitorConfig, MonitorListener monitorListener, RPCHook rpcHook) {
        this.monitorConfig = monitorConfig;
        this.monitorListener = monitorListener;

        this.defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        this.defaultMQAdminExt.setInstanceName(instanceName());
        this.defaultMQAdminExt.setNamesrvAddr(monitorConfig.getNamesrvAddr());

        this.defaultMQPullConsumer.setInstanceName(instanceName());
        this.defaultMQPullConsumer.setNamesrvAddr(monitorConfig.getNamesrvAddr());

        this.defaultMQPushConsumer.setInstanceName(instanceName());
        this.defaultMQPushConsumer.setNamesrvAddr(monitorConfig.getNamesrvAddr());
        try {
            this.defaultMQPushConsumer.setConsumeThreadMin(1);
            this.defaultMQPushConsumer.setConsumeThreadMax(1);
            // 订阅 Broker 偏移量迁移系统 Topic
            this.defaultMQPushConsumer.subscribe(TopicValidator.RMQ_SYS_OFFSET_MOVED_EVENT, "*");
            this.defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {

                /** 解码 OffsetMovedEvent 并转发给监听器。 */
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                    ConsumeConcurrentlyContext context) {
                    try {
                        OffsetMovedEvent ome =
                            OffsetMovedEvent.decode(msgs.get(0).getBody(), OffsetMovedEvent.class);

                        DeleteMsgsEvent deleteMsgsEvent = new DeleteMsgsEvent();
                        deleteMsgsEvent.setOffsetMovedEvent(ome);
                        deleteMsgsEvent.setEventTimestamp(msgs.get(0).getStoreTimestamp());

                        MonitorService.this.monitorListener.reportDeleteMsgsEvent(deleteMsgsEvent);
                    } catch (Exception e) {
                    }

                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
        } catch (MQClientException e) {
        }
    }

    /** 命令行入口：使用默认配置与 {@link DefaultMonitorListener} 启动监控。 */
    public static void main(String[] args) throws MQClientException {
        main0(args, null);
    }

    /**
     * 可注入 RPCHook 的启动入口，并注册 JVM 关闭钩子。
     * @param args 命令行参数（当前未使用）
     * @param rpcHook RPC 钩子
     */
    public static void main0(String[] args, RPCHook rpcHook) throws MQClientException {
        final MonitorService monitorService =
            new MonitorService(new MonitorConfig(), new DefaultMonitorListener(), rpcHook);
        monitorService.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            private volatile boolean hasShutdown = false;

            @Override
            public void run() {
                synchronized (this) {
                    if (!this.hasShutdown) {
                        this.hasShutdown = true;
                        monitorService.shutdown();
                    }
                }
            }
        }, "ShutdownHook"));
    }

    /** 基于时间戳与 NameServer 地址生成唯一客户端实例名。 */
    private String instanceName() {
        final int randomInteger = RandomUtils.nextInt(0, Integer.MAX_VALUE);
        String name = System.currentTimeMillis() + randomInteger + this.monitorConfig.getNamesrvAddr();
        return "MonitorService_" + name.hashCode();
    }

    /** 启动 Pull/Admin/Push 客户端并调度首轮监控任务。 */
    public void start() throws MQClientException {
        this.defaultMQPullConsumer.start();
        this.defaultMQAdminExt.start();
        this.defaultMQPushConsumer.start();
        this.startScheduleTask();
    }

    /** 关闭全部 MQ 客户端。 */
    public void shutdown() {
        this.defaultMQPullConsumer.shutdown();
        this.defaultMQAdminExt.shutdown();
        this.defaultMQPushConsumer.shutdown();
    }

    /** 按 {@link MonitorConfig#getRoundInterval()} 固定速率执行 {@link #doMonitorWork()}。 */
    private void startScheduleTask() {
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    MonitorService.this.doMonitorWork();
                } catch (Exception e) {
                    logger.error("doMonitorWork Exception", e);
                }
            }
        }, 1000 * 20, this.monitorConfig.getRoundInterval(), TimeUnit.MILLISECONDS);
    }

    /**
     * 执行一轮完整监控：遍历重试 Topic，上报积压与运行态。
     * @throws RemotingException 远程调用异常
     * @throws MQClientException 客户端异常
     * @throws InterruptedException 线程中断
     */
    public void doMonitorWork() throws RemotingException, MQClientException, InterruptedException {
        long beginTime = System.currentTimeMillis();
        this.monitorListener.beginRound();

        TopicList topicList = defaultMQAdminExt.fetchAllTopicList();
        for (String topic : topicList.getTopicList()) {
            // 仅处理 %RETRY% 前缀的重试 Topic，对应各消费组
            if (topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
                String consumerGroup = KeyBuilder.parseGroup(topic);

                try {
                    this.reportUndoneMsgs(consumerGroup);
                } catch (Exception e) {
                    // log.error("reportUndoneMsgs Exception", e);
                }

                try {
                    this.reportConsumerRunningInfo(consumerGroup);
                } catch (Exception e) {
                    // log.error("reportConsumerRunningInfo Exception", e);
                }
            }
        }
        this.monitorListener.endRound();
        long spentTimeMills = System.currentTimeMillis() - beginTime;
        logger.info("Execute one round monitor work, spent timemills: {}", spentTimeMills);
    }

    /** 按消费组汇总各 Topic 未消费消息并回调监听器。 */
    private void reportUndoneMsgs(final String consumerGroup) {
        ConsumeStats cs = null;
        try {
            cs = defaultMQAdminExt.examineConsumeStats(consumerGroup);
        } catch (Exception ignore) {
            return;
        }

        ConsumerConnection cc = null;
        try {
            cc = defaultMQAdminExt.examineConsumerConnectionInfo(consumerGroup);
        } catch (Exception ignore) {
            return;
        }

        if (cs != null) {

            // 将 offset 表按 Topic 维度拆分
            HashMap<String/* Topic */, ConsumeStats> csByTopic = new HashMap<>();
            {
                Iterator<Entry<MessageQueue, OffsetWrapper>> it = cs.getOffsetTable().entrySet().iterator();
                while (it.hasNext()) {
                    Entry<MessageQueue, OffsetWrapper> next = it.next();
                    MessageQueue mq = next.getKey();
                    OffsetWrapper ow = next.getValue();
                    ConsumeStats csTmp = csByTopic.get(mq.getTopic());
                    if (null == csTmp) {
                        csTmp = new ConsumeStats();
                        csByTopic.put(mq.getTopic(), csTmp);
                    }

                    csTmp.getOffsetTable().put(mq, ow);
                }
            }

            {
                Iterator<Entry<String, ConsumeStats>> it = csByTopic.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, ConsumeStats> next = it.next();
                    UndoneMsgs undoneMsgs = new UndoneMsgs();
                    undoneMsgs.setConsumerGroup(consumerGroup);
                    undoneMsgs.setTopic(next.getKey());
                    this.computeUndoneMsgs(undoneMsgs, next.getValue());
                    this.monitorListener.reportUndoneMsgs(undoneMsgs);
                    this.reportFailedMsgs(consumerGroup, next.getKey());
                }
            }
        }
    }

    /**
     * 拉取消费组各在线客户端（≥3.1.8）的运行态并上报。
     * @param consumerGroup 消费组名称
     */
    public void reportConsumerRunningInfo(final String consumerGroup) throws InterruptedException,
        MQBrokerException, RemotingException, MQClientException {
        ConsumerConnection cc = defaultMQAdminExt.examineConsumerConnectionInfo(consumerGroup);
        TreeMap<String, ConsumerRunningInfo> infoMap = new TreeMap<>();
        for (Connection c : cc.getConnectionSet()) {
            String clientId = c.getClientId();

            // 低版本客户端不支持运行态查询
            if (c.getVersion() < MQVersion.Version.V3_1_8_SNAPSHOT.ordinal()) {
                continue;
            }

            try {
                ConsumerRunningInfo info =
                    defaultMQAdminExt.getConsumerRunningInfo(consumerGroup, clientId, false);
                infoMap.put(clientId, info);
            } catch (Exception e) {
            }
        }

        if (!infoMap.isEmpty()) {
            this.monitorListener.reportConsumerRunningInfo(infoMap);
        }
    }

    /**
     * 根据消费统计计算总积压、单队列最大积压与最大消费延迟。
     * @param undoneMsgs 待填充的结果对象
     * @param consumeStats 该 Topic 的消费统计
     */
    private void computeUndoneMsgs(final UndoneMsgs undoneMsgs, final ConsumeStats consumeStats) {
        long total = 0;
        long singleMax = 0;
        long delayMax = 0;
        Iterator<Entry<MessageQueue, OffsetWrapper>> it = consumeStats.getOffsetTable().entrySet().iterator();
        while (it.hasNext()) {
            Entry<MessageQueue, OffsetWrapper> next = it.next();
            MessageQueue mq = next.getKey();
            OffsetWrapper ow = next.getValue();
            long diff = ow.getBrokerOffset() - ow.getConsumerOffset();

            if (diff > singleMax) {
                singleMax = diff;
            }

            if (diff > 0) {
                total += diff;
            }

            // 通过拉取队列末尾消息估算相对 lastTimestamp 的延迟
            if (ow.getLastTimestamp() > 0) {
                try {
                    long maxOffset = this.defaultMQPullConsumer.maxOffset(mq);
                    if (maxOffset > 0) {
                        PullResult pull = this.defaultMQPullConsumer.pull(mq, "*", maxOffset - 1, 1);
                        switch (pull.getPullStatus()) {
                            case FOUND:
                                long delay =
                                    pull.getMsgFoundList().get(0).getStoreTimestamp() - ow.getLastTimestamp();
                                if (delay > delayMax) {
                                    delayMax = delay;
                                }
                                break;
                            case NO_MATCHED_MSG:
                            case NO_NEW_MSG:
                            case OFFSET_ILLEGAL:
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }

        undoneMsgs.setUndoneMsgsTotal(total);
        undoneMsgs.setUndoneMsgsSingleMQ(singleMax);
        undoneMsgs.setUndoneMsgsDelayTimeMills(delayMax);
    }

    /** 预留：上报指定消费组/Topic 的失败消息统计（当前为空实现）。 */
    private void reportFailedMsgs(final String consumerGroup, final String topic) {

    }
}
