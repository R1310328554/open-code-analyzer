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

package com.alibaba.nacos.ai.utils;

import com.alibaba.nacos.api.ai.model.prompt.Prompt;
import com.alibaba.nacos.api.ai.model.prompt.PromptVersionInfo;

/**
 * Utility class for converting prompt related models.
 * <p>Prompt 模型转换工具，将服务层 {@link PromptVersionInfo} 转为客户端 {@link Prompt} 视图。</p>
 *
 * @author nacos
 */
public class PromptConvertUtils {
    
    private PromptConvertUtils() {
    }
    
    /**
     * Convert {@link PromptVersionInfo} to client-facing {@link Prompt}.
     * <p>复制 promptKey、version、template、md5 与 variables 至客户端 Prompt 对象。</p>
     *
     * @param versionInfo prompt version info from service layer, must not be {@code null}
     * @return client-facing prompt
     */
    public static Prompt toClientPrompt(PromptVersionInfo versionInfo) {
        Prompt prompt = new Prompt();
        prompt.setPromptKey(versionInfo.getPromptKey());
        prompt.setVersion(versionInfo.getVersion());
        prompt.setTemplate(versionInfo.getTemplate());
        prompt.setMd5(versionInfo.getMd5());
        prompt.setVariables(versionInfo.getVariables());
        return prompt;
    }
}
