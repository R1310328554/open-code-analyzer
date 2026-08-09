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

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.BoundaryType;
import org.apache.rocketmq.common.CheckRocksdbCqWriteResult;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.message.MessageRequestMode;
import org.apache.rocketmq.common.topic.TopicValidator;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.RollbackStats;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.AclInfo;
import org.apache.rocketmq.remoting.protocol.body.BrokerMemberGroup;
import org.apache.rocketmq.remoting.protocol.body.BrokerReplicasInfo;
import org.apache.rocketmq.remoting.protocol.body.BrokerStatsData;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;
import org.apache.rocketmq.remoting.protocol.body.ConsumeStatsList;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.protocol.body.EpochEntryCache;
import org.apache.rocketmq.remoting.protocol.body.GetBrokerLiteInfoResponseBody;
import org.apache.rocketmq.remoting.protocol.body.GetLiteClientInfoResponseBody;
import org.apache.rocketmq.remoting.protocol.body.GetLiteGroupInfoResponseBody;
import org.apache.rocketmq.remoting.protocol.body.GetLiteTopicInfoResponseBody;
import org.apache.rocketmq.remoting.protocol.body.GetParentTopicInfoResponseBody;
import org.apache.rocketmq.remoting.protocol.body.GroupList;
import org.apache.rocketmq.remoting.protocol.body.HARuntimeInfo;
import org.apache.rocketmq.remoting.protocol.body.KVTable;
import org.apache.rocketmq.remoting.protocol.body.ProducerConnection;
import org.apache.rocketmq.remoting.protocol.body.ProducerTableInfo;
import org.apache.rocketmq.remoting.protocol.body.QueryConsumeQueueResponseBody;
import org.apache.rocketmq.remoting.protocol.body.QueueTimeSpan;
import org.apache.rocketmq.remoting.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.remoting.protocol.body.TopicConfigSerializeWrapper;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.body.UserInfo;
import org.apache.rocketmq.remoting.protocol.header.ExportRocksDBConfigToJsonRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.ElectMasterResponseHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.GetMetaDataResponseHeader;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.protocol.statictopic.TopicQueueMappingDetail;
import org.apache.rocketmq.remoting.protocol.subscription.GroupForbidden;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.tools.admin.api.BrokerOperatorResult;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.apache.rocketmq.tools.admin.common.AdminToolResult;

/**
 * MQ 管理扩展默认实现：继承 {@link ClientConfig}，将 {@link MQAdminExt} 全部方法委托给 {@link DefaultMQAdminExtImpl}，供 mqadmin 命令行与运维工具使用。
 */
public class DefaultMQAdminExt extends ClientConfig implements MQAdminExt {
    /** 管理扩展核心实现，封装 Remoting 调用。 */
    private final DefaultMQAdminExtImpl defaultMQAdminExtImpl;
    /** AdminExt 在 MQClient 中注册的分组名。 */
    private String adminExtGroup = "admin_ext_group";
    /** 自动创建 Topic 时使用的系统 Key。 */
    private String createTopicKey = TopicValidator.AUTO_CREATE_TOPIC_KEY_TOPIC;
    /** 默认 Remoting 请求超时（毫秒）。 */
    private long timeoutMillis = 5000;

    public DefaultMQAdminExt() {
        this.defaultMQAdminExtImpl = new DefaultMQAdminExtImpl(this, null, timeoutMillis);
    }

    public DefaultMQAdminExt(long timeoutMillis) {
        this.defaultMQAdminExtImpl = new DefaultMQAdminExtImpl(this, null, timeoutMillis);
    }

    public DefaultMQAdminExt(RPCHook rpcHook) {
        this.defaultMQAdminExtImpl = new DefaultMQAdminExtImpl(this, rpcHook, timeoutMillis);
    }

    public DefaultMQAdminExt(RPCHook rpcHook, long timeoutMillis) {
        this.defaultMQAdminExtImpl = new DefaultMQAdminExtImpl(this, rpcHook, timeoutMillis);
    }

    public DefaultMQAdminExt(final String adminExtGroup) {
        this.adminExtGroup = adminExtGroup;
        this.defaultMQAdminExtImpl = new DefaultMQAdminExtImpl(this, timeoutMillis);
    }

    public DefaultMQAdminExt(final String adminExtGroup, long timeoutMillis) {
        this.adminExtGroup = adminExtGroup;
        this.defaultMQAdminExtImpl = new DefaultMQAdminExtImpl(this, timeoutMillis);
    }

    /** 创建 Topic（指定队列数与属性）。 */
    @Override
    public void createTopic(String key, String newTopic, int queueNum,
        Map<String, String> attributes) throws MQClientException {
        createTopic(key, newTopic, queueNum, 0, attributes);
    }

    /** 创建 Topic（指定队列数与属性）。 */
    @Override
    public void createTopic(String key, String newTopic, int queueNum, int topicSysFlag,
        Map<String, String> attributes) throws MQClientException {
        defaultMQAdminExtImpl.createTopic(key, newTopic, queueNum, topicSysFlag, attributes);
    }

    /** 按时间戳在队列中查找 offset。 */
    @Override
    public long searchOffset(MessageQueue mq, long timestamp) throws MQClientException {
        return defaultMQAdminExtImpl.searchOffset(mq, timestamp);
    }

    /** 按时间戳查找队列下界 offset。 */
    public long searchLowerBoundaryOffset(MessageQueue mq, long timestamp) throws MQClientException {
        return defaultMQAdminExtImpl.searchOffset(mq, timestamp, BoundaryType.LOWER);
    }

    /** 按时间戳查找队列上界 offset。 */
    public long searchUpperBoundaryOffset(MessageQueue mq, long timestamp) throws MQClientException {
        return defaultMQAdminExtImpl.searchOffset(mq, timestamp, BoundaryType.UPPER);
    }

    /** 返回队列最大逻辑 offset。 */
    @Override
    public long maxOffset(MessageQueue mq) throws MQClientException {
        return defaultMQAdminExtImpl.maxOffset(mq);
    }

    /** 返回队列最小逻辑 offset。 */
    @Override
    public long minOffset(MessageQueue mq) throws MQClientException {
        return defaultMQAdminExtImpl.minOffset(mq);
    }

    /** 返回队列最早消息存储时间。 */
    @Override
    public long earliestMsgStoreTime(MessageQueue mq) throws MQClientException {
        return defaultMQAdminExtImpl.earliestMsgStoreTime(mq);
    }

