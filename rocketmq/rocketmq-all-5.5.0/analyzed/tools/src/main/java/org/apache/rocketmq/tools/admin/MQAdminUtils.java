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
package org.apache.rocketmq.tools.admin;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;
import org.apache.rocketmq.remoting.protocol.admin.TopicOffset;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.protocol.statictopic.LogicQueueMappingItem;
import org.apache.rocketmq.remoting.protocol.statictopic.TopicConfigAndQueueMapping;
import org.apache.rocketmq.remoting.protocol.statictopic.TopicQueueMappingDetail;
import org.apache.rocketmq.remoting.protocol.statictopic.TopicQueueMappingOne;
import org.apache.rocketmq.remoting.protocol.statictopic.TopicQueueMappingUtils;
import org.apache.rocketmq.remoting.rpc.ClientMetadata;

import static org.apache.rocketmq.remoting.protocol.statictopic.TopicQueueMappingUtils.checkAndBuildMappingItems;
import static org.apache.rocketmq.remoting.protocol.statictopic.TopicQueueMappingUtils.getMappingDetailFromConfig;

/**
 * MQ 管理工具类：封装集群/Topic 元数据刷新、静态 Topic 重映射及物理/逻辑统计转换等管理操作。
 */
public class MQAdminUtils {


    /** 拉取并刷新集群 Broker 元数据，返回 {@link ClientMetadata}。 */
    public static ClientMetadata getBrokerMetadata(DefaultMQAdminExt defaultMQAdminExt) throws InterruptedException, RemotingConnectException, RemotingTimeoutException, RemotingSendRequestException, MQBrokerException {
        ClientMetadata clientMetadata  = new ClientMetadata();
        refreshClusterInfo(defaultMQAdminExt, clientMetadata);
        return clientMetadata;
    }

    /** 刷新集群元数据并加载指定 Topic 路由信息。 */
    public static ClientMetadata getBrokerAndTopicMetadata(String topic, DefaultMQAdminExt defaultMQAdminExt) throws InterruptedException, RemotingException, MQBrokerException {
        ClientMetadata clientMetadata  = new ClientMetadata();
        refreshClusterInfo(defaultMQAdminExt, clientMetadata);
        refreshTopicRouteInfo(topic, defaultMQAdminExt, clientMetadata);
        return clientMetadata;
    }

    /** 从 NameServer 拉取集群信息并写入 clientMetadata；集群为空则抛异常。 */
    public static void refreshClusterInfo(DefaultMQAdminExt defaultMQAdminExt, ClientMetadata clientMetadata) throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException {
        ClusterInfo clusterInfo  = defaultMQAdminExt.examineBrokerClusterInfo();
        if (clusterInfo == null
                || clusterInfo.getClusterAddrTable().isEmpty()) {
            throw new RuntimeException("The Cluster info is empty");
        }
        clientMetadata.refreshClusterInfo(clusterInfo);
    }

    /** 查询 Topic 路由并刷新到 clientMetadata；Topic 不存在时忽略。 */
    public static void refreshTopicRouteInfo(String topic, DefaultMQAdminExt defaultMQAdminExt, ClientMetadata clientMetadata) throws RemotingException, InterruptedException, MQBrokerException {
        TopicRouteData routeData = null;
        try {
            routeData = defaultMQAdminExt.examineTopicRouteInfo(topic);
        } catch (MQClientException exception) {
            if (exception.getResponseCode() != ResponseCode.TOPIC_NOT_EXIST) {
                throw new MQBrokerException(exception.getResponseCode(), exception.getErrorMessage());
            }
        }
        if (routeData != null
                && !routeData.getQueueDatas().isEmpty()) {
            clientMetadata.freshTopicRoute(topic, routeData);
        }
    }

