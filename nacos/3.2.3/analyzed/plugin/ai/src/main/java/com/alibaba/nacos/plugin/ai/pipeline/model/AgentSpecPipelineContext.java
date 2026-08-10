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

/**
 * AgentSpec 资源专用的发布流水线上下文。
 *
 * <p>继承 {@link ResourceFilesPipelineContext}，构造时自动将资源类型
 * 设为 {@link PublishPipelineResourceType#AGENTSPEC}，供 Agent 规格类
 * 多文件资源的发布与安全审计流程使用。</p>
 *
 * @author nacos
 */
public class AgentSpecPipelineContext extends ResourceFilesPipelineContext {
    
    /** 初始化 AgentSpec 类型的文件型流水线上下文。 */
    public AgentSpecPipelineContext() {
        setResourceType(PublishPipelineResourceType.AGENTSPEC);
    }
}
