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

package org.apache.rocketmq.proxy.service.admin;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.constant.PermName;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.common.topic.TopicValidator;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.client.impl.mqclient.MQClientAPIExt;
import org.apache.rocketmq.client.impl.mqclient.MQClientAPIFactory;
import org.apache.rocketmq.proxy.service.route.TopicRouteHelper;

/**
 * {@link AdminService} 默认实现：经 NameServer 查询路由并在 Broker 上建 Topic。
 */
public class DefaultAdminService implements AdminService {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME);
    /** 访问 NameServer/Broker 的 MQ 客户端工厂。 */
    private final MQClientAPIFactory mqClientAPIFactory;

    /** 注入 MQ 客户端工厂。 */
    public DefaultAdminService(MQClientAPIFactory mqClientAPIFactory) {
        this.mqClientAPIFactory = mqClientAPIFactory;
    }

    @Override
    /** 直接向 NameServer 拉取路由判断 Topic 是否存在。 */
    public boolean topicExist(String topic) {
        boolean topicExist;
        TopicRouteData topicRouteData;
        try {
            topicRouteData = this.getTopicRouteDataDirectlyFromNameServer(topic);
            topicExist = topicRouteData != null;
        } catch (Throwable e) {
            topicExist = false;
        }

        return topicExist;
    }

    @Override
    /** 获取 createTopic 与 sampleTopic 路由后委派 {@link #createTopicOnBroker}。 */
    public boolean createTopicOnTopicBrokerIfNotExist(String createTopic, String sampleTopic, int wQueueNum,
        int rQueueNum, boolean examineTopic, int retryCheckCount) {
        TopicRouteData curTopicRouteData = new TopicRouteData();
        try {
            curTopicRouteData = this.getTopicRouteDataDirectlyFromNameServer(createTopic);
        } catch (Exception e) {
            if (!TopicRouteHelper.isTopicNotExistError(e)) {
                log.error("get cur topic route {} failed.", createTopic, e);
                return false;
            }
        }

        TopicRouteData sampleTopicRouteData = null;
        try {
            sampleTopicRouteData = this.getTopicRouteDataDirectlyFromNameServer(sampleTopic);
        } catch (Exception e) {
            log.error("create topic {} failed.", createTopic, e);
            return false;
        }

        if (sampleTopicRouteData == null || sampleTopicRouteData.getBrokerDatas().isEmpty()) {
            return false;
        }

        try {
            return this.createTopicOnBroker(createTopic, wQueueNum, rQueueNum, curTopicRouteData.getBrokerDatas(),
                sampleTopicRouteData.getBrokerDatas(), examineTopic, retryCheckCount);
        } catch (Exception e) {
            log.error("create topic {} failed.", createTopic, e);
        }
        return false;
    }

    @Override
    /** 对缺失 Broker 调用 createTopic，可选轮询确认 Topic 可见。 */
    public boolean createTopicOnBroker(String topic, int wQueueNum, int rQueueNum, List<BrokerData> curBrokerDataList,
        List<BrokerData> sampleBrokerDataList, boolean examineTopic, int retryCheckCount) throws Exception {
        Set<String> curBrokerAddr = new HashSet<>();
        if (curBrokerDataList != null) {
            for (BrokerData brokerData : curBrokerDataList) {
                curBrokerAddr.add(brokerData.getBrokerAddrs().get(MixAll.MASTER_ID));
            }
        }

        TopicConfig topicConfig = new TopicConfig();
        topicConfig.setTopicName(topic);
        topicConfig.setWriteQueueNums(wQueueNum);
        topicConfig.setReadQueueNums(rQueueNum);
        topicConfig.setPerm(PermName.PERM_READ | PermName.PERM_WRITE);

        for (BrokerData brokerData : sampleBrokerDataList) {
            String addr = brokerData.getBrokerAddrs() == null ? null : brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);
            if (addr == null) {
                continue;
            }
            if (curBrokerAddr.contains(addr)) {
                continue;
            }

            try {
                this.getClient().createTopic(addr, TopicValidator.AUTO_CREATE_TOPIC_KEY_TOPIC, topicConfig, Duration.ofSeconds(3).toMillis());
            } catch (Exception e) {
                log.error("create topic on broker failed. topic:{}, broker:{}", topicConfig, addr, e);
            }
        }

        if (examineTopic) {
            // 轮询确认 Topic 已在 NameServer 可见
            int count = retryCheckCount;
            while (count-- > 0) {
                if (this.topicExist(topic)) {
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
    }

    /** 经 MQ 客户端从 NameServer 获取 Topic 路由。 */
    protected TopicRouteData getTopicRouteDataDirectlyFromNameServer(String topic) throws Exception {
        return this.getClient().getTopicRouteInfoFromNameServer(topic, Duration.ofSeconds(3).toMillis());
    }

    /** 返回工厂持有的 MQ 客户端实例。 */
    protected MQClientAPIExt getClient() {
        return this.mqClientAPIFactory.getClient();
    }
}
