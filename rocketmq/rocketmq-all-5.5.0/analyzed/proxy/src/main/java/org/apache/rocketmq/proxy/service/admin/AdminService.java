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

import java.util.List;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;

/**
 * Proxy 运维管理接口：Topic 存在性检查与按需创建。
 */
public interface AdminService {

    /** 查询 {@code topic} 是否已在 NameServer 注册路由。 */
    boolean topicExist(String topic);

    /**
     * 参照 sampleTopic 所在 Broker 为 createTopic 补建路由。
     *
     * @param createTopic 待创建 Topic
     * @param sampleTopic 参照 Topic
     * @param wQueueNum 写队列数
     * @param rQueueNum 读队列数
     * @param examineTopic 是否轮询确认创建成功
     * @param retryCheckCount 确认重试次数
     */
    boolean createTopicOnTopicBrokerIfNotExist(String createTopic, String sampleTopic, int wQueueNum,
        int rQueueNum, boolean examineTopic, int retryCheckCount);

    /**
     * 在 sample 列表有而 cur 列表无的 Broker 上创建 Topic。
     *
     * @param topic Topic 名称
     * @param wQueueNum 写队列数
     * @param rQueueNum 读队列数
     * @param curBrokerDataList 当前已存在路由的 Broker
     * @param sampleBrokerDataList 参照 Broker 列表
     * @param examineTopic 是否轮询确认
     * @param retryCheckCount 确认重试次数
     */
    boolean createTopicOnBroker(String topic, int wQueueNum, int rQueueNum, List<BrokerData> curBrokerDataList,
        List<BrokerData> sampleBrokerDataList, boolean examineTopic, int retryCheckCount) throws Exception;
}
