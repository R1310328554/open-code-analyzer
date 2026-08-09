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

import com.alibaba.fastjson2.JSON;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.admin.MQAdminExtInner;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.MQClientManager;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.BoundaryType;
import org.apache.rocketmq.common.CheckRocksdbCqWriteResult;
import org.apache.rocketmq.common.KeyBuilder;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.ServiceState;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.constant.PermName;
import org.apache.rocketmq.common.help.FAQUrl;
import org.apache.rocketmq.common.message.MessageClientExt;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.message.MessageRequestMode;
import org.apache.rocketmq.common.namesrv.NamesrvUtil;
import org.apache.rocketmq.common.topic.TopicValidator;
import org.apache.rocketmq.common.utils.NetworkUtil;
import org.apache.rocketmq.common.utils.ThreadUtils;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;
import org.apache.rocketmq.remoting.protocol.admin.RollbackStats;
import org.apache.rocketmq.remoting.protocol.admin.TopicOffset;
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
import org.apache.rocketmq.remoting.protocol.header.UpdateConsumerOffsetRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.UpdateGroupForbiddenRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.ElectMasterResponseHeader;
import org.apache.rocketmq.remoting.protocol.header.controller.GetMetaDataResponseHeader;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.QueueData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.protocol.statictopic.TopicConfigAndQueueMapping;
import org.apache.rocketmq.remoting.protocol.statictopic.TopicQueueMappingDetail;
import org.apache.rocketmq.remoting.protocol.subscription.GroupForbidden;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.tools.admin.api.BrokerOperatorResult;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.apache.rocketmq.tools.admin.api.TrackType;
import org.apache.rocketmq.tools.admin.common.AdminToolHandler;
import org.apache.rocketmq.tools.admin.common.AdminToolResult;
import org.apache.rocketmq.tools.admin.common.AdminToolsResultCodeEnum;
import org.apache.rocketmq.tools.command.CommandUtil;

/**
 * MQ 管理扩展核心实现：封装 {@link MQClientInstance} 与 Remoting API，完成 Topic/订阅组配置、消费统计、消息轨迹、偏移管理、NameServer/Controller 配置及并发批量运维。
 */
public class DefaultMQAdminExtImpl implements MQAdminExt, MQAdminExtInner {

    /** 环境变量名：SOCKS 代理 JSON 配置。 */
    private static final String SOCKS_PROXY_JSON = "socksProxyJson";

    /** 管理扩展实现日志。 */
    private final Logger logger = LoggerFactory.getLogger(DefaultMQAdminExtImpl.class);
    /** 关联的 {@link DefaultMQAdminExt} 配置与分组信息。 */
    private final DefaultMQAdminExt defaultMQAdminExt;
    /** AdminExt 生命周期状态。 */
    private ServiceState serviceState = ServiceState.CREATE_JUST;
    /** 底层 MQ 客户端实例，负责 Remoting 通信。 */
    private MQClientInstance mqClientInstance;
    /** 可选 RPC 钩子（鉴权等）。 */
    private RPCHook rpcHook;
    /** Remoting 默认超时（毫秒）。 */
    private long timeoutMillis = 20000;
    /** 随机数，用于负载均衡选 Broker 等。 */
    private Random random = new Random();

    /** shutdown 时需清理的 KV 命名空间列表。 */
    protected final List<String> kvNamespaceToDeleteList = Arrays.asList(NamesrvUtil.NAMESPACE_ORDER_TOPIC_CONFIG);
    /** 并发管理操作线程池（Topic 统计、位点重置等）。 */
    protected ThreadPoolExecutor threadPoolExecutor;

    public DefaultMQAdminExtImpl(DefaultMQAdminExt defaultMQAdminExt, long timeoutMillis) {
        this(defaultMQAdminExt, null, timeoutMillis);
    }

    public DefaultMQAdminExtImpl(DefaultMQAdminExt defaultMQAdminExt, RPCHook rpcHook, long timeoutMillis) {
        this.defaultMQAdminExt = defaultMQAdminExt;
        this.rpcHook = rpcHook;
        this.timeoutMillis = timeoutMillis;
    }

    /** 启动管理客户端：注册 AdminExt、初始化 MQClient 与并发线程池。 */
    @Override
    public void start() throws MQClientException {
        switch (this.serviceState) {
            case CREATE_JUST:
                this.serviceState = ServiceState.START_FAILED;

                this.defaultMQAdminExt.changeInstanceNameToPID();

                if ("{}".equals(this.defaultMQAdminExt.getSocksProxyConfig())) {
                    String proxyConfig = System.getenv(SOCKS_PROXY_JSON);
                    this.defaultMQAdminExt.setSocksProxyConfig(StringUtils.isNotEmpty(proxyConfig) ? proxyConfig : "{}");
                }

                this.mqClientInstance = MQClientManager.getInstance().getOrCreateMQClientInstance(this.defaultMQAdminExt, rpcHook);

                boolean registerOK = mqClientInstance.registerAdminExt(this.defaultMQAdminExt.getAdminExtGroup(), this);
                if (!registerOK) {
                    this.serviceState = ServiceState.CREATE_JUST;
                    throw new MQClientException("The adminExt group[" + this.defaultMQAdminExt.getAdminExtGroup() + "] has created already, specified another name please." + FAQUrl.suggestTodo(FAQUrl.GROUP_NAME_DUPLICATE_URL), null);
                }

                mqClientInstance.start();

                logger.info("the adminExt [{}] start OK", this.defaultMQAdminExt.getAdminExtGroup());

                this.serviceState = ServiceState.RUNNING;

                int threadPoolCoreSize = Integer.parseInt(System.getProperty("rocketmq.admin.threadpool.coresize", "20"));

                this.threadPoolExecutor = (ThreadPoolExecutor) ThreadUtils.newThreadPoolExecutor(threadPoolCoreSize, 100, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new ThreadFactoryImpl("DefaultMQAdminExtImpl_"));

                break;
            case RUNNING:
            case START_FAILED:
            case SHUTDOWN_ALREADY:
                throw new MQClientException("The AdminExt service state not OK, maybe started once, " + this.serviceState + FAQUrl.suggestTodo(FAQUrl.CLIENT_SERVICE_NOT_OK), null);
            default:
                break;
        }
    }

    /** 关闭管理客户端并释放 MQClient 与线程池资源。 */
    @Override
    public void shutdown() {
        switch (this.serviceState) {
            case CREATE_JUST:
                break;
            case RUNNING:
                this.mqClientInstance.unregisterAdminExt(this.defaultMQAdminExt.getAdminExtGroup());
                this.mqClientInstance.shutdown();

                logger.info("the adminExt [{}] shutdown OK", this.defaultMQAdminExt.getAdminExtGroup());
                this.serviceState = ServiceState.SHUTDOWN_ALREADY;
                this.threadPoolExecutor.shutdown();
                break;
            case SHUTDOWN_ALREADY:
                break;
            default:
                break;
        }
    }

    /** 向 Broker 容器动态添加 Broker 实例。 */
    @Override
    public void addBrokerToContainer(String brokerContainerAddr,
        String brokerConfig) throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException {
        this.mqClientInstance.getMQClientAPIImpl().addBroker(brokerContainerAddr, brokerConfig, 20000);
    }

    /** 从 Broker 容器移除指定 Broker。 */
    @Override
    public void removeBrokerFromContainer(String brokerContainerAddr, String clusterName, String brokerName,
        long brokerId) throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException {
        this.mqClientInstance.getMQClientAPIImpl().removeBroker(brokerContainerAddr, clusterName, brokerName, brokerId, 20000);
    }

    /** 统一执行 {@link AdminToolHandler} 并映射异常为 {@link AdminToolResult}。 */
    public AdminToolResult adminToolExecute(AdminToolHandler handler) {
        try {
            return handler.doExecute();
        } catch (RemotingException e) {
            logger.error("", e);
            return AdminToolResult.failure(AdminToolsResultCodeEnum.REMOTING_ERROR, e.getMessage());
        } catch (MQClientException e) {
            if (ResponseCode.TOPIC_NOT_EXIST == e.getResponseCode()) {
                return AdminToolResult.failure(AdminToolsResultCodeEnum.TOPIC_ROUTE_INFO_NOT_EXIST, e.getErrorMessage());
            }
            return AdminToolResult.failure(AdminToolsResultCodeEnum.MQ_CLIENT_ERROR, e.getMessage());
        } catch (InterruptedException e) {
            return AdminToolResult.failure(AdminToolsResultCodeEnum.INTERRUPT_ERROR, e.getMessage());
        } catch (Exception e) {
            return AdminToolResult.failure(AdminToolsResultCodeEnum.MQ_BROKER_ERROR, e.getMessage());
        }
    }

    /** 更新 Broker 运行时配置项。 */
    @Override
    public void updateBrokerConfig(String brokerAddr,
        Properties properties) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, UnsupportedEncodingException, InterruptedException, MQBrokerException, MQClientException {
        this.mqClientInstance.getMQClientAPIImpl().updateBrokerConfig(brokerAddr, properties, timeoutMillis);
    }

    /** 拉取 Broker 当前配置 Properties。 */
    @Override
    public Properties getBrokerConfig(
        final String brokerAddr) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, UnsupportedEncodingException, InterruptedException, MQBrokerException {
        return this.mqClientInstance.getMQClientAPIImpl().getBrokerConfig(brokerAddr, timeoutMillis);
    }

