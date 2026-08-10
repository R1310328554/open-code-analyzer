/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.ldap;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * LDAP 插件条件导入选择器。
 *
 * <p>依赖齐全时导入 {@link LdapAuthPluginConfig}；缺失 spring-ldap-core 时导入 {@link LdapDependencyMissingConfiguration}。</p>
 *
 * @author xiweng.yy
 */
public class LdapPluginImportSelector implements ImportSelector {
    
    /** 完整 LDAP 认证配置类全名。 */
    private static final String LDAP_PLUGIN_CONFIG =
        "com.alibaba.nacos.plugin.auth.impl.ldap.LdapAuthPluginConfig";
    
    /** 依赖缺失降级配置类全名。 */
    private static final String LDAP_MISSING_CONFIG =
        "com.alibaba.nacos.plugin.auth.impl.ldap.LdapDependencyMissingConfiguration";
    
    /** 按运行时依赖探测结果返回待导入的配置类名数组。 */
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        if (LdapPluginDependencyChecker.hasRequiredDependency()) {
            return new String[] {LDAP_PLUGIN_CONFIG};
        }
        return new String[] {LDAP_MISSING_CONFIG};
    }
}
