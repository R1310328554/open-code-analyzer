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

package com.alibaba.nacos.ai.service.a2a.identity;

/**
 * Nacos AI module A2A（Agent & AgentCard）identity Codec.
 *
 * <p>
 *     Agent and AgentCard allow user custom agent name without limit for now, but no limit means out of control and might cause un-expected behavior.
 *     So when storage in Nacos, it should be match some word limits.
 *     We need to encode and decode agent name as the identity to do storage.
 * </p>
 * <p>Agent 名称编解码 SPI：存储前将用户自定义名称编码为符合 Config 规则的 identity。</p>
 *
 * @author xiweng.yy
 */
public interface AgentIdCodec {
    
    /**
     * Encode agent name to identity.
     * <p>将 Agent 名称编码为存储用 identity（含必要前缀）。</p>
     *
     * @param agentName agent name
     * @return identity encoded from agent name
     */
    String encode(String agentName);
    
    /**
     * Encode agent name to identity for search, which means only do encode value without any prefix and suffix, used to do blur search.
     * <p>为模糊搜索编码名称：仅编码主体、不含前缀后缀。</p>
     *
     * @param agentName agent name
     * @return identity encoded from agent name
     */
    String encodeForSearch(String agentName);
    
    /**
     * Decode agent id to agent name.
     * <p>将 storage identity 解码还原为 Agent 名称。</p>
     *
     * @param agentId agent identity
     * @return agent name
     */
    String decode(String agentId);
}
