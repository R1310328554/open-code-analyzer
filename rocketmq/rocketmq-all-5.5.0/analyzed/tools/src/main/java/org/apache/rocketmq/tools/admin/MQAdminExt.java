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
import org.apache.rocketmq.client.MQAdmin;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.CheckRocksdbCqWriteResult;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.message.MessageRequestMode;
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
 * RocketMQ 管理扩展接口：在 {@link org.apache.rocketmq.client.MQAdmin} 基础上提供 Broker/Topic/消费组运维、集群元数据、偏移重置、ACL/用户管理及 Controller 操作等 API。
 */
public interface MQAdminExt extends MQAdmin {
    /** 启动管理客户端：注册 AdminExt、初始化 MQClient 与并发线程池。 */
    void start() throws MQClientException;/** 关闭管理客户端并释放 MQClient 与线程池资源。 */

    void shutdown();/** 向 Broker 容器动态添加 Broker 实例。 */

    void addBrokerToContainer(final String brokerContainerAddr, final String brokerConfig) throws InterruptedException,
        MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException;/** 从 Broker 容器移除指定 Broker。 */

    void removeBrokerFromContainer(final String brokerContainerAddr, String clusterName, final String brokerName,
        long brokerId) throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException;/** 更新 Broker 运行时配置项。 */

    void updateBrokerConfig(final String brokerAddr, final Properties properties) throws RemotingConnectException,
        RemotingSendRequestException, RemotingTimeoutException, UnsupportedEncodingException, InterruptedException, MQBrokerException, MQClientException;/** 拉取 Broker 当前配置 Properties。 */

    Properties getBrokerConfig(final String brokerAddr) throws RemotingConnectException,
        RemotingSendRequestException, RemotingTimeoutException, UnsupportedEncodingException, InterruptedException, MQBrokerException;/** 在指定 Broker 创建或更新 Topic 配置。 */

    void createAndUpdateTopicConfig(final String addr,
        final TopicConfig config) throws RemotingException, MQBrokerException,
        InterruptedException, MQClientException;/** 批量创建或更新 Topic 配置列表。 */

    void createAndUpdateTopicConfigList(final String addr,
        final List<TopicConfig> topicConfigList) throws InterruptedException, RemotingException, MQClientException;/** 创建或更新消费组订阅配置。 */

    void createAndUpdateSubscriptionGroupConfig(final String addr,
        final SubscriptionGroupConfig config) throws RemotingException,
        MQBrokerException, InterruptedException, MQClientException;/** 批量创建或更新消费组订阅配置。 */

    void createAndUpdateSubscriptionGroupConfigList(String brokerAddr,
        List<SubscriptionGroupConfig> configs) throws RemotingException,
        MQBrokerException, InterruptedException, MQClientException;/** 查询指定 Broker 上某消费组的订阅配置。 */

    SubscriptionGroupConfig examineSubscriptionGroupConfig(final String addr,
        final String group) throws InterruptedException, RemotingException, MQClientException, MQBrokerException;/** 查询 Topic 在各 Broker 上的统计（min/max offset、TPS 等）。 */

    TopicStatsTable examineTopicStats(
        final String topic) throws RemotingException, MQClientException, InterruptedException,
        MQBrokerException;/** 查询 Topic 在各 Broker 上的统计（min/max offset、TPS 等）。 */

    TopicStatsTable examineTopicStats(String brokerAddr,
        final String topic) throws RemotingException, MQClientException, InterruptedException,
        MQBrokerException;/** 并发查询 Topic 统计并封装为 {@link AdminToolResult}。 */

    AdminToolResult<TopicStatsTable> examineTopicStatsConcurrent(String topic);/** 从 NameServer 拉取全部 Topic 列表。 */

    TopicList fetchAllTopicList() throws RemotingException, MQClientException, InterruptedException;/** 按集群名拉取 Topic 列表。 */

