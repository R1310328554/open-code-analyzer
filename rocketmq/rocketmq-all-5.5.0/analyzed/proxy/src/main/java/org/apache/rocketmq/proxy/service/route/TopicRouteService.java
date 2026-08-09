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
package org.apache.rocketmq.proxy.service.route;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.annotations.VisibleForTesting;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.mqclient.MQClientAPIFactory;
import org.apache.rocketmq.client.latency.MQFaultStrategy;
import org.apache.rocketmq.client.latency.Resolver;
import org.apache.rocketmq.client.latency.ServiceDetector;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.thread.ThreadPoolMonitor;
import org.apache.rocketmq.common.utils.AbstractStartAndShutdown;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.proxy.common.Address;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.config.ConfigurationManager;
import org.apache.rocketmq.proxy.config.ProxyConfig;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.header.GetMaxOffsetRequestHeader;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * 主题路由服务抽象基类：维护 Caffeine 缓存、故障容忍策略与队列视图构建。
 */
public abstract class TopicRouteService extends AbstractStartAndShutdown {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME);

    private final MQFaultStrategy mqFaultStrategy;
    /** 主题名到 {@link MessageQueueView} 的本地缓存。 */
    protected final LoadingCache<String /* topicName */, MessageQueueView> topicCache;
    protected final ThreadPoolExecutor cacheRefreshExecutor;
    protected final List<MessageQueuePenalizer<AddressableMessageQueue>> penalizers = new ArrayList<>();
    protected MessageQueuePriorityProvider<AddressableMessageQueue> priorityProvider = new DefaultMessageQueuePriorityProvider();

    public TopicRouteService(MQClientAPIFactory mqClientAPIFactory) {
        ProxyConfig config = ConfigurationManager.getProxyConfig();

        this.cacheRefreshExecutor = ThreadPoolMonitor.createAndMonitor(
            config.getTopicRouteServiceThreadPoolNums(),
            config.getTopicRouteServiceThreadPoolNums(),
            1000 * 60,
            TimeUnit.MILLISECONDS,
            "TopicRouteCacheRefresh",
            config.getTopicRouteServiceThreadPoolQueueCapacity()
        );

        this.topicCache = Caffeine.newBuilder().maximumSize(config.getTopicRouteServiceCacheMaxNum())
            .expireAfterAccess(config.getTopicRouteServiceCacheExpiredSeconds(), TimeUnit.SECONDS)
            .refreshAfterWrite(config.getTopicRouteServiceCacheRefreshSeconds(), TimeUnit.SECONDS)
            .executor(cacheRefreshExecutor)
            .build(new CacheLoader<String, MessageQueueView>() {
                @Override
                public @Nullable MessageQueueView load(String topic) throws Exception {
                    try {
                        TopicRouteData topicRouteData = mqClientAPIFactory.getClient().getTopicRouteInfoFromNameServer(topic, Duration.ofSeconds(3).toMillis());
                        return buildMessageQueueView(topic, topicRouteData);
                    } catch (Exception e) {
                        // 主题不存在时缓存空占位视图
                        if (TopicRouteHelper.isTopicNotExistError(e)) {
                            return MessageQueueView.WRAPPED_EMPTY_QUEUE;
                        }
                        throw e;
                    }
                }

                @Override
                public @Nullable MessageQueueView reload(@NonNull String key,
                    @NonNull MessageQueueView oldValue) throws Exception {
                    try {
                        return load(key);
                    } catch (Exception e) {
                        log.warn(String.format("reload topic route from namesrv. topic: %s", key), e);
                        return oldValue;
                    }
                }
            });
        ServiceDetector serviceDetector = new ServiceDetector() {
            @Override
            public boolean detect(String endpoint, long timeoutMillis) {
                Optional<String> candidateTopic = pickTopic();
                if (!candidateTopic.isPresent()) {
                    return false;
                }
                try {
                    GetMaxOffsetRequestHeader requestHeader = new GetMaxOffsetRequestHeader();
                    requestHeader.setTopic(candidateTopic.get());
                    requestHeader.setQueueId(0);
                    Long maxOffset = mqClientAPIFactory.getClient().getMaxOffset(endpoint, requestHeader, timeoutMillis).get();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        };
        mqFaultStrategy = new MQFaultStrategy(extractClientConfigFromProxyConfig(config), new Resolver() {
            @Override
            public String resolve(String name) {
                try {
                    String brokerAddr = getBrokerAddr(ProxyContext.createForInner("MQFaultStrategy"), name);
                    return brokerAddr;
                } catch (Exception e) {
                    return null;
                }
            }
        }, serviceDetector);

        this.penalizers.addAll(buildPenalizerByMQFaultStrategy(mqFaultStrategy));
        this.init();
    }

    // 从缓存中任取一个主题用于 Broker 可达性探测
    private Optional<String> pickTopic() {
        if (topicCache.asMap().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(topicCache.asMap().keySet().iterator().next());
    }

    protected void init() {
        this.appendStartAndShutdown(this.mqFaultStrategy);
    }

    public ClientConfig extractClientConfigFromProxyConfig(ProxyConfig proxyConfig) {
        ClientConfig tempClientConfig = new ClientConfig();
        tempClientConfig.setSendLatencyEnable(proxyConfig.getSendLatencyEnable());
        tempClientConfig.setStartDetectorEnable(proxyConfig.getStartDetectorEnable());
        tempClientConfig.setDetectTimeout(proxyConfig.getDetectTimeout());
        tempClientConfig.setDetectInterval(proxyConfig.getDetectInterval());
        return tempClientConfig;
    }

    /** 更新 Broker 故障项：延迟、隔离与可达状态。 */
    public void updateFaultItem(final String brokerName, final long currentLatency, boolean isolation,
                                boolean reachable) {
        checkSendFaultToleranceEnable();
        this.mqFaultStrategy.updateFaultItem(brokerName, currentLatency, isolation, reachable);
    }

    public void checkSendFaultToleranceEnable() {
        boolean hotLatencySwitch = ConfigurationManager.getProxyConfig().isSendLatencyEnable();
        boolean hotDetectorSwitch = ConfigurationManager.getProxyConfig().isStartDetectorEnable();
        this.mqFaultStrategy.setSendLatencyFaultEnable(hotLatencySwitch);
        this.mqFaultStrategy.setStartDetectorEnable(hotDetectorSwitch);
    }

    public MQFaultStrategy getMqFaultStrategy() {
        return this.mqFaultStrategy;
    }

    /** 从缓存加载主题的全部读写队列视图。 */
    public MessageQueueView getAllMessageQueueView(ProxyContext ctx, String topicName) throws Exception {
        return getCacheMessageQueueWrapper(this.topicCache, topicName);
    }

    /** 获取当前生效的主题队列视图（子类实现）。 */
    public abstract MessageQueueView getCurrentMessageQueueView(ProxyContext ctx, String topicName) throws Exception;

    public abstract ProxyTopicRouteData getTopicRouteForProxy(ProxyContext ctx, List<Address> requestHostAndPortList,
        String topicName) throws Exception;

    public abstract String getBrokerAddr(ProxyContext ctx, String brokerName) throws Exception;

    public abstract AddressableMessageQueue buildAddressableMessageQueue(ProxyContext ctx, MessageQueue messageQueue) throws Exception;

    /** 从缓存取视图，空占位时抛出 TOPIC_NOT_EXIST。 */
    protected static MessageQueueView getCacheMessageQueueWrapper(LoadingCache<String, MessageQueueView> topicCache,
        String key) throws Exception {
        MessageQueueView res = topicCache.get(key);
        if (res != null && res.isEmptyCachedQueue()) {
            throw new MQClientException(ResponseCode.TOPIC_NOT_EXIST,
                "No topic route info in name server for the topic: " + key);
        }
        return res;
    }

    /** 校验路由数据是否同时包含队列与 Broker 信息。 */
    protected static boolean isTopicRouteValid(TopicRouteData routeData) {
        return routeData != null && routeData.getQueueDatas() != null && !routeData.getQueueDatas().isEmpty()
            && routeData.getBrokerDatas() != null && !routeData.getBrokerDatas().isEmpty();
    }

    /** 根据路由有效性构建 {@link MessageQueueView} 或空占位。 */
    protected MessageQueueView buildMessageQueueView(String topic, TopicRouteData topicRouteData) {
        if (isTopicRouteValid(topicRouteData)) {
            MessageQueueView tmp = new MessageQueueView(topic, topicRouteData, this.penalizers, this.priorityProvider);
            log.debug("load topic route from namesrv. topic: {}, queue: {}", topic, tmp);
            return tmp;
        }
        return MessageQueueView.WRAPPED_EMPTY_QUEUE;
    }

    public void setPriorityProvider(MessageQueuePriorityProvider<AddressableMessageQueue> priorityProvider) {
        this.priorityProvider = priorityProvider;
    }

    public void addPenalizer(MessageQueuePenalizer<AddressableMessageQueue> penalizer) {
        this.penalizers.add(penalizer);
    }

    @VisibleForTesting
    /** 根据 {@link MQFaultStrategy} 构建可用性与可达性惩罚器。 */
    public static List<MessageQueuePenalizer<AddressableMessageQueue>> buildPenalizerByMQFaultStrategy(MQFaultStrategy mqFaultStrategy) {
        List<MessageQueuePenalizer<AddressableMessageQueue>> penalizers = new ArrayList<>();
        penalizers.add(messageQueue -> {
            if (!mqFaultStrategy.isSendLatencyFaultEnable() || mqFaultStrategy.getAvailableFilter().filter(messageQueue)) {
                return 0;
            }
            return 10;
        });
        penalizers.add(messageQueue -> {
            if (!mqFaultStrategy.isSendLatencyFaultEnable() || mqFaultStrategy.getReachableFilter().filter(messageQueue)) {
                return 0;
            }
            return 100;
        });
        return penalizers;
    }
}
