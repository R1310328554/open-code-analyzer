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
package org.apache.rocketmq.broker.processor;

import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.broker.client.ConsumerGroupInfo;
import org.apache.rocketmq.broker.loadbalance.MessageRequestModeManager;
import org.apache.rocketmq.broker.topic.TopicRouteInfoManager;
import org.apache.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragelyByCircle;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.message.MessageQueueAssignment;
import org.apache.rocketmq.common.message.MessageRequestMode;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.netty.NettyRequestProcessor;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.body.QueryAssignmentRequestBody;
import org.apache.rocketmq.remoting.protocol.body.QueryAssignmentResponseBody;
import org.apache.rocketmq.remoting.protocol.body.SetMessageRequestModeRequestBody;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;

/**
 * 队列分配查询处理器：响应 QUERY_ASSIGNMENT / SET_MESSAGE_REQUEST_MODE，
 * 按消费模式与负载均衡策略为客户端计算 MessageQueue 分配结果。
 */
public class QueryAssignmentProcessor implements NettyRequestProcessor {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME);

    private final BrokerController brokerController;

    /** 负载均衡策略名 → 策略实例（平均分配、环形平均等）。 */
    private final ConcurrentHashMap<String, AllocateMessageQueueStrategy> name2LoadStrategy = new ConcurrentHashMap<>();

    private MessageRequestModeManager messageRequestModeManager;

    public QueryAssignmentProcessor(final BrokerController brokerController) {
        this.brokerController = brokerController;

        // 注册内置负载均衡策略（使用 Broker 日志而非 ClientLogger）
        AllocateMessageQueueAveragely allocateMessageQueueAveragely = new AllocateMessageQueueAveragely();
        name2LoadStrategy.put(allocateMessageQueueAveragely.getName(), allocateMessageQueueAveragely);
        AllocateMessageQueueAveragelyByCircle allocateMessageQueueAveragelyByCircle = new AllocateMessageQueueAveragelyByCircle();
        name2LoadStrategy.put(allocateMessageQueueAveragelyByCircle.getName(), allocateMessageQueueAveragelyByCircle);

        this.messageRequestModeManager = new MessageRequestModeManager(brokerController);
        this.messageRequestModeManager.load();
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx,
        RemotingCommand request) throws RemotingCommandException {
        switch (request.getCode()) {
            case RequestCode.QUERY_ASSIGNMENT:  // 查询客户端队列分配
                return this.queryAssignment(ctx, request);
            case RequestCode.SET_MESSAGE_REQUEST_MODE:  // 设置 Pull/Pop 请求模式
                return this.setMessageRequestMode(ctx, request);
            default:
                break;
        }
        return null;
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }

    /**
     *
     */
    private RemotingCommand queryAssignment(ChannelHandlerContext ctx, RemotingCommand request)
        throws RemotingCommandException {
        final QueryAssignmentRequestBody requestBody = QueryAssignmentRequestBody.decode(request.getBody(), QueryAssignmentRequestBody.class);
        final String topic = requestBody.getTopic();
        final String consumerGroup = requestBody.getConsumerGroup();
        final String clientId = requestBody.getClientId();
        final MessageModel messageModel = requestBody.getMessageModel();
        final String strategyName = requestBody.getStrategyName();

        final RemotingCommand response = RemotingCommand.createResponseCommand(null);
        final QueryAssignmentResponseBody responseBody = new QueryAssignmentResponseBody();

        SetMessageRequestModeRequestBody setMessageRequestModeRequestBody = this.messageRequestModeManager.getMessageRequestMode(topic, consumerGroup);

        if (setMessageRequestModeRequestBody == null) {
            setMessageRequestModeRequestBody = new SetMessageRequestModeRequestBody();
            setMessageRequestModeRequestBody.setTopic(topic);
            setMessageRequestModeRequestBody.setConsumerGroup(consumerGroup);

            if (topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
                // 重试 Topic 强制 Pull 模式
                setMessageRequestModeRequestBody.setMode(MessageRequestMode.PULL);
            } else {
                setMessageRequestModeRequestBody.setMode(brokerController.getBrokerConfig().getDefaultMessageRequestMode());
            }

            if (setMessageRequestModeRequestBody.getMode() == MessageRequestMode.POP) {
                setMessageRequestModeRequestBody.setPopShareQueueNum(brokerController.getBrokerConfig().getDefaultPopShareQueueNum());
            }
        }

        Set<MessageQueue> messageQueues = doLoadBalance(topic, consumerGroup, clientId, messageModel, strategyName, setMessageRequestModeRequestBody, ctx);

        Set<MessageQueueAssignment> assignments = null;
        if (messageQueues != null) {
            assignments = new HashSet<>();
            for (MessageQueue messageQueue : messageQueues) {
                MessageQueueAssignment messageQueueAssignment = new MessageQueueAssignment();
                messageQueueAssignment.setMessageQueue(messageQueue);
                if (setMessageRequestModeRequestBody != null) {
                    messageQueueAssignment.setMode(setMessageRequestModeRequestBody.getMode());
                }
                assignments.add(messageQueueAssignment);
            }
        }

        responseBody.setMessageQueueAssignments(assignments);
        response.setBody(responseBody.encode());
        response.setCode(ResponseCode.SUCCESS);
        response.setRemark(null);
        return response;
    }

    /**
     * 返回空集表示客户端应清空此前分配；null 表示无效结果，客户端应跳过更新。
     *
     * @param topic
     * @param consumerGroup
     * @param clientId
     * @param messageModel
     * @param strategyName
     * @return the MessageQueues assigned to this client
     */
    private Set<MessageQueue> doLoadBalance(final String topic, final String consumerGroup, final String clientId,
        final MessageModel messageModel, final String strategyName,
        SetMessageRequestModeRequestBody setMessageRequestModeRequestBody, final ChannelHandlerContext ctx) {
        Set<MessageQueue> assignedQueueSet = null;
        final TopicRouteInfoManager topicRouteInfoManager = this.brokerController.getTopicRouteInfoManager();

        switch (messageModel) {
            case BROADCASTING: {
                assignedQueueSet = topicRouteInfoManager.getTopicSubscribeInfo(topic);
                if (assignedQueueSet == null) {
                    log.warn("QueryLoad: no assignment for group[{}], the topic[{}] does not exist.", consumerGroup, topic);
                }
                break;
            }
            case CLUSTERING: {
                Set<MessageQueue> mqSet;
                if (MixAll.isLmq(topic)) {
                    mqSet = new HashSet<>();
                    mqSet.add(new MessageQueue(
                        topic, brokerController.getBrokerConfig().getBrokerName(), (int)MixAll.LMQ_QUEUE_ID));
                } else {
                    mqSet = topicRouteInfoManager.getTopicSubscribeInfo(topic);
                }
                if (null == mqSet) {
                    if (!topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
                        log.warn("QueryLoad: no assignment for group[{}], the topic[{}] does not exist.", consumerGroup, topic);
                    }
                    return null;
                }

                if (!brokerController.getBrokerConfig().isServerLoadBalancerEnable()) {
                    return mqSet;
                }

                List<String> cidAll = null;
                ConsumerGroupInfo consumerGroupInfo = this.brokerController.getConsumerManager().getConsumerGroupInfo(consumerGroup);
                if (consumerGroupInfo != null) {
                    cidAll = consumerGroupInfo.getAllClientId();
                }
                if (null == cidAll) {
                    log.warn("QueryLoad: no assignment for group[{}] topic[{}], get consumer id list failed", consumerGroup, topic);
                    return null;
                }

                List<MessageQueue> mqAll = new ArrayList<>();
                mqAll.addAll(mqSet);
                Collections.sort(mqAll);
                Collections.sort(cidAll);
                List<MessageQueue> allocateResult = null;

                try {
                    AllocateMessageQueueStrategy allocateMessageQueueStrategy = name2LoadStrategy.get(strategyName);
                    if (null == allocateMessageQueueStrategy) {
                        log.warn("QueryLoad: unsupported strategy [{}],  {}", strategyName, RemotingHelper.parseChannelRemoteAddr(ctx.channel()));
                        return null;
                    }

                    if (setMessageRequestModeRequestBody != null && setMessageRequestModeRequestBody.getMode() == MessageRequestMode.POP) {
                        allocateResult = allocate4Pop(allocateMessageQueueStrategy, consumerGroup, clientId, mqAll,
                            cidAll, setMessageRequestModeRequestBody.getPopShareQueueNum());

                    } else {
                        allocateResult = allocateMessageQueueStrategy.allocate(consumerGroup, clientId, mqAll, cidAll);
                    }
                } catch (Throwable e) {
                    log.error("QueryLoad: no assignment for group[{}] topic[{}], allocate message queue exception. strategy name: {}, ex: {}", consumerGroup, topic, strategyName, e);
                    return null;
                }

                assignedQueueSet = new HashSet<>();
                if (allocateResult != null) {
                    assignedQueueSet.addAll(allocateResult);
                }
                break;
            }
            default:
                break;
        }
        return assignedQueueSet;
    }

    /** Pop 模式下的队列分配：支持共享队列与 queueId=-1 的全队列 Pop。 */
    public List<MessageQueue> allocate4Pop(AllocateMessageQueueStrategy allocateMessageQueueStrategy,
        final String consumerGroup, final String clientId, List<MessageQueue> mqAll, List<String> cidAll,
        int popShareQueueNum) {

        List<MessageQueue> allocateResult;
        if (popShareQueueNum <= 0 || popShareQueueNum >= cidAll.size() - 1) {
            // popShareQueueNum 无效时，每个 client Pop 全部逻辑队列
            allocateResult = new ArrayList<>(mqAll.size());
            for (MessageQueue mq : mqAll) {
                // 必须新建 MessageQueue（queueId=-1），避免 AssignmentManager 缓存被修改
                MessageQueue newMq = new MessageQueue(mq.getTopic(), mq.getBrokerName(), -1);
                allocateResult.add(newMq);
            }

        } else {
            if (cidAll.size() <= mqAll.size()) {
                // Pop 模式下可共享后续 N 个 consumer 的队列分配
                allocateResult = allocateMessageQueueStrategy.allocate(consumerGroup, clientId, mqAll, cidAll);
                int index = cidAll.indexOf(clientId);
                if (index >= 0) {
                    for (int i = 1; i <= popShareQueueNum; i++) {
                        index++;
                        index = index % cidAll.size();
                        List<MessageQueue> tmp = allocateMessageQueueStrategy.allocate(consumerGroup, cidAll.get(index), mqAll, cidAll);
                        allocateResult.addAll(tmp);
                    }
                }
            } else {
                // consumer 数多于队列数时，保证每个 cid 至少分到一条队列
                allocateResult = allocate(consumerGroup, clientId, mqAll, cidAll);
            }
        }

        return allocateResult;
    }

    private List<MessageQueue> allocate(String consumerGroup, String currentCID, List<MessageQueue> mqAll,
        List<String> cidAll) {
        if (StringUtils.isBlank(currentCID)) {
            throw new IllegalArgumentException("currentCID is empty");
        }

        if (CollectionUtils.isEmpty(mqAll)) {
            throw new IllegalArgumentException("mqAll is null or mqAll empty");
        }
        if (CollectionUtils.isEmpty(cidAll)) {
            throw new IllegalArgumentException("cidAll is null or cidAll empty");
        }

        List<MessageQueue> result = new ArrayList<>();
        if (!cidAll.contains(currentCID)) {
            log.info("[BUG] ConsumerGroup: {} The consumerId: {} not in cidAll: {}",
                consumerGroup,
                currentCID,
                cidAll);
            return result;
        }

        int index = cidAll.indexOf(currentCID);
        result.add(mqAll.get(index % mqAll.size()));
        return result;
    }

    private RemotingCommand setMessageRequestMode(ChannelHandlerContext ctx,
        RemotingCommand request) throws RemotingCommandException {
        final RemotingCommand response = RemotingCommand.createResponseCommand(null);
        final SetMessageRequestModeRequestBody requestBody = SetMessageRequestModeRequestBody.decode(request.getBody(), SetMessageRequestModeRequestBody.class);

        final String topic = requestBody.getTopic();
        if (topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
            response.setCode(ResponseCode.NO_PERMISSION);
            response.setRemark("retry topic is not allowed to set mode");  // 重试 Topic 禁止切换请求模式
            return response;
        }
        TopicConfig topicConfig = this.brokerController.getTopicConfigManager().selectTopicConfig(topic);
        if (null == topicConfig) {
            response.setCode(ResponseCode.TOPIC_NOT_EXIST);
            response.setRemark("topic[" + topic + "] not exist");
            return response;
        }

        final String consumerGroup = requestBody.getConsumerGroup();
        SubscriptionGroupConfig groupConfig = this.brokerController.getSubscriptionGroupManager().findSubscriptionGroupConfig(consumerGroup);
        if (null == groupConfig) {
            response.setCode(ResponseCode.SUBSCRIPTION_GROUP_NOT_EXIST);
            response.setRemark("subscription group does not exist");
            return response;
        }

        this.messageRequestModeManager.setMessageRequestMode(topic, consumerGroup, requestBody);
        this.messageRequestModeManager.persist();

        response.setCode(ResponseCode.SUCCESS);
        response.setRemark(null);
        return response;
    }

    public MessageRequestModeManager getMessageRequestModeManager() {
        return messageRequestModeManager;
    }
}