    /** 按 Topic/Key 或集群条件索引查询消息。 */
    @Override
    public QueryResult queryMessage(String topic, String key, int maxNum, long begin, long end)
        throws MQClientException, InterruptedException {
        return defaultMQAdminExtImpl.queryMessage(topic, key, maxNum, begin, end);
    }


    /** 按 Topic/Key 或集群条件索引查询消息。 */
    public QueryResult queryMessage(String clusterName, String topic, String key, int maxNum, long begin, long end)
        throws MQClientException, InterruptedException, RemotingException {
        return defaultMQAdminExtImpl.queryMessage(clusterName, topic, key, maxNum, begin, end);
    }

    /** 按 Topic/Key 或集群条件索引查询消息。 */
    public QueryResult queryMessage(String clusterName, String topic, String key, int maxNum, long begin, long end, String keyType, String lastKey)
        throws MQClientException, InterruptedException, RemotingException {
        return defaultMQAdminExtImpl.queryMessage(clusterName, topic, key, maxNum, begin, end, keyType, lastKey);
    }

    /** 启动管理客户端：注册 AdminExt、初始化 MQClient 与并发线程池。 */
    @Override
    public void start() throws MQClientException {
        defaultMQAdminExtImpl.start();
    }

    /** 关闭管理客户端并释放 MQClient 与线程池资源。 */
    @Override
    public void shutdown() {
        defaultMQAdminExtImpl.shutdown();
    }

    /** 向 Broker 容器动态添加 Broker 实例。 */
    @Override
    public void addBrokerToContainer(String brokerContainerAddr, String brokerConfig) throws InterruptedException,
        RemotingConnectException, RemotingTimeoutException, RemotingSendRequestException, MQBrokerException {
        defaultMQAdminExtImpl.addBrokerToContainer(brokerContainerAddr, brokerConfig);
    }

    /** 从 Broker 容器移除指定 Broker。 */
    @Override
    public void removeBrokerFromContainer(String brokerContainerAddr, String clusterName, String brokerName,
        long brokerId) throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException,
        RemotingConnectException {
        defaultMQAdminExtImpl.removeBrokerFromContainer(brokerContainerAddr, clusterName, brokerName, brokerId);
    }

    /** 更新 Broker 运行时配置项。 */
    @Override
    public void updateBrokerConfig(String brokerAddr,
        Properties properties) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, UnsupportedEncodingException, InterruptedException, MQBrokerException, MQClientException {
        defaultMQAdminExtImpl.updateBrokerConfig(brokerAddr, properties);
    }

    /** 拉取 Broker 当前配置 Properties。 */
    @Override
    public Properties getBrokerConfig(final String brokerAddr) throws RemotingConnectException,
        RemotingSendRequestException, RemotingTimeoutException, UnsupportedEncodingException, InterruptedException, MQBrokerException {
        return defaultMQAdminExtImpl.getBrokerConfig(brokerAddr);
    }

    /** 在指定 Broker 创建或更新 Topic 配置。 */
    @Override
    public void createAndUpdateTopicConfig(String addr, TopicConfig config) throws RemotingException, MQBrokerException,
        InterruptedException, MQClientException {
        defaultMQAdminExtImpl.createAndUpdateTopicConfig(addr, config);
    }

    /** 批量创建或更新 Topic 配置列表。 */
    @Override
    public void createAndUpdateTopicConfigList(String addr,
        List<TopicConfig> topicConfigList) throws InterruptedException, RemotingException, MQClientException {
        defaultMQAdminExtImpl.createAndUpdateTopicConfigList(addr, topicConfigList);
    }

    /** 创建或更新消费组订阅配置。 */
    @Override
    public void createAndUpdateSubscriptionGroupConfig(String addr,
        SubscriptionGroupConfig config) throws RemotingException,
        MQBrokerException, InterruptedException, MQClientException {
        defaultMQAdminExtImpl.createAndUpdateSubscriptionGroupConfig(addr, config);
    }

