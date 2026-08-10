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

package com.alibaba.nacos.api.ai.remote.response;

import com.alibaba.nacos.api.ai.remote.AiRemoteConstants;
import com.alibaba.nacos.api.remote.response.Response;

/**
 * Agent 端点注册/注销操作的远程响应。
 *
 * <p>继承 {@link com.alibaba.nacos.api.remote.response.Response}，
 * {@link #type} 标识本次响应对应的请求类型。</p>
 *
 * @author xiweng.yy
 */
public class AgentEndpointResponse extends Response {
    
    /** 响应对应的请求类型，取值为 {@link AiRemoteConstants#REGISTER_ENDPOINT}、{@link AiRemoteConstants#DE_REGISTER_ENDPOINT} 或 {@link AiRemoteConstants#BATCH_REGISTER_ENDPOINT}。 */
    private String type;
    
    /** 获取响应对应的请求类型。 */
    public String getType() {
        return type;
    }
    
    /** 设置响应对应的请求类型。 */
    public void setType(String type) {
        this.type = type;
    }
}
