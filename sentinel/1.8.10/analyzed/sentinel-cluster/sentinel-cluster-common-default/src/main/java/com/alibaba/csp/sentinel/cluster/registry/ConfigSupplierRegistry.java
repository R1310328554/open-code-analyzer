/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.cluster.registry;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Supplier;

/**
 * 集群配置供应器注册表，管理命名空间 {@link Supplier} 的注册与获取。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ConfigSupplierRegistry {

    /**
     * 默认命名空间供应器，以应用名作为命名空间。
     */
    private static final Supplier<String> DEFAULT_APP_NAME_SUPPLIER = new Supplier<String>() {
        @Override
        public String get() {
            return AppNameUtil.getAppName();
        }
    };
    /**
     * 已注册的命名空间供应器。
     */
    private static Supplier<String> namespaceSupplier = DEFAULT_APP_NAME_SUPPLIER;

    /**
     * 获取已注册的命名空间供应器。
     *
     * @return 已注册的命名空间供应器
     */
    public static Supplier<String> getNamespaceSupplier() {
        return namespaceSupplier;
    }

    public static void setNamespaceSupplier(Supplier<String> namespaceSupplier) {
        AssertUtil.notNull(namespaceSupplier, "namespaceSupplier cannot be null");
        ConfigSupplierRegistry.namespaceSupplier = namespaceSupplier;
        RecordLog.info("[ConfigSupplierRegistry] New namespace supplier provided, current supplied: {}",
            namespaceSupplier.get());
    }

    private ConfigSupplierRegistry() {}
}
