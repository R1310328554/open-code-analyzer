/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.client.ai.remote.redo;

import com.alibaba.nacos.api.ai.model.a2a.AgentEndpoint;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * {@link AgentEndpoint} 单条与批量端点的统一包装类。
 *
 * <p>通过 {@link #isBatch()} 区分单端点注册与批量注册场景，供重做服务缓存并在连接恢复后重放。</p>
 *
 * @author xiweng.yy
 */
public class AgentEndpointWrapper {
    
    /** 端点数据集合（单条或批量）。 */
    private final Collection<AgentEndpoint> data;
    
    /** 是否为批量端点模式。 */
    private final boolean isBatch;
    
    /** 私有构造，通过 {@link #wrap} 工厂方法创建。 */
    private AgentEndpointWrapper(Collection<AgentEndpoint> data, boolean isBatch) {
        this.data = data;
        this.isBatch = isBatch;
    }
    
    /** 包装单个 Agent 端点。 */
    public static AgentEndpointWrapper wrap(AgentEndpoint data) {
        return new AgentEndpointWrapper(Collections.singletonList(data), false);
    }
    
    /** 包装批量 Agent 端点集合。 */
    public static AgentEndpointWrapper wrap(Collection<AgentEndpoint> data) {
        return new AgentEndpointWrapper(data, true);
    }
    
    /** 返回是否为批量模式。 */
    public boolean isBatch() {
        return isBatch;
    }
    
    /** 返回单个端点（批量模式下抛出异常）。 */
    public AgentEndpoint getData() {
        if (isBatch) {
            throw new UnsupportedOperationException("Can't get single data from batched data.");
        }
        return data.iterator().next();
    }
    
    /** 返回批量端点集合（单条模式下抛出异常）。 */
    public Collection<AgentEndpoint> getBatchData() {
        if (!isBatch) {
            throw new UnsupportedOperationException("Can't get batched data from single data.");
        }
        return data;
    }
    
    @Override
    /** 基于 isBatch 与 data 判断相等性。 */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgentEndpointWrapper that = (AgentEndpointWrapper) o;
        return isBatch == that.isBatch && Objects.equals(data, that.data);
    }
    
    @Override
    /** 返回哈希码。 */
    public int hashCode() {
        return Objects.hash(data, isBatch);
    }
}
