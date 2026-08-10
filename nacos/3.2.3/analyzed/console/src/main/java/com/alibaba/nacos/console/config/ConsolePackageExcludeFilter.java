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

package com.alibaba.nacos.console.config;

import com.alibaba.nacos.console.NacosConsole;
import com.alibaba.nacos.sys.filter.NacosPackageExcludeFilter;

import java.util.Set;

/**
 * 控制台包排除过滤器：Basic/Web 上下文扫描 {@code com.alibaba.nacos} 时排除 console 包，
 * 避免与独立 Console 上下文重复加载；Console 上下文本身不应用此过滤器。
 * Console module package exclude filter. Only Basic and Web contexts use NacosTypeExcludeFilter with
 * basePackages = "com.alibaba.nacos"; Console context uses default scan (console package only) and does not
 * apply this filter. So when this filter runs, it is always Basic/Web context and console package should be excluded.
 *
 * @author xiweng.yy
 */
public class ConsolePackageExcludeFilter implements NacosPackageExcludeFilter {
    
    /** 返回本过滤器负责的包前缀（console 根包） */
    @Override
    public String getResponsiblePackagePrefix() {
        return NacosConsole.class.getPackage().getName();
    }
    
    /**
     * 在 Basic/Web 上下文中始终排除 console 包下所有类。
     *
     * @param className        候选类全限定名
     * @param annotationNames  类上的注解名集合（未使用）
     * @return 恒为 {@code true}
     */
    @Override
    public boolean isExcluded(String className, Set<String> annotationNames) {
        return true;
    }
}
