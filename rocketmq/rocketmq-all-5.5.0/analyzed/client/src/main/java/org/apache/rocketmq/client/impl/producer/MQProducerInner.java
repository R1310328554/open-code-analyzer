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
package org.apache.rocketmq.client.impl.producer;

import java.util.Set;
import org.apache.rocketmq.client.producer.TransactionCheckListener;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.header.CheckTransactionStateRequestHeader;

/**
 * Producer 内部接口：供 MQClientInstance 回调，管理 Topic 路由、
 * 事务状态回查监听及单元化模式等内部协作能力。
 */
public interface MQProducerInner {
    /** 返回当前 Producer 负责发布的 Topic 集合。 */
    Set<String> getPublishTopicList();

    /** 判断指定 Topic 的路由信息是否需要刷新。 */
    boolean isPublishTopicNeedUpdate(final String topic);

    /** 返回旧版事务回查监听器（已废弃接口）。 */
    TransactionCheckListener checkListener();
    /** 返回新版事务回查监听器。 */
    TransactionListener getCheckListener();

    /** Broker 发起事务状态回查时的回调入口。 */
    void checkTransactionState(
        final String addr,
        final MessageExt msg,
        final CheckTransactionStateRequestHeader checkRequestHeader);

    /** 更新 Topic 的发布路由信息。 */
    void updateTopicPublishInfo(final String topic, final TopicPublishInfo info);

    /** 是否处于单元化部署模式。 */
    boolean isUnitMode();
}
