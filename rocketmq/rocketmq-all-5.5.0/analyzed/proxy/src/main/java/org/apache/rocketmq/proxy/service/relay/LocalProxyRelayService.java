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
package org.apache.rocketmq.proxy.service.relay;

import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.service.channel.SimpleChannel;
import org.apache.rocketmq.proxy.service.transaction.TransactionService;
import org.apache.rocketmq.remoting.RemotingServer;
import org.apache.rocketmq.remoting.netty.NettyRemotingAbstract;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.protocol.header.ConsumeMessageDirectlyResultRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.GetConsumerRunningInfoRequestHeader;

/**
 * 本地模式 Proxy 中继服务：将管理请求写回内嵌 Broker Remoting 层。
 */
public class LocalProxyRelayService extends AbstractProxyRelayService {

    /** 内嵌 {@link BrokerController} 实例。 */
    private final BrokerController brokerController;

    /** 绑定本地 Broker 与事务服务。 */
    public LocalProxyRelayService(BrokerController brokerController, TransactionService transactionService) {
        super(transactionService);
        this.brokerController = brokerController;
    }

    @Override
    /** 异步获取消费者运行信息并通过 Remoting 回写响应。 */
    public CompletableFuture<ProxyRelayResult<ConsumerRunningInfo>> processGetConsumerRunningInfo(
        ProxyContext context, RemotingCommand command, GetConsumerRunningInfoRequestHeader header) {
        CompletableFuture<ProxyRelayResult<ConsumerRunningInfo>> future = new CompletableFuture<>();
        future.thenAccept(proxyOutResult -> {
            RemotingServer remotingServer = this.brokerController.getRemotingServer();
            if (remotingServer instanceof NettyRemotingAbstract) {
                NettyRemotingAbstract nettyRemotingAbstract = (NettyRemotingAbstract) remotingServer;
                RemotingCommand remotingCommand = RemotingCommand.createResponseCommand(null);
                remotingCommand.setOpaque(command.getOpaque());
                remotingCommand.setCode(proxyOutResult.getCode());
                remotingCommand.setRemark(proxyOutResult.getRemark());
                if (proxyOutResult.getCode() == ResponseCode.SUCCESS && proxyOutResult.getResult() != null) {
                    ConsumerRunningInfo consumerRunningInfo = proxyOutResult.getResult();
                    remotingCommand.setBody(consumerRunningInfo.encode());
                }
                SimpleChannel simpleChannel = new SimpleChannel(context.getRemoteAddress(), context.getLocalAddress());
                nettyRemotingAbstract.processResponseCommand(simpleChannel.getChannelHandlerContext(), remotingCommand);
            }
        });
        return future;
    }

    @Override
    /** 异步触发直接消费并将结果编码写回 Remoting 响应。 */
    public CompletableFuture<ProxyRelayResult<ConsumeMessageDirectlyResult>> processConsumeMessageDirectly(
        ProxyContext context, RemotingCommand command,
        ConsumeMessageDirectlyResultRequestHeader header) {
        CompletableFuture<ProxyRelayResult<ConsumeMessageDirectlyResult>> future = new CompletableFuture<>();
        future.thenAccept(proxyOutResult -> {
            RemotingServer remotingServer = this.brokerController.getRemotingServer();
            if (remotingServer instanceof NettyRemotingAbstract) {
                NettyRemotingAbstract nettyRemotingAbstract = (NettyRemotingAbstract) remotingServer;
                RemotingCommand remotingCommand = RemotingCommand.createResponseCommand(null);
                remotingCommand.setOpaque(command.getOpaque());
                remotingCommand.setCode(proxyOutResult.getCode());
                remotingCommand.setRemark(proxyOutResult.getRemark());
                if (proxyOutResult.getCode() == ResponseCode.SUCCESS && proxyOutResult.getResult() != null) {
                    ConsumeMessageDirectlyResult consumeMessageDirectlyResult = proxyOutResult.getResult();
                    remotingCommand.setBody(consumeMessageDirectlyResult.encode());
                }
                SimpleChannel simpleChannel = new SimpleChannel(context.getRemoteAddress(), context.getLocalAddress());
                nettyRemotingAbstract.processResponseCommand(simpleChannel.getChannelHandlerContext(), remotingCommand);
            }
        });
        return future;
    }
}
