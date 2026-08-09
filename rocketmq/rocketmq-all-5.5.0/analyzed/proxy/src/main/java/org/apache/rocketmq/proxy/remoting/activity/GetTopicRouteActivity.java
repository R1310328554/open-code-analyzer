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

import com.alibaba.fastjson2.JSONWriter;
import com.google.common.net.HostAndPort;
import io.netty.channel.ChannelHandlerContext;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.proxy.common.Address;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.config.ConfigurationManager;
import org.apache.rocketmq.proxy.config.ProxyConfig;
import org.apache.rocketmq.proxy.processor.MessagingProcessor;
import org.apache.rocketmq.proxy.remoting.pipeline.RequestPipeline;
import org.apache.rocketmq.proxy.service.route.ProxyTopicRouteData;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.header.namesrv.GetRouteInfoRequestHeader;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;

import java.util.ArrayList;
import java.util.List;

/**
 * Topic 路由查询 Remoting Activity：返回 Proxy 视角的 {@link TopicRouteData}。
 */
public class GetTopicRouteActivity extends AbstractRemotingActivity {
    /** 构造路由查询 Activity。 */
    public GetTopicRouteActivity(RequestPipeline requestPipeline,
        MessagingProcessor messagingProcessor) {
        super(requestPipeline, messagingProcessor);
    }

    @Override
    protected RemotingCommand processRequest0(ChannelHandlerContext ctx, RemotingCommand request,
        ProxyContext context) throws Exception {
        // 读取 Proxy 配置以获取 Remoting 接入地址
        ProxyConfig proxyConfig = ConfigurationManager.getProxyConfig();
        final RemotingCommand response = RemotingCommand.createResponseCommand(null);
        final GetRouteInfoRequestHeader requestHeader =
            (GetRouteInfoRequestHeader) request.decodeCommandCustomHeader(GetRouteInfoRequestHeader.class);
        List<Address> addressList = new ArrayList<>();
        // AddressScheme 仅为占位符，不影响本场景下的路由结果
        addressList.add(new Address(HostAndPort.fromParts(proxyConfig.getRemotingAccessAddr(), proxyConfig.getRemotingListenPort())));
        // 从 NameServer/Broker 聚合 Topic 路由数据
        ProxyTopicRouteData proxyTopicRouteData = messagingProcessor.getTopicRouteDataForProxy(context, addressList, requestHeader.getTopic());
        // 转换为客户端可识别的 TopicRouteData
        TopicRouteData topicRouteData = proxyTopicRouteData.buildTopicRouteData();

        byte[] content;
        Boolean standardJsonOnly = requestHeader.getAcceptStandardJsonOnly();
        // 高版本或要求标准 JSON 时使用 BrowserCompatible 编码
        if (request.getVersion() >= MQVersion.Version.V4_9_4.ordinal() || null != standardJsonOnly && standardJsonOnly) {
            content = topicRouteData.encode(JSONWriter.Feature.BrowserCompatible, JSONWriter.Feature.MapSortField);
        } else {
            content = topicRouteData.encode();
        }

        response.setBody(content);
        response.setCode(ResponseCode.SUCCESS);
        response.setRemark(null);
        return response;
    }
}
