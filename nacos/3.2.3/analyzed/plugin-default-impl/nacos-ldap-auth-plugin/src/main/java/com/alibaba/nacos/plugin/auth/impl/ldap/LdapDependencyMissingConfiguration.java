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

import com.alibaba.nacos.plugin.auth.impl.authenticate.IAuthenticationManager;
import com.alibaba.nacos.plugin.auth.impl.authenticate.MissingLdapAuthenticationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LDAP 运行时依赖缺失时的降级配置。
 *
 * <p>当 classpath 中不存在 spring-ldap-core 时，注册 {@link MissingLdapAuthenticationManager} 占位 Bean 并输出安装指引日志。</p>
 *
 * @author xiweng.yy
 */
@Configuration(proxyBeanMethods = false)
public class LdapDependencyMissingConfiguration {
    
    private static final Logger LOGGER =
        LoggerFactory.getLogger(LdapDependencyMissingConfiguration.class);
    
    /** 注册占位 LDAP 认证管理器，所有请求均返回依赖缺失错误。 */
    @Bean(name = LdapPluginDependencyChecker.LDAP_AUTHENTICATION_MANAGER_BEAN_NAME)
    public IAuthenticationManager ldapAuthenticatoinManager() {
        String message = LdapPluginDependencyChecker.buildMissingDependencyMessage();
        LOGGER.warn(message);
        return new MissingLdapAuthenticationManager(message);
    }
}