    TopicList fetchTopicsByCLuster(
        String clusterName) throws RemotingException, MQClientException, InterruptedException;/** 拉取 Broker 运行时 KV 指标。 */

    KVTable fetchBrokerRuntimeStats(
        final String brokerAddr) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, InterruptedException, MQBrokerException;/** 查询消费组消费进度与 TPS（可指定 Topic/集群/Broker）。 */

    ConsumeStats examineConsumeStats(
        final String consumerGroup) throws RemotingException, MQClientException, InterruptedException,
        MQBrokerException;/** 检查 RocksDB ConsumeQueue 写入进度。 */

    CheckRocksdbCqWriteResult checkRocksdbCqWriteProgress(String brokerAddr, String topic, long checkStoreTime)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException, MQClientException;/** 查询消费组消费进度与 TPS（可指定 Topic/集群/Broker）。 */

    ConsumeStats examineConsumeStats(final String consumerGroup,
        final String topic) throws RemotingException, MQClientException,
        InterruptedException, MQBrokerException;/** 查询消费组消费进度与 TPS（可指定 Topic/集群/Broker）。 */

    ConsumeStats examineConsumeStats(final String clusterName, final String consumerGroup,
        final String topic) throws RemotingException, MQClientException,
        InterruptedException, MQBrokerException;/** 查询消费组消费进度与 TPS（可指定 Topic/集群/Broker）。 */

    ConsumeStats examineConsumeStats(final String brokerAddr, final String consumerGroup, final String topicName,
        final long timeoutMillis) throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException,
        RemotingConnectException, MQBrokerException;/** 并发汇总消费组消费统计。 */

    AdminToolResult<ConsumeStats> examineConsumeStatsConcurrent(String consumerGroup, String topic);/** 从 NameServer 获取集群 Broker 拓扑信息。 */

    ClusterInfo examineBrokerClusterInfo() throws InterruptedException, MQBrokerException, RemotingTimeoutException,
        RemotingSendRequestException, RemotingConnectException;/** 查询 Topic 路由（Broker 与队列分布）。 */

    TopicRouteData examineTopicRouteInfo(
        final String topic) throws RemotingException, MQClientException, InterruptedException;/** 查询消费组在线连接与订阅关系。 */

    ConsumerConnection examineConsumerConnectionInfo(final String consumerGroup) throws RemotingConnectException,
        RemotingSendRequestException, RemotingTimeoutException, InterruptedException, MQBrokerException, RemotingException,
        MQClientException;/** 查询消费组在线连接与订阅关系。 */

    ConsumerConnection examineConsumerConnectionInfo(
        String consumerGroup, String brokerAddr) throws InterruptedException, MQBrokerException,
        RemotingException, MQClientException;/** 查询指定 Topic 下生产者连接信息。 */

    ProducerConnection examineProducerConnectionInfo(final String producerGroup,
        final String topic) throws RemotingException,
        MQClientException, InterruptedException, MQBrokerException;/** 拉取 Broker 上全部生产者连接表。 */

    ProducerTableInfo getAllProducerInfo(final String brokerAddr) throws RemotingException,
        MQClientException, InterruptedException, MQBrokerException;/** 返回当前配置的 NameServer 地址列表。 */

    List<String> getNameServerAddressList();/** 在 NameServer 上撤销 Broker 写权限。 */

    int wipeWritePermOfBroker(final String namesrvAddr, String brokerName) throws RemotingCommandException,
        RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException, MQClientException;/** 在 NameServer 上恢复 Broker 写权限。 */

    int addWritePermOfBroker(final String namesrvAddr, String brokerName) throws RemotingCommandException,
        RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException, MQClientException;/** 本地写入 KV 配置（不经过 Broker）。 */

    void putKVConfig(final String namespace, final String key, final String value);/** 从 NameServer 读取 KV 配置值。 */

    String getKVConfig(final String namespace,
        final String key) throws RemotingException, MQClientException, InterruptedException;/** 按命名空间拉取 KV 配置表。 */

