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
import com.alibaba.nacos.copilot.model.PromptDebugRequest;
import com.alibaba.nacos.copilot.model.PromptDebugResponse;

/**
 * Prompt 调试服务接口：以用户自定义 Prompt 为系统提示词，流式返回模型推理与回复。
 * Prompt debug service interface.
 *
 * @author nacos
 */
public interface PromptDebugService {
    
    /**
     * 流式调试 Prompt：将 request 中的 Prompt 作为系统提示词、用户输入作为 user 消息，返回包含思考过程在内的完整模型响应。
     *
     * @param request  调试请求，含 Prompt 与用户输入
     * @param callback 流式响应回调
     */
    void debugPromptStream(PromptDebugRequest request,
        StreamResponseCallback<PromptDebugResponse> callback);
}
