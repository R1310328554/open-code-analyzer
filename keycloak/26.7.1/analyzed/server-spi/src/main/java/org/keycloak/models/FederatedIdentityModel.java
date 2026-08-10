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

package org.keycloak.models;

import org.keycloak.provider.ProviderEvent;

/**
 * 联邦身份模型：用户与外部 IdP 的关联（提供者别名、联邦用户 ID、用户名、令牌）。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FederatedIdentityModel {

    private String token;
    private final String userId;
    private final String identityProvider;
    private final String userName;

    /** @param providerAlias IdP 别名
     * @param userId 联邦侧用户 ID
     * @param userName 联邦侧用户名 */
    public FederatedIdentityModel(String providerAlias, String userId, String userName) {
        this(providerAlias, userId, userName, null);
    }

    /** 含访问令牌的构造。 */
    public FederatedIdentityModel(String providerAlias, String userId, String userName, String token) {
        this.identityProvider = providerAlias;
        this.userId = userId;
        this.userName = userName;
        this.token = token;
    }

    /** 复制联邦身份并替换用户 ID。 */
    public FederatedIdentityModel(FederatedIdentityModel originalIdentity, String userId) {
        identityProvider = originalIdentity.getIdentityProvider();
        this.userId = userId;
        userName = originalIdentity.getUserName();
        token = originalIdentity.getToken();
    }

    /** @return 联邦侧用户 ID */
    public String getUserId() {
        return userId;
    }

    /** @return 身份提供者别名 */
    public String getIdentityProvider() {
        return identityProvider;
    }

    /** @return 联邦侧用户名 */
    public String getUserName() {
        return userName;
    }

    /** @return 关联的访问令牌（若有） */
    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FederatedIdentityModel that = (FederatedIdentityModel) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (!identityProvider.equals(that.identityProvider)) return false;
        return userName != null ? userName.equals(that.userName) : that.userName == null;

    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + identityProvider.hashCode();
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        return result;
    }

    /** 联邦身份创建事件。 */
    public interface FederatedIdentityCreatedEvent extends ProviderEvent {
        KeycloakSession getKeycloakSession();
        RealmModel getRealm();
        UserModel getUser();
        FederatedIdentityModel getFederatedIdentity();
    }

    /** 联邦身份移除事件。 */
    public interface FederatedIdentityRemovedEvent extends ProviderEvent {
        KeycloakSession getKeycloakSession();
        RealmModel getRealm();
        UserModel getUser();
        FederatedIdentityModel getFederatedIdentity();
    }
}
