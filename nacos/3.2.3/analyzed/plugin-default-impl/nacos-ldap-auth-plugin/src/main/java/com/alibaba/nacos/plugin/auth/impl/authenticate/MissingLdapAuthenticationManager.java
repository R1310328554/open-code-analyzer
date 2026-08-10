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

package com.alibaba.nacos.plugin.auth.impl.authenticate;

import com.alibaba.nacos.plugin.auth.api.Permission;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUser;
import jakarta.servlet.http.HttpServletRequest;

/**
 * LDAP 运行时依赖缺失时的占位认证管理器。
 *
 * <p>所有认证与鉴权操作统一抛出包含安装指引的 {@link AccessException}，提示将 spring-ldap-core 放入 plugins 目录。</p>
 *
 * @author xiweng.yy
 */
public class MissingLdapAuthenticationManager implements IAuthenticationManager {
    
    /** 依赖缺失时的错误提示信息。 */
    private final String message;
    
    /**
     * @param message 向调用方返回的异常描述
     */
    public MissingLdapAuthenticationManager(String message) {
        this.message = message;
    }
    
    /** 用户名密码认证：直接拒绝并返回依赖缺失提示。 */
    @Override
    public NacosUser authenticate(String username, String rawPassword) throws AccessException {
        throw new AccessException(message);
    }
    
    /** JWT 令牌认证：直接拒绝并返回依赖缺失提示。 */
    @Override
    public NacosUser authenticate(String jwtToken) throws AccessException {
        throw new AccessException(message);
    }
    
    /** HTTP 请求认证：直接拒绝并返回依赖缺失提示。 */
    @Override
    public NacosUser authenticate(HttpServletRequest httpServletRequest) throws AccessException {
        throw new AccessException(message);
    }
    
    /** 权限校验：直接拒绝并返回依赖缺失提示。 */
    @Override
    public void authorize(Permission permission, NacosUser nacosUser) throws AccessException {
        throw new AccessException(message);
    }
    
    /** 依赖缺失时永远不是全局管理员。 */
    @Override
    public boolean hasGlobalAdminRole(String username) {
        return false;
    }
    
    /** 依赖缺失时永远不是全局管理员。 */
    @Override
    public boolean hasGlobalAdminRole() {
        return false;
    }
    
    /** 依赖缺失时永远不是全局管理员。 */
    @Override
    public boolean hasGlobalAdminRole(NacosUser nacosUser) {
        return false;
    }
}