    KVTable getKVListByNamespace(
        final String namespace) throws RemotingException, MQClientException, InterruptedException;/** 从集群 Broker 与 NameServer 删除 Topic。 */

    void deleteTopic(final String topicName,
        final String clusterName) throws RemotingException, MQBrokerException, InterruptedException, MQClientException;/** 从指定 Broker 集合删除 Topic。 */

    void deleteTopicInBroker(final Set<String> addrs, final String topic) throws RemotingException, MQBrokerException,
        InterruptedException, MQClientException;/** 并发从多个 Broker 删除 Topic。 */

    AdminToolResult<BrokerOperatorResult> deleteTopicInBrokerConcurrent(Set<String> addrs, String topic);/** 从 NameServer 删除 Topic 路由元数据。 */

    void deleteTopicInNameServer(final Set<String> addrs,
        final String topic) throws RemotingException, MQBrokerException,
        InterruptedException, MQClientException;/** 从 NameServer 删除 Topic 路由元数据。 */

    void deleteTopicInNameServer(final Set<String> addrs,
        final String clusterName,
        final String topic) throws RemotingException, MQBrokerException,
        InterruptedException, MQClientException;/** 删除 Broker 上指定消费组配置。 */

    void deleteSubscriptionGroup(final String addr, String groupName) throws RemotingException, MQBrokerException,
        InterruptedException, MQClientException;/** 删除 Broker 上指定消费组配置。 */

    void deleteSubscriptionGroup(final String addr, String groupName,
        boolean removeOffset) throws RemotingException, MQBrokerException,
        InterruptedException, MQClientException;/** 在 Broker/NameServer 创建或更新 KV 配置。 */

    void createAndUpdateKvConfig(String namespace, String key,
        String value) throws RemotingException, MQBrokerException,
        InterruptedException, MQClientException;/** 删除 KV 配置项。 */

    void deleteKvConfig(String namespace, String key) throws RemotingException, MQBrokerException, InterruptedException,
        MQClientException;/** 按时间戳重置消费位点（旧版 API，返回 RollbackStats）。 */

    List<RollbackStats> resetOffsetByTimestampOld(String consumerGroup, String topic, long timestamp, boolean force)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException;/** 按时间戳重置消费位点到各队列。 */

    Map<MessageQueue, Long> resetOffsetByTimestamp(String topic, String group, long timestamp, boolean isForce)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException;/** 按时间戳重置消费位点到各队列。 */

    Map<MessageQueue, Long> resetOffsetByTimestamp(String clusterName, String topic, String group, long timestamp, boolean isForce)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException;/** 新版按时间戳重置消费位点。 */

    void resetOffsetNew(String consumerGroup, String topic, long timestamp) throws RemotingException, MQBrokerException,
        InterruptedException, MQClientException;/** 并发向各 Broker 执行新版位点重置。 */

    AdminToolResult<BrokerOperatorResult> resetOffsetNewConcurrent(final String group, final String topic,
        final long timestamp);/** 查询消费组各客户端队列消费状态。 */

    Map<String, Map<MessageQueue, Long>> getConsumeStatus(String topic, String group,
        String clientAddr) throws RemotingException,
        MQBrokerException, InterruptedException, MQClientException;/** 创建或更新顺序消息全局配置。 */

    void createOrUpdateOrderConf(String key, String value,
        boolean isCluster) throws RemotingException, MQBrokerException,
        InterruptedException, MQClientException;/** 查询订阅指定 Topic 的消费组列表。 */

    GroupList queryTopicConsumeByWho(final String topic) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, InterruptedException, MQBrokerException, RemotingException, MQClientException;/** 查询某消费组订阅的全部 Topic。 */

    TopicList queryTopicsByConsumer(
        final String group) throws InterruptedException, MQBrokerException, RemotingException, MQClientException;/** 并发查询消费组订阅 Topic 列表。 */

