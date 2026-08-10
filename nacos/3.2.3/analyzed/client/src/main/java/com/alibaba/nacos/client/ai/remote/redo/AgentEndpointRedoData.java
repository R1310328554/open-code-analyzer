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

package com.alibaba.nacos.client.ai.remote.redo;

import com.alibaba.nacos.client.redo.data.RedoData;

import java.util.Objects;

/**
 * Agent 端点重做数据。
 *
 * <p>继承 {@link RedoData}，绑定 agentName 与 {@link AgentEndpointWrapper}，供 {@link AiGrpcRedoService} 在连接恢复后重试注册/注销操作。</p>
 *
 * @author xiweng.yy
 */
public class AgentEndpointRedoData extends RedoData<AgentEndpointWrapper> {
    
    /** Agent 名称。 */
    private final String agentName;
    
    /**
     * 构造 Agent 端点重做数据。
     *
     * @param agentName     Agent 名称
     * @param agentEndpoint 端点包装对象
     */
        this.agentName = agentName;
        this.set(agentEndpoint);
    }
    
    /** 返回 Agent 名称。 */
    public String getAgentName() {
        return agentName;
    }
    
    @Override
    /** 基于 agentName 与父类状态判断相等性。 */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AgentEndpointRedoData that = (AgentEndpointRedoData) o;
        return Objects.equals(agentName, that.agentName) && super.equals(o);
    }
    
    @Override
    /** 返回哈希码。 */
    public int hashCode() {
        return Objects.hash(super.hashCode(), agentName);
    }
}
