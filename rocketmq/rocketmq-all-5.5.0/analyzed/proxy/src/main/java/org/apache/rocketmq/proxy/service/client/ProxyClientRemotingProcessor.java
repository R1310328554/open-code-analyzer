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
package org.apache.rocketmq.proxy.service.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.nio.ByteBuffer;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.broker.client.ClientChannelInfo;
import org.apache.rocketmq.broker.client.ProducerManager;
import org.apache.rocketmq.client.impl.ClientRemotingProcessor;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.utils.NetworkUtil;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.proxy.common.utils.ProxyUtils;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.protocol.header.CheckTransactionStateRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.NotifyUnsubscribeLiteRequestHeader;

/**
 * Proxy 侧客户端 Remoting 处理器：转发事务回查与 Lite 退订通知。
 */
public class ProxyClientRemotingProcessor extends ClientRemotingProcessor {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME);
    /** 生产者通道管理器，用于事务回查转发。 */
    private final ProducerManager producerManager;
    /** 消费者管理器，用于 Lite 退订通知转发。 */
    private final ClusterConsumerManager consumerManager;

    /** 注入生产者与消费者管理器。 */
    public ProxyClientRemotingProcessor(ProducerManager producerManager, ClusterConsumerManager consumerManager) {
        super(null);
        this.producerManager = producerManager;
        this.consumerManager = consumerManager;
    }

    @Override
    /** 按请求码分发至事务回查或 Lite 退订处理。 */
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
        throws RemotingCommandException {
        if (request.getCode() == RequestCode.CHECK_TRANSACTION_STATE) { // 事务状态回查
            return this.checkTransactionState(ctx, request);
        } else if (request.getCode() == RequestCode.NOTIFY_UNSUBSCRIBE_LITE) { // Lite 退订通知
            return this.notifyUnsubscribeLite(ctx, request);
        }
        return null;
    }

    @Override
    /** 解码消息并将事务回查请求转发至对应生产者通道。 */
    public RemotingCommand checkTransactionState(ChannelHandlerContext ctx,
        RemotingCommand request) throws RemotingCommandException {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(request.getBody());
        final MessageExt messageExt = MessageDecoder.decode(byteBuffer, true, false, false);
        if (messageExt != null) {
            final String group = messageExt.getProperty(MessageConst.PROPERTY_PRODUCER_GROUP);
            if (group != null) {
                CheckTransactionStateRequestHeader requestHeader =
                    (CheckTransactionStateRequestHeader) request.decodeCommandCustomHeader(CheckTransactionStateRequestHeader.class);
                request.writeCustomHeader(requestHeader);
                request.addExtField(ProxyUtils.BROKER_ADDR, NetworkUtil.socketAddress2String(ctx.channel().remoteAddress()));
                // 查找生产者组可用通道并转发
                Channel channel = this.producerManager.getAvailableChannel(group);
                if (channel != null) {
                    channel.writeAndFlush(request);
                } else {
                    log.warn("check transaction failed, channel is empty. groupId={}, requestHeader:{}", group, requestHeader);
                }
            }
        }
        return null;
    }

    /**
     * 单向通知，返回 null 响应
     */
    /** 将 Lite 退订通知转发至目标消费者通道。 */
    public RemotingCommand notifyUnsubscribeLite(ChannelHandlerContext ctx,
        RemotingCommand request) throws RemotingCommandException {
        NotifyUnsubscribeLiteRequestHeader requestHeader =
            request.decodeCommandCustomHeader(NotifyUnsubscribeLiteRequestHeader.class);
        request.writeCustomHeader(requestHeader);
        final String clientId = requestHeader.getClientId();
        final String group = requestHeader.getConsumerGroup();
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(group)) {
            log.warn("notifyUnsubscribeLite clientId or group is null. {}", requestHeader);
            return null;
        }
        // 按消费组与 clientId 定位消费者通道
        ClientChannelInfo channelInfo = consumerManager.findChannel(group, clientId);
        if (channelInfo == null) {
            log.warn("notifyUnsubscribeLite channelInfo is null. {}", requestHeader);
            return null;
        }
        Channel channel = channelInfo.getChannel();
        if (channel == null) {
            log.warn("notifyUnsubscribeLite channel is null. {}", requestHeader);
            return null;
        }
        channel.writeAndFlush(request);
        return null;
    }
}
