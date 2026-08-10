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

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthConstants;
import com.alibaba.nacos.plugin.auth.impl.persistence.RoleInfo;
import com.alibaba.nacos.plugin.auth.impl.persistence.User;
import com.alibaba.nacos.plugin.auth.impl.roles.NacosRoleService;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserDetails;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserService;
import com.alibaba.nacos.plugin.auth.impl.utils.PasswordEncoderUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

/**
 * LDAP Spring Security 认证 Provider（已弃用）。
 *
 * <p>管理员用户走本地密码校验；普通用户经 LDAP 目录认证后映射为带 {@code LDAP_} 前缀的本地用户。新代码请使用 {@link com.alibaba.nacos.plugin.auth.impl.authenticate.LdapAuthenticationManager}。</p>
 *
 * @author zjw
 */
@Deprecated
public class LdapAuthenticationProvider implements AuthenticationProvider {
    
    /** 本地用户详情服务。 */
    private final NacosUserService userDetailsService;
    
    /** Nacos 角色服务。 */
    private final NacosRoleService nacosRoleService;
    
    /** Spring LDAP 操作模板。 */
    private final LdapTemplate ldapTemplate;
    
    /** LDAP 用户搜索属性前缀。 */
    private final String filterPrefix;
    
    /** 用户名是否区分大小写。 */
    private final boolean caseSensitive;
    
    /**
     * @param ldapTemplate       LDAP 模板
     * @param userDetailsService 用户服务
     * @param nacosRoleService   角色服务
     * @param filterPrefix       搜索属性前缀
     * @param caseSensitive      是否区分大小写
     */
    public LdapAuthenticationProvider(LdapTemplate ldapTemplate,
        NacosUserService userDetailsService,
        NacosRoleService nacosRoleService, String filterPrefix, boolean caseSensitive) {
        this.ldapTemplate = ldapTemplate;
        this.nacosRoleService = nacosRoleService;
        this.userDetailsService = userDetailsService;
        this.filterPrefix = filterPrefix;
        this.caseSensitive = caseSensitive;
    }
    
    /** Spring Security 认证入口：管理员本地校验，其余用户走 LDAP。 */
    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();
        
        // 全局管理员仍使用本地密码认证
        if (isAdmin(username)) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (PasswordEncoderUtil.matches(password, userDetails.getPassword())) {
                return new UsernamePasswordAuthenticationToken(userDetails, password,
                    userDetails.getAuthorities());
            } else {
                return null;
            }
        }
        
        if (!caseSensitive) {
            username = StringUtils.lowerCase(username);
        }
        
        try {
            if (!ldapLogin(username, password)) {
                return null;
            }
        } catch (Exception e) {
            Loggers.AUTH.error("[LDAP-LOGIN] failed", e);
            return null;
        }
        
        UserDetails userDetails;
        try {
            userDetails =
                userDetailsService.loadUserByUsername(AuthConstants.LDAP_PREFIX + username);
        } catch (UsernameNotFoundException exception) {
            // 首次 LDAP 登录自动创建本地占位用户
            userDetailsService.createUser(AuthConstants.LDAP_PREFIX + username,
                AuthConstants.LDAP_DEFAULT_ENCODED_PASSWORD, false);
            User user = new User();
            user.setUsername(AuthConstants.LDAP_PREFIX + username);
            user.setPassword(AuthConstants.LDAP_DEFAULT_ENCODED_PASSWORD);
            userDetails = new NacosUserDetails(user);
        }
        return new UsernamePasswordAuthenticationToken(userDetails, password,
            userDetails.getAuthorities());
    }
    
    /** 判断用户是否拥有 GLOBAL_ADMIN 角色。 */
    private boolean isAdmin(String username) {
        List<RoleInfo> roleInfos = nacosRoleService.getRoles(username);
        if (CollectionUtils.isEmpty(roleInfos)) {
            return false;
        }
        for (RoleInfo roleinfo : roleInfos) {
            if (AuthConstants.GLOBAL_ADMIN_ROLE.equals(roleinfo.getRole())) {
                return true;
            }
        }
        return false;
    }
    
    /** 使用 filterPrefix 构造 LDAP 过滤器并校验凭据。 */
    private boolean ldapLogin(String username, String password) throws AuthenticationException {
        return ldapTemplate.authenticate("", "(" + filterPrefix + "=" + username + ")", password);
    }
    
    /** 仅支持 UsernamePasswordAuthenticationToken 类型。 */
    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(UsernamePasswordAuthenticationToken.class);
    }
    
}
