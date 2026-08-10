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

import java.util.List;

/**
 * Pipeline global configuration, containing enabled flag and ordered node list.
 * <p>流水线全局配置，包含启用开关与有序节点列表。</p>
 *
 * @author kiro
 * @since 3.2.0
 */
public class PipelineConfig {
    
    /** 流水线是否全局启用。 */
    /** Whether the pipeline is globally enabled.
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    private boolean enabled;
    
    /** 有序流水线节点配置列表。 */
    /** Ordered list of pipeline node configurations.
      * <p>Nacos AI 模块 API；详见上方英文说明。</p>
     */
    private List<PipelineNodeConfig> nodes;
    
    public PipelineConfig() {
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public List<PipelineNodeConfig> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<PipelineNodeConfig> nodes) {
        this.nodes = nodes;
    }
}
