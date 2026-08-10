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

package org.keycloak.testframework.realm;

import java.util.HashMap;

import org.keycloak.representations.idm.IdentityProviderRepresentation;

/**
 * {@link IdentityProviderRepresentation} 的流式构建器，用于在测试中配置外部身份提供者。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class IdentityProviderBuilder extends Builder<IdentityProviderRepresentation> {

    /** 基于已有 IdP 表示对象构造构建器。 */
    private IdentityProviderBuilder(IdentityProviderRepresentation rep) {
        super(rep);
    }

    /** 创建空的身份提供者构建器。 */
    public static IdentityProviderBuilder create() {
        return new IdentityProviderBuilder(new IdentityProviderRepresentation());
    }

    /** 设置 IdP 别名（登录页与联邦链接中的标识）。 */
    public IdentityProviderBuilder alias(String alias) {
        rep.setAlias(alias);
        return this;
    }

    /** 设置 IdP 实现提供者 ID（如 oidc、saml）。 */
    public IdentityProviderBuilder providerId(String providerId) {
        rep.setProviderId(providerId);
        return this;
    }

    /** 设置登录页展示名称。 */
    public IdentityProviderBuilder displayName(String displayName) {
        rep.setDisplayName(displayName);
        return this;
    }

    /** 在登录页隐藏该 IdP。 */
    public IdentityProviderBuilder hideOnLoginPage() {
        rep.setHideOnLogin(true);
        return this;
    }

    /** 设置是否在用户会话中存储 IdP 令牌。 */
    public IdentityProviderBuilder storeToken(boolean storeToken) {
        rep.setStoreToken(storeToken);
        return this;
    }

    /** 设置首次联邦登录时是否从 IdP 令牌读取角色。 */
    public IdentityProviderBuilder addReadTokenRoleOnCreate(boolean addReadTokenRoleOnCreate) {
        rep.setAddReadTokenRoleOnCreate(addReadTokenRoleOnCreate);
        return this;
    }

    /** 添加 IdP 配置项键值对。 */
    public IdentityProviderBuilder attribute(String name, String value) {
        rep.setConfig(createIfNull(rep.getConfig(), HashMap::new));
        rep.getConfig().put(name, value);
        return this;
    }

    /** 返回底层 {@link IdentityProviderRepresentation}。 */
    public IdentityProviderRepresentation build() {
        return rep;
    }

}
