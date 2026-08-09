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
package org.apache.rocketmq.proxy.service.transaction;

import java.util.List;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.proxy.common.ProxyContext;

/**
 * Proxy 事务消息服务：管理事务订阅、缓存事务上下文并生成结束事务请求。
 */
public interface TransactionService {

    /** 为生产者组批量注册事务 Topic 订阅。 */
    void addTransactionSubscription(ProxyContext ctx, String group, List<String> topicList);

    /** 为生产者组注册单个事务 Topic 订阅。 */
    void addTransactionSubscription(ProxyContext ctx, String group, String topic);

    /** 用新 Topic 列表替换生产者组现有事务订阅。 */
    void replaceTransactionSubscription(ProxyContext ctx, String group, List<String> topicList);

    /** 取消生产者组全部事务 Topic 订阅。 */
    void unSubscribeAllTransactionTopic(ProxyContext ctx, String group);

    /** 按 Broker 地址缓存半消息对应的事务数据。 */
    TransactionData addTransactionDataByBrokerAddr(ProxyContext ctx, String brokerAddr, String topic, String producerGroup, long tranStateTableOffset, long commitLogOffset, String transactionId,
        Message message);

    /** 按 Broker 名称缓存半消息对应的事务数据。 */
    TransactionData addTransactionDataByBrokerName(ProxyContext ctx, String brokerName, String topic, String producerGroup, long tranStateTableOffset, long commitLogOffset, String transactionId,
        Message message);

    /** 构造提交或回滚事务的结束事务请求头。 */
    EndTransactionRequestData genEndTransactionRequestHeader(ProxyContext ctx, String topic, String producerGroup, Integer commitOrRollback,
        boolean fromTransactionCheck, String msgId, String transactionId);

    /** 事务状态回查发送失败时的回调处理。 */
    void onSendCheckTransactionStateFailed(ProxyContext context, String producerGroup, TransactionData transactionData);
}
