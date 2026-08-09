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

package org.apache.rocketmq.proxy.remoting.activity;

import io.netty.channel.ChannelHandlerContext;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.header.EndTransactionRequestHeader;
import org.apache.rocketmq.common.sysflag.MessageSysFlag;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.processor.MessagingProcessor;
import org.apache.rocketmq.proxy.processor.TransactionStatus;
import org.apache.rocketmq.proxy.remoting.pipeline.RequestPipeline;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * 事务消息 Remoting Activity：处理提交/回滚决议并通知 {@link MessagingProcessor}。
 */
public class TransactionActivity extends AbstractRemotingActivity {

    /** 构造事务 Activity。 */
    public TransactionActivity(RequestPipeline requestPipeline,
        MessagingProcessor messagingProcessor) {
        super(requestPipeline, messagingProcessor);
    }

    @Override
    protected RemotingCommand processRequest0(ChannelHandlerContext ctx, RemotingCommand request,
        ProxyContext context) throws Exception {
        RemotingCommand response = RemotingCommand.createResponseCommand(null);
        response.setCode(ResponseCode.SUCCESS);
        response.setRemark(null);

        // 解析事务结束请求头
        final EndTransactionRequestHeader requestHeader = (EndTransactionRequestHeader) request.decodeCommandCustomHeader(EndTransactionRequestHeader.class);

        // 默认未知状态，按 commitOrRollback 字段解析
        TransactionStatus transactionStatus = TransactionStatus.UNKNOWN;
        switch (requestHeader.getCommitOrRollback()) {
            case MessageSysFlag.TRANSACTION_COMMIT_TYPE: // 提交
                transactionStatus = TransactionStatus.COMMIT;
                break;
            case MessageSysFlag.TRANSACTION_ROLLBACK_TYPE: // 回滚
                transactionStatus = TransactionStatus.ROLLBACK;
                break;
            default:
                break;
        }

        // 委托处理器完成事务二阶段
        this.messagingProcessor.endTransaction(
            context,
            requestHeader.getTopic(),
            requestHeader.getTransactionId(),
            requestHeader.getMsgId(),
            requestHeader.getProducerGroup(),
            transactionStatus,
            requestHeader.getFromTransactionCheck()
        );
        return response;
    }
}
