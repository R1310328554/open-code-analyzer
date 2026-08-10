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

package com.alibaba.nacos.airegistry.config;

import com.alibaba.nacos.airegistry.NacosAiRegistry;
import com.alibaba.nacos.sys.filter.NacosPackageExcludeFilter;

import java.util.Set;

/**
 * McpRegistry module package exclude filter. Only Basic and Web contexts use NacosTypeExcludeFilter with
 * basePackages = "com.alibaba.nacos"; McpRegistry context uses default scan (airegistry package only) and does not
 * apply this filter. So when this filter runs, it is always Basic/Web context and airegistry package should be excluded.
 * <p>AI Registry 包排除过滤器：Basic/Web 上下文扫描 com.alibaba.nacos 时排除 airegistry 包，避免与独立 Registry 应用重复加载 Bean。</p>
 *
 * @author xiweng.yy
 */
public class AiRegistryPackageExcludeFilter implements NacosPackageExcludeFilter {
    
    /** 返回本过滤器负责的包前缀（airegistry 根包）。 */
    @Override
    public String getResponsiblePackagePrefix() {
        return NacosAiRegistry.class.getPackage().getName();
    }
    
    /** Basic/Web 上下文中始终排除 airegistry 包下全部类。 */
    @Override
    public boolean isExcluded(String className, Set<String> annotationNames) {
        return true;
    }
}
