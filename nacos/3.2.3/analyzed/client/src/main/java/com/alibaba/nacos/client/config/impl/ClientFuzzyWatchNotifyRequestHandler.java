/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.client.config.impl;

import com.alibaba.nacos.api.config.remote.request.ConfigFuzzyWatchChangeNotifyRequest;
import com.alibaba.nacos.api.config.remote.request.ConfigFuzzyWatchSyncRequest;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.common.remote.client.Connection;
import com.alibaba.nacos.common.remote.client.ServerRequestHandler;

/**
 * 服务端模糊监听推送请求处理器。
 *
 * <p>处理 {@link ConfigFuzzyWatchSyncRequest}（差异对账同步）与 {@link ConfigFuzzyWatchChangeNotifyRequest}（单条配置变更/删除通知），委托 {@link ConfigFuzzyWatchGroupKeyHolder} 更新本地模糊监听上下文。</p>
 *
 * @author shiyiyue
 */
public class ClientFuzzyWatchNotifyRequestHandler implements ServerRequestHandler {
    
    /** 模糊监听上下文持有者，负责实际处理逻辑。 */
    ConfigFuzzyWatchGroupKeyHolder configFuzzyWatchGroupKeyHolder;
    
    public ClientFuzzyWatchNotifyRequestHandler(
        ConfigFuzzyWatchGroupKeyHolder configFuzzyWatchGroupKeyHolder) {
        
        this.configFuzzyWatchGroupKeyHolder = configFuzzyWatchGroupKeyHolder;
    }
    
    @Override
    public Response requestReply(Request request, Connection connection) {
        // 模糊监听差异对账同步请求
        if (request instanceof ConfigFuzzyWatchSyncRequest) {
            return configFuzzyWatchGroupKeyHolder.handleFuzzyWatchSyncNotifyRequest(
                (ConfigFuzzyWatchSyncRequest) request);
        }
        // 单条配置变更/删除的模糊监听推送
        if (request instanceof ConfigFuzzyWatchChangeNotifyRequest) {
            return configFuzzyWatchGroupKeyHolder.handlerFuzzyWatchChangeNotifyRequest(
                (ConfigFuzzyWatchChangeNotifyRequest) request);
        }
        return null;
    }
}
