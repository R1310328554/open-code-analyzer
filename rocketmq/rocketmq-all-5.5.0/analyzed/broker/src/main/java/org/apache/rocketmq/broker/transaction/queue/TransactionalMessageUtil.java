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
package org.apache.rocketmq.broker.transaction.queue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageAccessor;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageExtBrokerInner;
import org.apache.rocketmq.common.sysflag.MessageSysFlag;
import org.apache.rocketmq.common.topic.TopicValidator;

/** 事务消息 topic/消费组命名与半消息重建、免疫期计算的静态工具。 */
public class TransactionalMessageUtil {
    /** Op 消息删除标记 tag。 */
    public static final String REMOVE_TAG = "d";
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final String OFFSET_SEPARATOR = ",";
    /** 半消息中记录客户端事务 ID 的用户属性键。 */
    public static final String TRANSACTION_ID = "__transactionId__";

    /** 返回队列模式 Op 消息系统 topic。 */
    public static String buildOpTopic() {
        return TopicValidator.RMQ_SYS_TRANS_OP_HALF_TOPIC;
    }

    /** 返回 RocksDB 模式 Op 消息系统 topic。 */
    public static String buildOpTopicForRocksDB() {
        return TopicValidator.RMQ_SYS_ROCKSDB_TRANS_OP_HALF_TOPIC;
    }

    /** 返回队列模式半消息系统 topic。 */
    public static String buildHalfTopic() {
        return TopicValidator.RMQ_SYS_TRANS_HALF_TOPIC;
    }

    /** 返回 RocksDB 模式半消息系统 topic。 */
    public static String buildHalfTopicForRocksDB() {
        return TopicValidator.RMQ_SYS_ROCKSDB_TRANS_HALF_TOPIC;
    }

    /** 返回 Broker 内部事务扫描用的系统消费组。 */
    public static String buildConsumerGroup() {
        return MixAll.CID_SYS_RMQ_TRANS;
    }

    /** 从半消息还原真实 topic/queue 并打上 TRANSACTION_PREPARED 标志。 */
    public static MessageExtBrokerInner buildTransactionalMessageFromHalfMessage(MessageExt msgExt) {
        final MessageExtBrokerInner msgInner = new MessageExtBrokerInner();
        msgInner.setWaitStoreMsgOK(false);
        msgInner.setMsgId(msgExt.getMsgId());
        msgInner.setTopic(msgExt.getProperty(MessageConst.PROPERTY_REAL_TOPIC));
        msgInner.setBody(msgExt.getBody());
        final String realQueueIdStr = msgExt.getProperty(MessageConst.PROPERTY_REAL_QUEUE_ID);
        if (StringUtils.isNumeric(realQueueIdStr)) {
            msgInner.setQueueId(Integer.parseInt(realQueueIdStr));
        }
        msgInner.setFlag(msgExt.getFlag());
        msgInner.setTagsCode(MessageExtBrokerInner.tagsString2tagsCode(msgInner.getTags()));
        msgInner.setBornTimestamp(msgExt.getBornTimestamp());
        msgInner.setBornHost(msgExt.getBornHost());
        msgInner.setTransactionId(msgExt.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX));

        MessageAccessor.setProperties(msgInner, msgExt.getProperties());
        MessageAccessor.putProperty(msgInner, MessageConst.PROPERTY_TRANSACTION_PREPARED, "true");
        MessageAccessor.clearProperty(msgInner, MessageConst.PROPERTY_TRANSACTION_PREPARED_QUEUE_OFFSET);
        MessageAccessor.clearProperty(msgInner, MessageConst.PROPERTY_REAL_QUEUE_ID);
        msgInner.setPropertiesString(MessageDecoder.messageProperties2String(msgInner.getProperties()));

        int sysFlag = msgExt.getSysFlag();
        sysFlag |= MessageSysFlag.TRANSACTION_PREPARED_TYPE;
        msgInner.setSysFlag(sysFlag);

        return msgInner;
    }

    /** 解析免疫期秒数字符串，返回不低于 transactionTimeout 的毫秒值。 */
    public static long getImmunityTime(String checkImmunityTimeStr, long transactionTimeout) {
        long checkImmunityTime = 0;

        try {
            checkImmunityTime = Long.parseLong(checkImmunityTimeStr) * 1000;
        } catch (Throwable ignored) {
        }

        // 若配置了自定义首次回查免疫期，不得低于 transactionTimeout
        // 默认免疫期等于 transactionTimeout
        if (checkImmunityTime < transactionTimeout) {
            checkImmunityTime = transactionTimeout;
        }
        return checkImmunityTime;
    }
}
