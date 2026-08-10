/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Authorization;
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig.PathConfig;
import org.keycloak.representations.idm.authorization.Permission;

/**
 * 封装 UMA/授权服务返回的权限上下文，用于在适配器侧判断资源与 scope 是否已授权。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class AuthorizationContext {

    /** 携带 authorization 声明的访问令牌。 */
    private final AccessToken authzToken;
    /** 当前请求匹配的策略路径配置。 */
    private final PathConfig current;
    /** 是否已通过授权检查。 */
    private boolean granted;

    /**
     * 构造已授权的上下文。
     *
     * @param authzToken 含权限信息的访问令牌
     * @param current 当前路径配置，可为 null
     */
    public AuthorizationContext(AccessToken authzToken, PathConfig current) {
        this.authzToken = authzToken;
        this.current = current;
        this.granted = true;
    }

    /** 构造未授权的空上下文。 */
    public AuthorizationContext() {
        this(null, null);
        this.granted = false;
    }

    /**
     * 判断令牌中是否包含对指定资源（及可选 scope）的权限。
     *
     * @param resourceName 资源名称或 ID
     * @param scopeName scope 名称；为 null 时仅检查资源级权限
     * @return 有对应权限时返回 true
     */
    public boolean hasPermission(String resourceName, String scopeName) {
        if (this.authzToken == null) {
            return false;
        }

        Authorization authorization = this.authzToken.getAuthorization();

        if (authorization == null) {
            return false;
        }

        for (Permission permission : authorization.getPermissions()) {
            if (resourceName.equalsIgnoreCase(permission.getResourceName()) || resourceName.equalsIgnoreCase(permission.getResourceId())) {
                if (scopeName == null) {
                    return true;
                }

                if (permission.getScopes().contains(scopeName)) {
                    return true;
                }
            }
        }

        if (current != null && scopeName == null) {
            if (current.getName().equals(resourceName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否拥有指定资源的任意 scope 权限。
     *
     * @param resourceName 资源名称
     * @return 有资源级权限时返回 true
     */
    public boolean hasResourcePermission(String resourceName) {
        return hasPermission(resourceName, null);
    }

    /**
     * 判断令牌权限列表中是否包含指定 scope（不限定资源）。
     *
     * @param scopeName scope 名称
     * @return 存在该 scope 时返回 true
     */
    public boolean hasScopePermission(String scopeName) {
        if (this.authzToken == null) {
            return false;
        }

        Authorization authorization = this.authzToken.getAuthorization();

        if (authorization == null) {
            return false;
        }

        for (Permission permission : authorization.getPermissions()) {
            if (permission.getScopes().contains(scopeName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 返回令牌中全部权限的不可变列表；无令牌或无 authorization 声明时返回空列表。
     *
     * @return 权限列表
     */
    public List<Permission> getPermissions() {
        if (this.authzToken == null) {
            return Collections.emptyList();
        }

        Authorization authorization = this.authzToken.getAuthorization();

        if (authorization == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(new ArrayList<>(authorization.getPermissions()));
    }

    /** 返回授权是否已通过。 */
    public boolean isGranted() {
        return granted;
    }
}