    /** 根据给定 Broker 集合，返回其所在集群内的全部 Broker 名。 */
    public static Set<String> getAllBrokersInSameCluster(Collection<String> brokers, DefaultMQAdminExt defaultMQAdminExt) throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException {
        ClusterInfo clusterInfo  = defaultMQAdminExt.examineBrokerClusterInfo();
        if (clusterInfo == null
                || clusterInfo.getClusterAddrTable().isEmpty()) {
            throw new RuntimeException("The Cluster info is empty");
        }
        Set<String> allBrokers = new HashSet<>();
        for (String broker: brokers) {
            if (allBrokers.contains(broker)) {
                continue;
            }
            for (Set<String> clusterBrokers : clusterInfo.getClusterAddrTable().values()) {
                if (clusterBrokers.contains(broker)) {
                    allBrokers.addAll(clusterBrokers);
                    break;
                }
            }
        }
        return allBrokers;
    }

    /** 为同集群内尚未出现在 brokerConfigMap 中的 Broker 补全占位 Topic 映射配置。 */
    public static  void completeNoTargetBrokers(Map<String, TopicConfigAndQueueMapping> brokerConfigMap, DefaultMQAdminExt defaultMQAdminExt) throws InterruptedException, RemotingConnectException, RemotingTimeoutException, RemotingSendRequestException, MQBrokerException {
        TopicConfigAndQueueMapping configMapping = brokerConfigMap.values().iterator().next();
        String topic = configMapping.getTopicName();
        int queueNum = configMapping.getMappingDetail().getTotalQueues();
        long newEpoch = configMapping.getMappingDetail().getEpoch();
        Set<String> allBrokers = getAllBrokersInSameCluster(brokerConfigMap.keySet(), defaultMQAdminExt);
        for (String broker: allBrokers) {
            if (!brokerConfigMap.containsKey(broker)) {
                brokerConfigMap.put(broker, new TopicConfigAndQueueMapping(new TopicConfig(topic, 0, 0), new TopicQueueMappingDetail(topic, queueNum, broker, newEpoch)));
            }
        }
    }

    /** 校验各 Broker 的 Master 地址是否可解析，否则抛异常。 */
    public static void checkIfMasterAlive(Collection<String> brokers, DefaultMQAdminExt defaultMQAdminExt, ClientMetadata clientMetadata) {
        for (String broker : brokers) {
            String addr = clientMetadata.findMasterBrokerAddr(broker);
            if (addr == null) {
                throw new RuntimeException("Can't find addr for broker " + broker);
            }
        }
    }

    public static void updateTopicConfigMappingAll(Map<String, TopicConfigAndQueueMapping> brokerConfigMap, DefaultMQAdminExt defaultMQAdminExt, boolean force) throws Exception {
        ClientMetadata clientMetadata = getBrokerMetadata(defaultMQAdminExt);
        checkIfMasterAlive(brokerConfigMap.keySet(), defaultMQAdminExt, clientMetadata);
        // 部分成功、部分失败会导致各 Broker 间数据不一致
        for (Map.Entry<String, TopicConfigAndQueueMapping> entry : brokerConfigMap.entrySet()) {
            String broker = entry.getKey();
            String addr = clientMetadata.findMasterBrokerAddr(broker);
            TopicConfigAndQueueMapping configMapping = entry.getValue();
            defaultMQAdminExt.createStaticTopic(addr, defaultMQAdminExt.getCreateTopicKey(), configMapping, configMapping.getMappingDetail(), force);
        }
    }

