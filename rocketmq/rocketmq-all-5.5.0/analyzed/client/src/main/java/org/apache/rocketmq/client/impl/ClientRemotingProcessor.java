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
package org.apache.rocketmq.client.impl;

import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.impl.producer.MQProducerInner;
import org.apache.rocketmq.client.producer.RequestFutureHolder;
import org.apache.rocketmq.client.producer.RequestResponseFuture;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.compression.Compressor;
import org.apache.rocketmq.common.compression.CompressorFactory;
import org.apache.rocketmq.common.message.MessageAccessor;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.sysflag.MessageSysFlag;
import org.apache.rocketmq.common.utils.NetworkUtil;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.netty.NettyRequestProcessor;
import org.apache.rocketmq.remoting.protocol.NamespaceUtil;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.protocol.body.GetConsumerStatusBody;
import org.apache.rocketmq.remoting.protocol.body.ResetOffsetBody;
import org.apache.rocketmq.remoting.protocol.header.CheckTransactionStateRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.ConsumeMessageDirectlyResultRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.GetConsumerRunningInfoRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.GetConsumerStatusRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.NotifyConsumerIdsChangedRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.ReplyMessageRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.ResetOffsetRequestHeader;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 客户端 Remoting 入站处理器：处理 Broker 主动下发的请求，
 * 包括事务回查、消费组变更通知、Offset 重置、Request-Reply 响应等。
 */
public class ClientRemotingProcessor implements NettyRequestProcessor {
    /** 日志记录器。 */
    private final Logger logger = LoggerFactory.getLogger(ClientRemotingProcessor.class);
    /** 所属 {@link MQClientInstance} 工厂。 */
    private final MQClientInstance mqClientFactory;

    /** 绑定客户端实例。 */
    public ClientRemotingProcessor(final MQClientInstance mqClientFactory) {
        this.mqClientFactory = mqClientFactory;
    }

