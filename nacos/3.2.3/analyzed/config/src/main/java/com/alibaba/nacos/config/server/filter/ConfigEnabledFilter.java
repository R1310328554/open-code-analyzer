/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.filter;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.config.server.Config;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alibaba.nacos.sys.filter.NacosPackageExcludeFilter;

import java.util.Set;

import static com.alibaba.nacos.sys.env.EnvUtil.FUNCTION_MODE_AI;
import static com.alibaba.nacos.sys.env.EnvUtil.FUNCTION_MODE_CONFIG;
import static com.alibaba.nacos.sys.env.EnvUtil.FUNCTION_MODE_MICROSERVICE;

/**
 * 配置模块启用过滤器：按 {@code nacos.functionMode} 决定是否在 Spring 扫描中排除
 * {@code com.alibaba.nacos.config.server} 包下组件。
 * Config module enabled filter by spring packages scan.
 *
 * @author xiweng.yy
 */
public class ConfigEnabledFilter implements NacosPackageExcludeFilter {
    
    /** 返回配置模块根包名，供 Nacos 包扫描排除逻辑匹配 */
    @Override
    public String getResponsiblePackagePrefix() {
        return Config.class.getPackage().getName();
    }
    
    /**
     * 非 config/microservice/ai 模式时排除配置模块类。
     *
     * @param className       待扫描类全名
     * @param annotationNames 类上注解名集合
     * @return 是否从扫描中排除
     */
    @Override
    public boolean isExcluded(String className, Set<String> annotationNames) {
        String functionMode = EnvUtil.getFunctionMode();
        // 未指定 functionMode 时不排除（默认加载配置模块）
        if (StringUtils.isEmpty(functionMode)) {
            return false;
        }
        return !FUNCTION_MODE_CONFIG.equals(functionMode)
            && !FUNCTION_MODE_MICROSERVICE.equals(functionMode)
            && !FUNCTION_MODE_AI.equals(functionMode);
    }
}
