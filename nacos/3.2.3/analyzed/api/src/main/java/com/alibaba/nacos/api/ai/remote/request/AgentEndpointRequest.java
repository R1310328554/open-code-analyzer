/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ai.remote.request;

import com.alibaba.nacos.api.ai.model.a2a.AgentEndpoint;
import com.alibaba.nacos.api.ai.remote.AiRemoteConstants;

/**
 * 向 Nacos AI 模块注册或注销 Agent 端点的远程请求。
 *
 * <p>继承 {@link AbstractAgentRequest}，携带 {@link com.alibaba.nacos.api.ai.model.a2a.AgentEndpoint}
 * 及操作类型 {@link #type}，用于服务发现与健康检查场景。</p>
 *
 * @author xiweng.yy
 */
public class AgentEndpointRequest extends AbstractAgentRequest {
    
    /** Agent 端点信息（地址、端口、元数据等）。 */
    private AgentEndpoint endpoint;
    
    /** 操作类型，取值为 {@link AiRemoteConstants#REGISTER_ENDPOINT} 或 {@link AiRemoteConstants#DE_REGISTER_ENDPOINT}。 */
    private String type;
    
    /** 获取 Agent 端点信息。 */
    public AgentEndpoint getEndpoint() {
        return endpoint;
    }
    
    /** 设置 Agent 端点信息。 */
    public void setEndpoint(AgentEndpoint endpoint) {
        this.endpoint = endpoint;
    }
    
    /** 获取注册/注销操作类型。 */
    public String getType() {
        return type;
    }
    
    /** 设置注册/注销操作类型。 */
    public void setType(String type) {
        this.type = type;
    }
}
