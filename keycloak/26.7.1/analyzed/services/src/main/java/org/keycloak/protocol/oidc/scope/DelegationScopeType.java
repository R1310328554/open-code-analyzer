/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.protocol.oidc.scope;

import jakarta.annotation.Nonnull;

import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.fgap.AdminPermissions;

/**
 * 委托（impersonation）参数化 scope 类型。
 * <p>基于 {@link UsernameScopeType}，校验目标用户存在且当前用户具备对其的模拟权限；不允许用户指定自身为委托目标。</p>
 *
 * @author rmartinc
 */
public class DelegationScopeType extends UsernameScopeType {

    /** 类型标识：delegation */
    public static final String TYPE = "delegation";

    /** 无参构造，供 SPI 反射实例化 */
    public DelegationScopeType() {
    }

    /** @param session Keycloak 会话 */
    public DelegationScopeType(KeycloakSession session) {
        super(session);
    }

    /** @return 类型名称 {@link #TYPE} */
    @Override
    public String getTypeName() {
        return TYPE;
    }

    /** @return 是否允许重复（委托 scope 不可重复） */
    @Override
    public boolean isRepeatable() {
        return false;
    }

    /** @param session Keycloak 会话 @return 带会话的实例 */
    @Override
    public ParameterizedScopeTypeProvider create(KeycloakSession session) {
        return new DelegationScopeType(session);
    }

    /**
     * 校验委托目标用户：禁止指向自身，且当前用户须具备模拟权限。
     * @param currentUser 当前登录用户
     * @param scope 客户端范围
     * @param parameter 目标用户名参数
     * @throws InvalidScopeParameterException 校验失败时
     */
    @Override
    public void validateParameterWithUser(@Nonnull UserModel currentUser, @Nonnull ClientScopeModel scope, @Nonnull String parameter) throws InvalidScopeParameterException {
        UserModel targetUser = resolveUser(scope, parameter);
        if (targetUser.getId().equals(currentUser.getId())) {
            throw new InvalidScopeParameterException("User cannot target themselves");
        }
        RealmModel realm = scope.getRealm();
        AdminPermissionEvaluator evaluator = AdminPermissions.evaluator(session, realm, realm, targetUser);
        if (!evaluator.users().canImpersonate(currentUser, session.getContext().getClient())) {
            throw new InvalidScopeParameterException(String.format("User '%s' cannot be impersonated by the administrator '%s' in realm '%s'",
                    currentUser.getUsername(), targetUser.getUsername(), realm.getName()));
        }
    }

}
