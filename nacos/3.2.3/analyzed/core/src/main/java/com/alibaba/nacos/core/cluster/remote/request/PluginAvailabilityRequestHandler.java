/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.cluster.remote.request;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.api.remote.response.ResponseCode;
import com.alibaba.nacos.core.cluster.remote.response.PluginAvailabilityResponse;
import com.alibaba.nacos.core.plugin.PluginManager;
import com.alibaba.nacos.core.plugin.model.PluginInfo;
import com.alibaba.nacos.core.remote.RequestHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link PluginAvailabilityRequest} 处理器：通过 {@link PluginManager} 查询单插件或全部插件启用状态。
 * Handler for PluginAvailabilityRequest.
 *
 * @author WangzJi
 * @since 3.2.0
 */
@Component
@Since("3.2.0")
public class PluginAvailabilityRequestHandler
    extends RequestHandler<PluginAvailabilityRequest, PluginAvailabilityResponse> {
    
    /** 插件生命周期与可用性管理器。 */
    private final PluginManager pluginManager;
    
    /**
     * 注入插件管理器。
     *
     * @param pluginManager 插件管理器实例
     */
    public PluginAvailabilityRequestHandler(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }
    
    /**
     * 处理插件可用性查询：参数非法时返回 FAIL，否则按单插件或全量模式应答。
     *
     * @param request 查询请求
     * @param meta RPC 元数据
     * @return 可用性响应
     * @throws NacosException 处理异常
     */
    @Override
    public PluginAvailabilityResponse handle(PluginAvailabilityRequest request, RequestMeta meta)
        throws NacosException {
        if (!request.isQueryAll() && request.getPluginId() == null) {
            return createErrorResponse("Either queryAll must be true or pluginId must be provided");
        }
        
        if (request.isQueryAll()) {
            return handleQueryAllRequest();
        }
        
        return handleSinglePluginRequest(request.getPluginId());
    }
    
    /** 构造带错误消息的失败响应。 */
    private PluginAvailabilityResponse createErrorResponse(String message) {
        PluginAvailabilityResponse response = new PluginAvailabilityResponse();
        response.setResultCode(ResponseCode.FAIL.getCode());
        response.setMessage(message);
        return response;
    }
    
    /** 列出全部插件并返回 pluginId → 是否启用的映射。 */
    private PluginAvailabilityResponse handleQueryAllRequest() {
        List<PluginInfo> plugins = pluginManager.listAllPlugins();
        Map<String, Boolean> availabilityMap = new HashMap<>(plugins.size());
        plugins.forEach(pluginInfo -> {
            availabilityMap.put(pluginInfo.getPluginId(), pluginInfo.isEnabled());
        });
        
        PluginAvailabilityResponse response = new PluginAvailabilityResponse();
        response.setPluginAvailabilityMap(availabilityMap);
        response.setResultCode(ResponseCode.SUCCESS.getCode());
        return response;
    }
    
    /** 查询单个插件是否可用并写入响应。 */
    private PluginAvailabilityResponse handleSinglePluginRequest(String pluginId) {
        boolean available = pluginManager.isPluginAvailable(pluginId);
        
        PluginAvailabilityResponse response = new PluginAvailabilityResponse();
        response.setAvailable(available);
        response.setPluginId(pluginId);
        response.setResultCode(ResponseCode.SUCCESS.getCode());
        return response;
    }
}
