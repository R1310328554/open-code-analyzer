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

package com.alibaba.nacos.copilot.service;

import com.alibaba.nacos.copilot.adapter.StreamResponseCallback;
import com.alibaba.nacos.copilot.model.SkillOptimizationRequest;
import com.alibaba.nacos.copilot.model.SkillOptimizationResponse;

/**
 * Skill 优化服务接口：针对指定 Skill 文件流式返回优化建议与结果。
 * Skill optimization service interface.
 *
 * @author nacos
 */
public interface SkillOptimizationService {
    
    /**
     * 流式优化 Skill，支持对话历史、MCP 工具与优化目标等上下文。
     *
     * @param request  优化请求
     * @param callback 流式响应回调
     */
    void optimizeSkillStream(SkillOptimizationRequest request,
        StreamResponseCallback<SkillOptimizationResponse> callback);
}
