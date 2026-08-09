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
import org.apache.rocketmq.common.BrokerConfig;
import org.apache.rocketmq.proxy.common.ProxyContext;

/**
 * 本地（Broker 内嵌）事务服务：生产者通道已由 Broker producerManager 管理，无需额外心跳订阅。
 */
/** 内嵌 Broker 模式下的 {@link TransactionService} 实现。 */
public class LocalTransactionService extends AbstractTransactionService {

    protected final BrokerConfig brokerConfig;

    /** @param brokerConfig 本地 Broker 配置 */
    public LocalTransactionService(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
    }

    @Override
    /** 本地模式无需维护事务主题订阅。 */
    public void addTransactionSubscription(ProxyContext ctx, String group, List<String> topicList) {

    }

    @Override
    public void addTransactionSubscription(ProxyContext ctx, String group, String topic) {

    }

    @Override
    public void replaceTransactionSubscription(ProxyContext ctx, String group, List<String> topicList) {

    }

    @Override
    public void unSubscribeAllTransactionTopic(ProxyContext ctx, String group) {

    }

    @Override
    /** 直接返回本地 Broker 名称。 */
    protected String getBrokerNameByAddr(String brokerAddr) {
        return this.brokerConfig.getBrokerName();
    }
}