    /** 静态 Topic 重映射：分五步切换 Leader、禁止旧 Leader 写入、计算逻辑偏移并同步全集群。 */
    public static void remappingStaticTopic(String topic, Set<String> brokersToMapIn, Set<String> brokersToMapOut, Map<String, TopicConfigAndQueueMapping> brokerConfigMap, int blockSeqSize, boolean force, DefaultMQAdminExt defaultMQAdminExt) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {

        ClientMetadata clientMetadata = MQAdminUtils.getBrokerMetadata(defaultMQAdminExt);
        MQAdminUtils.checkIfMasterAlive(brokerConfigMap.keySet(), defaultMQAdminExt, clientMetadata);
        // 开始重映射
        // 步骤1：允许新 Leader 在无 logicOffset 时可写入
        for (String broker: brokersToMapIn) {
            String addr = clientMetadata.findMasterBrokerAddr(broker);
            TopicConfigAndQueueMapping configMapping = brokerConfigMap.get(broker);
            defaultMQAdminExt.createStaticTopic(addr, defaultMQAdminExt.getCreateTopicKey(), configMapping, configMapping.getMappingDetail(), force);
        }
        // 步骤2：禁止旧 Leader 写入
        for (String broker: brokersToMapOut) {
            String addr = clientMetadata.findMasterBrokerAddr(broker);
            TopicConfigAndQueueMapping configMapping = brokerConfigMap.get(broker);
            defaultMQAdminExt.createStaticTopic(addr, defaultMQAdminExt.getCreateTopicKey(), configMapping, configMapping.getMappingDetail(), force);
        }
        // 步骤3：确定逻辑偏移量
        for (String broker: brokersToMapOut) {
            String addr = clientMetadata.findMasterBrokerAddr(broker);
            TopicStatsTable statsTable = defaultMQAdminExt.examineTopicStats(addr, topic);
            TopicConfigAndQueueMapping mapOutConfig = brokerConfigMap.get(broker);
            for (Map.Entry<Integer, List<LogicQueueMappingItem>> entry : mapOutConfig.getMappingDetail().getHostedQueues().entrySet()) {
                List<LogicQueueMappingItem> items = entry.getValue();
                Integer globalId = entry.getKey();
                if (items.size() < 2) {
                    continue;
                }
                LogicQueueMappingItem newLeader = items.get(items.size() - 1);
                LogicQueueMappingItem oldLeader = items.get(items.size() - 2);
                if (newLeader.getLogicOffset()  > 0) {
                    continue;
                }
                TopicOffset topicOffset = statsTable.getOffsetTable().get(new MessageQueue(topic, oldLeader.getBname(), oldLeader.getQueueId()));
                if (topicOffset == null) {
                    throw new RuntimeException("Cannot get the max offset for old leader " + oldLeader);
                }
                // TODO：校验 maxOffset 是否可能返回 -1
                if (topicOffset.getMaxOffset() < oldLeader.getStartOffset()) {
                    throw new RuntimeException("The max offset is smaller then the start offset " + oldLeader + " " + topicOffset.getMaxOffset());
                }
                newLeader.setLogicOffset(TopicQueueMappingUtils.blockSeqRoundUp(oldLeader.computeStaticQueueOffsetStrictly(topicOffset.getMaxOffset()), blockSeqSize));
                TopicConfigAndQueueMapping mapInConfig = brokerConfigMap.get(newLeader.getBname());
                // 刷新新 Leader 映射信息
                TopicQueueMappingDetail.putMappingInfo(mapInConfig.getMappingDetail(), globalId, items);
            }
        }
        // 步骤4：带 logicOffset 写入新 Leader
        for (String broker: brokersToMapIn) {
            String addr = clientMetadata.findMasterBrokerAddr(broker);
            TopicConfigAndQueueMapping configMapping = brokerConfigMap.get(broker);
            defaultMQAdminExt.createStaticTopic(addr, defaultMQAdminExt.getCreateTopicKey(), configMapping, configMapping.getMappingDetail(), force);
        }
        // 步骤5：同步非目标 Broker 配置
        for (String broker: brokerConfigMap.keySet()) {
            if (brokersToMapIn.contains(broker) || brokersToMapOut.contains(broker)) {
                continue;
            }
            String addr = clientMetadata.findMasterBrokerAddr(broker);
            TopicConfigAndQueueMapping configMapping = brokerConfigMap.get(broker);
            defaultMQAdminExt.createStaticTopic(addr, defaultMQAdminExt.getCreateTopicKey(), configMapping, configMapping.getMappingDetail(), force);
        }
    }

