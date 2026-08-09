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
package org.apache.rocketmq.broker.util;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.broker.schedule.ScheduleMessageService;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.message.MessageAccessor;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageExtBrokerInner;
import org.apache.rocketmq.common.sysflag.MessageSysFlag;
import org.apache.rocketmq.common.topic.TopicValidator;
import org.apache.rocketmq.common.utils.QueueTypeUtils;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.store.PutMessageResult;
import org.apache.rocketmq.store.PutMessageStatus;
import org.apache.rocketmq.store.config.BrokerRole;
import org.apache.rocketmq.store.queue.ConsumeQueueStoreInterface;
import org.apache.rocketmq.store.timer.TimerMessageStore;

/**
 * Broker 入队前校验与消息变换工具：topic/存储状态检查、
 * 定时/延迟/LMQ 配额处理及消息回退发送。
 */
public class HookUtils {

    protected static final Logger LOG = LoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME);

    private static final AtomicLong PRINT_TIMES = new AtomicLong(0);

    /** Linux 下单个文件名最大 255 字节；topic 名长度上限与此对齐。 */
    private static final Integer MAX_TOPIC_LENGTH = 255;

    /** 入队前校验：存储可用性、Broker 角色、topic/body 合法性及页缓存压力。 */
    public static PutMessageResult checkBeforePutMessage(BrokerController brokerController, final MessageExt msg) {
        if (brokerController.getMessageStore().isShutdown()) {
            LOG.warn("message store has shutdown, so putMessage is forbidden");
            return new PutMessageResult(PutMessageStatus.SERVICE_NOT_AVAILABLE, null);
        }

        if (!brokerController.getMessageStoreConfig().isDuplicationEnable() && BrokerRole.SLAVE == brokerController.getMessageStoreConfig().getBrokerRole()) {
            long value = PRINT_TIMES.getAndIncrement();
            if ((value % 50000) == 0) {
                LOG.warn("message store is in slave mode, so putMessage is forbidden ");
            }

            return new PutMessageResult(PutMessageStatus.SERVICE_NOT_AVAILABLE, null);
        }

        if (!brokerController.getMessageStore().getRunningFlags().isWriteable()) {
            long value = PRINT_TIMES.getAndIncrement();
            if ((value % 50000) == 0) {
                LOG.warn("message store is not writeable, so putMessage is forbidden " + brokerController.getMessageStore().getRunningFlags().getFlagBits());
            }

            return new PutMessageResult(PutMessageStatus.SERVICE_NOT_AVAILABLE, null);
        } else {
            PRINT_TIMES.set(0);
        }

        final byte[] topicData = msg.getTopic().getBytes(MessageDecoder.CHARSET_UTF8);
        boolean retryTopic = msg.getTopic() != null && msg.getTopic().startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX);
        if (!retryTopic && topicData.length > Byte.MAX_VALUE) {
            LOG.warn("putMessage message topic[{}] length too long {}, but it is not supported by broker",
                msg.getTopic(), topicData.length);
            return new PutMessageResult(PutMessageStatus.MESSAGE_ILLEGAL, null);
        }

        if (topicData.length > MAX_TOPIC_LENGTH) {
            LOG.warn("putMessage message topic[{}] length too long {}, but it is not supported by broker",
                msg.getTopic(), topicData.length);
            return new PutMessageResult(PutMessageStatus.MESSAGE_ILLEGAL, null);
        }

        if (msg.getBody() == null) {
            LOG.warn("putMessage message topic[{}], but message body is null", msg.getTopic());
            return new PutMessageResult(PutMessageStatus.MESSAGE_ILLEGAL, null);
        }

        if (brokerController.getMessageStore().isOSPageCacheBusy()) {
            return new PutMessageResult(PutMessageStatus.OS_PAGE_CACHE_BUSY, null);
        }
        return null;
    }

    /** 校验内部批量消息标志与 ConsumeQueue 类型是否一致。 */
    public static PutMessageResult checkInnerBatch(BrokerController brokerController, final MessageExt msg) {
        if (msg.getProperties().containsKey(MessageConst.PROPERTY_INNER_NUM)
            && !MessageSysFlag.check(msg.getSysFlag(), MessageSysFlag.INNER_BATCH_FLAG)) {
            LOG.warn("[BUG]The message had property {} but is not an inner batch", MessageConst.PROPERTY_INNER_NUM);
            return new PutMessageResult(PutMessageStatus.MESSAGE_ILLEGAL, null);
        }

        if (MessageSysFlag.check(msg.getSysFlag(), MessageSysFlag.INNER_BATCH_FLAG)) {
            Optional<TopicConfig> topicConfig = Optional.ofNullable(brokerController.getTopicConfigManager().getTopicConfigTable().get(msg.getTopic()));
            if (!QueueTypeUtils.isBatchCq(topicConfig)) {
                LOG.error("[BUG]The message is an inner batch but cq type is not batch cq");
                return new PutMessageResult(PutMessageStatus.MESSAGE_ILLEGAL, null);
            }
        }

        return null;
    }

    /** 非事务消息处理定时轮与 delayLevel 延迟投递变换。 */
    public static PutMessageResult handleScheduleMessage(BrokerController brokerController,
        final MessageExtBrokerInner msg) {
        final int tranType = MessageSysFlag.getTransactionValue(msg.getSysFlag());
        if (tranType == MessageSysFlag.TRANSACTION_NOT_TYPE
            || tranType == MessageSysFlag.TRANSACTION_COMMIT_TYPE) {
            if (!isRolledTimerMessage(msg)) {
                if (checkIfTimerMessage(msg)) {
                    if (!brokerController.getMessageStoreConfig().isTimerWheelEnable()) {
                        // 未启用定时轮时拒绝定时消息
                        return new PutMessageResult(PutMessageStatus.WHEEL_TIMER_NOT_ENABLE, null);
                    }
                    PutMessageResult transformRes = transformTimerMessage(brokerController, msg);
                    if (null != transformRes) {
                        return transformRes;
                    }
                }
            }
            // 延迟级别投递：改写 topic 为系统 schedule topic
            if (msg.getDelayTimeLevel() > 0) {
                transformDelayLevelMessage(brokerController, msg);
            }
        }
        return null;
    }

    /** 校验 LMQ 多路分发目标数量是否超过配额。 */
    public static PutMessageResult handleLmqQuota(BrokerController brokerController, final MessageExtBrokerInner msg) {
        if (!brokerController.getMessageStoreConfig().isEnableLmqQuota()
            || !brokerController.getMessageStoreConfig().isEnableLmq()
            || !brokerController.getMessageStoreConfig().isEnableMultiDispatch()
            || !msg.needDispatchLMQ()) {
            return null;
        }

        ConsumeQueueStoreInterface cqStore = brokerController.getMessageStore().getQueueStore();
        String[] queueNames =
            msg.getProperty(MessageConst.PROPERTY_INNER_MULTI_DISPATCH).split(MixAll.LMQ_DISPATCH_SEPARATOR);
        for (String queueName : queueNames) {
            if (!MixAll.isLmq(queueName)) {
                continue;
            }
            if (cqStore.getLmqNum() >= brokerController.getMessageStoreConfig().getMaxLmqConsumeQueueNum()) {
                if (!cqStore.isLmqExist(queueName)) {
                    return new PutMessageResult(PutMessageStatus.LMQ_CONSUME_QUEUE_NUM_EXCEEDED, null);
                }
            }
        }
        return null;
    }

    private static boolean isRolledTimerMessage(MessageExtBrokerInner msg) {
        return TimerMessageStore.TIMER_TOPIC.equals(msg.getTopic());
    }

    public static boolean checkIfTimerMessage(MessageExtBrokerInner msg) {
        if (msg.getDelayTimeLevel() > 0) {
            if (null != msg.getProperty(MessageConst.PROPERTY_TIMER_DELIVER_MS)) {
                MessageAccessor.clearProperty(msg, MessageConst.PROPERTY_TIMER_DELIVER_MS);
            }
            if (null != msg.getProperty(MessageConst.PROPERTY_TIMER_DELAY_SEC)) {
                MessageAccessor.clearProperty(msg, MessageConst.PROPERTY_TIMER_DELAY_SEC);
            }
            if (null != msg.getProperty(MessageConst.PROPERTY_TIMER_DELAY_MS)) {
                MessageAccessor.clearProperty(msg, MessageConst.PROPERTY_TIMER_DELAY_MS);
            }
            return false;
            //return this.defaultMessageStore.getMessageStoreConfig().isTimerInterceptDelayLevel();
        }
        // 二次确认：排除 delayLevel 与 timer topic 冲突
        if (TimerMessageStore.TIMER_TOPIC.equals(msg.getTopic()) || null != msg.getProperty(MessageConst.PROPERTY_TIMER_OUT_MS)) {
            return false;
        }
        return null != msg.getProperty(MessageConst.PROPERTY_TIMER_DELIVER_MS) || null != msg.getProperty(MessageConst.PROPERTY_TIMER_DELAY_MS) || null != msg.getProperty(MessageConst.PROPERTY_TIMER_DELAY_SEC);
    }

    private static PutMessageResult transformTimerMessage(BrokerController brokerController,
        MessageExtBrokerInner msg) {
        // 计算 deliverMs 并改写为 timer topic 消息
        int delayLevel = msg.getDelayTimeLevel();
        long deliverMs;
        try {
            if (msg.getProperty(MessageConst.PROPERTY_TIMER_DELAY_SEC) != null) {
                deliverMs = System.currentTimeMillis() + Long.parseLong(msg.getProperty(MessageConst.PROPERTY_TIMER_DELAY_SEC)) * 1000;
            } else if (msg.getProperty(MessageConst.PROPERTY_TIMER_DELAY_MS) != null) {
                deliverMs = System.currentTimeMillis() + Long.parseLong(msg.getProperty(MessageConst.PROPERTY_TIMER_DELAY_MS));
            } else {
                deliverMs = Long.parseLong(msg.getProperty(MessageConst.PROPERTY_TIMER_DELIVER_MS));
            }
        } catch (Exception e) {
            return new PutMessageResult(PutMessageStatus.WHEEL_TIMER_MSG_ILLEGAL, null);
        }
        if (deliverMs > System.currentTimeMillis()) {
            if (delayLevel <= 0 && deliverMs - System.currentTimeMillis() > brokerController.getMessageStoreConfig().getTimerMaxDelaySec() * 1000L) {
                return new PutMessageResult(PutMessageStatus.WHEEL_TIMER_MSG_ILLEGAL, null);
            }

            int timerPrecisionMs = brokerController.getMessageStoreConfig().getTimerPrecisionMs();
            if (deliverMs % timerPrecisionMs == 0) {
                deliverMs -= timerPrecisionMs;
            } else {
                deliverMs = deliverMs / timerPrecisionMs * timerPrecisionMs;
            }

            if (brokerController.getTimerMessageStore().isReject(deliverMs)) {
                return new PutMessageResult(PutMessageStatus.WHEEL_TIMER_FLOW_CONTROL, null);
            }
            MessageAccessor.putProperty(msg, MessageConst.PROPERTY_TIMER_OUT_MS, deliverMs + "");
            MessageAccessor.putProperty(msg, MessageConst.PROPERTY_REAL_TOPIC, msg.getTopic());
            MessageAccessor.putProperty(msg, MessageConst.PROPERTY_REAL_QUEUE_ID, String.valueOf(msg.getQueueId()));
            msg.setPropertiesString(MessageDecoder.messageProperties2String(msg.getProperties()));
            msg.setTopic(TimerMessageStore.TIMER_TOPIC);
            msg.setQueueId(0);
        } else if (null != msg.getProperty(MessageConst.PROPERTY_TIMER_DEL_UNIQKEY)) {
            return new PutMessageResult(PutMessageStatus.WHEEL_TIMER_MSG_ILLEGAL, null);
        }
        return null;
    }

    /** 将 delayLevel 消息路由到 {@link TopicValidator#RMQ_SYS_SCHEDULE_TOPIC} 对应队列。 */
    public static void transformDelayLevelMessage(BrokerController brokerController, MessageExtBrokerInner msg) {

        if (msg.getDelayTimeLevel() > brokerController.getScheduleMessageService().getMaxDelayLevel()) {
            msg.setDelayTimeLevel(brokerController.getScheduleMessageService().getMaxDelayLevel());
        }

        // 备份真实 topic 与 queueId 到用户属性
        MessageAccessor.putProperty(msg, MessageConst.PROPERTY_REAL_TOPIC, msg.getTopic());
        MessageAccessor.putProperty(msg, MessageConst.PROPERTY_REAL_QUEUE_ID, String.valueOf(msg.getQueueId()));
        msg.setPropertiesString(MessageDecoder.messageProperties2String(msg.getProperties()));

        msg.setTopic(TopicValidator.RMQ_SYS_SCHEDULE_TOPIC);
        msg.setQueueId(ScheduleMessageService.delayLevel2QueueId(msg.getDelayTimeLevel()));
    }

    /** 将消息列表逐条回发到指定 Broker 地址。 */
    public static boolean sendMessageBack(BrokerController brokerController, List<MessageExt> msgList,
        String brokerName, String brokerAddr) {
        try {
            Iterator<MessageExt> it = msgList.iterator();
            while (it.hasNext()) {
                MessageExt msg = it.next();
                msg.setWaitStoreMsgOK(false);
                brokerController.getBrokerOuterAPI().sendMessageToSpecificBroker(brokerAddr, brokerName, msg, "InnerSendMessageBackGroup", 3000);
                it.remove();
            }
        } catch (Exception e) {
            LOG.error("send message back to broker {} addr {} failed", brokerName, brokerAddr, e);
            return false;
        }
        return true;
    }
}