    /** 按请求码分发到对应处理方法。 */
    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx,
        RemotingCommand request) throws RemotingCommandException {
        switch (request.getCode()) {
            case RequestCode.CHECK_TRANSACTION_STATE: // 事务状态回查
                return this.checkTransactionState(ctx, request);
            case RequestCode.NOTIFY_CONSUMER_IDS_CHANGED: // 消费实例列表变更
                return this.notifyConsumerIdsChanged(ctx, request);
            case RequestCode.RESET_CONSUMER_CLIENT_OFFSET: // 重置消费位点
                return this.resetOffset(ctx, request);
            case RequestCode.GET_CONSUMER_STATUS_FROM_CLIENT: // 查询消费进度（已废弃）
                return this.getConsumeStatus(ctx, request);

            case RequestCode.GET_CONSUMER_RUNNING_INFO: // 获取消费者运行信息
                return this.getConsumerRunningInfo(ctx, request);

            case RequestCode.CONSUME_MESSAGE_DIRECTLY: // 直接消费单条消息（运维）
                return this.consumeMessageDirectly(ctx, request);

            case RequestCode.PUSH_REPLY_MESSAGE_TO_CLIENT: // Request-Reply 响应推送
                return this.receiveReplyMessage(ctx, request);
            default:
                break;
        }
        return null;
    }

    /** 是否拒绝处理新请求；客户端始终返回 false。 */
    @Override
    public boolean rejectRequest() {
        return false;
    }

    /** 处理 Broker 发起的事务状态回查，委托对应 Producer 执行本地事务检查。 */
    public RemotingCommand checkTransactionState(ChannelHandlerContext ctx,
        RemotingCommand request) throws RemotingCommandException {
        final CheckTransactionStateRequestHeader requestHeader =
            (CheckTransactionStateRequestHeader) request.decodeCommandCustomHeader(CheckTransactionStateRequestHeader.class);
        final ByteBuffer byteBuffer = ByteBuffer.wrap(request.getBody());
        final MessageExt messageExt = MessageDecoder.decode(byteBuffer);
        if (messageExt != null) {
            if (StringUtils.isNotEmpty(this.mqClientFactory.getClientConfig().getNamespace())) {
                messageExt.setTopic(NamespaceUtil
                    .withoutNamespace(messageExt.getTopic(), this.mqClientFactory.getClientConfig().getNamespace()));
            }
            String transactionId = messageExt.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX);
            if (null != transactionId && !"".equals(transactionId)) {
                messageExt.setTransactionId(transactionId);
            }
            final String group = messageExt.getProperty(MessageConst.PROPERTY_PRODUCER_GROUP);
            if (group != null) {
                MQProducerInner producer = this.mqClientFactory.selectProducer(group);
                if (producer != null) {
                    final String addr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
                    producer.checkTransactionState(addr, messageExt, requestHeader);
                } else {
                    logger.debug("checkTransactionState, pick producer by group[{}] failed", group);
                }
            } else {
                logger.warn("checkTransactionState, pick producer group failed");
            }
        } else {
            logger.warn("checkTransactionState, decode message failed");
        }

        return null;
    }

    /** 消费组在线实例变更时触发立即 Rebalance。 */
    public RemotingCommand notifyConsumerIdsChanged(ChannelHandlerContext ctx,
        RemotingCommand request) throws RemotingCommandException {
        try {
            final NotifyConsumerIdsChangedRequestHeader requestHeader =
                (NotifyConsumerIdsChangedRequestHeader) request.decodeCommandCustomHeader(NotifyConsumerIdsChangedRequestHeader.class);
            logger.info("receive broker's notification[{}], the consumer group: {} changed, rebalance immediately",
                RemotingHelper.parseChannelRemoteAddr(ctx.channel()),
                requestHeader.getConsumerGroup());
            this.mqClientFactory.rebalanceImmediately();
        } catch (Exception e) {
            logger.error("notifyConsumerIdsChanged exception", UtilAll.exceptionSimpleDesc(e));
        }
        return null;
    }

    /** 按 Broker 指令重置指定 Topic/Group 的消费位点。 */
    public RemotingCommand resetOffset(ChannelHandlerContext ctx,
        RemotingCommand request) throws RemotingCommandException {
        final ResetOffsetRequestHeader requestHeader =
            (ResetOffsetRequestHeader) request.decodeCommandCustomHeader(ResetOffsetRequestHeader.class);
        logger.info("invoke reset offset operation from broker. brokerAddr={}, topic={}, group={}, timestamp={}",
            RemotingHelper.parseChannelRemoteAddr(ctx.channel()), requestHeader.getTopic(), requestHeader.getGroup(),
            requestHeader.getTimestamp());
        Map<MessageQueue, Long> offsetTable = new HashMap<>();
        if (request.getBody() != null) {
            ResetOffsetBody body = ResetOffsetBody.decode(request.getBody(), ResetOffsetBody.class);
            offsetTable = body.getOffsetTable();
        }
        this.mqClientFactory.resetOffset(requestHeader.getTopic(), requestHeader.getGroup(), offsetTable);
        return null;
    }

    /** 查询消费进度（已废弃，保留兼容）。 */
    @Deprecated
    public RemotingCommand getConsumeStatus(ChannelHandlerContext ctx,
        RemotingCommand request) throws RemotingCommandException {
        final RemotingCommand response = RemotingCommand.createResponseCommand(null);
        final GetConsumerStatusRequestHeader requestHeader =
            (GetConsumerStatusRequestHeader) request.decodeCommandCustomHeader(GetConsumerStatusRequestHeader.class);

        Map<MessageQueue, Long> offsetTable = this.mqClientFactory.getConsumerStatus(requestHeader.getTopic(), requestHeader.getGroup());
        GetConsumerStatusBody body = new GetConsumerStatusBody();
        body.setMessageQueueTable(offsetTable);
        response.setBody(body.encode());
        response.setCode(ResponseCode.SUCCESS);
        return response;
    }

    /** 返回消费者运行快照，可选附带 JVM 线程栈。 */
    private RemotingCommand getConsumerRunningInfo(ChannelHandlerContext ctx,
        RemotingCommand request) throws RemotingCommandException {
        final RemotingCommand response = RemotingCommand.createResponseCommand(null);
        final GetConsumerRunningInfoRequestHeader requestHeader =
            (GetConsumerRunningInfoRequestHeader) request.decodeCommandCustomHeader(GetConsumerRunningInfoRequestHeader.class);

        ConsumerRunningInfo consumerRunningInfo = this.mqClientFactory.consumerRunningInfo(requestHeader.getConsumerGroup());
        if (null != consumerRunningInfo) {
            if (requestHeader.isJstackEnable()) {
                Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
                String jstack = UtilAll.jstack(map);
                consumerRunningInfo.setJstack(jstack);
            }

            response.setCode(ResponseCode.SUCCESS);
            response.setBody(consumerRunningInfo.encode());
        } else {
            response.setCode(ResponseCode.SYSTEM_ERROR);
            response.setRemark(String.format("The Consumer Group <%s> not exist in this consumer", requestHeader.getConsumerGroup()));
        }

        return response;
    }

    /** 运维场景：绕过正常拉取流程直接消费单条消息。 */
    private RemotingCommand consumeMessageDirectly(ChannelHandlerContext ctx,
        RemotingCommand request) throws RemotingCommandException {
        final RemotingCommand response = RemotingCommand.createResponseCommand(null);
        final ConsumeMessageDirectlyResultRequestHeader requestHeader =
            (ConsumeMessageDirectlyResultRequestHeader) request
                .decodeCommandCustomHeader(ConsumeMessageDirectlyResultRequestHeader.class);

        final MessageExt msg = MessageDecoder.clientDecode(ByteBuffer.wrap(request.getBody()), true);

        ConsumeMessageDirectlyResult result =
            this.mqClientFactory.consumeMessageDirectly(msg, requestHeader.getConsumerGroup(), requestHeader.getBrokerName());

        if (null != result) {
            response.setCode(ResponseCode.SUCCESS);
            response.setBody(result.encode());
        } else {
            response.setCode(ResponseCode.SYSTEM_ERROR);
            response.setRemark(String.format("The Consumer Group <%s> not exist in this consumer", requestHeader.getConsumerGroup()));
        }

        return response;
    }

    /** 接收 Broker 推送的 Request-Reply 响应消息并匹配等待中的 Future。 */
    private RemotingCommand receiveReplyMessage(ChannelHandlerContext ctx,
        RemotingCommand request) throws RemotingCommandException {

        final RemotingCommand response = RemotingCommand.createResponseCommand(null);
        long receiveTime = System.currentTimeMillis();
        ReplyMessageRequestHeader requestHeader = (ReplyMessageRequestHeader) request.decodeCommandCustomHeader(ReplyMessageRequestHeader.class);

        try {
            MessageExt msg = new MessageExt();
            msg.setTopic(requestHeader.getTopic());
            msg.setQueueId(requestHeader.getQueueId());
            msg.setStoreTimestamp(requestHeader.getStoreTimestamp());

            if (requestHeader.getBornHost() != null) {
                msg.setBornHost(NetworkUtil.string2SocketAddress(requestHeader.getBornHost()));
            }

            if (requestHeader.getStoreHost() != null) {
                msg.setStoreHost(NetworkUtil.string2SocketAddress(requestHeader.getStoreHost()));
            }

            byte[] body = request.getBody();
            int sysFlag = requestHeader.getSysFlag();
            if ((sysFlag & MessageSysFlag.COMPRESSED_FLAG) == MessageSysFlag.COMPRESSED_FLAG) {
                try {
                    Compressor compressor = CompressorFactory.getCompressor(MessageSysFlag.getCompressionType(sysFlag));
                    body = compressor.decompress(body);
                } catch (IOException e) {
                    logger.warn("err when uncompress constant", e);
                }
            }
            msg.setBody(body);
            msg.setFlag(requestHeader.getFlag());
            MessageAccessor.setProperties(msg, MessageDecoder.string2messageProperties(requestHeader.getProperties()));
            MessageAccessor.putProperty(msg, MessageConst.PROPERTY_REPLY_MESSAGE_ARRIVE_TIME, String.valueOf(receiveTime));
            msg.setBornTimestamp(requestHeader.getBornTimestamp());
            msg.setReconsumeTimes(requestHeader.getReconsumeTimes() == null ? 0 : requestHeader.getReconsumeTimes());
            logger.debug("receive reply message :{}", msg);

            processReplyMessage(msg);

            response.setCode(ResponseCode.SUCCESS);
            response.setRemark(null);
        } catch (Exception e) {
            logger.warn("unknown err when receiveReplyMsg", e);
            response.setCode(ResponseCode.SYSTEM_ERROR);
            response.setRemark("process reply message fail");
        }
        return response;
    }

    /** 按 correlationId 将回复消息交给对应的 {@link RequestResponseFuture}。 */
    private void processReplyMessage(MessageExt replyMsg) {
        final String correlationId = replyMsg.getUserProperty(MessageConst.PROPERTY_CORRELATION_ID);
        final RequestResponseFuture requestResponseFuture = RequestFutureHolder.getInstance().getRequestFutureTable().get(correlationId);
        if (requestResponseFuture != null) {
            requestResponseFuture.putResponseMessage(replyMsg);

            RequestFutureHolder.getInstance().getRequestFutureTable().remove(correlationId);

            if (requestResponseFuture.getRequestCallback() != null) {
                requestResponseFuture.getRequestCallback().onSuccess(replyMsg);
            }
        } else {
            String bornHost = replyMsg.getBornHostString();
            logger.warn("receive reply message, but not matched any request, CorrelationId: {} , reply from host: {}",
                correlationId, bornHost);
        }
    }
}