    /** 遍历集群全部 Broker，拉取指定静态 Topic 的配置与队列映射。 */
    public static Map<String, TopicConfigAndQueueMapping> examineTopicConfigAll(String topic, DefaultMQAdminExt defaultMQAdminExt) throws RemotingException,  InterruptedException, MQBrokerException {
        Map<String, TopicConfigAndQueueMapping> brokerConfigMap = new HashMap<>();
        ClientMetadata clientMetadata = new ClientMetadata();
        // 检查集群内全部 Broker
        ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
        if (clusterInfo != null
                && clusterInfo.getBrokerAddrTable() != null) {
            clientMetadata.refreshClusterInfo(clusterInfo);
        }
        for (String broker : clientMetadata.getBrokerAddrTable().keySet()) {
            String addr = clientMetadata.findMasterBrokerAddr(broker);
            try {
                TopicConfigAndQueueMapping mapping = (TopicConfigAndQueueMapping) defaultMQAdminExt.examineTopicConfig(addr, topic);
                // 允许配置为 null（Topic 尚未创建）
                if (mapping != null) {
                    if (mapping.getMappingDetail() != null) {
                        assert mapping.getMappingDetail().getBname().equals(broker);
                    }
                    brokerConfigMap.put(broker, mapping);
                }
            }  catch (MQBrokerException exception1) {
                if (exception1.getResponseCode() != ResponseCode.TOPIC_NOT_EXIST) {
                    throw exception1;
                }
            }
        }
        return brokerConfigMap;
    }


    /** 仅根据 Topic 路由中的 Broker 列表拉取各 Broker 上的 Topic 配置。 */
    public static Map<String, TopicConfigAndQueueMapping> examineTopicConfigFromRoute(String topic, TopicRouteData topicRouteData, DefaultMQAdminExt defaultMQAdminExt) throws RemotingException,  InterruptedException, MQBrokerException {
        Map<String, TopicConfigAndQueueMapping> brokerConfigMap = new HashMap<>();
        for (BrokerData bd : topicRouteData.getBrokerDatas()) {
            String broker = bd.getBrokerName();
            String addr = bd.selectBrokerAddr();
            if (addr == null) {
                continue;
            }
            try {
                TopicConfigAndQueueMapping mapping = (TopicConfigAndQueueMapping) defaultMQAdminExt.examineTopicConfig(addr, topic);
                //allow the config is null
                if (mapping != null) {
                    if (mapping.getMappingDetail() != null) {
                        assert mapping.getMappingDetail().getBname().equals(broker);
                    }
                    brokerConfigMap.put(broker, mapping);
                }
            } catch (MQBrokerException exception) {
                if (exception.getResponseCode() != ResponseCode.TOPIC_NOT_EXIST) {
                    throw exception;
                }
            }
        }
        return brokerConfigMap;
    }

