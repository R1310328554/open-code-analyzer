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

import com.alibaba.nacos.sys.utils.ApplicationUtils;
import org.springframework.util.ClassUtils;

/**
 * LDAP 插件运行时依赖检测工具。
 *
 * <p>通过 ClassLoader 探测 spring-ldap-core 是否存在于 plugins classpath，供 ImportSelector 决定加载完整或降级配置。</p>
 *
 * @author xiweng.yy
 */
public final class LdapPluginDependencyChecker {
    
    /** LDAP 认证管理器 Spring Bean 名称（保留历史拼写 authenticatoin）。 */
    public static final String LDAP_AUTHENTICATION_MANAGER_BEAN_NAME = "ldapAuthenticatoinManager";
    
    /** spring-ldap-core 核心类全名，用于依赖探测。 */
    static final String LDAP_TEMPLATE_CLASS_NAME = "org.springframework.ldap.core.LdapTemplate";
    
    private LdapPluginDependencyChecker() {
    }
    
    /** 检测 LdapTemplate 类是否存在于当前 ClassLoader。 */
    public static boolean hasRequiredDependency() {
        return hasRequiredDependency(LDAP_TEMPLATE_CLASS_NAME);
    }
    
    /** 检测指定类名是否可通过 ClassLoader 加载。 */
    static boolean hasRequiredDependency(String className) {
        return ClassUtils.isPresent(className, resolveClassLoader());
    }
    
    /**
     * 构建 LDAP 运行时依赖缺失时的提示文案。
     *
     * @return 引导用户将 spring-ldap-core 放入 plugins 目录的英文说明
     */
    public static String buildMissingDependencyMessage() {
        return "LDAP auth plugin requires org.springframework.ldap:spring-ldap-core in "
            + "plugins/classpath "
            + "when nacos.core.auth.system.type=ldap. Please add spring-ldap-core jar into "
            + "the plugins "
            + "directory.";
    }
    
    /** 解析用于类探测的 ClassLoader：优先线程上下文，其次 ApplicationUtils，最后本类加载器。 */
    private static ClassLoader resolveClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (null != classLoader) {
            return classLoader;
        }
        try {
            return ApplicationUtils.getClassLoader();
        } catch (NullPointerException ignored) {
            return LdapPluginDependencyChecker.class.getClassLoader();
        }
    }
}
