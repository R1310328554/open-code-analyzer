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

package org.apache.rocketmq.client.rpchook;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * RPC 钩子：在出站 Remoting 请求头中注入 namespace 信息，
 * 供多租户/逻辑隔离场景下的 Broker 路由识别。
 */
public class NamespaceRpcHook implements RPCHook {
    /** 客户端配置，读取 namespaceV2。 */
    private final ClientConfig clientConfig;

    /** 绑定客户端配置。 */
    public NamespaceRpcHook(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    /** 若配置了 namespaceV2，在请求扩展字段中标记并携带 namespace。 */
    @Override
    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {
        if (StringUtils.isNotEmpty(clientConfig.getNamespaceV2())) {
            request.addExtField(MixAll.RPC_REQUEST_HEADER_NAMESPACED_FIELD, "true");
            request.addExtField(MixAll.RPC_REQUEST_HEADER_NAMESPACE_FIELD, clientConfig.getNamespaceV2());
        }
    }

    /** 响应后无额外处理（占位实现）。 */
    @Override
    public void doAfterResponse(String remoteAddr, RemotingCommand request,
        RemotingCommand response) {

    }
}
