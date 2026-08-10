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

package com.alibaba.nacos.core.namespace.injector;

import com.alibaba.nacos.api.model.response.Namespace;

/**
 * 命名空间详情注入器抽象基类：子类构造时自动注册到 {@link NamespaceDetailInjectorHolder}，用于向 {@link com.alibaba.nacos.api.model.response.Namespace} 补充配置数、服务数等统计信息。
 * Namespace detail injector.
 *
 * @author xiweng.yy
 */
public abstract class AbstractNamespaceDetailInjector {
    
    /** 构造时向 Holder 注册当前注入器实例。 */
    protected AbstractNamespaceDetailInjector() {
        NamespaceDetailInjectorHolder.getInstance().registerInjector(this);
    }
    
    /**
     * 向命名空间对象注入扩展详情（如配置条数、服务实例数等）。
     *
     * @param namespace namespace
     */
    public abstract void injectDetail(Namespace namespace);
}