    AdminToolResult<TopicList> queryTopicsByConsumerConcurrent(final String group);/** 查询消费组对某 Topic 的订阅表达式。 */

    SubscriptionData querySubscription(final String group,
        final String topic) throws InterruptedException, MQBrokerException, RemotingException, MQClientException;/** 查询消费组在各队列上的消费时间跨度。 */

    List<QueueTimeSpan> queryConsumeTimeSpan(final String topic,
        final String group) throws InterruptedException, MQBrokerException,
        RemotingException, MQClientException;/** 并发查询消费时间跨度。 */

    AdminToolResult<List<QueueTimeSpan>> queryConsumeTimeSpanConcurrent(final String topic, final String group);/** 清理集群内过期消费进度。 */

    boolean cleanExpiredConsumerQueue(String cluster) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException;/** 清理指定 Broker 上过期消费进度。 */

    boolean cleanExpiredConsumerQueueByAddr(String addr) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException;/** 触发集群删除过期 CommitLog。 */

    boolean deleteExpiredCommitLog(String cluster) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException;/** 触发指定 Broker 删除过期 CommitLog。 */

    boolean deleteExpiredCommitLogByAddr(String addr) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException;/** 清理集群未使用 Topic。 */

    boolean cleanUnusedTopic(String cluster) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException;/** 清理指定 Broker 未使用 Topic。 */

    boolean cleanUnusedTopicByAddr(String addr) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException;/** 拉取消费端运行态（订阅、ProcessQueue、可选 jstack/metrics）。 */

    ConsumerRunningInfo getConsumerRunningInfo(final String consumerGroup, final String clientId, final boolean jstack)
        throws RemotingException, MQClientException, InterruptedException;/** 拉取消费端运行态（订阅、ProcessQueue、可选 jstack/metrics）。 */

    ConsumerRunningInfo getConsumerRunningInfo(final String consumerGroup, final String clientId, final boolean jstack,
        final boolean metrics)
        throws RemotingException, MQClientException, InterruptedException;/** 向指定消费端直接投递并消费一条消息（运维调试）。 */

    ConsumeMessageDirectlyResult consumeMessageDirectly(String consumerGroup,
        String clientId,
        String topic,
        String msgId) throws RemotingException, MQClientException, InterruptedException, MQBrokerException;/** 向指定消费端直接投递并消费一条消息（运维调试）。 */

    ConsumeMessageDirectlyResult consumeMessageDirectly(String clusterName, String consumerGroup,
        String clientId,
        String topic,
        String msgId) throws RemotingException, MQClientException, InterruptedException, MQBrokerException;/** 查询消息在各消费组的投递/消费轨迹。 */

    List<MessageTrack> messageTrackDetail(
        MessageExt msg) throws RemotingException, MQClientException, InterruptedException,
        MQBrokerException;/** 并发查询消息轨迹详情。 */

    List<MessageTrack> messageTrackDetailConcurrent(
        MessageExt msg) throws RemotingException, MQClientException, InterruptedException,
        MQBrokerException;/** 将源消费组位点克隆到目标消费组。 */

    void cloneGroupOffset(String srcGroup, String destGroup, String topic, boolean isOffline) throws RemotingException,
        MQClientException, InterruptedException, MQBrokerException;/** 查看 Broker 指定统计项明细。 */

    BrokerStatsData viewBrokerStatsData(final String brokerAddr, final String statsName, final String statsKey)
        throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQClientException,
        InterruptedException;/** 返回 Topic 所在集群名集合。 */

    Set<String> getClusterList(final String topic) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException;/** 拉取 Broker 上全部消费组统计。 */

    ConsumeStatsList fetchConsumeStatsInBroker(final String brokerAddr, boolean isOrder,
        long timeoutMillis) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, MQClientException, InterruptedException;/** 返回 Topic 关联的集群列表。 */

    Set<String> getTopicClusterList(
        final String topic) throws InterruptedException, MQBrokerException, MQClientException, RemotingException;/** 拉取 Broker 全部订阅组配置。 */

