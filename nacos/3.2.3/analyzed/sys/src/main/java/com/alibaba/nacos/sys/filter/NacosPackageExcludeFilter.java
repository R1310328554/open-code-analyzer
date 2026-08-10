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

package com.alibaba.nacos.sys.filter;

import java.util.Set;

/**
 * 按模块包前缀排除 Spring Bean 的 SPI 过滤器接口。
 *
 * <p>各模块通过 {@link com.alibaba.nacos.common.spi.NacosServiceLoader} 注册实现，供 {@link NacosTypeExcludeFilter} 在组件扫描阶段按部署形态裁剪类。</p>
 *
 * @author xiweng.yy
 */
public interface NacosPackageExcludeFilter {
    
    /**
     * 返回本过滤器负责的 Java 包前缀。
     *
     * @return package prefix
     */
    String getResponsiblePackagePrefix();
    
    /**
     * 根据类名与注解集合判断是否应从 Spring 容器排除。
     *
     * @param className       name of this class
     * @param annotationNames annotations of this class
     * @return {@code true} if should be excluded, otherwise {@code false}
     */
    boolean isExcluded(String className, Set<String> annotationNames);
}
