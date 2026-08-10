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

package com.alibaba.nacos.ai.config;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alibaba.nacos.sys.filter.NacosPackageExcludeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Nacos AI Component enabled filter.
 * <p>Nacos AI 模块包扫描排除过滤器：当 function mode 非 ai 或 {@link #AI_ENABLED_KEY} 为 false 时，排除 {@code com.alibaba.nacos.ai} 包下组件的加载。</p>
 *
 * @author xiweng.yy
 */
public class AiEnabledFilter implements NacosPackageExcludeFilter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AiEnabledFilter.class);
    
    /** 配置项：是否启用 AI 扩展（默认 true） */
    public static final String AI_ENABLED_KEY = "nacos.extension.ai.enabled";
    
    @Override
    public String getResponsiblePackagePrefix() {
        return "com.alibaba.nacos.ai";
    }
    
    @Override
    public boolean isExcluded(String className, Set<String> annotationNames) {
        String functionMode = EnvUtil.getFunctionMode();
        // function mode 非空且非 ai 时禁用 AI 模块
        if (StringUtils.isNotEmpty(functionMode)
            && !EnvUtil.FUNCTION_MODE_AI.equals(functionMode)) {
            LOGGER.warn(
                "AI module disabled because function mode is {}, and AI mode requires empty or 'ai' mode",
                functionMode);
            return true;
        }
        boolean aiDisabled = !EnvUtil.getProperty(AI_ENABLED_KEY, Boolean.class, true);
        if (aiDisabled) {
            LOGGER.warn("AI module disabled because set {} as false", AI_ENABLED_KEY);
        }
        return aiDisabled;
    }
}
