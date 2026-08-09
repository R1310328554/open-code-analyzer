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
package org.apache.rocketmq.proxy.remoting.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.common.utils.NetworkUtil;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.processor.channel.ChannelProtocolType;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.netty.AttributeKeys;
import org.apache.rocketmq.remoting.protocol.LanguageCode;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * 上下文初始化流水线：从 Netty 通道属性填充 {@link ProxyContext}。
 */
public class ContextInitPipeline implements RequestPipeline {

    @Override
    public void execute(ChannelHandlerContext ctx, RemotingCommand request, ProxyContext context) throws Exception {
        // 读取 Netty 通道上缓存的客户端属性
        Channel channel = ctx.channel();
        // 客户端语言代码
        LanguageCode languageCode = RemotingHelper.getAttributeValue(AttributeKeys.LANGUAGE_CODE_KEY, channel);
        // 客户端唯一标识
        String clientId = RemotingHelper.getAttributeValue(AttributeKeys.CLIENT_ID_KEY, channel);
        Integer version = RemotingHelper.getAttributeValue(AttributeKeys.VERSION_KEY, channel);
        // 填充动作名、协议类型、通道与地址信息
        context.setAction(RemotingHelper.getRequestCodeDesc(request.getCode()))
            .setProtocolType(ChannelProtocolType.REMOTING.getName())
            .setChannel(channel)
            .setLocalAddress(NetworkUtil.socketAddress2String(ctx.channel().localAddress()))
            .setRemoteAddress(RemotingHelper.parseChannelRemoteAddr(ctx.channel()));
        if (languageCode != null) {
            // 写入客户端语言
            context.setLanguage(languageCode.name());
        }
        if (clientId != null) {
            context.setClientID(clientId);
        }
        if (version != null) {
            // 写入客户端 MQ 版本描述
            context.setClientVersion(MQVersion.getVersionDesc(version));
        }
    }
}
