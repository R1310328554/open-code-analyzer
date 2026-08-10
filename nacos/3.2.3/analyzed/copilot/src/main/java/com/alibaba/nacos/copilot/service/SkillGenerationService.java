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
import com.alibaba.nacos.copilot.model.SkillGenerationRequest;
import com.alibaba.nacos.copilot.model.SkillGenerationResponse;

/**
 * Skill 生成服务接口：根据背景信息与对话历史生成符合 Agent Skill 规范的 Skill。
 * Skill generation service interface.
 *
 * @author nacos
 */
public interface SkillGenerationService {
    
    /**
     * 同步生成 Skill：阻塞等待模型输出并解析 JSON 结果。
     *
     * @param request 生成请求
     * @return 含 Skill 对象的生成响应
     */
    SkillGenerationResponse generateSkill(SkillGenerationRequest request);
    
    /**
     * 流式生成 Skill，按分片推送生成过程。
     *
     * @param request  生成请求
     * @param callback 流式响应回调
     */
    void generateSkillStream(SkillGenerationRequest request,
        StreamResponseCallback<SkillGenerationResponse> callback);
}
