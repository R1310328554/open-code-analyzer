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

package com.alibaba.nacos.core.namespace.filter;

import com.alibaba.nacos.core.config.AbstractDynamicConfig;
import com.alibaba.nacos.sys.env.EnvUtil;

/**
 * 命名空间校验全局开关配置，默认关闭；开启后 {@link NamespaceValidationRequestFilter} 才会执行校验。
 * The type Namespace validation config, default is disable.
 *
 * @author FangYuan
 * @since 2025-08-13 13:33:16
 */
public class NamespaceValidationConfig extends AbstractDynamicConfig {
    
    /** 动态配置分组名。 */
    private static final String NAMESPACE_VALIDATION = "NamespaceValidation";
    
    /** 单例实例。 */
    private static final NamespaceValidationConfig INSTANCE = new NamespaceValidationConfig();
    
    /** 全局命名空间校验开关（实际默认从环境读取为 false）。 */
    private boolean namespaceValidationEnabled = true;
    
    /** 注册动态配置并加载初始开关值。 */
    protected NamespaceValidationConfig() {
        super(NAMESPACE_VALIDATION);
        resetConfig();
    }
    
    /** 获取命名空间校验配置单例。 */
    public static NamespaceValidationConfig getInstance() {
        return INSTANCE;
    }
    
    /** 从 {@code nacos.core.namespace.validation.enabled} 读取全局开关。 */
    @Override
    protected void getConfigFromEnv() {
        namespaceValidationEnabled =
            EnvUtil.getProperty("nacos.core.namespace.validation.enabled", Boolean.class, false);
    }
    
    /** 全局命名空间校验是否启用。 */
    public boolean isNamespaceValidationEnabled() {
        return namespaceValidationEnabled;
    }
    
    @Override
    protected String printConfig() {
        return "NamespaceValidationConfig{" + "namespaceValidationEnabled="
            + namespaceValidationEnabled + "}";
    }
}
