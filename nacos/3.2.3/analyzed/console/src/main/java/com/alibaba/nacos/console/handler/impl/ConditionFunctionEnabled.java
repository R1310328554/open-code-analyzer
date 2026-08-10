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

package com.alibaba.nacos.console.handler.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 功能模块启用条件：根据 {@link EnvUtil#getFunctionMode()} 判定 naming/config/ai 等模块 Handler 是否加载。
 * 未启用时使用 NoOp 实现替代，避免依赖模块缺失导致启动失败。
 * The condition of target function or module is enabled.
 * When target module such as `naming`, `config` or `ai` is disabled or dependency module is disabled
 * The target handler should not be loaded and should use noop handler replaced.
 *
 * @author xiweng.yy
 */
public class ConditionFunctionEnabled implements Condition {
    
    /** 待判定的目标功能模式标识（如 config、naming、ai） */
    private final String targetFunctionMode;
    
    /** 构造指定功能模式的启用条件 */
    public ConditionFunctionEnabled(String targetFunctionMode) {
        this.targetFunctionMode = targetFunctionMode;
    }
    
    /** 判定目标功能模块是否在当前部署模式下启用。 */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String functionMode = EnvUtil.getFunctionMode();
        // 未配置 function mode 时默认全部功能启用
        if (StringUtils.isEmpty(functionMode)) {
            return true;
        }
        // 配置的 function mode 与目标一致时表示该功能已启用
        if (functionMode.equalsIgnoreCase(targetFunctionMode)) {
            return true;
        }
        // 微服务模式同时启用 config 与 naming
        if (EnvUtil.FUNCTION_MODE_MICROSERVICE.equalsIgnoreCase(functionMode)) {
            return EnvUtil.FUNCTION_MODE_CONFIG.equalsIgnoreCase(targetFunctionMode)
                || EnvUtil.FUNCTION_MODE_NAMING.equalsIgnoreCase(targetFunctionMode);
        }
        // AI 模式依赖 config 与 naming 均已启用
        if (EnvUtil.FUNCTION_MODE_AI.equalsIgnoreCase(functionMode)) {
            return EnvUtil.FUNCTION_MODE_CONFIG.equalsIgnoreCase(targetFunctionMode)
                || EnvUtil.FUNCTION_MODE_NAMING.equalsIgnoreCase(targetFunctionMode);
        }
        return false;
    }
    
    /** naming 模块启用条件（{@link EnvUtil#FUNCTION_MODE_NAMING}）。 */
    public static class ConditionNamingEnabled extends ConditionFunctionEnabled {
        
        public ConditionNamingEnabled() {
            super(EnvUtil.FUNCTION_MODE_NAMING);
        }
    }
    
    /** config 模块启用条件（{@link EnvUtil#FUNCTION_MODE_CONFIG}）。 */
    public static class ConditionConfigEnabled extends ConditionFunctionEnabled {
        
        public ConditionConfigEnabled() {
            super(EnvUtil.FUNCTION_MODE_CONFIG);
        }
    }
    
    /** AI 模块启用条件（{@link EnvUtil#FUNCTION_MODE_AI}）。 */
    public static class ConditionAiEnabled extends ConditionFunctionEnabled {
        
        public ConditionAiEnabled() {
            super(EnvUtil.FUNCTION_MODE_AI);
        }
    }
}
