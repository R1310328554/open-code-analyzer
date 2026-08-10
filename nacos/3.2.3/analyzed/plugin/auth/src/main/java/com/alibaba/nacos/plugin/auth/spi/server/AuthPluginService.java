/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.spi.server;

import com.alibaba.nacos.plugin.auth.api.AuthResult;
import com.alibaba.nacos.plugin.auth.api.IdentityContext;
import com.alibaba.nacos.plugin.auth.api.Permission;
import com.alibaba.nacos.plugin.auth.api.Resource;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.exception.AccessException;

import java.util.Collection;

/**
 * 服务端认证插件 SPI 接口，定义身份校验与权限校验的核心能力。
 *
 * <p>各认证实现（Nacos 内置、LDAP、OIDC 等）需实现此接口并通过
 * {@link java.util.ServiceLoader} 注册，由 {@link AuthPluginManager} 统一管理。</p>
 *
 * @author Wuyfee
 * @author xiweng.yy
 */
public interface AuthPluginService {
    
    /**
     * 声明本插件需要从请求中提取的身份信息字段名（如 username、password、accessToken）。
     *
     * @return 身份字段名集合
     */
    Collection<String> identityNames();
    
    /**
     * 判断本插件是否对指定操作类型和资源类型启用认证。
     *
     * @param action 请求操作类型，参见 {@link ActionTypes}
     * @param type   请求资源类型，参见 {@link com.alibaba.nacos.plugin.auth.constant.SignType}
     * @return 启用认证返回 {@code true}，否则返回 {@code false}
     */
    boolean enableAuth(ActionTypes action, String type);
    
    /**
     * 校验请求中的身份上下文是否合法（身份认证）。
     *
     * @param identityContext 从请求中提取的用户身份信息
     * @param resource        本次请求关联的资源
     * @return 校验结果
     * @throws AccessException 身份认证失败时抛出
     */
    AuthResult validateIdentity(IdentityContext identityContext, Resource resource)
        throws AccessException;
    
    /**
     * 校验已认证身份是否拥有指定资源的访问权限（权限认证）。
     *
     * @param identityContext 已验证的用户身份信息
     * @param permission      待校验的权限
     * @return 校验结果
     * @throws AccessException 权限校验失败时抛出
     */
    AuthResult validateAuthority(IdentityContext identityContext, Permission permission)
        throws AccessException;
    
    /**
     * 返回本插件的唯一服务名称，用于 {@link AuthPluginManager} 索引与查找。
     *
     * @return 认证服务名称
     */
    String getAuthServiceName();
    
    /**
     * 本插件是否需要客户端登录流程。
     *
     * @return 需要登录返回 {@code true}，否则返回 {@code false}
     * @since 2.2.2
     */
    default boolean isLoginEnabled() {
        return false;
    }
    
    /**
     * 本插件是否要求管理员角色才能访问。
     *
     * @return 需要管理员角色返回 {@code true}
     */
    default boolean isAdminRequest() {
        return true;
    }
}