    SubscriptionGroupWrapper getAllSubscriptionGroup(final String brokerAddr,
        long timeoutMillis)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException,
        MQBrokerException, RemotingCommandException;/** 拉取 Broker 用户订阅组配置（不含系统组）。 */

    SubscriptionGroupWrapper getUserSubscriptionGroup(final String brokerAddr,
        long timeoutMillis)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException,
        MQBrokerException, RemotingCommandException;/** 拉取 Broker 全部 Topic 配置。 */

    TopicConfigSerializeWrapper getAllTopicConfig(final String brokerAddr,
        long timeoutMillis) throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException,
        RemotingConnectException, MQBrokerException, RemotingCommandException;/** 拉取 Broker 用户 Topic 配置。 */

    TopicConfigSerializeWrapper getUserTopicConfig(final String brokerAddr, final boolean specialTopic,
        long timeoutMillis) throws InterruptedException, RemotingException,
        MQBrokerException, MQClientException;/** 更新消费组在指定队列上的 commit offset。 */

    void updateConsumeOffset(String brokerAddr, String consumeGroup, MessageQueue mq,
        long offset) throws RemotingException, InterruptedException, MQBrokerException;
    /**
     * 更新 NameServer 配置。
     * <br>
     * 命令码：RequestCode.UPDATE_NAMESRV_CONFIG
     *
     * <br> nameServers 为空时使用客户端已配置的 NameServer 列表。
     */
    void updateNameServerConfig(final Properties properties,
        final List<String> nameServers) throws InterruptedException, RemotingConnectException,
        UnsupportedEncodingException, RemotingSendRequestException, RemotingTimeoutException,
        MQClientException, MQBrokerException;

    /**
     * 获取 NameServer 配置。
     * <br>
     * 命令码：RequestCode.GET_NAMESRV_CONFIG
     * <br> nameServers 为空时使用客户端已配置的 NameServer 列表。
     *
     * @return NameServer 配置映射
     */
    Map<String, Properties> getNameServerConfig(final List<String> nameServers) throws InterruptedException,
        RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException,
        MQClientException, UnsupportedEncodingException;

    /**
     * 分页查询 ConsumeQueue 数据。
     *
     * @param brokerAddr Broker 地址
     * @param topic Topic 名称
     * @param queueId 队列 ID
     * @param index 起始逻辑 offset
     * @param count 拉取条数
     * @param consumerGroup 消费组（可选过滤）
     */
    QueryConsumeQueueResponseBody queryConsumeQueue(final String brokerAddr,
        final String topic, final int queueId,
        final long index, final int count, final String consumerGroup)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException, MQClientException;
/** 导出 Broker RocksDB 配置为 JSON。 */

    void exportRocksDBConfigToJson(String brokerAddr,
        List<ExportRocksDBConfigToJsonRequestHeader.ConfigType> configType)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException, MQClientException;/** 恢复半消息事务检查（事务消息运维）。 */

    boolean resumeCheckHalfMessage(final String topic,
        final String msgId) throws RemotingException, MQClientException, InterruptedException, MQBrokerException;/** 设置 Topic 消费组的 POP/Pull 请求模式。 */

    void setMessageRequestMode(final String brokerAddr, final String topic, final String consumerGroup,
        final MessageRequestMode mode, final int popWorkGroupSize, final long timeoutMillis)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException,
        RemotingConnectException, MQClientException;
    @Deprecated
    /** 按时间戳在队列中查找 offset。 */
    long searchOffset(final String brokerAddr, final String topicName,
        final int queueId, final long timestamp, final long timeoutMillis)
        throws RemotingException, MQBrokerException, InterruptedException;/** 重置指定队列的消费位点。 */

    void resetOffsetByQueueId(final String brokerAddr, final String consumerGroup,
        final String topicName, final int queueId, final long resetOffset)
        throws RemotingException, InterruptedException, MQBrokerException;/** 查询指定 Broker 上某 Topic 的配置。 */

