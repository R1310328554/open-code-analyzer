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

package com.alibaba.nacos.plugin.ai.pipeline.model;

import com.alibaba.nacos.plugin.ai.pipeline.spi.PublishPipelineService;

/**
 * 发布流水线所支持的 AI 资源类型枚举。
 *
 * <p>每种 AI 资源类型对应一个枚举值。流水线插件通过
 * {@link PublishPipelineService#pipelineResourceTypes()} 声明支持的类型，
 * {@code PublishPipelineManager} 再按资源类型路由到对应插件链。</p>
 *
 * @author mosong.lp
 * @since 3.2.0
 */
public enum PublishPipelineResourceType {
    
    /**
     * Skill 技能资源。
     */
    SKILL,
    
    /**
     * Prompt 提示词资源。
     */
    PROMPT,
    
    /**
     * AgentSpec Agent 规格资源。
     */
    AGENTSPEC
}
