/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.authenticate;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthConstants;
import com.alibaba.nacos.plugin.auth.impl.persistence.User;
import com.alibaba.nacos.plugin.auth.impl.roles.NacosRoleService;
import com.alibaba.nacos.plugin.auth.impl.token.TokenManagerDelegate;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUser;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserDetails;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserService;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * LDAP 认证管理器。
 *
 * <p>优先尝试本地 Nacos 用户认证；失败后回退至 LDAP 目录校验，并在首次登录时自动创建带 {@code LDAP_} 前缀的本地用户记录。</p>
 *
 * @author Weizhan▪Yun
 * @date 2023/1/17 13:25
 */
public class LdapAuthenticationManager extends AbstractAuthenticationManager {
    
    /** LDAP 用户搜索过滤器属性前缀（如 uid）。 */
    private final String filterPrefix;
    
    /** 用户名是否区分大小写。 */
    private final boolean caseSensitive;
    
    /** Spring LDAP 操作模板。 */
    private final LdapTemplate ldapTemplate;
    
    /**
     * 构造 LDAP 认证管理器。
     *
     * @param ldapTemplate       LDAP 模板
     * @param userDetailsService 本地用户服务
     * @param jwtTokenManager    JWT 令牌管理器
     * @param roleService        角色服务
     * @param filterPrefix       LDAP 搜索属性前缀
     * @param caseSensitive      是否区分大小写
     */
    public LdapAuthenticationManager(LdapTemplate ldapTemplate, NacosUserService userDetailsService,
        TokenManagerDelegate jwtTokenManager, NacosRoleService roleService, String filterPrefix,
        boolean caseSensitive) {
        super(userDetailsService, jwtTokenManager, roleService);
        this.ldapTemplate = ldapTemplate;
        this.filterPrefix = filterPrefix;
        this.caseSensitive = caseSensitive;
    }
    
    /**
     * 用户名密码认证：本地优先，失败则走 LDAP 并签发 JWT。
     *
     * @param username    用户名
     * @param rawPassword 明文密码
     * @return 认证成功的 {@link NacosUser}
     * @throws AccessException 用户不存在或 LDAP 校验失败
     */
    @Override
    public NacosUser authenticate(String username, String rawPassword) throws AccessException {
        if (StringUtils.isBlank(username)) {
            throw new AccessException("user not found!");
        }
        
        if (!caseSensitive) {
            username = username.toLowerCase();
        }
        
        // 已是 LDAP 前缀用户则拒绝，避免重复 LDAP 认证路径
        if (username.toUpperCase().startsWith(AuthConstants.LDAP_PREFIX)) {
            throw new AccessException("user not found!");
        }
        
        try {
            return super.authenticate(username, rawPassword);
        } catch (AccessException | UsernameNotFoundException ignored) {
            if (Loggers.AUTH.isWarnEnabled()) {
                Loggers.AUTH.warn("try login with LDAP, user: {}", username);
            }
        }
        
        UserDetails userDetails;
        try {
            if (!ldapLogin(username, rawPassword)) {
                throw new AccessException("LDAP login failed.");
            }
            userDetails =
                userDetailsService.loadUserByUsername(AuthConstants.LDAP_PREFIX + username);
        } catch (UsernameNotFoundException exception) {
            // 首次 LDAP 登录：自动创建本地占位用户
            String ldapUsername = AuthConstants.LDAP_PREFIX + username;
            userDetailsService.createUser(ldapUsername, AuthConstants.LDAP_DEFAULT_ENCODED_PASSWORD,
                false);
            User user = new User();
            user.setUsername(ldapUsername);
            user.setPassword(AuthConstants.LDAP_DEFAULT_ENCODED_PASSWORD);
            userDetails = new NacosUserDetails(user);
        } catch (Exception e) {
            Loggers.AUTH.error("[LDAP-LOGIN] failed", e);
            throw new AccessException("user not found");
        }
        
        return new NacosUser(userDetails.getUsername(),
            jwtTokenManager.createToken(userDetails.getUsername()));
    }
    
    /** 使用 EqualsFilter 向 LDAP 目录校验用户名与密码。 */
    private boolean ldapLogin(String username, String password) {
        return ldapTemplate.authenticate("", new EqualsFilter(filterPrefix, username).toString(),
            password);
    }
}