    TopicConfig examineTopicConfig(final String addr,
        final String topic) throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException;/** 创建静态 Topic 并写入队列映射。 */

    void createStaticTopic(final String addr, final String defaultTopic, final TopicConfig topicConfig,
        final TopicQueueMappingDetail mappingDetail,
        final boolean force) throws RemotingException, InterruptedException, MQBrokerException;/** 更新并返回消费组 Topic 读禁止状态。 */

    GroupForbidden updateAndGetGroupReadForbidden(String brokerAddr, String groupName, String topicName,
        Boolean readable)
        throws RemotingException, InterruptedException, MQBrokerException;/** 按 Topic/Key 或集群条件索引查询消息。 */

    MessageExt queryMessage(String clusterName,
        String topic,
        String msgId) throws RemotingException, MQBrokerException, InterruptedException, MQClientException;/** 查询 Broker 主从复制 HA 状态。 */

    HARuntimeInfo getBrokerHAStatus(String brokerAddr) throws RemotingConnectException, RemotingSendRequestException,
        RemotingTimeoutException, InterruptedException, MQBrokerException;/** 从 Controller 查询副本 InSync 状态。 */

    BrokerReplicasInfo getInSyncStateData(String controllerAddress,
        List<String> brokers) throws RemotingException, InterruptedException, MQBrokerException;/** 拉取 Broker Epoch 缓存（Controller 协议）。 */

    EpochEntryCache getBrokerEpochCache(
        String brokerAddr) throws RemotingException, InterruptedException, MQBrokerException;/** 获取 Controller 集群元数据。 */

    GetMetaDataResponseHeader getControllerMetaData(
        String controllerAddr) throws RemotingException, InterruptedException, MQBrokerException;
    /**
     * 在 Slave Broker 上重置 Master flush offset。
     *
     * @param brokerAddr Slave Broker 地址
     * @param masterFlushOffset Master flush offset
     */
    void resetMasterFlushOffset(String brokerAddr, long masterFlushOffset)
        throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException;

    /**
     * 获取 Controller 配置。
     * <br>
     * 命令码：RequestCode.GET_CONTROLLER_CONFIG
     *
     * @return Controller 配置映射
     */
    Map<String, Properties> getControllerConfig(
        List<String> controllerServers) throws InterruptedException, RemotingTimeoutException,
        RemotingSendRequestException, RemotingConnectException, MQClientException, UnsupportedEncodingException;

    /**
     * 更新 Controller 配置。
     * <br>
     * 命令码：RequestCode.UPDATE_CONTROLLER_CONFIG
     */
    void updateControllerConfig(final Properties properties,
        final List<String> controllers) throws InterruptedException, RemotingConnectException,
        UnsupportedEncodingException, RemotingSendRequestException, RemotingTimeoutException, MQClientException, MQBrokerException;

    /**
     * 手动触发 Controller 选举 Broker Master。
     *
     * @param controllerAddr Controller 地址
     * @param clusterName 集群名
     * @param brokerName Broker 名
     * @param brokerId Broker ID
     * @return 选举响应与 Broker 成员组
     */
    Pair<ElectMasterResponseHeader, BrokerMemberGroup> electMaster(String controllerAddr, String clusterName, String brokerName,
                                                                   Long brokerId) throws RemotingException, InterruptedException, MQBrokerException;

    /** 清理 Controller 中指定 Broker 元数据。 */
    void cleanControllerBrokerData(String controllerAddr, String clusterName, String brokerName,
        String brokerControllerIdsToClean,
        boolean isCleanLivingBroker) throws RemotingException, InterruptedException, MQBrokerException;
/** 更新冷读流控消费组配置。 */

    void updateColdDataFlowCtrGroupConfig(final String brokerAddr, final Properties properties)
        throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, UnsupportedEncodingException, InterruptedException, MQBrokerException;/** 移除冷读流控消费组配置。 */

