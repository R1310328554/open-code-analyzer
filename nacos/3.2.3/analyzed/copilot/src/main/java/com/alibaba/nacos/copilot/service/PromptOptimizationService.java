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
import com.alibaba.nacos.copilot.model.PromptOptimizationRequest;
import com.alibaba.nacos.copilot.model.PromptOptimizationResponse;

/**
 * Prompt 优化服务接口：基于 Copilot 对现有 Prompt 进行结构化改进并流式返回结果。
 * Prompt optimization service interface.
 *
 * @author nacos
 */
public interface PromptOptimizationService {
    
    /**
     * 流式优化 Prompt，按分片推送优化过程与结果。
     *
     * @param request  优化请求，含原始 Prompt 与优化目标
     * @param callback 流式响应回调
     */
    void optimizePromptStream(PromptOptimizationRequest request,
        StreamResponseCallback<PromptOptimizationResponse> callback);
}
