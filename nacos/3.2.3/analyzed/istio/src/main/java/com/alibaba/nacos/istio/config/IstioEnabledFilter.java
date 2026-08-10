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

package com.alibaba.nacos.istio.config;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.istio.IstioApp;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alibaba.nacos.sys.filter.NacosPackageExcludeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.alibaba.nacos.sys.env.EnvUtil.FUNCTION_MODE_NAMING;
import static com.alibaba.nacos.sys.env.EnvUtil.FUNCTION_MODE_MICROSERVICE;

/**
 * Istio 模块启用过滤器：在 Spring 包扫描阶段决定是否加载 {@link com.alibaba.nacos.istio.IstioApp} 包下组件。
 *
 * <p>命名模式未启用或 {@code nacos.extension.naming.istio.enabled=false} 时排除 Istio 包。</p>
 *
 * @author xiweng.yy
 */
public class IstioEnabledFilter implements NacosPackageExcludeFilter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(IstioEnabledFilter.class);
    
    /** 配置项：是否启用 Istio 扩展（默认 false）。 */
    private static final String ISTIO_ENABLED_KEY = "nacos.extension.naming.istio.enabled";
    
    @Override
    public String getResponsiblePackagePrefix() {
        return IstioApp.class.getPackage().getName();
    }
    
    @Override
    public boolean isExcluded(String className, Set<String> annotationNames) {
        String functionMode = EnvUtil.getFunctionMode();
        // 非 naming/microservice 功能模式下命名模块未启动，Istio 依赖命名故一并禁用
        if (isNamingDisabled(functionMode)) {
            LOGGER.warn(
                "Istio module disabled because function mode is {}, and Istio depend naming module",
                functionMode);
            return true;
        }
        boolean istioDisabled = !EnvUtil.getProperty(ISTIO_ENABLED_KEY, Boolean.class, false);
        if (istioDisabled) {
            LOGGER.warn("Istio module disabled because set {} as false", ISTIO_ENABLED_KEY);
        }
        return istioDisabled;
    }
    
    /** 当前功能模式是否未启用命名（及 Istio 所依赖的命名能力）。 */
    private boolean isNamingDisabled(String functionMode) {
        if (StringUtils.isEmpty(functionMode)) {
            return false;
        }
        return !FUNCTION_MODE_NAMING.equals(functionMode)
            && !FUNCTION_MODE_MICROSERVICE.equals(functionMode);
    }
}