    void removeColdDataFlowCtrGroupConfig(final String brokerAddr, final String consumerGroup)
        throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, UnsupportedEncodingException, InterruptedException, MQBrokerException;/** 查询冷读流控配置 JSON。 */

    String getColdDataFlowCtrInfo(final String brokerAddr)
        throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, UnsupportedEncodingException, InterruptedException, MQBrokerException;/** 设置 CommitLog 预读模式。 */

    String setCommitLogReadAheadMode(final String brokerAddr, String mode)
        throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, UnsupportedEncodingException, InterruptedException, MQBrokerException;/** 在 Broker 创建用户账号。 */

    void createUser(String brokerAddr, String username, String password, String userType) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException;/** 在 Broker 创建用户账号。 */

    void createUser(String brokerAddr, UserInfo userInfo) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException;/** 更新 Broker 用户账号。 */

    void updateUser(String brokerAddr, String username, String password, String userType, String userStatus) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException;/** 更新 Broker 用户账号。 */

    void updateUser(String brokerAddr, UserInfo userInfo) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException;/** 删除 Broker 用户。 */

    void deleteUser(String brokerAddr, String username) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException;/** 查询 Broker 用户信息。 */

    UserInfo getUser(String brokerAddr, String username) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException;/** 列出 Broker 用户列表。 */

    List<UserInfo> listUser(String brokerAddr, String filter) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException;/** 在 Broker 创建 ACL 规则。 */

    void createAcl(String brokerAddr, String subject, List<String> resources, List<String> actions, List<String> sourceIps, String decision) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException;/** 在 Broker 创建 ACL 规则。 */

    void createAcl(String brokerAddr, AclInfo aclInfo) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException;/** 更新 Broker ACL 规则。 */

    void updateAcl(String brokerAddr, String subject, List<String> resources, List<String> actions, List<String> sourceIps, String decision) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException;/** 更新 Broker ACL 规则。 */

    void updateAcl(String brokerAddr, AclInfo aclInfo) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException;/** 删除 Broker ACL 规则。 */

    void deleteAcl(String brokerAddr, String subject, String resource) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException;/** 查询 Broker ACL 规则。 */

    AclInfo getAcl(String brokerAddr, String subject) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException;/** 列出 Broker ACL 规则。 */

    List<AclInfo> listAcl(String brokerAddr, String subjectFilter, String resourceFilter) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException;/** 导出 Broker POP 消费记录。 */

    void exportPopRecords(String brokerAddr, long timeout) throws RemotingConnectException,
        RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException;/** 切换 Broker 定时消息引擎。 */

    void switchTimerEngine(String brokerAddr, String desTimerEngine) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, UnsupportedEncodingException, InterruptedException, MQBrokerException;/** 查询 Broker Lite 模式信息。 */

    GetBrokerLiteInfoResponseBody getBrokerLiteInfo(final String brokerAddr)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException;/** 查询 Lite 父 Topic 信息。 */

    GetParentTopicInfoResponseBody getParentTopicInfo(final String brokerAddr, final String topic)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException;/** 查询 Lite 子 Topic 信息。 */

    GetLiteTopicInfoResponseBody getLiteTopicInfo(final String brokerAddr, final String parentTopic,
        final String liteTopic)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException;/** 查询 Lite 消费端信息。 */

    GetLiteClientInfoResponseBody getLiteClientInfo(final String brokerAddr, final String parentTopic,
        final String group, final String clientId)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException;/** 查询 Lite 消费组 TopK 信息。 */

    GetLiteGroupInfoResponseBody getLiteGroupInfo(final String brokerAddr, final String group,
        final String liteTopic, final int topK)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException;/** 触发 Lite 消费端消息分发。 */

    void triggerLiteDispatch(final String brokerAddr, final String group, final String clientId)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException;
}
