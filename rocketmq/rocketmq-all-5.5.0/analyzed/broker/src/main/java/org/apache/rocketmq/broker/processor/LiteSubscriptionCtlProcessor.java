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
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.broker.lite.LiteSubscriptionRegistry;
import org.apache.rocketmq.broker.lite.LiteQuotaException;
import org.apache.rocketmq.broker.lite.LiteMetadataUtil;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.lite.LiteSubscriptionDTO;
import org.apache.rocketmq.common.lite.LiteUtil;
import org.apache.rocketmq.remoting.netty.NettyRequestProcessor;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.body.LiteSubscriptionCtlRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lite 订阅控制处理器：处理客户端增量/全量订阅注册与注销，
 * 维护 Lite topic 到 LMQ 的映射关系。
 */
public class LiteSubscriptionCtlProcessor implements NettyRequestProcessor {
    protected final Logger log = LoggerFactory.getLogger(LoggerName.ROCKETMQ_POP_LITE_LOGGER_NAME);

    private final BrokerController brokerController;
    private final LiteSubscriptionRegistry liteSubscriptionRegistry;

    /** @param brokerController Broker 控制器；@param liteSubscriptionRegistry Lite 订阅注册表 */
    public LiteSubscriptionCtlProcessor(BrokerController brokerController, LiteSubscriptionRegistry liteSubscriptionRegistry) {
        this.brokerController = brokerController;
        this.liteSubscriptionRegistry = liteSubscriptionRegistry;
    }

    @Override
    /** 按 action 分发 PARTIAL/COMPLETE 的 ADD/REMOVE 订阅操作。 */
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
        if (request.getBody() == null) {
            return RemotingCommand.createResponseCommand(ResponseCode.ILLEGAL_OPERATION,
                "Request body is null.");
        }

        final LiteSubscriptionCtlRequestBody requestBody = LiteSubscriptionCtlRequestBody
            .decode(request.getBody(), LiteSubscriptionCtlRequestBody.class);

        Set<LiteSubscriptionDTO> entrySet = requestBody.getSubscriptionSet();
        if (CollectionUtils.isEmpty(entrySet)) {
            return RemotingCommand.createResponseCommand(ResponseCode.ILLEGAL_OPERATION,
                "LiteSubscriptionCtlRequestBody is empty.");
        }

        try {
            for (LiteSubscriptionDTO entry : entrySet) {
                final String clientId = entry.getClientId();
                final String group = entry.getGroup();
                final String topic = entry.getTopic();
                if (StringUtils.isBlank(clientId)) {
                    log.warn("clientId is blank, {}", entry);
                    continue;
                }
                if (StringUtils.isBlank(group)) {
                    log.warn("group is blank, {}", entry);
                    continue;
                }
                if (StringUtils.isBlank(topic)) {
                    log.warn("topic is blank, {}", entry);
                    continue;
                }
                final Set<String> lmqNameSet = toLmqNameSet(entry);
                switch (entry.getAction()) {
                    case PARTIAL_ADD:
                        checkConsumeEnable(group);
                        this.liteSubscriptionRegistry.updateClientChannel(clientId, ctx.channel());
                        this.liteSubscriptionRegistry.addPartialSubscription(clientId, group, topic, lmqNameSet, entry.getOffsetOption());
                        break;
                    case PARTIAL_REMOVE:
                        this.liteSubscriptionRegistry.removePartialSubscription(clientId, group, topic, lmqNameSet);
                        break;
                    case COMPLETE_ADD:
                        checkConsumeEnable(group);
                        this.liteSubscriptionRegistry.updateClientChannel(clientId, ctx.channel());
                        this.liteSubscriptionRegistry.addCompleteSubscription(clientId, group, topic, lmqNameSet,
                            entry.getVersion());
                        break;
                    case COMPLETE_REMOVE:
                        this.liteSubscriptionRegistry.removeCompleteSubscription(clientId);
                        break;
                }
            }
            return RemotingCommand.createResponseCommand(ResponseCode.SUCCESS, null);
        } catch (LiteQuotaException e) {
            return RemotingCommand.createResponseCommand(ResponseCode.LITE_SUBSCRIPTION_QUOTA_EXCEEDED, e.toString());
        } catch (IllegalStateException e) {
            return RemotingCommand.createResponseCommand(ResponseCode.ILLEGAL_OPERATION, e.toString());
        } catch (Exception e) {
            log.error("LiteSubscriptionCtlProcessor error", e);
            return RemotingCommand.createResponseCommand(ResponseCode.SYSTEM_ERROR, e.toString());
        }
    }

    /** 校验消费组是否允许消费，否则抛出 IllegalStateException。 */
    private void checkConsumeEnable(String group) {
        if (!LiteMetadataUtil.isConsumeEnable(group, brokerController)) {
            throw new IllegalStateException("Consumer group is not allowed to consume.");
        }
    }

    /** 将 Lite topic 集合转换为 LMQ 全名集合。 */
    private Set<String> toLmqNameSet(LiteSubscriptionDTO liteSubscriptionDTO) {
        if (CollectionUtils.isEmpty(liteSubscriptionDTO.getLiteTopicSet())) {
            return Collections.emptySet();
        }
        return liteSubscriptionDTO.getLiteTopicSet().stream()
            .map(liteTopic -> LiteUtil.toLmqName(liteSubscriptionDTO.getTopic(), liteTopic))
            .collect(Collectors.toSet());
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }

}
