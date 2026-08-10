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

import java.util.HashSet;
import java.util.Set;

/**
 * 命名空间详情注入器注册中心（单例）：聚合各模块 {@link AbstractNamespaceDetailInjector}，在查询 namespace 列表时统一调用 {@link #injectDetail(Namespace)}。
 * Holder of namespace detail injector.
 *
 * @author xiweng.yy
 */
public class NamespaceDetailInjectorHolder {
    
    /** 单例实例。 */
    private static final NamespaceDetailInjectorHolder INSTANCE =
        new NamespaceDetailInjectorHolder();
    
    /** 已注册的详情注入器集合。 */
    private final Set<AbstractNamespaceDetailInjector> namespaceDetailInjectors;
    
    /** 私有构造，初始化注入器集合。 */
    private NamespaceDetailInjectorHolder() {
        this.namespaceDetailInjectors = new HashSet<>();
    }
    
    /** 获取 Holder 单例。 */
    public static NamespaceDetailInjectorHolder getInstance() {
        return INSTANCE;
    }
    
    /** 注册一个命名空间详情注入器（子类构造时自动调用）。 */
    public void registerInjector(AbstractNamespaceDetailInjector injector) {
        this.namespaceDetailInjectors.add(injector);
    }
    
    /**
     * 依次调用所有已注册注入器，向同一 namespace 对象写入扩展详情。
     *
     * @param namespace namespace
     */
    public void injectDetail(Namespace namespace) {
        for (AbstractNamespaceDetailInjector each : this.namespaceDetailInjectors) {
            each.injectDetail(namespace);
        }
    }
}