    /** 批量创建或更新消费组订阅配置。 */
    @Override
    public void createAndUpdateSubscriptionGroupConfigList(String brokerAddr,
        List<SubscriptionGroupConfig> configs) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        defaultMQAdminExtImpl.createAndUpdateSubscriptionGroupConfigList(brokerAddr, configs);
    }

    /** 查询指定 Broker 上某消费组的订阅配置。 */
    @Override
    public SubscriptionGroupConfig examineSubscriptionGroupConfig(String addr, String group)
        throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        return defaultMQAdminExtImpl.examineSubscriptionGroupConfig(addr, group);
    }

    /** 查询指定 Broker 上某 Topic 的配置。 */
    @Override
    public TopicConfig examineTopicConfig(String addr,
        String topic) throws RemotingSendRequestException, RemotingConnectException, RemotingTimeoutException, InterruptedException, MQBrokerException {
        return defaultMQAdminExtImpl.examineTopicConfig(addr, topic);
    }

    /** 查询 Topic 在各 Broker 上的统计（min/max offset、TPS 等）。 */
    @Override
    public TopicStatsTable examineTopicStats(
        String topic) throws RemotingException, MQClientException, InterruptedException,
        MQBrokerException {
        return defaultMQAdminExtImpl.examineTopicStats(topic);
    }

    /** 查询 Topic 在各 Broker 上的统计（min/max offset、TPS 等）。 */
    @Override
    public TopicStatsTable examineTopicStats(String brokerAddr,
        String topic) throws RemotingException, MQClientException, InterruptedException,
        MQBrokerException {
        return defaultMQAdminExtImpl.examineTopicStats(brokerAddr, topic);
    }

    /** 并发查询 Topic 统计并封装为 {@link AdminToolResult}。 */
    @Override
    public AdminToolResult<TopicStatsTable> examineTopicStatsConcurrent(String topic) {
        return defaultMQAdminExtImpl.examineTopicStatsConcurrent(topic);
    }

    /** 从 NameServer 拉取全部 Topic 列表。 */
    @Override
    public TopicList fetchAllTopicList() throws RemotingException, MQClientException, InterruptedException {
        return this.defaultMQAdminExtImpl.fetchAllTopicList();
    }

    /** 按集群名拉取 Topic 列表。 */
    @Override
    public TopicList fetchTopicsByCLuster(
        String clusterName) throws RemotingException, MQClientException, InterruptedException {
        return this.defaultMQAdminExtImpl.fetchTopicsByCLuster(clusterName);
    }

    /** 拉取 Broker 运行时 KV 指标。 */
    @Override
    public KVTable fetchBrokerRuntimeStats(
        final String brokerAddr) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, InterruptedException, MQBrokerException {
        return this.defaultMQAdminExtImpl.fetchBrokerRuntimeStats(brokerAddr);
    }

    /** 查询消费组消费进度与 TPS（可指定 Topic/集群/Broker）。 */
    @Override
    public ConsumeStats examineConsumeStats(
        String consumerGroup) throws RemotingException, MQClientException, InterruptedException,
        MQBrokerException {
        return examineConsumeStats(consumerGroup, null);
    }

    /** 查询消费组消费进度与 TPS（可指定 Topic/集群/Broker）。 */
    @Override
    public ConsumeStats examineConsumeStats(String clusterName, String consumerGroup,
        String topic) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        return defaultMQAdminExtImpl.examineConsumeStats(clusterName, consumerGroup, topic);
    }

    /** 查询消费组消费进度与 TPS（可指定 Topic/集群/Broker）。 */
    @Override
    public ConsumeStats examineConsumeStats(String consumerGroup,
        String topic) throws RemotingException, MQClientException,
        InterruptedException, MQBrokerException {
        return defaultMQAdminExtImpl.examineConsumeStats(consumerGroup, topic);
    }

    /** 查询消费组消费进度与 TPS（可指定 Topic/集群/Broker）。 */
    @Override
    public ConsumeStats examineConsumeStats(final String brokerAddr, final String consumerGroup,
        final String topicName, final long timeoutMillis)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException, MQBrokerException {
        return this.defaultMQAdminExtImpl.examineConsumeStats(brokerAddr, consumerGroup, topicName, timeoutMillis);
    }

    /** 并发汇总消费组消费统计。 */
    @Override
    public AdminToolResult<ConsumeStats> examineConsumeStatsConcurrent(String consumerGroup, String topic) {
        return defaultMQAdminExtImpl.examineConsumeStatsConcurrent(consumerGroup, topic);
    }

    /** 从 NameServer 获取集群 Broker 拓扑信息。 */
    @Override
    public ClusterInfo examineBrokerClusterInfo() throws InterruptedException, RemotingConnectException, RemotingTimeoutException,
        RemotingSendRequestException, MQBrokerException {
        return defaultMQAdminExtImpl.examineBrokerClusterInfo();
    }

    /** 查询 Topic 路由（Broker 与队列分布）。 */
    @Override
    public TopicRouteData examineTopicRouteInfo(
        String topic) throws RemotingException, MQClientException, InterruptedException {
        return defaultMQAdminExtImpl.examineTopicRouteInfo(topic);
    }

    /** 查询消费组在线连接与订阅关系。 */
    @Override
    public ConsumerConnection examineConsumerConnectionInfo(
        String consumerGroup) throws InterruptedException, MQBrokerException,
        RemotingException, MQClientException {
        return defaultMQAdminExtImpl.examineConsumerConnectionInfo(consumerGroup);
    }

    /** 查询消费组在线连接与订阅关系。 */
    @Override
    public ConsumerConnection examineConsumerConnectionInfo(
        String consumerGroup, String brokerAddr) throws InterruptedException, MQBrokerException,
        RemotingException, MQClientException {
        return defaultMQAdminExtImpl.examineConsumerConnectionInfo(consumerGroup, brokerAddr);
    }

    /** 查询指定 Topic 下生产者连接信息。 */
    @Override
    public ProducerConnection examineProducerConnectionInfo(String producerGroup,
        final String topic) throws RemotingException,
        MQClientException, InterruptedException, MQBrokerException {
        return defaultMQAdminExtImpl.examineProducerConnectionInfo(producerGroup, topic);
    }

    /** 拉取 Broker 上全部生产者连接表。 */
    @Override
    public ProducerTableInfo getAllProducerInfo(
        final String brokerAddr) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        return defaultMQAdminExtImpl.getAllProducerInfo(brokerAddr);
    }

    /** 从 NameServer 删除 Topic 路由元数据。 */
    @Override
    public void deleteTopicInNameServer(Set<String> addrs, String clusterName,
        String topic) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        defaultMQAdminExtImpl.deleteTopicInNameServer(addrs, clusterName, topic);
    }

    /** 返回当前配置的 NameServer 地址列表。 */
    @Override
    public List<String> getNameServerAddressList() {
        return this.defaultMQAdminExtImpl.getNameServerAddressList();
    }

    /** 在 NameServer 上撤销 Broker 写权限。 */
    @Override
    public int wipeWritePermOfBroker(final String namesrvAddr, String brokerName) throws RemotingCommandException,
        RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException, MQClientException {
        return defaultMQAdminExtImpl.wipeWritePermOfBroker(namesrvAddr, brokerName);
    }

    /** 在 NameServer 上恢复 Broker 写权限。 */
    @Override
    public int addWritePermOfBroker(String namesrvAddr,
        String brokerName) throws RemotingCommandException, RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException, MQClientException {
        return defaultMQAdminExtImpl.addWritePermOfBroker(namesrvAddr, brokerName);
    }

    /** 本地写入 KV 配置（不经过 Broker）。 */
    @Override
    public void putKVConfig(String namespace, String key, String value) {
        defaultMQAdminExtImpl.putKVConfig(namespace, key, value);
    }

    /** 从 NameServer 读取 KV 配置值。 */
    @Override
    public String getKVConfig(String namespace,
        String key) throws RemotingException, MQClientException, InterruptedException {
        return defaultMQAdminExtImpl.getKVConfig(namespace, key);
    }

    /** 按命名空间拉取 KV 配置表。 */
    @Override
    public KVTable getKVListByNamespace(
        String namespace) throws RemotingException, MQClientException, InterruptedException {
        return defaultMQAdminExtImpl.getKVListByNamespace(namespace);
    }

    /** 从集群 Broker 与 NameServer 删除 Topic。 */
    @Override
    public void deleteTopic(String topicName,
        String clusterName) throws RemotingException, MQBrokerException, InterruptedException,
        MQClientException {
        defaultMQAdminExtImpl.deleteTopic(topicName, clusterName);
    }

    /** 从指定 Broker 集合删除 Topic。 */
    @Override
    public void deleteTopicInBroker(Set<String> addrs,
        String topic) throws RemotingException, MQBrokerException, InterruptedException,
        MQClientException {
        defaultMQAdminExtImpl.deleteTopicInBroker(addrs, topic);
    }

    /** 并发从多个 Broker 删除 Topic。 */
    @Override
    public AdminToolResult<BrokerOperatorResult> deleteTopicInBrokerConcurrent(Set<String> addrs, String topic) {
        return defaultMQAdminExtImpl.deleteTopicInBrokerConcurrent(addrs, topic);
    }

    /** 从 NameServer 删除 Topic 路由元数据。 */
    @Override
    public void deleteTopicInNameServer(Set<String> addrs,
        String topic) throws RemotingException, MQBrokerException, InterruptedException,
        MQClientException {
        defaultMQAdminExtImpl.deleteTopicInNameServer(addrs, topic);
    }

    /** 删除 Broker 上指定消费组配置。 */
    @Override
    public void deleteSubscriptionGroup(String addr,
        String groupName) throws RemotingException, MQBrokerException, InterruptedException,
        MQClientException {
        defaultMQAdminExtImpl.deleteSubscriptionGroup(addr, groupName);
    }

    /** 删除 Broker 上指定消费组配置。 */
    @Override
    public void deleteSubscriptionGroup(String addr,
        String groupName, boolean removeOffset) throws RemotingException, MQBrokerException, InterruptedException,
        MQClientException {
        defaultMQAdminExtImpl.deleteSubscriptionGroup(addr, groupName, removeOffset);
    }

    /** 在 Broker/NameServer 创建或更新 KV 配置。 */
    @Override
    public void createAndUpdateKvConfig(String namespace, String key,
        String value) throws RemotingException, MQBrokerException,
        InterruptedException, MQClientException {
        defaultMQAdminExtImpl.createAndUpdateKvConfig(namespace, key, value);
    }

    /** 删除 KV 配置项。 */
    @Override
    public void deleteKvConfig(String namespace,
        String key) throws RemotingException, MQBrokerException, InterruptedException,
        MQClientException {
        defaultMQAdminExtImpl.deleteKvConfig(namespace, key);
    }

    /** 按时间戳重置消费位点（旧版 API，返回 RollbackStats）。 */
    @Override
    public List<RollbackStats> resetOffsetByTimestampOld(String consumerGroup, String topic, long timestamp,
        boolean force)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return defaultMQAdminExtImpl.resetOffsetByTimestampOld(consumerGroup, topic, timestamp, force);
    }

    /** 按时间戳重置消费位点（旧版 API，返回 RollbackStats）。 */
    public List<RollbackStats> resetOffsetByTimestampOld(String clusterName, String consumerGroup, String topic, long timestamp,
        boolean force)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return defaultMQAdminExtImpl.resetOffsetByTimestampOld(clusterName, consumerGroup, topic, timestamp, force);
    }

    /** 按时间戳重置消费位点到各队列。 */
    @Override
    public Map<MessageQueue, Long> resetOffsetByTimestamp(String clusterName, String topic, String group,
        long timestamp, boolean isForce) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return defaultMQAdminExtImpl.resetOffsetByTimestamp(clusterName, topic, group, timestamp, isForce);
    }

    /** 按时间戳重置消费位点到各队列。 */
    @Override
    public Map<MessageQueue, Long> resetOffsetByTimestamp(String topic, String group, long timestamp, boolean isForce)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return resetOffsetByTimestamp(topic, group, timestamp, isForce, false);
    }

    /** 按时间戳重置消费位点到各队列。 */
    public Map<MessageQueue, Long> resetOffsetByTimestamp(String clusterName, String topic, String group,
        long timestamp, boolean isForce, boolean isC)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return defaultMQAdminExtImpl.resetOffsetByTimestamp(clusterName, topic, group, timestamp, isForce, isC);
    }


    /** 按时间戳重置消费位点到各队列。 */
    public Map<MessageQueue, Long> resetOffsetByTimestamp(String topic, String group, long timestamp, boolean isForce,
        boolean isC)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return defaultMQAdminExtImpl.resetOffsetByTimestamp(null, topic, group, timestamp, isForce, isC);
    }

    /** 新版按时间戳重置消费位点。 */
    @Override
    public void resetOffsetNew(String consumerGroup, String topic,
        long timestamp) throws RemotingException, MQBrokerException,
        InterruptedException, MQClientException {
        this.defaultMQAdminExtImpl.resetOffsetNew(consumerGroup, topic, timestamp);
    }

    /** 并发向各 Broker 执行新版位点重置。 */
    @Override
    public AdminToolResult<BrokerOperatorResult> resetOffsetNewConcurrent(final String group, final String topic,
        final long timestamp) {
        return this.defaultMQAdminExtImpl.resetOffsetNewConcurrent(group, topic, timestamp);
    }

    /** 查询消费组各客户端队列消费状态。 */
    @Override
    public Map<String, Map<MessageQueue, Long>> getConsumeStatus(String topic, String group,
        String clientAddr) throws RemotingException,
        MQBrokerException, InterruptedException, MQClientException {
        return defaultMQAdminExtImpl.getConsumeStatus(topic, group, clientAddr);
    }

    /** 创建或更新顺序消息全局配置。 */
    @Override
    public void createOrUpdateOrderConf(String key, String value,
        boolean isCluster) throws RemotingException, MQBrokerException,
        InterruptedException, MQClientException {
        defaultMQAdminExtImpl.createOrUpdateOrderConf(key, value, isCluster);
    }

    /** 查询订阅指定 Topic 的消费组列表。 */
    @Override
    public GroupList queryTopicConsumeByWho(
        String topic) throws InterruptedException, MQBrokerException, RemotingException,
        MQClientException {
        return this.defaultMQAdminExtImpl.queryTopicConsumeByWho(topic);
    }

    /** 查询某消费组订阅的全部 Topic。 */
    @Override
    public TopicList queryTopicsByConsumer(String group)
        throws InterruptedException, MQBrokerException, RemotingException, MQClientException {
        return this.defaultMQAdminExtImpl.queryTopicsByConsumer(group);
    }

    /** 并发查询消费组订阅 Topic 列表。 */
    @Override
    public AdminToolResult<TopicList> queryTopicsByConsumerConcurrent(String group) {
        return defaultMQAdminExtImpl.queryTopicsByConsumerConcurrent(group);
    }

    /** 查询消费组对某 Topic 的订阅表达式。 */
    @Override
    public SubscriptionData querySubscription(String group, String topic)
        throws InterruptedException, MQBrokerException, RemotingException, MQClientException {
        return this.defaultMQAdminExtImpl.querySubscription(group, topic);
    }

    /** 查询消费组在各队列上的消费时间跨度。 */
    @Override
    public List<QueueTimeSpan> queryConsumeTimeSpan(final String topic,
        final String group) throws InterruptedException, MQBrokerException,
        RemotingException, MQClientException {
        return this.defaultMQAdminExtImpl.queryConsumeTimeSpan(topic, group);
    }

    /** 并发查询消费时间跨度。 */
    @Override
    public AdminToolResult<List<QueueTimeSpan>> queryConsumeTimeSpanConcurrent(String topic, String group) {
        return defaultMQAdminExtImpl.queryConsumeTimeSpanConcurrent(topic, group);
    }

    /** 清理集群内过期消费进度。 */
    @Override
    public boolean cleanExpiredConsumerQueue(
        String cluster) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException {
        return defaultMQAdminExtImpl.cleanExpiredConsumerQueue(cluster);
    }

    /** 清理指定 Broker 上过期消费进度。 */
    @Override
    public boolean cleanExpiredConsumerQueueByAddr(
        String addr) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException {
        return defaultMQAdminExtImpl.cleanExpiredConsumerQueueByAddr(addr);
    }

    /** 触发集群删除过期 CommitLog。 */
    @Override
    public boolean deleteExpiredCommitLog(String cluster) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException {
        return defaultMQAdminExtImpl.deleteExpiredCommitLog(cluster);
    }

    /** 触发指定 Broker 删除过期 CommitLog。 */
    @Override
    public boolean deleteExpiredCommitLogByAddr(
        String addr) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException {
        return defaultMQAdminExtImpl.deleteExpiredCommitLogByAddr(addr);
    }

    /** 清理集群未使用 Topic。 */
    @Override
    public boolean cleanUnusedTopic(String cluster) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException {
        return defaultMQAdminExtImpl.cleanUnusedTopic(cluster);
    }

    /** 清理指定 Broker 未使用 Topic。 */
    @Override
    public boolean cleanUnusedTopicByAddr(String addr) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException {
        return defaultMQAdminExtImpl.cleanUnusedTopicByAddr(addr);
    }

    /** 拉取消费端运行态（订阅、ProcessQueue、可选 jstack/metrics）。 */
    @Override
    public ConsumerRunningInfo getConsumerRunningInfo(String consumerGroup, String clientId,
        boolean jstack) throws RemotingException,
        MQClientException, InterruptedException {
        return defaultMQAdminExtImpl.getConsumerRunningInfo(consumerGroup, clientId, jstack);
    }

    /** 拉取消费端运行态（订阅、ProcessQueue、可选 jstack/metrics）。 */
    @Override
    public ConsumerRunningInfo getConsumerRunningInfo(String consumerGroup, String clientId,
        boolean jstack, boolean metrics) throws RemotingException,
        MQClientException, InterruptedException {
        return defaultMQAdminExtImpl.getConsumerRunningInfo(consumerGroup, clientId, jstack, metrics);
    }

    /** 向指定消费端直接投递并消费一条消息（运维调试）。 */
    @Override
    public ConsumeMessageDirectlyResult consumeMessageDirectly(final String consumerGroup, final String clientId,
        final String topic,
        final String msgId) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        return defaultMQAdminExtImpl.consumeMessageDirectly(consumerGroup, clientId, topic, msgId);
    }

    /** 向指定消费端直接投递并消费一条消息（运维调试）。 */
    @Override
    public ConsumeMessageDirectlyResult consumeMessageDirectly(final String clusterName, final String consumerGroup,
        final String clientId,
        final String topic,
        final String msgId) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        return defaultMQAdminExtImpl.consumeMessageDirectly(clusterName, consumerGroup, clientId, topic, msgId);
    }

    /** 查询消息在各消费组的投递/消费轨迹。 */
    @Override
    public List<MessageTrack> messageTrackDetail(
        MessageExt msg) throws RemotingException, MQClientException, InterruptedException,
        MQBrokerException {
        return this.defaultMQAdminExtImpl.messageTrackDetail(msg);
    }

    /** 并发查询消息轨迹详情。 */
    @Override
    public List<MessageTrack> messageTrackDetailConcurrent(
        MessageExt msg) throws RemotingException, MQClientException, InterruptedException,
        MQBrokerException {
        return this.defaultMQAdminExtImpl.messageTrackDetailConcurrent(msg);
    }

    /** 将源消费组位点克隆到目标消费组。 */
    @Override
    public void cloneGroupOffset(String srcGroup, String destGroup, String topic,
        boolean isOffline) throws RemotingException,
        MQClientException, InterruptedException, MQBrokerException {
        this.defaultMQAdminExtImpl.cloneGroupOffset(srcGroup, destGroup, topic, isOffline);
    }

    /** 查看 Broker 指定统计项明细。 */
    @Override
    public BrokerStatsData viewBrokerStatsData(String brokerAddr, String statsName,
        String statsKey) throws RemotingConnectException,
        RemotingSendRequestException, RemotingTimeoutException, MQClientException, InterruptedException {
        return this.defaultMQAdminExtImpl.viewBrokerStatsData(brokerAddr, statsName, statsKey);
    }

    /** 返回 Topic 所在集群名集合。 */
    @Override
    public Set<String> getClusterList(String topic) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException {
        return this.defaultMQAdminExtImpl.getClusterList(topic);
    }

    /** 拉取 Broker 上全部消费组统计。 */
    @Override
    public ConsumeStatsList fetchConsumeStatsInBroker(final String brokerAddr, boolean isOrder,
        long timeoutMillis) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException {
        return this.defaultMQAdminExtImpl.fetchConsumeStatsInBroker(brokerAddr, isOrder, timeoutMillis);
    }

    /** 返回 Topic 关联的集群列表。 */
    @Override
    public Set<String> getTopicClusterList(
        final String topic) throws InterruptedException, MQBrokerException, MQClientException, RemotingException {
        return this.defaultMQAdminExtImpl.getTopicClusterList(topic);
    }

    /** 拉取 Broker 全部订阅组配置。 */
    @Override
    public SubscriptionGroupWrapper getAllSubscriptionGroup(final String brokerAddr, long timeoutMillis)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException,
        RemotingConnectException, MQBrokerException, RemotingCommandException {
        return this.defaultMQAdminExtImpl.getAllSubscriptionGroup(brokerAddr, timeoutMillis);
    }

    /** 拉取 Broker 用户订阅组配置（不含系统组）。 */
    @Override
    public SubscriptionGroupWrapper getUserSubscriptionGroup(final String brokerAddr, long timeoutMillis)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException,
        RemotingConnectException, MQBrokerException, RemotingCommandException {
        return this.defaultMQAdminExtImpl.getUserSubscriptionGroup(brokerAddr, timeoutMillis);
    }

    /** 拉取 Broker 全部 Topic 配置。 */
    @Override
    public TopicConfigSerializeWrapper getAllTopicConfig(final String brokerAddr,
        long timeoutMillis) throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException,
        RemotingConnectException, MQBrokerException, RemotingCommandException {
        return this.defaultMQAdminExtImpl.getAllTopicConfig(brokerAddr, timeoutMillis);
    }

    /** 拉取 Broker 用户 Topic 配置。 */
    @Override
    public TopicConfigSerializeWrapper getUserTopicConfig(final String brokerAddr, final boolean specialTopic,
        long timeoutMillis) throws InterruptedException, RemotingException,
        MQBrokerException, MQClientException {
        return this.defaultMQAdminExtImpl.getUserTopicConfig(brokerAddr, specialTopic, timeoutMillis);
    }

    /* (non-Javadoc)
     * @see org.apache.rocketmq.client.MQAdmin#queryMessageByUniqKey(java.lang.String, java.lang.String)
     */
    /** 按 Topic 与 msgId 查看单条消息。 */
    @Override
    public MessageExt viewMessage(String topic, String msgId)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return this.defaultMQAdminExtImpl.viewMessage(topic, msgId);
    }

    /** 按 Topic/Key 或集群条件索引查询消息。 */
    @Override
    public MessageExt queryMessage(String clusterName, String topic, String msgId)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return this.defaultMQAdminExtImpl.queryMessage(clusterName, topic, msgId);
    }

    /** 委托 {@link DefaultMQAdminExtImpl}#getAdminExtGroup。 */
    public String getAdminExtGroup() {
        return adminExtGroup;
    }

    /** 委托 {@link DefaultMQAdminExtImpl}#setAdminExtGroup。 */
    public void setAdminExtGroup(String adminExtGroup) {
        this.adminExtGroup = adminExtGroup;
    }

    /** 委托 {@link DefaultMQAdminExtImpl}#getCreateTopicKey。 */
    public String getCreateTopicKey() {
        return createTopicKey;
    }

    /** 委托 {@link DefaultMQAdminExtImpl}#setCreateTopicKey。 */
    public void setCreateTopicKey(String createTopicKey) {
        this.createTopicKey = createTopicKey;
    }

    /** 更新消费组在指定队列上的 commit offset。 */
    @Override
    public void updateConsumeOffset(String brokerAddr, String consumeGroup, MessageQueue mq,
        long offset) throws RemotingException, InterruptedException, MQBrokerException {
        this.defaultMQAdminExtImpl.updateConsumeOffset(brokerAddr, consumeGroup, mq, offset);
    }

    /** 更新 NameServer 配置（可指定 NS 列表）。 */
    @Override
    public void updateNameServerConfig(final Properties properties, final List<String> nameServers)
        throws InterruptedException, RemotingConnectException,
        UnsupportedEncodingException, MQBrokerException, RemotingTimeoutException,
        MQClientException, RemotingSendRequestException {
        this.defaultMQAdminExtImpl.updateNameServerConfig(properties, nameServers);
    }

    /** 拉取 NameServer 配置。 */
    @Override
    public Map<String, Properties> getNameServerConfig(final List<String> nameServers)
        throws InterruptedException, RemotingTimeoutException,
        RemotingSendRequestException, RemotingConnectException, MQClientException,
        UnsupportedEncodingException {
        return this.defaultMQAdminExtImpl.getNameServerConfig(nameServers);
    }

    /** 分页查询 ConsumeQueue 条目。 */
    @Override
    public QueryConsumeQueueResponseBody queryConsumeQueue(String brokerAddr, String topic, int queueId, long index,
        int count, String consumerGroup)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException, MQClientException {
        return this.defaultMQAdminExtImpl.queryConsumeQueue(
            brokerAddr, topic, queueId, index, count, consumerGroup
        );
    }

    /** 检查 RocksDB ConsumeQueue 写入进度。 */
    @Override
    public CheckRocksdbCqWriteResult checkRocksdbCqWriteProgress(String brokerAddr, String topic, long checkStoreTime)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException, MQClientException {
        return this.defaultMQAdminExtImpl.checkRocksdbCqWriteProgress(brokerAddr, topic, checkStoreTime);
    }

    /** 导出 Broker RocksDB 配置为 JSON。 */
    @Override
    public void exportRocksDBConfigToJson(String brokerAddr,
        List<ExportRocksDBConfigToJsonRequestHeader.ConfigType> configType)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException, MQClientException {
        this.defaultMQAdminExtImpl.exportRocksDBConfigToJson(brokerAddr, configType);
    }

    /** 恢复半消息事务检查（事务消息运维）。 */
    @Override
    public boolean resumeCheckHalfMessage(String topic,
        String msgId)
        throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        return this.defaultMQAdminExtImpl.resumeCheckHalfMessage(topic, msgId);
    }

    /** 设置 Topic 消费组的 POP/Pull 请求模式。 */
    @Override
    public void setMessageRequestMode(final String brokerAddr, final String topic, final String consumerGroup,
        final MessageRequestMode mode, final int popShareQueueNum, final long timeoutMillis)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException,
        RemotingConnectException, MQClientException {
        this.defaultMQAdminExtImpl.setMessageRequestMode(brokerAddr, topic, consumerGroup, mode, popShareQueueNum, timeoutMillis);
    }

    /** 创建静态 Topic 并写入队列映射。 */
    @Override
    public void createStaticTopic(String addr, String defaultTopic, TopicConfig topicConfig,
        TopicQueueMappingDetail mappingDetail,
        boolean force) throws RemotingException, InterruptedException, MQBrokerException {
        this.defaultMQAdminExtImpl.createStaticTopic(addr, defaultTopic, topicConfig, mappingDetail, force);
    }

    @Deprecated
    /** 按时间戳在队列中查找 offset。 */
    @Override
    public long searchOffset(final String brokerAddr, final String topicName,
        final int queueId, final long timestamp, final long timeoutMillis)
        throws RemotingException, MQBrokerException, InterruptedException {
        return this.defaultMQAdminExtImpl.searchOffset(brokerAddr, topicName, queueId, timestamp, timeoutMillis);
    }

    /** 重置指定队列的消费位点。 */
    @Override
    public void resetOffsetByQueueId(final String brokerAddr, final String consumerGroup,
        final String topicName, final int queueId, final long resetOffset)
        throws RemotingException, InterruptedException, MQBrokerException {
        this.defaultMQAdminExtImpl.resetOffsetByQueueId(brokerAddr, consumerGroup, topicName, queueId, resetOffset);
    }

    /** 查询 Broker 主从复制 HA 状态。 */
    @Override
    public HARuntimeInfo getBrokerHAStatus(String brokerAddr) throws RemotingConnectException,
        RemotingSendRequestException, RemotingTimeoutException, InterruptedException, MQBrokerException {
        return this.defaultMQAdminExtImpl.getBrokerHAStatus(brokerAddr);
    }

    /** 从 Controller 查询副本 InSync 状态。 */
    @Override
    public BrokerReplicasInfo getInSyncStateData(String controllerAddress,
        List<String> brokers) throws RemotingException, InterruptedException, MQBrokerException {
        return this.defaultMQAdminExtImpl.getInSyncStateData(controllerAddress, brokers);
    }

    /** 拉取 Broker Epoch 缓存（Controller 协议）。 */
    @Override
    public EpochEntryCache getBrokerEpochCache(
        String brokerAddr) throws RemotingException, InterruptedException, MQBrokerException {
        return this.defaultMQAdminExtImpl.getBrokerEpochCache(brokerAddr);
    }

    /** 获取 Controller 集群元数据。 */
    public GetMetaDataResponseHeader getControllerMetaData(
        String controllerAddr) throws RemotingException, InterruptedException, MQBrokerException {
        return this.defaultMQAdminExtImpl.getControllerMetaData(controllerAddr);
    }

    /** 在 Slave 上重置 Master flush offset。 */
    @Override
    public void resetMasterFlushOffset(String brokerAddr, long masterFlushOffset)
        throws InterruptedException, RemotingConnectException, RemotingTimeoutException, RemotingSendRequestException, MQBrokerException {
        this.defaultMQAdminExtImpl.resetMasterFlushOffset(brokerAddr, masterFlushOffset);
    }

    /** 按 UniqKey 查询消息。 */
    public QueryResult queryMessageByUniqKey(String clusterName, String topic, String key, int maxNum, long begin,
        long end)
        throws MQClientException, InterruptedException {
        return defaultMQAdminExtImpl.queryMessageByUniqKey(clusterName, topic, key, maxNum, begin, end);
    }

    /** 委托 {@link DefaultMQAdminExtImpl}#getDefaultMQAdminExtImpl。 */
    public DefaultMQAdminExtImpl getDefaultMQAdminExtImpl() {
        return defaultMQAdminExtImpl;
    }

    public GroupForbidden updateAndGetGroupReadForbidden(String brokerAddr, //
        String groupName, //
        String topicName, //
        Boolean readable) //
        throws RemotingException,
        InterruptedException, MQBrokerException {
        return this.defaultMQAdminExtImpl.updateAndGetGroupReadForbidden(brokerAddr, groupName, topicName, readable);
    }

    /** 拉取 Controller 配置。 */
    @Override
    public Map<String, Properties> getControllerConfig(
        List<String> controllerServers) throws InterruptedException, RemotingTimeoutException,
        RemotingSendRequestException, RemotingConnectException, MQClientException,
        UnsupportedEncodingException {
        return this.defaultMQAdminExtImpl.getControllerConfig(controllerServers);
    }

    /** 更新 Controller 配置。 */
    @Override
    public void updateControllerConfig(Properties properties,
        List<String> controllers) throws InterruptedException, RemotingConnectException, UnsupportedEncodingException, RemotingSendRequestException, RemotingTimeoutException, MQClientException, MQBrokerException {
        this.defaultMQAdminExtImpl.updateControllerConfig(properties, controllers);
    }

    /** 手动触发 Controller 选举 Broker Master。 */
    @Override
    public Pair<ElectMasterResponseHeader, BrokerMemberGroup> electMaster(String controllerAddr, String clusterName,
        String brokerName, Long brokerId) throws RemotingException, InterruptedException, MQBrokerException {
        return this.defaultMQAdminExtImpl.electMaster(controllerAddr, clusterName, brokerName, brokerId);
    }

    /** 清理 Controller 中 Broker 元数据。 */
    @Override
    public void cleanControllerBrokerData(String controllerAddr, String clusterName, String brokerName,
        String brokerControllerIdsToClean,
        boolean isCleanLivingBroker) throws RemotingException, InterruptedException, MQBrokerException {
        this.defaultMQAdminExtImpl.cleanControllerBrokerData(controllerAddr, clusterName, brokerName, brokerControllerIdsToClean, isCleanLivingBroker);
    }

    /** 更新冷读流控消费组配置。 */
    @Override
    public void updateColdDataFlowCtrGroupConfig(String brokerAddr, Properties properties)
        throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException,
        UnsupportedEncodingException, InterruptedException, MQBrokerException {
        defaultMQAdminExtImpl.updateColdDataFlowCtrGroupConfig(brokerAddr, properties);
    }

    /** 移除冷读流控消费组配置。 */
    @Override
    public void removeColdDataFlowCtrGroupConfig(String brokerAddr, String consumerGroup)
        throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException,
        UnsupportedEncodingException, InterruptedException, MQBrokerException {
        defaultMQAdminExtImpl.removeColdDataFlowCtrGroupConfig(brokerAddr, consumerGroup);
    }

    /** 查询冷读流控配置 JSON。 */
    @Override
    public String getColdDataFlowCtrInfo(String brokerAddr)
        throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException,
        UnsupportedEncodingException, InterruptedException, MQBrokerException {
        return defaultMQAdminExtImpl.getColdDataFlowCtrInfo(brokerAddr);
    }

    /** 设置 CommitLog 预读模式。 */
    @Override
    public String setCommitLogReadAheadMode(String brokerAddr, String mode)
        throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException,
        InterruptedException, MQBrokerException {
        return defaultMQAdminExtImpl.setCommitLogReadAheadMode(brokerAddr, mode);
    }

    /** 在 Broker 创建用户账号。 */
    @Override
    public void createUser(String brokerAddr,
        UserInfo userInfo) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        defaultMQAdminExtImpl.createUser(brokerAddr, userInfo);
    }

    /** 在 Broker 创建用户账号。 */
    @Override
    public void createUser(String brokerAddr, String username, String password,
        String userType) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        defaultMQAdminExtImpl.createUser(brokerAddr, username, password, userType);
    }

    /** 更新 Broker 用户账号。 */
    @Override
    public void updateUser(String brokerAddr, String username,
        String password, String userType,
        String userStatus) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        defaultMQAdminExtImpl.updateUser(brokerAddr, username, password, userType, userStatus);
    }

    /** 更新 Broker 用户账号。 */
    @Override
    public void updateUser(String brokerAddr,
        UserInfo userInfo) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        defaultMQAdminExtImpl.updateUser(brokerAddr, userInfo);
    }

    /** 删除 Broker 用户。 */
    @Override
    public void deleteUser(String brokerAddr,
        String username) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        defaultMQAdminExtImpl.deleteUser(brokerAddr, username);
    }

    /** 查询 Broker 用户信息。 */
    @Override
    public UserInfo getUser(String brokerAddr,
        String username) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        return defaultMQAdminExtImpl.getUser(brokerAddr, username);
    }

    /** 列出 Broker 用户列表。 */
    @Override
    public List<UserInfo> listUser(String brokerAddr,
        String filter) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        return defaultMQAdminExtImpl.listUser(brokerAddr, filter);
    }

    /** 在 Broker 创建 ACL 规则。 */
    @Override
    public void createAcl(String brokerAddr, String subject, List<String> resources, List<String> actions,
        List<String> sourceIps,
        String decision) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        defaultMQAdminExtImpl.createAcl(brokerAddr, subject, resources, actions, sourceIps, decision);
    }

    /** 在 Broker 创建 ACL 规则。 */
    @Override
    public void createAcl(String brokerAddr,
        AclInfo aclInfo) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        defaultMQAdminExtImpl.createAcl(brokerAddr, aclInfo);
    }

    /** 更新 Broker ACL 规则。 */
    @Override
    public void updateAcl(String brokerAddr, String subject, List<String> resources, List<String> actions,
        List<String> sourceIps,
        String decision) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        defaultMQAdminExtImpl.updateAcl(brokerAddr, subject, resources, actions, sourceIps, decision);
    }

    /** 更新 Broker ACL 规则。 */
    @Override
    public void updateAcl(String brokerAddr,
        AclInfo aclInfo) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        defaultMQAdminExtImpl.updateAcl(brokerAddr, aclInfo);
    }

    /** 删除 Broker ACL 规则。 */
    @Override
    public void deleteAcl(String brokerAddr, String subject,
        String resource) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        defaultMQAdminExtImpl.deleteAcl(brokerAddr, subject, resource);
    }

    /** 查询 Broker ACL 规则。 */
    @Override
    public AclInfo getAcl(String brokerAddr,
        String subject) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        return defaultMQAdminExtImpl.getAcl(brokerAddr, subject);
    }

    /** 列出 Broker ACL 规则。 */
    @Override
    public List<AclInfo> listAcl(String brokerAddr, String subjectFilter,
        String resourceFilter) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        return defaultMQAdminExtImpl.listAcl(brokerAddr, subjectFilter, resourceFilter);
    }

    /** 导出 Broker POP 消费记录。 */
    @Override
    public void exportPopRecords(String brokerAddr, long timeout) throws RemotingConnectException,
        RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        defaultMQAdminExtImpl.exportPopRecords(brokerAddr, timeout);
    }

    /** 切换 Broker 定时消息引擎。 */
    @Override
    public void switchTimerEngine(String brokerAddr, String desTimerEngine) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException,
        UnsupportedEncodingException, InterruptedException, MQBrokerException {
        defaultMQAdminExtImpl.switchTimerEngine(brokerAddr, desTimerEngine);
    }

    /** 查询 Broker Lite 模式信息。 */
    @Override
    public GetBrokerLiteInfoResponseBody getBrokerLiteInfo(final String brokerAddr)
        throws RemotingException, MQBrokerException, InterruptedException {
        return defaultMQAdminExtImpl.getBrokerLiteInfo(brokerAddr);
    }

    /** 查询 Lite 父 Topic 信息。 */
    @Override
    public GetParentTopicInfoResponseBody getParentTopicInfo(final String brokerAddr, final String topic)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return defaultMQAdminExtImpl.getParentTopicInfo(brokerAddr, topic);
    }

    /** 查询 Lite 子 Topic 信息。 */
    @Override
    public GetLiteTopicInfoResponseBody getLiteTopicInfo(final String brokerAddr, final String parentTopic,
        final String liteTopic)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return defaultMQAdminExtImpl.getLiteTopicInfo(brokerAddr, parentTopic, liteTopic);
    }

    /** 查询 Lite 消费端信息。 */
    @Override
    public GetLiteClientInfoResponseBody getLiteClientInfo(final String brokerAddr, final String parentTopic,
        final String group, final String clientId)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return defaultMQAdminExtImpl.getLiteClientInfo(brokerAddr, parentTopic, group, clientId);
    }

    /** 查询 Lite 消费组 TopK 信息。 */
    @Override
    public GetLiteGroupInfoResponseBody getLiteGroupInfo(final String brokerAddr, final String group,
        final String liteTopic, final int topK)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return defaultMQAdminExtImpl.getLiteGroupInfo(brokerAddr, group, liteTopic, topK);
    }

    /** 触发 Lite 消费端消息分发。 */
    @Override
    public void triggerLiteDispatch(final String brokerAddr, final String group, final String clientId)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        defaultMQAdminExtImpl.triggerLiteDispatch(brokerAddr, group, clientId);
    }

}
