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

package com.alibaba.nacos.ai.pipeline.model;

import java.util.Properties;

/**
 * Configuration for a single pipeline node, containing the node ID and custom properties.
 * <p>单个流水线节点配置，包含节点 ID 与自定义属性。</p>
 *
 * @author kiro
 * @since 3.2.0
 */
public class PipelineNodeConfig {
    
    /** 节点 ID，对应 {@code PublishPipelineService.pipelineId()}。 */
    /** Node ID, corresponding to {@code PublishPipelineService.pipelineId()}.
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    private String pipelineId;
    
    /** 节点自定义配置属性（如 endpoint、timeout）。 */
    /** Custom configuration properties for this node (e.g. endpoint, timeout).
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    private Properties properties;
    
    public PipelineNodeConfig() {
    }
    
    public String getPipelineId() {
        return pipelineId;
    }
    
    public void setPipelineId(String pipelineId) {
        this.pipelineId = pipelineId;
    }
    
    public Properties getProperties() {
        return properties;
    }
    
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
