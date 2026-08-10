/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ai.model.prompt;

import com.alibaba.nacos.api.ai.model.NacosAiConfigKeyCodec;
import com.alibaba.nacos.api.utils.StringUtils;

/**
 * Prompt 存储工具类，封装 Nacos Config group/dataId 构建规则。
 *
 * <p>设计模式与 {@link com.alibaba.nacos.api.ai.model.skills.SkillUtils}、
 * {@link com.alibaba.nacos.api.ai.model.agentspecs.AgentSpecUtils} 一致，
 * 提供 Prompt 专用的 group 前缀与版本化 group 构造方法。</p>
 *
 * @author nacos
 */
public class PromptUtils {
    
    /**
     * Prompt 版本化 group 前缀；每个版本存储于
     * {@code prompt__{enc_promptKey}__{enc_version}} 形式的 group 中。
     */
    public static final String PROMPT_GROUP_PREFIX = "prompt__";
    
    /** Prompt 版本主内容 dataId（固定为 {@code content.json}）。 */
    public static final String PROMPT_MAIN_DATA_ID = "content.json";
    
    private static final String DOUBLE_UNDERSCORE = "__";
    
    private PromptUtils() {
    }
    
    /**
     * 构建指定 Prompt 版本的 Nacos Config group。
     *
     * @param promptKey Prompt 键名
     * @param version   版本号，如 {@code 1.0.0}
     * @return 配置 group 字符串，如 {@code prompt__{enc_promptKey}__{enc_version}}
     * @throws IllegalArgumentException 当 promptKey 或 version 为空时
     */
    public static String buildPromptVersionGroup(String promptKey, String version) {
        if (StringUtils.isBlank(promptKey)) {
            throw new IllegalArgumentException("Prompt key cannot be blank");
        }
        if (StringUtils.isBlank(version)) {
            throw new IllegalArgumentException("Version cannot be blank");
        }
        return PROMPT_GROUP_PREFIX + NacosAiConfigKeyCodec.encodeVersionedGroupSegment(promptKey)
            + DOUBLE_UNDERSCORE + NacosAiConfigKeyCodec.encodeVersionedGroupSegment(version);
    }
}
