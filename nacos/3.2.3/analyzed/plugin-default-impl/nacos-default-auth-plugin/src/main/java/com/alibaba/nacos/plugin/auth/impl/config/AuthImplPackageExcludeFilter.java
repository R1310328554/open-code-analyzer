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

package com.alibaba.nacos.plugin.auth.impl.config;

import com.alibaba.nacos.sys.filter.NacosPackageExcludeFilter;

import java.util.Set;

/**
 * 默认鉴权实现模块的包扫描排除过滤器。
 *
 * <p>Basic 与 Web 上下文通过 {@code basePackages = "com.alibaba.nacos"} 做组件扫描； 鉴权插件 Bean 由 Console 上下文下的独立 AutoConfiguration 加载，而非全局扫描。 因此在此过滤器生效时，应排除 {@code com.alibaba.nacos.plugin.auth.impl} 包，避免重复注册。</p>
 *
 * @author xiweng.yy
 */
public class AuthImplPackageExcludeFilter implements NacosPackageExcludeFilter {
    
    /** 返回本过滤器负责的包前缀。 */
    @Override
    public String getResponsiblePackagePrefix() {
        return "com.alibaba.nacos.plugin.auth.impl";
    }
    
    /** 鉴权实现包内类一律排除，不参与 Basic/Web 扫描。 */
    @Override
    public boolean isExcluded(String className, Set<String> annotationNames) {
        return true;
    }
}
