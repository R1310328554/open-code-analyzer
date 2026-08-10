/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.policy;

import java.util.LinkedList;
import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * 默认密码策略管理器：按 realm 已启用的策略顺序依次调用各 {@link PasswordPolicyProvider} 校验密码。
 * <p>任一策略返回 {@link PolicyError} 即终止并返回该错误。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class DefaultPasswordPolicyManagerProvider implements PasswordPolicyManagerProvider {

    private KeycloakSession session;

    /** @param session Keycloak 会话 */
    public DefaultPasswordPolicyManagerProvider(KeycloakSession session) {
        this.session = session;
    }

    /** 在指定 realm 与用户上下文中校验密码。 */
    @Override
    public PolicyError validate(RealmModel realm, UserModel user, String password) {
        for (PasswordPolicyProvider p : getProviders(realm, session)) {
            PolicyError policyError = p.validate(realm, user, password);
            if (policyError != null) {
                return policyError;
            }
        }
        return null;
    }

    /** 使用当前会话 realm 校验密码（无用户模型）。 */
    @Override
    public PolicyError validate(String user, String password) {
        for (PasswordPolicyProvider p : getProviders(session)) {
            PolicyError policyError = p.validate(user, password);
            if (policyError != null) {
                return policyError;
            }
        }
        return null;
    }

    @Override
    public void close() {
    }

    private List<PasswordPolicyProvider> getProviders(KeycloakSession session) {
        return getProviders(session.getContext().getRealm(), session);

    }

    /** 按 realm 密码策略配置顺序解析全部 {@link PasswordPolicyProvider}。 */
    private List<PasswordPolicyProvider> getProviders(RealmModel realm, KeycloakSession session) {
        LinkedList<PasswordPolicyProvider> list = new LinkedList<>();
        PasswordPolicy policy = realm.getPasswordPolicy();
        for (String id : policy.getPolicies()) {
            PasswordPolicyProvider provider = session.getProvider(PasswordPolicyProvider.class, id);
            list.add(provider);
        }
        return list;
    }

}
