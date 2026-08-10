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

package com.alibaba.nacos.plugin.auth.impl.ldap;

import com.alibaba.nacos.plugin.auth.impl.authenticate.IAuthenticationManager;
import com.alibaba.nacos.plugin.auth.impl.authenticate.LdapAuthenticationManager;
import com.alibaba.nacos.plugin.auth.impl.condition.ConditionOnLdapAuth;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthConstants;
import com.alibaba.nacos.plugin.auth.impl.roles.NacosRoleService;
import com.alibaba.nacos.plugin.auth.impl.token.TokenManagerDelegate;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;

/**
 * LDAP 认证插件 Spring 配置。
 *
 * <p>在 {@link ConditionOnLdapAuth} 满足时注册 LDAP 连接、认证 Provider 与 {@link IAuthenticationManager} Bean。</p>
 *
 * @author onewe
 */
@Configuration(proxyBeanMethods = false)
@Conditional(ConditionOnLdapAuth.class)
public class LdapAuthPluginConfig {
    
    /** LDAP 服务器 URL，默认 ldap://localhost:389。 */
    @Value(("${" + AuthConstants.NACOS_CORE_AUTH_LDAP_URL + ":ldap://localhost:389}"))
    private String ldapUrl;
    
    /** LDAP 搜索基准 DN，默认 dc=example,dc=org。 */
    @Value(("${" + AuthConstants.NACOS_CORE_AUTH_LDAP_BASEDC + ":dc=example,dc=org}"))
    private String ldapBaseDc;
    
    /** LDAP 连接超时（毫秒），默认 3000。 */
    @Value(("${" + AuthConstants.NACOS_CORE_AUTH_LDAP_TIMEOUT + ":3000}"))
    private String ldapTimeOut;
    
    /** LDAP 绑定用户 DN，默认 cn=admin,dc=example,dc=org。 */
    @Value(("${" + AuthConstants.NACOS_CORE_AUTH_LDAP_USERDN + ":cn=admin,dc=example,dc=org}"))
    private String userDn;
    
    /** LDAP 绑定用户密码。 */
    @Value(("${" + AuthConstants.NACOS_CORE_AUTH_LDAP_PASSWORD + ":password}"))
    private String password;
    
    /** LDAP 用户搜索属性前缀，默认 uid。 */
    @Value(("${" + AuthConstants.NACOS_CORE_AUTH_LDAP_FILTER_PREFIX + ":uid}"))
    private String filterPrefix;
    
    /** 用户名是否区分大小写，默认 true。 */
    @Value(("${" + AuthConstants.NACOS_CORE_AUTH_CASE_SENSITIVE + ":true}"))
    private boolean caseSensitive;
    
    /**
     * 是否忽略 LDAP 部分结果异常，参见 {@link LdapTemplate#setIgnorePartialResultException(boolean)}。
     */
    @Value(("${" + AuthConstants.NACOS_CORE_AUTH_IGNORE_PARTIAL_RESULT_EXCEPTION + ":false}"))
    private boolean ignorePartialResultException;
    
    /** 创建 LdapTemplate，并按配置设置部分结果异常忽略策略。 */
    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource ldapContextSource) {
        LdapTemplate ldapTemplate = new LdapTemplate(ldapContextSource);
        ldapTemplate.setIgnorePartialResultException(ignorePartialResultException);
        return ldapTemplate;
    }
    
    /** 创建带 SSL 支持与连接池的 Nacos LDAP 上下文源。 */
    @Bean
    public LdapContextSource ldapContextSource() {
        return new NacosLdapContextSource(ldapUrl, ldapBaseDc, userDn, password, ldapTimeOut);
    }
    
    /** 注册已弃用的 Spring Security LDAP AuthenticationProvider。 */
    @Bean
    public LdapAuthenticationProvider ldapAuthenticationProvider(LdapTemplate ldapTemplate,
        NacosUserService userDetailsService, NacosRoleService nacosRoleService) {
        return new LdapAuthenticationProvider(ldapTemplate, userDetailsService, nacosRoleService,
            filterPrefix,
            caseSensitive);
    }
    
    /** 注册 Nacos LDAP 认证管理器 Bean。 */
    @Bean
    public IAuthenticationManager ldapAuthenticatoinManager(LdapTemplate ldapTemplate,
        NacosUserService userDetailsService, TokenManagerDelegate jwtTokenManager,
        NacosRoleService roleService) {
        return new LdapAuthenticationManager(ldapTemplate, userDetailsService, jwtTokenManager,
            roleService,
            filterPrefix, caseSensitive);
    }
    
    /** 将 LDAP AuthenticationProvider 挂入 Spring Security 全局认证配置。 */
    @Bean
    public GlobalAuthenticationConfigurerAdapter authenticationConfigurer(
        LdapAuthenticationProvider ldapAuthenticationProvider) {
        return new GlobalAuthenticationConfigurerAdapter() {
            
            @Override
            public void init(AuthenticationManagerBuilder auth) throws Exception {
                auth.authenticationProvider(ldapAuthenticationProvider);
            }
        };
    }
    
}