    /** 将物理队列的 Topic 统计（min/max offset）聚合转换为逻辑队列维度的统计。 */
    public static void convertPhysicalTopicStats(String topic, Map<String, TopicConfigAndQueueMapping> brokerConfigMap, TopicStatsTable topicStatsTable) {
        Map<Integer, TopicQueueMappingOne> globalIdMap = checkAndBuildMappingItems(getMappingDetailFromConfig(brokerConfigMap.values()), true, false);
        for (Map.Entry<Integer, TopicQueueMappingOne> entry: globalIdMap.entrySet()) {
            Integer qid = entry.getKey();
            TopicQueueMappingOne mappingOne =  entry.getValue();
            LogicQueueMappingItem minItem = TopicQueueMappingUtils.findLogicQueueMappingItem(mappingOne.getItems(), 0, true);
            LogicQueueMappingItem maxItem = TopicQueueMappingUtils.findLogicQueueMappingItem(mappingOne.getItems(), Long.MAX_VALUE, true);
            assert  minItem != null && maxItem != null;
            TopicOffset minTopicOffset = topicStatsTable.getOffsetTable().get(new MessageQueue(topic, minItem.getBname(), minItem.getQueueId()));
            TopicOffset maxTopicOffset = topicStatsTable.getOffsetTable().get(new MessageQueue(topic, maxItem.getBname(), maxItem.getQueueId()));

            if (minTopicOffset == null
                || maxTopicOffset == null) {
                continue;
            }
            long min = minItem.computeStaticQueueOffsetLoosely(minTopicOffset.getMinOffset());
            if (min < 0)
                min = 0;
            long max = maxItem.computeStaticQueueOffsetStrictly(maxTopicOffset.getMaxOffset());
            if (max < 0)
                max = 0;
            long timestamp = maxTopicOffset.getLastUpdateTimestamp();

            TopicOffset topicOffset = new TopicOffset();
            topicOffset.setMinOffset(min);
            topicOffset.setMaxOffset(max);
            topicOffset.setLastUpdateTimestamp(timestamp);
            topicStatsTable.getOffsetTable().put(new MessageQueue(topic, TopicQueueMappingUtils.getMockBrokerName(mappingOne.getMappingDetail().getScope()), qid), topicOffset);
        }
    }


    /** 将物理队列的消费进度转换为静态 Topic 逻辑队列维度的 {@link ConsumeStats}。 */
    public static ConsumeStats convertPhysicalConsumeStats(Map<String, TopicConfigAndQueueMapping> brokerConfigMap, ConsumeStats physicalResult) {
        Map<Integer, TopicQueueMappingOne> globalIdMap = checkAndBuildMappingItems(getMappingDetailFromConfig(brokerConfigMap.values()), true, false);
        ConsumeStats result = new ConsumeStats();
        result.setConsumeTps(physicalResult.getConsumeTps());
        for (Map.Entry<Integer, TopicQueueMappingOne> entry : globalIdMap.entrySet()) {
            Integer qid = entry.getKey();
            TopicQueueMappingOne mappingOne = entry.getValue();
            MessageQueue messageQueue = new MessageQueue(mappingOne.getTopic(), TopicQueueMappingUtils.getMockBrokerName(mappingOne.getMappingDetail().getScope()), qid);
            OffsetWrapper offsetWrapper = new OffsetWrapper();
            long brokerOffset = -1;
            long consumerOffset = -1;
            long lastTimestamp = -1; // 时间戳选取逻辑后续可优化
            for (int i = mappingOne.getItems().size() - 1; i >= 0; i--) {
                LogicQueueMappingItem item = mappingOne.getItems().get(i);
                MessageQueue phyQueue = new MessageQueue(mappingOne.getTopic(), item.getBname(), item.getQueueId());
                OffsetWrapper phyOffsetWrapper = physicalResult.getOffsetTable().get(phyQueue);
                if (phyOffsetWrapper == null) {
                    continue;
                }

                if (consumerOffset == -1
                    && phyOffsetWrapper.getConsumerOffset() >= 0) {
                    consumerOffset = phyOffsetWrapper.getConsumerOffset();
                    lastTimestamp = phyOffsetWrapper.getLastTimestamp();
                }
                if (brokerOffset == -1
                    && item.getLogicOffset() >= 0) {
                    brokerOffset = item.computeStaticQueueOffsetStrictly(phyOffsetWrapper.getBrokerOffset());
                }
                if (consumerOffset >= 0
                    && brokerOffset >= 0) {
                    break;
                }
            }
            if (brokerOffset >= 0
                && consumerOffset >= 0) {
                offsetWrapper.setBrokerOffset(brokerOffset);
                offsetWrapper.setConsumerOffset(consumerOffset);
                offsetWrapper.setLastTimestamp(lastTimestamp);
                result.getOffsetTable().put(messageQueue, offsetWrapper);
            }
        }
        return result;
    }
}
