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
package org.apache.rocketmq.broker.longpolling;

import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * LMQ 长轮询挂起服务：继承 PullRequestHoldService，针对 lite 队列优化 hold 表清理逻辑。
 */
public class LmqPullRequestHoldService extends PullRequestHoldService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME);

    /** 绑定 Broker 控制器。 */
    public LmqPullRequestHoldService(BrokerController brokerController) {
        super(brokerController);
    }

    @Override
    /** Broker 容器模式下附加 broker 标识前缀。 */
    public String getServiceName() {
        if (brokerController != null && brokerController.getBrokerConfig().isInBrokerContainer()) {
            return this.brokerController.getBrokerIdentity().getIdentifier() + LmqPullRequestHoldService.class.getSimpleName();
        }
        return LmqPullRequestHoldService.class.getSimpleName();
    }

    @Override
    /** 扫描挂起 pull 请求，有新消息则唤醒；LMQ 队列为空时移除 hold 条目。 */
    public void checkHoldRequest() {
        for (String key : pullRequestTable.keySet()) {
            int idx = key.lastIndexOf(TOPIC_QUEUEID_SEPARATOR);
            if (idx <= 0 || idx >= key.length() - 1) {
                pullRequestTable.remove(key);
                continue;
            }
            String topic = key.substring(0, idx);
            int queueId = Integer.parseInt(key.substring(idx + 1));
            try {
                final long offset = brokerController.getMessageStore().getMaxOffsetInQueue(topic, queueId);
                this.notifyMessageArriving(topic, queueId, offset);
            } catch (Throwable e) {
                LOGGER.error("check hold request failed. topic={}, queueId={}", topic, queueId, e);
            }
            if (MixAll.isLmq(topic)) {
                ManyPullRequest mpr = pullRequestTable.get(key);
                if (mpr == null || mpr.getPullRequestList() == null || mpr.getPullRequestList().isEmpty()) {
                    pullRequestTable.remove(key);
                }
            }
        }
    }
}
