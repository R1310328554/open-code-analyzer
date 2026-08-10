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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The Holder of {@link AgentIdCodec}.
 * <p>{@link AgentIdCodec} 的 Spring 持有者，无自定义 Bean 时默认 {@link AsciiAgentIdCodec}。</p>
 *
 * @author xiweng.yy
 */
@Component
public class AgentIdCodecHolder {
    
    /** 实际使用的 Agent ID 编解码器。 */
    private final AgentIdCodec agentIdCodec;
    
    public AgentIdCodecHolder(ObjectProvider<AgentIdCodec> agentIdCodecsProvider) {
        this.agentIdCodec = agentIdCodecsProvider.getIfAvailable(AsciiAgentIdCodec::new); // 缺省 ASCII 实现
    }
    
    /**
     * Encode agent name to identity.
     *
     * @param agentName agent name
     * @return identity encoded from agent name
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    public String encode(String agentName) {
        return agentIdCodec.encode(agentName);
    }
    
    /**
     * Encode agent name to identity for search, which means only do encode value without any prefix and suffix, used to do blur search.
     *
     * @param agentName agent name
     * @return identity encoded from agent name
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    public String encodeForSearch(String agentName) {
        return agentIdCodec.encodeForSearch(agentName);
    }
    
    /**
     * Decode agent id to agent name.
     *
     * @param agentId agent identity
     * @return agent name
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    public String decode(String agentId) {
        return agentIdCodec.decode(agentId);
    }
}