    /** 在指定 Broker 创建或更新 Topic 配置。 */
    @Override
    public void createAndUpdateTopicConfig(String addr,
        TopicConfig config) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        this.mqClientInstance.getMQClientAPIImpl().createTopic(addr, this.defaultMQAdminExt.getCreateTopicKey(), config, timeoutMillis);
    }

    /** 批量创建或更新 Topic 配置列表。 */
    @Override
    public void createAndUpdateTopicConfigList(final String brokerAddr,
        final List<TopicConfig> topicConfigList) throws RemotingException, InterruptedException, MQClientException {
        this.mqClientInstance.getMQClientAPIImpl().createTopicList(brokerAddr, topicConfigList, timeoutMillis);
    }

    /** 创建或更新消费组订阅配置。 */
    @Override
    public void createAndUpdateSubscriptionGroupConfig(String addr,
        SubscriptionGroupConfig config) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        this.mqClientInstance.getMQClientAPIImpl().createSubscriptionGroup(addr, config, timeoutMillis);
    }

    /** 批量创建或更新消费组订阅配置。 */
    @Override
    public void createAndUpdateSubscriptionGroupConfigList(String brokerAddr,
        List<SubscriptionGroupConfig> configs) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        this.mqClientInstance.getMQClientAPIImpl().createSubscriptionGroupList(brokerAddr, configs, timeoutMillis);
    }

    /** 查询指定 Broker 上某消费组的订阅配置。 */
    @Override
    public SubscriptionGroupConfig examineSubscriptionGroupConfig(String addr,
        String group) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        SubscriptionGroupWrapper wrapper = this.mqClientInstance.getMQClientAPIImpl().getAllSubscriptionGroup(addr, timeoutMillis);
        return wrapper.getSubscriptionGroupTable().get(group);
    }

    /** 查询指定 Broker 上某 Topic 的配置。 */
    @Override
    public TopicConfig examineTopicConfig(String addr,
        String topic) throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException {
        return this.mqClientInstance.getMQClientAPIImpl().getTopicConfig(addr, topic, timeoutMillis);
    }

    /** 查询 Topic 在各 Broker 上的统计（min/max offset、TPS 等）。 */
    @Override
    public TopicStatsTable examineTopicStats(
        String topic) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        TopicRouteData topicRouteData = this.examineTopicRouteInfo(topic);
        TopicStatsTable topicStatsTable = new TopicStatsTable();

        for (BrokerData bd : topicRouteData.getBrokerDatas()) {
            String addr = bd.selectBrokerAddr();
            if (addr != null) {
                TopicStatsTable tst = this.mqClientInstance.getMQClientAPIImpl().getTopicStatsInfo(addr, topic, timeoutMillis);
                topicStatsTable.getOffsetTable().putAll(tst.getOffsetTable());
            }
        }

        // 合并静态 Topic 逻辑队列统计
        Map<String, TopicConfigAndQueueMapping> brokerConfigMap = MQAdminUtils.examineTopicConfigFromRoute(topic, topicRouteData, defaultMQAdminExt);
        MQAdminUtils.convertPhysicalTopicStats(topic, brokerConfigMap, topicStatsTable);

        if (topicStatsTable.getOffsetTable().isEmpty()) {
            throw new MQClientException("Not found the topic stats info", null);
        }

        return topicStatsTable;
    }

    /** 并发查询 Topic 统计并封装为 {@link AdminToolResult}。 */
    @Override
    public AdminToolResult<TopicStatsTable> examineTopicStatsConcurrent(final String topic) {
        return adminToolExecute(new AdminToolHandler() {
            @Override
            public AdminToolResult doExecute() throws Exception {
                final TopicStatsTable topicStatsTable = new TopicStatsTable();
                TopicRouteData topicRouteData = examineTopicRouteInfo(topic);

                if (topicRouteData == null || CollectionUtils.isEmpty(topicRouteData.getBrokerDatas())) {
                    return AdminToolResult.success(topicStatsTable);
                }
                final CountDownLatch latch = new CountDownLatch(topicRouteData.getBrokerDatas().size());
                for (final BrokerData bd : topicRouteData.getBrokerDatas()) {
                    threadPoolExecutor.submit(new Runnable() {
                        /** 后台线程：过期清理、压缩上传与优雅关闭。 */
                        @Override
                        public void run() {
                            try {
                                String addr = bd.selectBrokerAddr();
                                if (addr != null) {
                                    TopicStatsTable tst = mqClientInstance.getMQClientAPIImpl().getTopicStatsInfo(addr, topic, timeoutMillis);
                                    topicStatsTable.getOffsetTable().putAll(tst.getOffsetTable());
                                    topicStatsTable.setTopicPutTps(topicStatsTable.getTopicPutTps() + tst.getTopicPutTps());
                                }
                            } catch (Exception e) {
                                logger.error("getTopicStatsInfo error. topic={}", topic, e);
                            } finally {
                                latch.countDown();
                            }
                        }
                    });
                }
                latch.await(timeoutMillis, TimeUnit.MILLISECONDS);

                return AdminToolResult.success(topicStatsTable);
            }
        });
    }

    /** 查询 Topic 在各 Broker 上的统计（min/max offset、TPS 等）。 */
    @Override
    public TopicStatsTable examineTopicStats(String brokerAddr,
        String topic) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        return this.mqClientInstance.getMQClientAPIImpl().getTopicStatsInfo(brokerAddr, topic, timeoutMillis);
    }

    /** 从 NameServer 拉取全部 Topic 列表。 */
    @Override
    public TopicList fetchAllTopicList() throws RemotingException, MQClientException, InterruptedException {
        return this.mqClientInstance.getMQClientAPIImpl().getTopicListFromNameServer(timeoutMillis);
    }

    /** 按集群名拉取 Topic 列表。 */
    @Override
    public TopicList fetchTopicsByCLuster(
        String clusterName) throws RemotingException, MQClientException, InterruptedException {
        return this.mqClientInstance.getMQClientAPIImpl().getTopicsByCluster(clusterName, timeoutMillis);
    }

    /** 拉取 Broker 运行时 KV 指标。 */
    @Override
    public KVTable fetchBrokerRuntimeStats(
        final String brokerAddr) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException, MQBrokerException {
        return this.mqClientInstance.getMQClientAPIImpl().getBrokerRuntimeInfo(brokerAddr, timeoutMillis);
    }

    /** 查询消费组消费进度与 TPS（可指定 Topic/集群/Broker）。 */
    @Override
    public ConsumeStats examineConsumeStats(
        String consumerGroup,
        String topic) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        return examineConsumeStats(null, consumerGroup, topic);
    }

    /** 查询消费组消费进度与 TPS（可指定 Topic/集群/Broker）。 */
    @Override
    public ConsumeStats examineConsumeStats(
        String consumerGroup) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        return examineConsumeStats(null, consumerGroup, null);
    }

    /** 查询消费组消费进度与 TPS（可指定 Topic/集群/Broker）。 */
    @Override
    public ConsumeStats examineConsumeStats(String clusterName, String consumerGroup,
        String topic) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        TopicRouteData topicRouteData = null;
        List<String> routeTopics = new ArrayList<>();
        routeTopics.add(MixAll.getRetryTopic(consumerGroup));
        if (topic != null) {
            routeTopics.add(topic);
            routeTopics.add(KeyBuilder.buildPopRetryTopic(topic, consumerGroup));
        }

        // LMQ/定时 Topic 用 clusterName 作为路由键查询
        if (!StringUtils.isEmpty(topic) && (MixAll.isLmq(topic) || topic.equals(TopicValidator.SYSTEM_TOPIC_PREFIX + "wheel_timer")) && !StringUtils.isEmpty(clusterName)) {
            routeTopics.add(clusterName);
        }

        for (int i = 0; i < routeTopics.size(); i++) {
            try {
                topicRouteData = this.examineTopicRouteInfo(routeTopics.get(i));
                if (topicRouteData != null) {
                    break;
                }
            } catch (Throwable e) {
                if (i == routeTopics.size() - 1) {
                    throw e;
                }
            }
        }
        ConsumeStats result = new ConsumeStats();

        for (BrokerData bd : topicRouteData.getBrokerDatas()) {
            String addr = bd.selectBrokerAddr();
            if (addr != null) {
                ConsumeStats consumeStats = this.mqClientInstance.getMQClientAPIImpl().getConsumeStats(addr, consumerGroup, topic, timeoutMillis * 3);
                result.getOffsetTable().putAll(consumeStats.getOffsetTable());
                double value = result.getConsumeTps() + consumeStats.getConsumeTps();
                result.setConsumeTps(value);
            }
        }

        Set<String> topics = new HashSet<>();
        for (MessageQueue messageQueue : result.getOffsetTable().keySet()) {
            topics.add(messageQueue.getTopic());
        }

        ConsumeStats staticResult = null;

        if (StringUtils.isEmpty(clusterName)) {

            staticResult = new ConsumeStats();
            staticResult.setConsumeTps(result.getConsumeTps());
            // Topic 维度保留物理统计；消费组维度另行转换
            // 静态 Topic 需单独转换逻辑 offset 表

            for (String currentTopic : topics) {
                TopicRouteData currentRoute = this.examineTopicRouteInfo(currentTopic);
                if (currentRoute.getTopicQueueMappingByBroker() == null
                    || currentRoute.getTopicQueueMappingByBroker().isEmpty()) {
                    //normal topic
                    for (Map.Entry<MessageQueue, OffsetWrapper> entry : result.getOffsetTable().entrySet()) {
                        if (entry.getKey().getTopic().equals(currentTopic)) {
                            staticResult.getOffsetTable().put(entry.getKey(), entry.getValue());
                        }
                    }
                }
                Map<String, TopicConfigAndQueueMapping> brokerConfigMap = MQAdminUtils.examineTopicConfigFromRoute(currentTopic, currentRoute, defaultMQAdminExt);
                ConsumeStats consumeStats = MQAdminUtils.convertPhysicalConsumeStats(brokerConfigMap, result);
                staticResult.getOffsetTable().putAll(consumeStats.getOffsetTable());
            }

        } else {
            staticResult = result;
        }

        if (staticResult.getOffsetTable().isEmpty()) {
            ConsumerConnection connection;
            try {
                connection = this.examineConsumerConnectionInfo(consumerGroup);
            } catch (Exception e) {
                throw new MQClientException(ResponseCode.CONSUMER_NOT_ONLINE,
                    "Not found the consumer group consume stats, because return offset table is empty, maybe the consumer not online");
            }

            if (connection.getMessageModel().equals(MessageModel.BROADCASTING)) {
                throw new MQClientException(ResponseCode.BROADCAST_CONSUMPTION,
                    "Not found the consumer group consume stats, because return offset table is empty, the consumer is under the broadcast mode");
            }
        }

        return staticResult;
    }

    /** 查询消费组消费进度与 TPS（可指定 Topic/集群/Broker）。 */
    @Override
    public ConsumeStats examineConsumeStats(String brokerAddr, String consumerGroup, String topicName,
        long timeoutMillis) throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException, MQBrokerException {
        return this.mqClientInstance.getMQClientAPIImpl().getConsumeStats(brokerAddr, consumerGroup, topicName, timeoutMillis);
    }

    /** 并发汇总消费组消费统计。 */
    @Override
    public AdminToolResult<ConsumeStats> examineConsumeStatsConcurrent(final String consumerGroup, final String topic) {

        return adminToolExecute(new AdminToolHandler() {
            @Override
            public AdminToolResult doExecute() throws Exception {
                TopicRouteData topicRouteData = null;
                List<String> routeTopics = new ArrayList<>();
                routeTopics.add(MixAll.getRetryTopic(consumerGroup));
                if (topic != null) {
                    routeTopics.add(topic);
                    routeTopics.add(KeyBuilder.buildPopRetryTopic(topic, consumerGroup));
                }
                for (int i = 0; i < routeTopics.size(); i++) {
                    try {
                        topicRouteData = examineTopicRouteInfo(routeTopics.get(i));
                        if (topicRouteData != null) {
                            break;
                        }
                    } catch (Throwable e) {
                        continue;
                    }
                }
                if (topicRouteData == null || CollectionUtils.isEmpty(topicRouteData.getBrokerDatas())) {
                    return AdminToolResult.failure(AdminToolsResultCodeEnum.TOPIC_ROUTE_INFO_NOT_EXIST, "topic router info not found");
                }

                final ConsumeStats result = new ConsumeStats();
                final CountDownLatch latch = new CountDownLatch(topicRouteData.getBrokerDatas().size());
                final Map<String, Double> consumerTpsMap = new ConcurrentHashMap<>(topicRouteData.getBrokerDatas().size());
                for (final BrokerData bd : topicRouteData.getBrokerDatas()) {
                    threadPoolExecutor.submit(new Runnable() {
                        /** 后台线程：过期清理、压缩上传与优雅关闭。 */
                        @Override
                        public void run() {
                            try {
                                String addr = bd.selectBrokerAddr();
                                if (addr != null) {
                                    ConsumeStats consumeStats = mqClientInstance.getMQClientAPIImpl().getConsumeStats(addr, consumerGroup, topic, timeoutMillis);
                                    result.getOffsetTable().putAll(consumeStats.getOffsetTable());
                                    consumerTpsMap.put(addr, consumeStats.getConsumeTps());
                                }
                            } catch (Exception e) {
                                logger.error("getConsumeStats error. topic={}, consumerGroup={}", topic, consumerGroup, e);
                            } finally {
                                latch.countDown();
                            }
                        }
                    });
                }
                latch.await(timeoutMillis, TimeUnit.MILLISECONDS);

                for (Double tps : consumerTpsMap.values()) {
                    result.setConsumeTps(result.getConsumeTps() + tps);
                }

                if (result.getOffsetTable().isEmpty()) {
                    ConsumerConnection connection;
                    try {
                        connection = examineConsumerConnectionInfo(consumerGroup);
                    } catch (Exception e) {
                        return AdminToolResult.failure(AdminToolsResultCodeEnum.CONSUMER_NOT_ONLINE, "Not found the "
                            + "consumer group consume stats, because return offset table is empty, maybe the consumer not consume any message");
                    }

                    if (connection.getMessageModel().equals(MessageModel.BROADCASTING)) {
                        return AdminToolResult.failure(AdminToolsResultCodeEnum.BROADCAST_CONSUMPTION, "Not found the "
                            + "consumer group consume stats, because return offset table is empty, the consumer is under the broadcast mode");
                    }
                }
                return AdminToolResult.success(result);
            }
        });
    }

    /** 从 NameServer 获取集群 Broker 拓扑信息。 */
    @Override
    public ClusterInfo examineBrokerClusterInfo() throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException {
        return this.mqClientInstance.getMQClientAPIImpl().getBrokerClusterInfo(timeoutMillis);
    }

    /** 查询 Topic 路由（Broker 与队列分布）。 */
    @Override
    public TopicRouteData examineTopicRouteInfo(
        String topic) throws RemotingException, MQClientException, InterruptedException {
        return this.mqClientInstance.getMQClientAPIImpl().getTopicRouteInfoFromNameServer(topic, timeoutMillis);
    }

    /** 按 Topic 与 msgId 查看单条消息。 */
    @Override
    public MessageExt viewMessage(String topic,
        String msgId) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        try {
            MessageDecoder.decodeMessageId(msgId);
            return this.mqClientInstance.getMQAdminImpl().viewMessage(topic, msgId);
        } catch (Exception e) {
            logger.warn("the msgId maybe created by new client. msgId={}", msgId, e);
        }
        return this.mqClientInstance.getMQAdminImpl().queryMessageByUniqKey(topic, msgId);
    }

    /** 按 Topic/Key 或集群条件索引查询消息。 */
    @Override
    public MessageExt queryMessage(String clusterName, String topic,
        String msgId) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        try {
            MessageDecoder.decodeMessageId(msgId);
            return this.mqClientInstance.getMQAdminImpl().viewMessage(topic, msgId);
        } catch (Exception e) {
            logger.warn("the msgId maybe created by new client. msgId={}", msgId, e);
        }
        return this.mqClientInstance.getMQAdminImpl().queryMessageByUniqKey(clusterName, topic, msgId);
    }

    /** 查询消费组在线连接与订阅关系。 */
    @Override
    public ConsumerConnection examineConsumerConnectionInfo(
        String consumerGroup) throws InterruptedException, MQBrokerException,
        RemotingException, MQClientException {
        ConsumerConnection result = new ConsumerConnection();
        String topic = MixAll.getRetryTopic(consumerGroup);
        List<BrokerData> brokers = this.examineTopicRouteInfo(topic).getBrokerDatas();
        BrokerData brokerData = brokers.get(random.nextInt(brokers.size()));
        String addr = null;
        if (brokerData != null) {
            addr = brokerData.selectBrokerAddr();
            if (StringUtils.isNotBlank(addr)) {
                result = this.mqClientInstance.getMQClientAPIImpl().getConsumerConnectionList(addr, consumerGroup, timeoutMillis);
            }
        }

        if (result.getConnectionSet().isEmpty()) {
            logger.warn("the consumer group not online. brokerAddr={}, group={}", addr, consumerGroup);
            throw new MQClientException(ResponseCode.CONSUMER_NOT_ONLINE, "Not found the consumer group connection");
        }

        return result;
    }

    /** 查询消费组在线连接与订阅关系。 */
    @Override
    public ConsumerConnection examineConsumerConnectionInfo(
        String consumerGroup, String brokerAddr) throws InterruptedException, MQBrokerException,
        RemotingException, MQClientException {
        ConsumerConnection result =
            this.mqClientInstance.getMQClientAPIImpl().getConsumerConnectionList(brokerAddr, consumerGroup, timeoutMillis);

        if (result.getConnectionSet().isEmpty()) {
            logger.warn("the consumer group not online. brokerAddr={}, group={}", brokerAddr, consumerGroup);
            throw new MQClientException(ResponseCode.CONSUMER_NOT_ONLINE, "Not found the consumer group connection");
        }

        return result;
    }

    /** 查询指定 Topic 下生产者连接信息。 */
    @Override
    public ProducerConnection examineProducerConnectionInfo(String producerGroup,
        final String topic) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        ProducerConnection result = new ProducerConnection();
        List<BrokerData> brokers = this.examineTopicRouteInfo(topic).getBrokerDatas();
        BrokerData brokerData = brokers.get(random.nextInt(brokers.size()));
        String addr = null;
        if (brokerData != null) {
            addr = brokerData.selectBrokerAddr();
            if (StringUtils.isNotBlank(addr)) {
                result = this.mqClientInstance.getMQClientAPIImpl().getProducerConnectionList(addr, producerGroup, timeoutMillis);
            }
        }

        if (result.getConnectionSet().isEmpty()) {
            logger.warn("the producer group not online. brokerAddr={}, group={}", addr, producerGroup);
            throw new MQClientException("Not found the producer group connection", null);
        }

        return result;
    }

    /** 拉取 Broker 上全部生产者连接表。 */
    @Override
    public ProducerTableInfo getAllProducerInfo(
        final String brokerAddr) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        return this.mqClientInstance.getMQClientAPIImpl().getAllProducerInfo(brokerAddr, timeoutMillis);
    }

    /** 返回当前配置的 NameServer 地址列表。 */
    @Override
    public List<String> getNameServerAddressList() {
        return this.mqClientInstance.getMQClientAPIImpl().getNameServerAddressList();
    }

    /** 在 NameServer 上撤销 Broker 写权限。 */
    @Override
    public int wipeWritePermOfBroker(final String namesrvAddr,
        String brokerName) throws RemotingCommandException, RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException, MQClientException {
        return this.mqClientInstance.getMQClientAPIImpl().wipeWritePermOfBroker(namesrvAddr, brokerName, timeoutMillis);
    }

    /** 在 NameServer 上恢复 Broker 写权限。 */
    @Override
    public int addWritePermOfBroker(String namesrvAddr, String brokerName) throws RemotingCommandException,
        RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException, MQClientException {
        return this.mqClientInstance.getMQClientAPIImpl().addWritePermOfBroker(namesrvAddr, brokerName, timeoutMillis);
    }

    /** 本地写入 KV 配置（不经过 Broker）。 */
    @Override
    public void putKVConfig(String namespace, String key, String value) {
    }

    @Override
    public String getKVConfig(String namespace,
        String key) throws RemotingException, MQClientException, InterruptedException {
        return this.mqClientInstance.getMQClientAPIImpl().getKVConfigValue(namespace, key, timeoutMillis);
    }

    /** 按命名空间拉取 KV 配置表。 */
    @Override
    public KVTable getKVListByNamespace(
        String namespace) throws RemotingException, MQClientException, InterruptedException {
        return this.mqClientInstance.getMQClientAPIImpl().getKVListByNamespace(namespace, timeoutMillis);
    }

    /** 从集群 Broker 与 NameServer 删除 Topic。 */
    @Override
    public void deleteTopic(String topicName,
        String clusterName) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        Set<String> brokerAddressSet = CommandUtil.fetchMasterAndSlaveAddrByClusterName(this.defaultMQAdminExt, clusterName);
        this.deleteTopicInBroker(brokerAddressSet, topicName);
        List<String> nameServerList = this.getNameServerAddressList();
        Set<String> nameServerSet = new HashSet<>(nameServerList);
        this.deleteTopicInNameServer(nameServerSet, topicName);
        for (String namespace : this.kvNamespaceToDeleteList) {
            this.deleteKvConfig(namespace, topicName);
        }
    }

    /** 从指定 Broker 集合删除 Topic。 */
    @Override
    public void deleteTopicInBroker(Set<String> addrs,
        String topic) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        for (String addr : addrs) {
            this.mqClientInstance.getMQClientAPIImpl().deleteTopicInBroker(addr, topic, timeoutMillis);
        }
    }

    /** 并发从多个 Broker 删除 Topic。 */
    @Override
    public AdminToolResult<BrokerOperatorResult> deleteTopicInBrokerConcurrent(final Set<String> addrs,
        final String topic) {
        final List<String> successList = new CopyOnWriteArrayList<>();
        final List<String> failureList = new CopyOnWriteArrayList<>();
        final CountDownLatch latch = new CountDownLatch(addrs.size());
        for (final String addr : addrs) {
            threadPoolExecutor.submit(new Runnable() {
                /** 后台线程：过期清理、压缩上传与优雅关闭。 */
                @Override
                public void run() {
                    try {
                        mqClientInstance.getMQClientAPIImpl().deleteTopicInBroker(addr, topic, timeoutMillis);
                        successList.add(addr);
                    } catch (Exception e) {
                        logger.error("deleteTopicInBroker error. topic={}, broker={}", topic, addr, e);
                        failureList.add(addr);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        try {
            latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
        }

        BrokerOperatorResult result = new BrokerOperatorResult();
        result.setSuccessList(successList);
        result.setFailureList(failureList);
        return AdminToolResult.success(result);
    }

    /** 从 NameServer 删除 Topic 路由元数据。 */
    @Override
    public void deleteTopicInNameServer(Set<String> addrs,
        String topic) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        if (addrs == null) {
            String ns = this.mqClientInstance.getMQClientAPIImpl().fetchNameServerAddr();
            addrs = new HashSet(Arrays.asList(ns.split(";")));
        }
        for (String addr : addrs) {
            this.mqClientInstance.getMQClientAPIImpl().deleteTopicInNameServer(addr, topic, timeoutMillis);
        }
    }

    /** 删除 Broker 上指定消费组配置。 */
    @Override
    public void deleteSubscriptionGroup(String addr,
        String groupName) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        this.mqClientInstance.getMQClientAPIImpl().deleteSubscriptionGroup(addr, groupName, false, timeoutMillis);
    }

    /** 删除 Broker 上指定消费组配置。 */
    @Override
    public void deleteSubscriptionGroup(String addr, String groupName,
        boolean removeOffset) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        this.mqClientInstance.getMQClientAPIImpl().deleteSubscriptionGroup(addr, groupName, removeOffset, timeoutMillis);
    }

    /** 在 Broker/NameServer 创建或更新 KV 配置。 */
    @Override
    public void createAndUpdateKvConfig(String namespace, String key,
        String value) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        this.mqClientInstance.getMQClientAPIImpl().putKVConfigValue(namespace, key, value, timeoutMillis);
    }

    /** 删除 KV 配置项。 */
    @Override
    public void deleteKvConfig(String namespace,
        String key) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        this.mqClientInstance.getMQClientAPIImpl().deleteKVConfigValue(namespace, key, timeoutMillis);
    }

    /** 按时间戳重置消费位点（旧版 API，返回 RollbackStats）。 */
    public List<RollbackStats> resetOffsetByTimestampOld(String clusterName, String consumerGroup, String topic,
        long timestamp,
        boolean force) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        String routeTopic = topic;
        // Use clusterName topic to get topic route for lmq or rmq_sys_wheel_timer
        if (!StringUtils.isEmpty(topic) && (MixAll.isLmq(topic) || topic.equals(TopicValidator.SYSTEM_TOPIC_PREFIX + "wheel_timer"))
            && !StringUtils.isEmpty(clusterName)) {
            routeTopic = clusterName;
        }
        TopicRouteData topicRouteData = this.examineTopicRouteInfo(routeTopic);
        List<RollbackStats> rollbackStatsList = new ArrayList<>();
        Map<String, QueueData> topicRouteMap = new HashMap<>();
        for (QueueData queueData : topicRouteData.getQueueDatas()) {
            topicRouteMap.put(queueData.getBrokerName(), queueData);
        }
        for (BrokerData bd : topicRouteData.getBrokerDatas()) {
            String addr = bd.selectBrokerAddr();
            if (addr != null) {
                rollbackStatsList.addAll(resetOffsetByTimestampOld(addr, topicRouteMap.get(bd.getBrokerName()), consumerGroup, topic, timestamp, force));
            }
        }
        return rollbackStatsList;
    }

    /** 按时间戳重置消费位点（旧版 API，返回 RollbackStats）。 */
    @Override
    public List<RollbackStats> resetOffsetByTimestampOld(String consumerGroup, String topic, long timestamp,
        boolean force) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return resetOffsetByTimestampOld(null, consumerGroup, topic, timestamp, force);
    }

    /** 按时间戳重置消费位点（旧版 API，返回 RollbackStats）。 */
    private List<RollbackStats> resetOffsetByTimestampOld(String brokerAddr, QueueData queueData, String consumerGroup,
        String topic, long timestamp,
        boolean force) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        List<RollbackStats> rollbackStatsList = new ArrayList<>();
        ConsumeStats consumeStats = this.mqClientInstance.getMQClientAPIImpl().getConsumeStats(brokerAddr, consumerGroup, timeoutMillis);

        boolean hasConsumed = false;
        for (Map.Entry<MessageQueue, OffsetWrapper> entry : consumeStats.getOffsetTable().entrySet()) {
            MessageQueue queue = entry.getKey();
            OffsetWrapper offsetWrapper = entry.getValue();
            if (topic.equals(queue.getTopic())) {
                hasConsumed = true;
                RollbackStats rollbackStats = resetOffsetConsumeOffset(brokerAddr, consumerGroup, queue, offsetWrapper, timestamp, force);
                rollbackStatsList.add(rollbackStats);
            }
        }

        if (!hasConsumed) {
            Map<MessageQueue, TopicOffset> topicStatus = this.mqClientInstance.getMQClientAPIImpl().getTopicStatsInfo(brokerAddr, topic, timeoutMillis).getOffsetTable();
            for (int i = 0; i < queueData.getReadQueueNums(); i++) {
                MessageQueue queue = new MessageQueue(topic, queueData.getBrokerName(), i);
                OffsetWrapper offsetWrapper = new OffsetWrapper();
                offsetWrapper.setBrokerOffset(topicStatus.get(queue).getMaxOffset());
                offsetWrapper.setConsumerOffset(topicStatus.get(queue).getMinOffset());

                RollbackStats rollbackStats = resetOffsetConsumeOffset(brokerAddr, consumerGroup, queue, offsetWrapper, timestamp, force);
                rollbackStatsList.add(rollbackStats);
            }
        }
        return rollbackStatsList;
    }

    /** 按时间戳重置消费位点到各队列。 */
    @Override
    public Map<MessageQueue, Long> resetOffsetByTimestamp(String topic, String group, long timestamp,
        boolean isForce) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return resetOffsetByTimestamp(null, topic, group, timestamp, isForce, false);
    }

    /** 新版按时间戳重置消费位点。 */
    @Override
    public void resetOffsetNew(String consumerGroup, String topic,
        long timestamp) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        try {
            this.resetOffsetByTimestamp(topic, consumerGroup, timestamp, true);
        } catch (MQClientException e) {
            if (ResponseCode.CONSUMER_NOT_ONLINE == e.getResponseCode()) {
                this.resetOffsetByTimestampOld(consumerGroup, topic, timestamp, true);
                return;
            }
            throw e;
        }
    }

    /** 并发向各 Broker 执行新版位点重置。 */
    @Override
    public AdminToolResult<BrokerOperatorResult> resetOffsetNewConcurrent(final String group, final String topic,
        final long timestamp) {
        return adminToolExecute(new AdminToolHandler() {
            @Override
            public AdminToolResult doExecute() throws Exception {
                TopicRouteData topicRouteData = examineTopicRouteInfo(topic);
                if (topicRouteData == null || CollectionUtils.isEmpty(topicRouteData.getBrokerDatas())) {
                    return AdminToolResult.failure(AdminToolsResultCodeEnum.TOPIC_ROUTE_INFO_NOT_EXIST, "topic router info not found");
                }
                final Map<String, QueueData> topicRouteMap = new HashMap<>();
                for (QueueData queueData : topicRouteData.getQueueDatas()) {
                    topicRouteMap.put(queueData.getBrokerName(), queueData);
                }

                final CopyOnWriteArrayList successList = new CopyOnWriteArrayList();
                final CopyOnWriteArrayList failureList = new CopyOnWriteArrayList();
                final CountDownLatch latch = new CountDownLatch(topicRouteData.getBrokerDatas().size());
                for (final BrokerData bd : topicRouteData.getBrokerDatas()) {
                    threadPoolExecutor.submit(new Runnable() {
                        /** 后台线程：过期清理、压缩上传与优雅关闭。 */
                        @Override
                        public void run() {
                            String addr = bd.selectBrokerAddr();
                            try {
                                if (addr != null) {
                                    Map<MessageQueue, Long> offsetTable = mqClientInstance.getMQClientAPIImpl().invokeBrokerToResetOffset(addr, topic, group, timestamp, true, timeoutMillis, false);
                                    if (offsetTable != null) {
                                        successList.add(addr);
                                    } else {
                                        failureList.add(addr);
                                    }
                                }
                            } catch (MQClientException e) {
                                if (ResponseCode.CONSUMER_NOT_ONLINE == e.getResponseCode()) {
                                    try {
                                        resetOffsetByTimestampOld(addr, topicRouteMap.get(bd.getBrokerName()), group, topic, timestamp, true);
                                        successList.add(addr);
                                    } catch (Exception e2) {
                                        logger.error("resetOffsetByTimestampOld error. addr={}, topic={}, group={}, timestamp={}", addr, topic, group, timestamp, e);
                                        failureList.add(addr);
                                    }
                                } else if (ResponseCode.SYSTEM_ERROR == e.getResponseCode()) {
                                    // CODE: 1  DESC: THe consumer group <GID_newggghh> not exist, never online
                                    successList.add(addr);
                                } else {
                                    failureList.add(addr);
                                    logger.error("resetOffsetNewConcurrent error. addr={}, topic={}, group={}, timestamp={}", addr, topic, group, timestamp, e);
                                }
                            } catch (Exception e) {
                                failureList.add(addr);
                                logger.error("resetOffsetNewConcurrent error. addr={}, topic={}, group={}, timestamp={}", addr, topic, group, timestamp, e);
                            } finally {
                                latch.countDown();
                            }
                        }
                    });
                }
                latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
                BrokerOperatorResult result = new BrokerOperatorResult();
                result.setSuccessList(successList);
                result.setFailureList(failureList);
                if (successList.size() == topicRouteData.getBrokerDatas().size()) {
                    return AdminToolResult.success(result);
                } else {
                    return AdminToolResult.failure(AdminToolsResultCodeEnum.MQ_BROKER_ERROR, "operator failure", result);
                }
            }
        });
    }

    /** 按时间戳重置消费位点到各队列。 */
    public Map<MessageQueue, Long> resetOffsetByTimestamp(String clusterName, String topic, String group,
        long timestamp, boolean isForce,
        boolean isC) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        String routeTopic = topic;
        // Use clusterName topic to get topic route for lmq or rmq_sys_wheel_timer
        if (!StringUtils.isEmpty(topic) && (MixAll.isLmq(topic) || topic.equals(TopicValidator.SYSTEM_TOPIC_PREFIX + "wheel_timer"))
            && !StringUtils.isEmpty(clusterName)) {
            routeTopic = clusterName;
        }
        TopicRouteData topicRouteData = this.examineTopicRouteInfo(routeTopic);
        List<BrokerData> brokerDatas = topicRouteData.getBrokerDatas();
        Map<MessageQueue, Long> allOffsetTable = new HashMap<>();
        if (brokerDatas != null) {
            for (BrokerData brokerData : brokerDatas) {
                String addr = brokerData.selectBrokerAddr();
                if (addr != null) {
                    Map<MessageQueue, Long> offsetTable = this.mqClientInstance.getMQClientAPIImpl().invokeBrokerToResetOffset(addr, topic, group, timestamp, isForce, timeoutMillis, isC);
                    if (offsetTable != null) {
                        allOffsetTable.putAll(offsetTable);
                    }
                }
            }
        }
        return allOffsetTable;
    }

    /** 向 Broker 发送单队列位点重置请求。 */
    private RollbackStats resetOffsetConsumeOffset(String brokerAddr, String consumeGroup, MessageQueue queue,
        OffsetWrapper offsetWrapper, long timestamp,
        boolean force) throws RemotingException, InterruptedException, MQBrokerException {
        long resetOffset;
        if (timestamp == -1) {
            resetOffset = this.mqClientInstance.getMQClientAPIImpl().getMaxOffset(brokerAddr, queue, timeoutMillis);
        } else {
            resetOffset = this.mqClientInstance.getMQClientAPIImpl().searchOffset(brokerAddr, queue, timestamp, timeoutMillis);
        }

        RollbackStats rollbackStats = new RollbackStats();
        rollbackStats.setBrokerName(queue.getBrokerName());
        rollbackStats.setQueueId(queue.getQueueId());
        rollbackStats.setBrokerOffset(offsetWrapper.getBrokerOffset());
        rollbackStats.setConsumerOffset(offsetWrapper.getConsumerOffset());
        rollbackStats.setTimestampOffset(resetOffset);
        rollbackStats.setRollbackOffset(offsetWrapper.getConsumerOffset());

        if (force || resetOffset <= offsetWrapper.getConsumerOffset()) {
            rollbackStats.setRollbackOffset(resetOffset);
            UpdateConsumerOffsetRequestHeader requestHeader = new UpdateConsumerOffsetRequestHeader();
            requestHeader.setConsumerGroup(consumeGroup);
            requestHeader.setTopic(queue.getTopic());
            requestHeader.setQueueId(queue.getQueueId());
            requestHeader.setCommitOffset(resetOffset);
            requestHeader.setBrokerName(queue.getBrokerName());
            this.mqClientInstance.getMQClientAPIImpl().updateConsumerOffset(brokerAddr, requestHeader, timeoutMillis);
        }
        return rollbackStats;
    }

    /** 查询消费组各客户端队列消费状态。 */
    @Override
    public Map<String, Map<MessageQueue, Long>> getConsumeStatus(String topic, String group,
        String clientAddr) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        TopicRouteData topicRouteData = this.examineTopicRouteInfo(topic);
        List<BrokerData> brokerDatas = topicRouteData.getBrokerDatas();
        if (brokerDatas != null && brokerDatas.size() > 0) {
            String addr = brokerDatas.get(0).selectBrokerAddr();
            if (addr != null) {
                return this.mqClientInstance.getMQClientAPIImpl().invokeBrokerToGetConsumerStatus(addr, topic, group, clientAddr, timeoutMillis);
            }
        }
        return Collections.EMPTY_MAP;
    }

    /** 创建或更新顺序消息全局配置。 */
    @Override
    public void createOrUpdateOrderConf(String key, String value,
        boolean isCluster) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {

        if (isCluster) {
            this.mqClientInstance.getMQClientAPIImpl().putKVConfigValue(NamesrvUtil.NAMESPACE_ORDER_TOPIC_CONFIG, key, value, timeoutMillis);
        } else {
            String oldOrderConfs = null;
            try {
                oldOrderConfs = this.mqClientInstance.getMQClientAPIImpl().getKVConfigValue(NamesrvUtil.NAMESPACE_ORDER_TOPIC_CONFIG, key, timeoutMillis);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Map<String, String> orderConfMap = new HashMap<>();
            if (!UtilAll.isBlank(oldOrderConfs)) {
                String[] oldOrderConfArr = oldOrderConfs.split(";");
                for (String oldOrderConf : oldOrderConfArr) {
                    String[] items = oldOrderConf.split(":");
                    orderConfMap.put(items[0], oldOrderConf);
                }
            }
            String[] items = value.split(":");
            orderConfMap.put(items[0], value);

            StringBuilder newOrderConf = new StringBuilder();
            String splitor = "";
            for (Map.Entry<String, String> entry : orderConfMap.entrySet()) {
                newOrderConf.append(splitor).append(entry.getValue());
                splitor = ";";
            }
            this.mqClientInstance.getMQClientAPIImpl().putKVConfigValue(NamesrvUtil.NAMESPACE_ORDER_TOPIC_CONFIG, key, newOrderConf.toString(), timeoutMillis);
        }
    }

    /** 查询订阅指定 Topic 的消费组列表。 */
    @Override
    public GroupList queryTopicConsumeByWho(
        String topic) throws InterruptedException, MQBrokerException, RemotingException, MQClientException {
        TopicRouteData topicRouteData = this.examineTopicRouteInfo(topic);

        for (BrokerData bd : topicRouteData.getBrokerDatas()) {
            String addr = bd.selectBrokerAddr();
            if (addr != null) {
                return this.mqClientInstance.getMQClientAPIImpl().queryTopicConsumeByWho(addr, topic, timeoutMillis);
            }
        }
        return null;
    }

    /** 查询消费组对某 Topic 的订阅表达式。 */
    @Override
    public SubscriptionData querySubscription(String group,
        String topic) throws InterruptedException, MQBrokerException, RemotingException, MQClientException {
        TopicRouteData topicRouteData = this.examineTopicRouteInfo(topic);

        for (BrokerData bd : topicRouteData.getBrokerDatas()) {
            String addr = bd.selectBrokerAddr();
            if (addr != null) {
                return this.mqClientInstance.getMQClientAPIImpl().querySubscriptionByConsumer(addr, group, topic, timeoutMillis);
            }
        }
        return null;
    }

    /** 查询某消费组订阅的全部 Topic。 */
    @Override
    public TopicList queryTopicsByConsumer(
        String group) throws InterruptedException, MQBrokerException, RemotingException, MQClientException {
        String retryTopic = MixAll.getRetryTopic(group);
        TopicRouteData topicRouteData = this.examineTopicRouteInfo(retryTopic);
        TopicList result = new TopicList();

        //Query all brokers
        for (BrokerData bd : topicRouteData.getBrokerDatas()) {
            String addr = bd.selectBrokerAddr();
            if (addr != null) {
                TopicList topicList = this.mqClientInstance.getMQClientAPIImpl().queryTopicsByConsumer(addr, group, timeoutMillis);
                result.getTopicList().addAll(topicList.getTopicList());
            }
        }

        return result;
    }

    /** 并发查询消费组订阅 Topic 列表。 */
    @Override
    public AdminToolResult<TopicList> queryTopicsByConsumerConcurrent(final String group) {
        return adminToolExecute(new AdminToolHandler() {
            @Override
            public AdminToolResult doExecute() throws Exception {
                String retryTopic = MixAll.getRetryTopic(group);
                TopicRouteData topicRouteData = examineTopicRouteInfo(retryTopic);

                if (topicRouteData == null || CollectionUtils.isEmpty(topicRouteData.getBrokerDatas())) {
                    return AdminToolResult.failure(AdminToolsResultCodeEnum.TOPIC_ROUTE_INFO_NOT_EXIST, "router info not found.");
                }
                final TopicList result = new TopicList();
                final CountDownLatch latch = new CountDownLatch(topicRouteData.getBrokerDatas().size());
                for (final BrokerData bd : topicRouteData.getBrokerDatas()) {
                    threadPoolExecutor.submit(new Runnable() {
                        /** 后台线程：过期清理、压缩上传与优雅关闭。 */
                        @Override
                        public void run() {
                            try {
                                String addr = bd.selectBrokerAddr();
                                if (addr != null) {
                                    TopicList topicList = mqClientInstance.getMQClientAPIImpl().queryTopicsByConsumer(addr, group, timeoutMillis);
                                    result.getTopicList().addAll(topicList.getTopicList());
                                }
                            } catch (Exception e) {
                                logger.error("queryTopicsByConsumer error. group={}", group, e);
                            } finally {
                                latch.countDown();
                            }
                        }
                    });
                }
                latch.await(timeoutMillis, TimeUnit.MILLISECONDS);

                return AdminToolResult.success(result);
            }
        });
    }

    /** 查询消费组在各队列上的消费时间跨度。 */
    @Override
    public List<QueueTimeSpan> queryConsumeTimeSpan(final String topic,
        final String group) throws InterruptedException, MQBrokerException, RemotingException, MQClientException {
        List<QueueTimeSpan> spanSet = new ArrayList<>();
        TopicRouteData topicRouteData = this.examineTopicRouteInfo(topic);
        for (BrokerData bd : topicRouteData.getBrokerDatas()) {
            String addr = bd.selectBrokerAddr();
            if (addr != null) {
                spanSet.addAll(this.mqClientInstance.getMQClientAPIImpl().queryConsumeTimeSpan(addr, topic, group, timeoutMillis));
            }
        }
        return spanSet;
    }

    /** 并发查询消费时间跨度。 */
    @Override
    public AdminToolResult<List<QueueTimeSpan>> queryConsumeTimeSpanConcurrent(final String topic, final String group) {
        return adminToolExecute(new AdminToolHandler() {
            @Override
            public AdminToolResult doExecute() throws Exception {
                final List<QueueTimeSpan> spanSet = new CopyOnWriteArrayList<>();
                TopicRouteData topicRouteData = examineTopicRouteInfo(topic);

                if (topicRouteData == null || topicRouteData.getBrokerDatas() == null || topicRouteData.getBrokerDatas().size() == 0) {
                    return AdminToolResult.success(spanSet);
                }
                final CountDownLatch latch = new CountDownLatch(topicRouteData.getBrokerDatas().size());
                for (final BrokerData bd : topicRouteData.getBrokerDatas()) {
                    threadPoolExecutor.submit(new Runnable() {
                        /** 后台线程：过期清理、压缩上传与优雅关闭。 */
                        @Override
                        public void run() {
                            try {
                                String addr = bd.selectBrokerAddr();
                                if (addr != null) {
                                    spanSet.addAll(mqClientInstance.getMQClientAPIImpl().queryConsumeTimeSpan(addr, topic, group, timeoutMillis));
                                }
                            } catch (Exception e) {
                                logger.error("queryConsumeTimeSpan error. topic={}, group={}", topic, group, e);
                            } finally {
                                latch.countDown();
                            }
                        }
                    });
                }
                latch.await(timeoutMillis, TimeUnit.MILLISECONDS);

                return AdminToolResult.success(spanSet);
            }
        });
    }

    /** 清理集群内过期消费进度。 */
    @Override
    public boolean cleanExpiredConsumerQueue(
        String cluster) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQClientException, InterruptedException {
        boolean result = false;
        try {
            ClusterInfo clusterInfo = examineBrokerClusterInfo();
            if (null == cluster || "".equals(cluster)) {
                for (String targetCluster : clusterInfo.retrieveAllClusterNames()) {
                    result = cleanExpiredConsumerQueueByCluster(clusterInfo, targetCluster);
                }
            } else {
                result = cleanExpiredConsumerQueueByCluster(clusterInfo, cluster);
            }
        } catch (MQBrokerException e) {
            logger.error("cleanExpiredConsumerQueue error.", e);
        }

        return result;
    }

    /** 遍历集群全部 Broker 清理过期消费进度。 */
    public boolean cleanExpiredConsumerQueueByCluster(ClusterInfo clusterInfo,
        String cluster) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQClientException, InterruptedException {
        boolean result = false;
        String[] addrs = clusterInfo.retrieveAllAddrByCluster(cluster);
        for (String addr : addrs) {
            result = cleanExpiredConsumerQueueByAddr(addr);
        }
        return result;
    }

    /** 清理指定 Broker 上过期消费进度。 */
    @Override
    public boolean cleanExpiredConsumerQueueByAddr(
        String addr) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQClientException, InterruptedException {
        boolean result = mqClientInstance.getMQClientAPIImpl().cleanExpiredConsumeQueue(addr, timeoutMillis);
        logger.warn("clean expired ConsumeQueue on target broker={}, execute result={}", addr, result);
        return result;
    }

    /** 触发集群删除过期 CommitLog。 */
    @Override
    public boolean deleteExpiredCommitLog(
        String cluster) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQClientException, InterruptedException {
        boolean result = false;
        try {
            ClusterInfo clusterInfo = examineBrokerClusterInfo();
            if (StringUtils.isEmpty(cluster)) {
                for (String targetCluster : clusterInfo.retrieveAllClusterNames()) {
                    result = deleteExpiredCommitLogByCluster(clusterInfo, targetCluster);
                }
            } else {
                result = deleteExpiredCommitLogByCluster(clusterInfo, cluster);
            }
        } catch (MQBrokerException e) {
            logger.error("deleteExpiredCommitLog error.", e);
        }

        return result;
    }

    /** 遍历集群触发 CommitLog 过期删除。 */
    public boolean deleteExpiredCommitLogByCluster(ClusterInfo clusterInfo,
        String cluster) throws RemotingConnectException,
        RemotingSendRequestException, RemotingTimeoutException, MQClientException, InterruptedException {
        boolean result = false;
        String[] addrs = clusterInfo.retrieveAllAddrByCluster(cluster);
        for (String addr : addrs) {
            result = deleteExpiredCommitLogByAddr(addr);
        }
        return result;
    }

    /** 触发指定 Broker 删除过期 CommitLog。 */
    @Override
    public boolean deleteExpiredCommitLogByAddr(
        String addr) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQClientException, InterruptedException {
        boolean result = mqClientInstance.getMQClientAPIImpl().deleteExpiredCommitLog(addr, timeoutMillis);
        logger.warn("Delete expired CommitLog on target broker={}, execute result={}", addr, result);
        return result;
    }

    /** 清理集群未使用 Topic。 */
    @Override
    public boolean cleanUnusedTopic(
        String cluster) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQClientException, InterruptedException {
        boolean result = false;
        try {
            ClusterInfo clusterInfo = examineBrokerClusterInfo();
            if (StringUtils.isEmpty(cluster)) {
                for (String targetCluster : clusterInfo.retrieveAllClusterNames()) {
                    result = cleanUnusedTopicByCluster(clusterInfo, targetCluster);
                }
            } else {
                result = cleanUnusedTopicByCluster(clusterInfo, cluster);
            }
        } catch (MQBrokerException e) {
            logger.error("cleanExpiredConsumerQueue error.", e);
        }

        return result;
    }

    /** 遍历集群清理未使用 Topic。 */
    public boolean cleanUnusedTopicByCluster(ClusterInfo clusterInfo,
        String cluster) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQClientException, InterruptedException {
        boolean result = false;
        String[] addrs = clusterInfo.retrieveAllAddrByCluster(cluster);
        for (String addr : addrs) {
            result = cleanUnusedTopicByAddr(addr);
        }
        return result;
    }

    /** 清理指定 Broker 未使用 Topic。 */
    @Override
    public boolean cleanUnusedTopicByAddr(
        String addr) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQClientException, InterruptedException {
        boolean result = mqClientInstance.getMQClientAPIImpl().cleanUnusedTopicByAddr(addr, timeoutMillis);
        logger.warn("clean unused topic on target broker={}, execute result={}", addr, result);
        return result;
    }

    /** 拉取消费端运行态（订阅、ProcessQueue、可选 jstack/metrics）。 */
    @Override
    public ConsumerRunningInfo getConsumerRunningInfo(String consumerGroup, String clientId,
        boolean jstack) throws RemotingException, MQClientException, InterruptedException {
        return this.getConsumerRunningInfo(consumerGroup, clientId, jstack, false);
    }

    /** 拉取消费端运行态（订阅、ProcessQueue、可选 jstack/metrics）。 */
    @Override
    public ConsumerRunningInfo getConsumerRunningInfo(String consumerGroup, String clientId, boolean jstack,
        boolean metrics) throws RemotingException, MQClientException, InterruptedException {
        String topic = MixAll.RETRY_GROUP_TOPIC_PREFIX + consumerGroup;
        TopicRouteData topicRouteData = this.examineTopicRouteInfo(topic);
        List<BrokerData> brokerDatas = topicRouteData.getBrokerDatas();
        if (brokerDatas != null) {
            for (BrokerData brokerData : brokerDatas) {
                String addr = brokerData.selectBrokerAddr();
                if (addr != null) {
                    return this.mqClientInstance.getMQClientAPIImpl().getConsumerRunningInfo(addr, consumerGroup, clientId, jstack, timeoutMillis);
                }
            }
        }
        return null;
    }

    /** 向指定消费端直接投递并消费一条消息（运维调试）。 */
    @Override
    public ConsumeMessageDirectlyResult consumeMessageDirectly(final String consumerGroup, final String clientId,
        final String topic,
        final String msgId) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        MessageExt msg = this.viewMessage(topic, msgId);
        if (msg.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX) == null) {
            return this.mqClientInstance.getMQClientAPIImpl().consumeMessageDirectly(NetworkUtil.socketAddress2String(msg.getStoreHost()), consumerGroup, clientId, topic, msgId, timeoutMillis);
        } else {
            MessageClientExt msgClient = (MessageClientExt) msg;
            return this.mqClientInstance.getMQClientAPIImpl().consumeMessageDirectly(NetworkUtil.socketAddress2String(msg.getStoreHost()), consumerGroup, clientId, topic, msgClient.getOffsetMsgId(), timeoutMillis);
        }
    }

    /** 向指定消费端直接投递并消费一条消息（运维调试）。 */
    @Override
    public ConsumeMessageDirectlyResult consumeMessageDirectly(final String clusterName, final String consumerGroup,
        final String clientId,
        final String topic,
        final String msgId) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        MessageExt msg = this.queryMessage(clusterName, topic, msgId);
        if (msg.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX) == null) {
            return this.mqClientInstance.getMQClientAPIImpl().consumeMessageDirectly(NetworkUtil.socketAddress2String(msg.getStoreHost()), consumerGroup, clientId, topic, msgId, timeoutMillis);
        } else {
            MessageClientExt msgClient = (MessageClientExt) msg;
            return this.mqClientInstance.getMQClientAPIImpl().consumeMessageDirectly(NetworkUtil.socketAddress2String(msg.getStoreHost()), consumerGroup, clientId, topic, msgClient.getOffsetMsgId(), timeoutMillis);
        }
    }

    /** 查询消息在各消费组的投递/消费轨迹。 */
    @Override
    public List<MessageTrack> messageTrackDetail(
        MessageExt msg) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        List<MessageTrack> result = new ArrayList<>();

        GroupList groupList = this.queryTopicConsumeByWho(msg.getTopic());

        for (String group : groupList.getGroupList()) {

            MessageTrack mt = new MessageTrack();
            mt.setConsumerGroup(group);
            mt.setTrackType(TrackType.UNKNOWN);
            ConsumerConnection cc = null;
            try {
                cc = this.examineConsumerConnectionInfo(group);
            } catch (MQBrokerException e) {
                if (ResponseCode.CONSUMER_NOT_ONLINE == e.getResponseCode()) {
                    mt.setTrackType(TrackType.NOT_ONLINE);
                }
                mt.setExceptionDesc("CODE:" + e.getResponseCode() + " DESC:" + e.getErrorMessage());
                result.add(mt);
                continue;
            } catch (Exception e) {
                mt.setExceptionDesc(UtilAll.exceptionSimpleDesc(e));
                result.add(mt);
                continue;
            }

            switch (cc.getConsumeType()) {
                case CONSUME_ACTIVELY:
                    mt.setTrackType(TrackType.PULL);
                    break;
                case CONSUME_PASSIVELY:
                    boolean ifConsumed = false;
                    try {
                        ifConsumed = this.consumed(msg, group);
                    } catch (MQClientException e) {
                        if (ResponseCode.CONSUMER_NOT_ONLINE == e.getResponseCode()) {
                            mt.setTrackType(TrackType.NOT_ONLINE);
                            mt.setExceptionDesc("CODE:" + e.getResponseCode() + " DESC:" + e.getErrorMessage());
                        }
                        if (ResponseCode.BROADCAST_CONSUMPTION == e.getResponseCode()) {
                            mt.setTrackType(TrackType.CONSUME_BROADCASTING);
                        }
                        result.add(mt);
                        continue;
                    } catch (MQBrokerException e) {
                        if (ResponseCode.CONSUMER_NOT_ONLINE == e.getResponseCode()) {
                            mt.setTrackType(TrackType.NOT_ONLINE);
                            mt.setExceptionDesc("CODE:" + e.getResponseCode() + " DESC:" + e.getErrorMessage());
                        }
                        if (ResponseCode.BROADCAST_CONSUMPTION == e.getResponseCode()) {
                            mt.setTrackType(TrackType.CONSUME_BROADCASTING);
                        }
                        result.add(mt);
                        continue;
                    } catch (Exception e) {
                        mt.setExceptionDesc(UtilAll.exceptionSimpleDesc(e));
                        result.add(mt);
                        continue;
                    }

                    if (ifConsumed) {
                        mt.setTrackType(TrackType.CONSUMED);
                        Iterator<Entry<String, SubscriptionData>> it = cc.getSubscriptionTable().entrySet().iterator();
                        while (it.hasNext()) {
                            Entry<String, SubscriptionData> next = it.next();
                            if (next.getKey().equals(msg.getTopic())) {
                                if (next.getValue().getTagsSet().contains(msg.getTags()) || next.getValue().getTagsSet().contains("*") || next.getValue().getTagsSet().isEmpty()) {
                                } else {
                                    mt.setTrackType(TrackType.CONSUMED_BUT_FILTERED);
                                }
                            }
                        }
                    } else {
                        mt.setTrackType(TrackType.NOT_CONSUME_YET);
                    }
                    break;
                default:
                    break;
            }
            result.add(mt);
        }

        return result;
    }

    /** 并发查询消息轨迹详情。 */
    @Override
    public List<MessageTrack> messageTrackDetailConcurrent(
        final MessageExt msg) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        final List<MessageTrack> result = new ArrayList<>();

        GroupList groupList = this.queryTopicConsumeByWho(msg.getTopic());

        final CountDownLatch countDownLatch = new CountDownLatch(groupList.getGroupList().size());

        for (final String group : groupList.getGroupList()) {

            threadPoolExecutor.submit(new Runnable() {
                /** 后台线程：过期清理、压缩上传与优雅关闭。 */
                @Override
                public void run() {
                    MessageTrack mt = new MessageTrack();
                    mt.setConsumerGroup(group);
                    mt.setTrackType(TrackType.UNKNOWN);
                    ConsumerConnection cc = null;
                    try {
                        cc = DefaultMQAdminExtImpl.this.examineConsumerConnectionInfo(group);
                    } catch (MQBrokerException e) {
                        if (ResponseCode.CONSUMER_NOT_ONLINE == e.getResponseCode()) {
                            mt.setTrackType(TrackType.NOT_ONLINE);
                        }
                        mt.setExceptionDesc("CODE:" + e.getResponseCode() + " DESC:" + e.getErrorMessage());
                        result.add(mt);
                        countDownLatch.countDown();
                        return;
                    } catch (Exception e) {
                        mt.setExceptionDesc(UtilAll.exceptionSimpleDesc(e));
                        result.add(mt);
                        countDownLatch.countDown();
                        return;
                    }

                    switch (cc.getConsumeType()) {
                        case CONSUME_ACTIVELY:
                            mt.setTrackType(TrackType.PULL);
                            break;
                        case CONSUME_PASSIVELY:
                            boolean ifConsumed = false;
                            try {
                                ifConsumed = DefaultMQAdminExtImpl.this.consumed(msg, group);
                            } catch (MQClientException e) {
                                if (ResponseCode.CONSUMER_NOT_ONLINE == e.getResponseCode()) {
                                    mt.setTrackType(TrackType.NOT_ONLINE);
                                }
                                mt.setExceptionDesc("CODE:" + e.getResponseCode() + " DESC:" + e.getErrorMessage());
                                result.add(mt);
                                countDownLatch.countDown();
                                return;
                            } catch (MQBrokerException e) {
                                if (ResponseCode.CONSUMER_NOT_ONLINE == e.getResponseCode()) {
                                    mt.setTrackType(TrackType.NOT_ONLINE);
                                }
                                mt.setExceptionDesc("CODE:" + e.getResponseCode() + " DESC:" + e.getErrorMessage());
                                result.add(mt);
                                countDownLatch.countDown();
                                return;
                            } catch (Exception e) {
                                mt.setExceptionDesc(UtilAll.exceptionSimpleDesc(e));
                                result.add(mt);
                                countDownLatch.countDown();
                                return;
                            }

                            if (ifConsumed) {
                                mt.setTrackType(TrackType.CONSUMED);
                                Iterator<Entry<String, SubscriptionData>> it = cc.getSubscriptionTable().entrySet().iterator();
                                while (it.hasNext()) {
                                    Entry<String, SubscriptionData> next = it.next();
                                    if (next.getKey().equals(msg.getTopic())) {
                                        if (next.getValue().getTagsSet().contains(msg.getTags()) || next.getValue().getTagsSet().contains("*") || next.getValue().getTagsSet().isEmpty()) {
                                        } else {
                                            mt.setTrackType(TrackType.CONSUMED_BUT_FILTERED);
                                        }
                                    }
                                }
                            } else {
                                mt.setTrackType(TrackType.NOT_CONSUME_YET);
                            }
                            break;
                        default:
                            break;
                    }
                    result.add(mt);
                    countDownLatch.countDown();
                    return;
                }
            });
        }

        countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);

        return result;
    }

    /** 判断消息是否已被指定消费组消费（同步）。 */
    public boolean consumed(final MessageExt msg,
        final String group) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {

        ConsumeStats cstats = this.examineConsumeStats(group);

        ClusterInfo ci = this.examineBrokerClusterInfo();

        Iterator<Entry<MessageQueue, OffsetWrapper>> it = cstats.getOffsetTable().entrySet().iterator();
        while (it.hasNext()) {
            Entry<MessageQueue, OffsetWrapper> next = it.next();
            MessageQueue mq = next.getKey();
            if (mq.getTopic().equals(msg.getTopic()) && mq.getQueueId() == msg.getQueueId()) {
                BrokerData brokerData = ci.getBrokerAddrTable().get(mq.getBrokerName());
                if (brokerData != null) {
                    String addr = NetworkUtil.convert2IpString(brokerData.getBrokerAddrs().get(MixAll.MASTER_ID));
                    if (NetworkUtil.socketAddress2String(msg.getStoreHost()).equals(addr)) {
                        if (next.getValue().getConsumerOffset() > msg.getQueueOffset()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /** 并发判断消息消费状态。 */
    public boolean consumedConcurrent(final MessageExt msg,
        final String group) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {

        AdminToolResult<ConsumeStats> cstats = this.examineConsumeStatsConcurrent(group, null);

        if (!cstats.isSuccess()) {
            throw new MQClientException(cstats.getCode(), cstats.getErrorMsg());
        }

        ClusterInfo ci = this.examineBrokerClusterInfo();

        if (cstats.isSuccess()) {
            for (Entry<MessageQueue, OffsetWrapper> next : cstats.getData().getOffsetTable().entrySet()) {
                MessageQueue mq = next.getKey();
                if (mq.getTopic().equals(msg.getTopic()) && mq.getQueueId() == msg.getQueueId()) {
                    BrokerData brokerData = ci.getBrokerAddrTable().get(mq.getBrokerName());
                    if (brokerData != null) {
                        String addr = brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);
                        if (addr.equals(NetworkUtil.socketAddress2String(msg.getStoreHost()))) {
                            if (next.getValue().getConsumerOffset() > msg.getQueueOffset()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /** 将源消费组位点克隆到目标消费组。 */
    @Override
    public void cloneGroupOffset(String srcGroup, String destGroup, String topic,
        boolean isOffline) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        String retryTopic = MixAll.getRetryTopic(srcGroup);
        TopicRouteData topicRouteData = this.examineTopicRouteInfo(retryTopic);

        for (BrokerData bd : topicRouteData.getBrokerDatas()) {
            String addr = bd.selectBrokerAddr();
            if (addr != null) {
                this.mqClientInstance.getMQClientAPIImpl().cloneGroupOffset(addr, srcGroup, destGroup, topic, isOffline, timeoutMillis);
            }
        }
    }

    /** 查看 Broker 指定统计项明细。 */
    @Override
    public BrokerStatsData viewBrokerStatsData(String brokerAddr, String statsName,
        String statsKey) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQClientException, InterruptedException {
        return this.mqClientInstance.getMQClientAPIImpl().viewBrokerStatsData(brokerAddr, statsName, statsKey, timeoutMillis);
    }

    /** 返回 Topic 所在集群名集合。 */
    @Override
    public Set<String> getClusterList(
        String topic) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQClientException, InterruptedException {
        return this.mqClientInstance.getMQClientAPIImpl().getClusterList(topic, timeoutMillis);
    }

    /** 拉取 Broker 上全部消费组统计。 */
    @Override
    public ConsumeStatsList fetchConsumeStatsInBroker(final String brokerAddr, boolean isOrder,
        long timeoutMillis) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQClientException, InterruptedException {
        return this.mqClientInstance.getMQClientAPIImpl().fetchConsumeStatsInBroker(brokerAddr, isOrder, timeoutMillis);
    }

    /** 返回 Topic 关联的集群列表。 */
    @Override
    public Set<String> getTopicClusterList(
        final String topic) throws InterruptedException, MQBrokerException, MQClientException, RemotingException {
        Set<String> clusterSet = new HashSet<>();
        ClusterInfo clusterInfo = examineBrokerClusterInfo();
        TopicRouteData topicRouteData = examineTopicRouteInfo(topic);
        BrokerData brokerData = topicRouteData.getBrokerDatas().get(0);
        String brokerName = brokerData.getBrokerName();
        Iterator<Map.Entry<String, Set<String>>> it = clusterInfo.getClusterAddrTable().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Set<String>> next = it.next();
            if (next.getValue().contains(brokerName)) {
                clusterSet.add(next.getKey());
            }
        }
        return clusterSet;
    }

    /** 拉取 Broker 全部订阅组配置。 */
    @Override
    public SubscriptionGroupWrapper getAllSubscriptionGroup(final String brokerAddr, long timeoutMillis)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException,
        RemotingConnectException, MQBrokerException, RemotingCommandException {
        return this.mqClientInstance.getMQClientAPIImpl().getAllSubscriptionGroup(brokerAddr, timeoutMillis);
    }

    /** 拉取 Broker 用户订阅组配置（不含系统组）。 */
    @Override
    public SubscriptionGroupWrapper getUserSubscriptionGroup(final String brokerAddr, long timeoutMillis)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException,
        RemotingConnectException, MQBrokerException, RemotingCommandException {
        SubscriptionGroupWrapper subscriptionGroupWrapper = this.mqClientInstance.getMQClientAPIImpl().getAllSubscriptionGroup(brokerAddr, timeoutMillis);

        Iterator<Entry<String, SubscriptionGroupConfig>> iterator = subscriptionGroupWrapper.getSubscriptionGroupTable().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SubscriptionGroupConfig> configEntry = iterator.next();
            if (MixAll.isSysConsumerGroup(configEntry.getKey()) || MixAll.isPredefinedGroup(configEntry.getKey())) {
                iterator.remove();
            }
        }

        return subscriptionGroupWrapper;
    }

    /** 拉取 Broker 全部 Topic 配置。 */
    @Override
    public TopicConfigSerializeWrapper getAllTopicConfig(final String brokerAddr, long timeoutMillis)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException,
        RemotingConnectException, MQBrokerException, RemotingCommandException {
        return this.mqClientInstance.getMQClientAPIImpl().getAllTopicConfig(brokerAddr, timeoutMillis);
    }

    /** 拉取 Broker 用户 Topic 配置。 */
    @Override
    public TopicConfigSerializeWrapper getUserTopicConfig(final String brokerAddr, final boolean specialTopic,
        long timeoutMillis) throws InterruptedException, RemotingException, MQBrokerException, MQClientException {
        TopicConfigSerializeWrapper topicConfigSerializeWrapper = this.getAllTopicConfig(brokerAddr, timeoutMillis);
        TopicList topicList = this.mqClientInstance.getMQClientAPIImpl().getSystemTopicListFromBroker(brokerAddr, timeoutMillis);
        Iterator<Entry<String, TopicConfig>> iterator = topicConfigSerializeWrapper.getTopicConfigTable().entrySet().iterator();
        while (iterator.hasNext()) {
            TopicConfig topicConfig = iterator.next().getValue();
            if (topicList.getTopicList().contains(topicConfig.getTopicName())
                || TopicValidator.isSystemTopic(topicConfig.getTopicName())) {
                iterator.remove();
            } else if (!specialTopic && StringUtils.startsWithAny(topicConfig.getTopicName(),
                MixAll.RETRY_GROUP_TOPIC_PREFIX, MixAll.DLQ_GROUP_TOPIC_PREFIX)) {
                iterator.remove();
            } else if (!PermName.isValid(topicConfig.getPerm())) {
                iterator.remove();
            }
        }
        return topicConfigSerializeWrapper;
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
        this.mqClientInstance.getMQAdminImpl().createTopic(key, newTopic, queueNum, topicSysFlag, attributes);
    }

    /** 创建静态 Topic 并写入队列映射。 */
    @Override
    public void createStaticTopic(final String addr, final String defaultTopic, final TopicConfig topicConfig,
        final TopicQueueMappingDetail mappingDetail,
        final boolean force) throws RemotingException, InterruptedException, MQBrokerException {
        this.mqClientInstance.getMQClientAPIImpl().createStaticTopic(addr, defaultTopic, topicConfig, mappingDetail, force, timeoutMillis);
    }

    /** 按时间戳在队列中查找 offset。 */
    @Override
    public long searchOffset(MessageQueue mq, long timestamp) throws MQClientException {
        return this.mqClientInstance.getMQAdminImpl().searchOffset(mq, timestamp);
    }

    /** 按时间戳在队列中查找 offset。 */
    public long searchOffset(MessageQueue mq, long timestamp, BoundaryType boundaryType) throws MQClientException {
        return this.mqClientInstance.getMQAdminImpl().searchOffset(mq, timestamp, boundaryType);
    }

    /** 返回队列最大逻辑 offset。 */
    @Override
    public long maxOffset(MessageQueue mq) throws MQClientException {
        return this.mqClientInstance.getMQAdminImpl().maxOffset(mq);
    }

    /** 返回队列最小逻辑 offset。 */
    @Override
    public long minOffset(MessageQueue mq) throws MQClientException {
        return this.mqClientInstance.getMQAdminImpl().minOffset(mq);
    }

    /** 返回队列最早消息存储时间。 */
    @Override
    public long earliestMsgStoreTime(MessageQueue mq) throws MQClientException {
        return this.mqClientInstance.getMQAdminImpl().earliestMsgStoreTime(mq);
    }

    /** 按 Topic/Key 或集群条件索引查询消息。 */
    @Override
    public QueryResult queryMessage(String topic, String key, int maxNum, long begin,
        long end) throws MQClientException, InterruptedException {

        return this.mqClientInstance.getMQAdminImpl().queryMessage(topic, key, maxNum, begin, end);
    }

    /** 按 Topic/Key 或集群条件索引查询消息。 */
    public QueryResult queryMessage(String clusterName, String topic, String key, int maxNum, long begin,
        long end) throws MQClientException, InterruptedException, RemotingException {
        return this.mqClientInstance.getMQAdminImpl().queryMessage(clusterName, topic, key, maxNum, begin, end, false, MessageConst.INDEX_KEY_TYPE, null);
    }

    /** 按 Topic/Key 或集群条件索引查询消息。 */
    public QueryResult queryMessage(String clusterName, String topic, String key, int maxNum, long begin, long end, String keyType, String lastKey) throws MQClientException, InterruptedException, RemotingException {
        return this.mqClientInstance.getMQAdminImpl().queryMessage(clusterName, topic, key, maxNum, begin, end, false, keyType, lastKey);
    }
    /** 更新消费组在指定队列上的 commit offset。 */
    @Override
    public void updateConsumeOffset(String brokerAddr, String consumeGroup, MessageQueue mq,
        long offset) throws RemotingException, InterruptedException, MQBrokerException {
        UpdateConsumerOffsetRequestHeader requestHeader = new UpdateConsumerOffsetRequestHeader();
        requestHeader.setConsumerGroup(consumeGroup);
        requestHeader.setTopic(mq.getTopic());
        requestHeader.setQueueId(mq.getQueueId());
        requestHeader.setCommitOffset(offset);
        requestHeader.setBrokerName(mq.getBrokerName());
        this.mqClientInstance.getMQClientAPIImpl().updateConsumerOffset(brokerAddr, requestHeader, timeoutMillis);
    }

    /** 更新 NameServer 配置（可指定 NS 列表）。 */
    @Override
    public void updateNameServerConfig(final Properties properties,
        final List<String> nameServers) throws InterruptedException, RemotingConnectException, UnsupportedEncodingException, RemotingSendRequestException, RemotingTimeoutException, MQClientException, MQBrokerException {
        this.mqClientInstance.getMQClientAPIImpl().updateNameServerConfig(properties, nameServers, timeoutMillis);
    }

    /** 拉取 NameServer 配置。 */
    @Override
    public Map<String, Properties> getNameServerConfig(
        final List<String> nameServers) throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException, MQClientException, UnsupportedEncodingException {
        return this.mqClientInstance.getMQClientAPIImpl().getNameServerConfig(nameServers, timeoutMillis);
    }

    /** 分页查询 ConsumeQueue 条目。 */
    @Override
    public QueryConsumeQueueResponseBody queryConsumeQueue(String brokerAddr, String topic, int queueId, long index,
        int count,
        String consumerGroup) throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException, MQClientException {
        return this.mqClientInstance.getMQClientAPIImpl().queryConsumeQueue(brokerAddr, topic, queueId, index, count, consumerGroup, timeoutMillis);
    }

    /** 检查 RocksDB ConsumeQueue 写入进度。 */
    @Override
    public CheckRocksdbCqWriteResult checkRocksdbCqWriteProgress(String brokerAddr, String topic, long checkStoreTime)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException, MQClientException {
        return this.mqClientInstance.getMQClientAPIImpl().checkRocksdbCqWriteProgress(brokerAddr, topic, checkStoreTime, timeoutMillis);
    }

    /** 导出 Broker RocksDB 配置为 JSON。 */
    @Override
    public void exportRocksDBConfigToJson(String brokerAddr,
        List<ExportRocksDBConfigToJsonRequestHeader.ConfigType> configType)
        throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException, MQClientException {
        this.mqClientInstance.getMQClientAPIImpl().exportRocksDBConfigToJson(brokerAddr, configType, timeoutMillis);
    }

    /** 恢复半消息事务检查（事务消息运维）。 */
    @Override
    public boolean resumeCheckHalfMessage(final String topic,
        final String msgId) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        MessageExt msg = this.viewMessage(topic, msgId);
        if (msg.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX) == null) {
            return this.mqClientInstance.getMQClientAPIImpl().resumeCheckHalfMessage(NetworkUtil.socketAddress2String(msg.getStoreHost()), topic, msgId, timeoutMillis);
        } else {
            MessageClientExt msgClient = (MessageClientExt) msg;
            return this.mqClientInstance.getMQClientAPIImpl().resumeCheckHalfMessage(NetworkUtil.socketAddress2String(msg.getStoreHost()), topic, msgClient.getOffsetMsgId(), timeoutMillis);
        }
    }

    /** 设置 Topic 消费组的 POP/Pull 请求模式。 */
    @Override
    public void setMessageRequestMode(final String brokerAddr, final String topic, final String consumerGroup,
        final MessageRequestMode mode, final int popShareQueueNum,
        final long timeoutMillis) throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException, MQClientException {
        this.mqClientInstance.getMQClientAPIImpl().setMessageRequestMode(brokerAddr, topic, consumerGroup, mode, popShareQueueNum, timeoutMillis);
    }

    @Deprecated
    /** 按时间戳在队列中查找 offset。 */
    @Override
    public long searchOffset(final String brokerAddr, final String topicName, final int queueId, final long timestamp,
        final long timeoutMillis) throws RemotingException, MQBrokerException, InterruptedException {
        return this.mqClientInstance.getMQClientAPIImpl().searchOffset(brokerAddr, topicName, queueId, timestamp, timeoutMillis);
    }

    /** 按 UniqKey 查询消息。 */
    public QueryResult queryMessageByUniqKey(String clusterName, String topic, String key, int maxNum, long begin,
        long end) throws MQClientException, InterruptedException {
        return this.mqClientInstance.getMQAdminImpl().queryMessageByUniqKey(clusterName, topic, key, maxNum, begin, end);
    }

    /** 重置指定队列的消费位点。 */
    @Override
    public void resetOffsetByQueueId(final String brokerAddr, final String consumeGroup, final String topicName,
        final int queueId, final long resetOffset) throws RemotingException, InterruptedException, MQBrokerException {
        UpdateConsumerOffsetRequestHeader requestHeader = new UpdateConsumerOffsetRequestHeader();
        requestHeader.setConsumerGroup(consumeGroup);
        requestHeader.setTopic(topicName);
        requestHeader.setQueueId(queueId);
        requestHeader.setCommitOffset(resetOffset);
        this.mqClientInstance.getMQClientAPIImpl().updateConsumerOffset(brokerAddr, requestHeader, timeoutMillis);
        try {
            Map<MessageQueue, Long> result = mqClientInstance.getMQClientAPIImpl()
                .invokeBrokerToResetOffset(brokerAddr, topicName, consumeGroup, 0, queueId, resetOffset, timeoutMillis);
            if (null != result) {
                for (Map.Entry<MessageQueue, Long> entry : result.entrySet()) {
                    logger.info("Reset single message queue {} offset from {} to {}",
                        JSON.toJSONString(entry.getKey()), entry.getValue(), resetOffset);
                }
            }
        } catch (MQClientException e) {
            throw new MQBrokerException(e.getResponseCode(), e.getMessage());
        }
    }

    /** 按时间戳重置消费位点到各队列。 */
    @Override
    public Map<MessageQueue, Long> resetOffsetByTimestamp(String clusterName, String topic, String group,
        long timestamp, boolean isForce) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return resetOffsetByTimestamp(clusterName, topic, group, timestamp, isForce, false);
    }

    /** 查询 Broker 主从复制 HA 状态。 */
    @Override
    public HARuntimeInfo getBrokerHAStatus(
        String brokerAddr) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException, MQBrokerException {
        return this.mqClientInstance.getMQClientAPIImpl().getBrokerHAStatus(brokerAddr, timeoutMillis);
    }

    /** 从 Controller 查询副本 InSync 状态。 */
    @Override
    public BrokerReplicasInfo getInSyncStateData(String controllerAddress,
        List<String> brokers) throws RemotingException, InterruptedException, MQBrokerException {
        return this.mqClientInstance.getMQClientAPIImpl().getInSyncStateData(controllerAddress, brokers);
    }

    /** 拉取 Broker Epoch 缓存（Controller 协议）。 */
    @Override
    public EpochEntryCache getBrokerEpochCache(
        String brokerAddr) throws RemotingException, InterruptedException, MQBrokerException {
        return this.mqClientInstance.getMQClientAPIImpl().getBrokerEpochCache(brokerAddr);
    }

    /** 获取 Controller 集群元数据。 */
    @Override
    public GetMetaDataResponseHeader getControllerMetaData(
        String controllerAddr) throws RemotingException, InterruptedException, MQBrokerException {
        return this.mqClientInstance.getMQClientAPIImpl().getControllerMetaData(controllerAddr);
    }

    /** 在 Slave 上重置 Master flush offset。 */
    @Override
    public void resetMasterFlushOffset(String brokerAddr,
        long masterFlushOffset) throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException {
        this.mqClientInstance.getMQClientAPIImpl().resetMasterFlushOffset(brokerAddr, masterFlushOffset);
    }

    /** 手动触发 Controller 选举 Broker Master。 */
    @Override
    public Pair<ElectMasterResponseHeader, BrokerMemberGroup> electMaster(String controllerAddr, String clusterName,
        String brokerName, Long brokerId) throws RemotingException, InterruptedException, MQBrokerException {
        return this.mqClientInstance.getMQClientAPIImpl().electMaster(controllerAddr, clusterName, brokerName, brokerId);
    }

    /** 更新并返回消费组 Topic 读禁止状态。 */
    @Override
    public GroupForbidden updateAndGetGroupReadForbidden(String brokerAddr, String groupName, String topicName,
        Boolean readable) throws RemotingException, InterruptedException, MQBrokerException {
        UpdateGroupForbiddenRequestHeader requestHeader = new UpdateGroupForbiddenRequestHeader();
        requestHeader.setGroup(groupName);
        requestHeader.setTopic(topicName);
        requestHeader.setReadable(readable);
        return this.mqClientInstance.getMQClientAPIImpl().updateAndGetGroupForbidden(brokerAddr, requestHeader, timeoutMillis);
    }

    /** 从 NameServer 删除 Topic 路由元数据。 */
    @Override
    public void deleteTopicInNameServer(Set<String> addrs, String clusterName,
        String topic) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        if (addrs == null) {
            String ns = this.mqClientInstance.getMQClientAPIImpl().fetchNameServerAddr();
            addrs = new HashSet(Arrays.asList(ns.split(";")));
        }
        for (String addr : addrs) {
            this.mqClientInstance.getMQClientAPIImpl().deleteTopicInNameServer(addr, clusterName, topic, timeoutMillis);
        }
    }

    /** 拉取 Controller 配置。 */
    @Override
    public Map<String, Properties> getControllerConfig(
        List<String> controllerServers) throws InterruptedException, RemotingTimeoutException,
        RemotingSendRequestException, RemotingConnectException, MQClientException,
        UnsupportedEncodingException {
        return this.mqClientInstance.getMQClientAPIImpl().getControllerConfig(controllerServers, timeoutMillis);
    }

    /** 更新 Controller 配置。 */
    @Override
    public void updateControllerConfig(Properties properties,
        List<String> controllers) throws InterruptedException, RemotingConnectException, UnsupportedEncodingException,
        RemotingSendRequestException, RemotingTimeoutException, MQClientException, MQBrokerException {
        this.mqClientInstance.getMQClientAPIImpl().updateControllerConfig(properties, controllers, timeoutMillis);
    }

    /** 清理 Controller 中 Broker 元数据。 */
    @Override
    public void cleanControllerBrokerData(String controllerAddr, String clusterName, String brokerName,
        String brokerIdSetToClean, boolean isCleanLivingBroker)
        throws RemotingException, InterruptedException, MQBrokerException {
        this.mqClientInstance.getMQClientAPIImpl().cleanControllerBrokerData(controllerAddr, clusterName, brokerName, brokerIdSetToClean, isCleanLivingBroker);
    }

    /** MQ 管理操作：getMqClientInstance。 */
    public MQClientInstance getMqClientInstance() {
        return mqClientInstance;
    }

    /** 更新冷读流控消费组配置。 */
    @Override
    public void updateColdDataFlowCtrGroupConfig(String brokerAddr, Properties properties)
        throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException,
        UnsupportedEncodingException, InterruptedException, MQBrokerException {
        this.mqClientInstance.getMQClientAPIImpl().updateColdDataFlowCtrGroupConfig(brokerAddr, properties, timeoutMillis);
    }

    /** 移除冷读流控消费组配置。 */
    @Override
    public void removeColdDataFlowCtrGroupConfig(String brokerAddr, String consumerGroup)
        throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException,
        UnsupportedEncodingException, InterruptedException, MQBrokerException {
        this.mqClientInstance.getMQClientAPIImpl().removeColdDataFlowCtrGroupConfig(brokerAddr, consumerGroup, timeoutMillis);
    }

    /** 查询冷读流控配置 JSON。 */
    @Override
    public String getColdDataFlowCtrInfo(String brokerAddr)
        throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException,
        UnsupportedEncodingException, InterruptedException, MQBrokerException {
        return this.mqClientInstance.getMQClientAPIImpl().getColdDataFlowCtrInfo(brokerAddr, timeoutMillis);
    }

    /** 设置 CommitLog 预读模式。 */
    @Override
    public String setCommitLogReadAheadMode(String brokerAddr, String mode)
        throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException, MQBrokerException {
        return this.mqClientInstance.getMQClientAPIImpl().setCommitLogReadAheadMode(brokerAddr, mode, timeoutMillis);
    }

    /** 在 Broker 创建用户账号。 */
    @Override
    public void createUser(String brokerAddr,
        UserInfo userInfo) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        this.mqClientInstance.getMQClientAPIImpl().createUser(brokerAddr, userInfo, timeoutMillis);
    }

    /** 在 Broker 创建用户账号。 */
    @Override
    public void createUser(String brokerAddr, String username, String password,
        String userType) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        UserInfo userInfo = UserInfo.of(username, password, userType);
        this.createUser(brokerAddr, userInfo);
    }

    /** 更新 Broker 用户账号。 */
    @Override
    public void updateUser(String brokerAddr, String username,
        String password, String userType,
        String userStatus) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        UserInfo userInfo = UserInfo.of(username, password, userType, userStatus);
        this.mqClientInstance.getMQClientAPIImpl().updateUser(brokerAddr, userInfo, timeoutMillis);
    }

    /** 更新 Broker 用户账号。 */
    @Override
    public void updateUser(String brokerAddr,
        UserInfo userInfo) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        this.mqClientInstance.getMQClientAPIImpl().updateUser(brokerAddr, userInfo, timeoutMillis);
    }

    /** 删除 Broker 用户。 */
    @Override
    public void deleteUser(String brokerAddr,
        String username) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        this.mqClientInstance.getMQClientAPIImpl().deleteUser(brokerAddr, username, timeoutMillis);
    }

    /** 查询 Broker 用户信息。 */
    @Override
    public UserInfo getUser(String brokerAddr,
        String username) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        return this.mqClientInstance.getMQClientAPIImpl().getUser(brokerAddr, username, timeoutMillis);
    }

    /** 列出 Broker 用户列表。 */
    @Override
    public List<UserInfo> listUser(String brokerAddr,
        String filter) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        return this.mqClientInstance.getMQClientAPIImpl().listUser(brokerAddr, filter, timeoutMillis);
    }

    /** 在 Broker 创建 ACL 规则。 */
    @Override
    public void createAcl(String brokerAddr, String subject, List<String> resources, List<String> actions,
        List<String> sourceIps,
        String decision) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        AclInfo aclInfo = AclInfo.of(subject, resources, actions, sourceIps, decision);
        this.createAcl(brokerAddr, aclInfo);
    }

    /** 在 Broker 创建 ACL 规则。 */
    @Override
    public void createAcl(String brokerAddr,
        AclInfo aclInfo) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        this.mqClientInstance.getMQClientAPIImpl().createAcl(brokerAddr, aclInfo, timeoutMillis);
    }

    /** 更新 Broker ACL 规则。 */
    @Override
    public void updateAcl(String brokerAddr, String subject, List<String> resources, List<String> actions,
        List<String> sourceIps,
        String decision) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        AclInfo aclInfo = AclInfo.of(subject, resources, actions, sourceIps, decision);
        this.updateAcl(brokerAddr, aclInfo);
    }

    /** 更新 Broker ACL 规则。 */
    @Override
    public void updateAcl(String brokerAddr,
        AclInfo aclInfo) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        this.mqClientInstance.getMQClientAPIImpl().updateAcl(brokerAddr, aclInfo, timeoutMillis);
    }

    /** 删除 Broker ACL 规则。 */
    @Override
    public void deleteAcl(String brokerAddr, String subject,
        String resource) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        this.mqClientInstance.getMQClientAPIImpl().deleteAcl(brokerAddr, subject, resource, timeoutMillis);
    }

    /** 查询 Broker ACL 规则。 */
    @Override
    public AclInfo getAcl(String brokerAddr,
        String subject) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        return this.mqClientInstance.getMQClientAPIImpl().getAcl(brokerAddr, subject, timeoutMillis);
    }

    /** 列出 Broker ACL 规则。 */
    @Override
    public List<AclInfo> listAcl(String brokerAddr, String subjectFilter,
        String resourceFilter) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        return this.mqClientInstance.getMQClientAPIImpl().listAcl(brokerAddr, subjectFilter, resourceFilter, timeoutMillis);
    }

    /** 导出 Broker POP 消费记录。 */
    @Override
    public void exportPopRecords(String brokerAddr, long timeout) throws RemotingConnectException,
        RemotingSendRequestException, RemotingTimeoutException, MQBrokerException, InterruptedException {
        this.mqClientInstance.getMQClientAPIImpl().exportPopRecord(brokerAddr, timeout);
    }

    /** 切换 Broker 定时消息引擎。 */
    @Override
    public void switchTimerEngine(String brokerAddr, String desTimerEngine) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, UnsupportedEncodingException, InterruptedException, MQBrokerException {
        this.mqClientInstance.getMQClientAPIImpl().switchTimerEngine(brokerAddr, desTimerEngine, timeoutMillis);
    }


    /** 查询 Broker Lite 模式信息。 */
    @Override
    public GetBrokerLiteInfoResponseBody getBrokerLiteInfo(String brokerAddr)
        throws RemotingException, MQBrokerException, InterruptedException {
        return this.mqClientInstance.getMQClientAPIImpl().getBrokerLiteInfo(brokerAddr, timeoutMillis);
    }

    /** 查询 Lite 父 Topic 信息。 */
    @Override
    public GetParentTopicInfoResponseBody getParentTopicInfo(String brokerAddr, String topic)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return this.mqClientInstance.getMQClientAPIImpl().getParentTopicInfo(brokerAddr, topic, timeoutMillis);
    }

    /** 查询 Lite 子 Topic 信息。 */
    @Override
    public GetLiteTopicInfoResponseBody getLiteTopicInfo(String brokerAddr, String parentTopic, String liteTopic)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return this.mqClientInstance.getMQClientAPIImpl().getLiteTopicInfo(brokerAddr, parentTopic, liteTopic,
            timeoutMillis);
    }

    /** 查询 Lite 消费端信息。 */
    @Override
    public GetLiteClientInfoResponseBody getLiteClientInfo(String brokerAddr, String parentTopic, String group,
        String clientId)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return this.mqClientInstance.getMQClientAPIImpl().getLiteClientInfo(brokerAddr, parentTopic, group, clientId,
         timeoutMillis);
    }

    /** 查询 Lite 消费组 TopK 信息。 */
    @Override
    public GetLiteGroupInfoResponseBody getLiteGroupInfo(String brokerAddr, String group, String liteTopic, int topK)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        return this.mqClientInstance.getMQClientAPIImpl().getLiteGroupInfo(brokerAddr, group, liteTopic, topK, timeoutMillis);
    }

    /** 触发 Lite 消费端消息分发。 */
    @Override
    public void triggerLiteDispatch(String brokerAddr, String group, String clientId)
        throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        this.mqClientInstance.getMQClientAPIImpl().triggerLiteDispatch(brokerAddr, group, clientId, timeoutMillis);
    }

}
