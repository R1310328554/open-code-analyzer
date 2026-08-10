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

package org.keycloak.migration;

import java.util.List;
import java.util.Map;

import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.Provider;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;

/**
 * 数据库/模型版本迁移提供者 SPI：提供跨版本升级所需的通用操作。
 * <p>包括 claim 掩码映射、内置协议映射器、OIDC 默认客户端范围创建等。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface MigrationProvider extends Provider {

    /**
     * 将 1.1.0 客户端 {@link ClaimMask} 转换为 1.2.0.Beta1 协议映射器列表。
     * @param claimMask mask used on ClientModel in 1.1.0
     * @return set of 1.2.0.Beta1 protocol mappers corresponding to given claimMask
     */
    List<ProtocolMapperRepresentation> getMappersForClaimMask(Long claimMask);

    /** 获取指定协议的内置协议映射器模板。 */
    Map<String, ProtocolMapperModel> getBuiltinMappers(String protocol);

    /** 为领域配置 admin-cli 客户端及所需角色。 */
    void setupAdminCli(RealmModel realm);


    /**
     * 添加 {@code roles} OIDC 客户端范围，已存在则直接返回。
     *
     * @param realm
     * @return created or already existing client scope 'roles'
     */
    ClientScopeModel addOIDCRolesClientScope(RealmModel realm);


    /**
     * 添加 {@code web-origins} OIDC 客户端范围，已存在则直接返回。
     *
     * @param realm
     * @return created or already existing client scope 'web-origins'
     */
    ClientScopeModel addOIDCWebOriginsClientScope(RealmModel realm);

    /**
     * 添加可选客户端范围 {@code microprofile-jwt}，已存在则返回现有范围。
     *
     * @param realm the realm to which the scope is to be added.
     * @return a reference to the {@code microprofile-jwt} client scope that was either created or already exists in the realm.
     */
    ClientScopeModel addOIDCMicroprofileJWTClientScope(RealmModel realm);

    /**
     * 添加 {@code acr} OIDC 客户端范围，已存在则直接返回。
     *
     * @param realm
     * @return created or already existing client scope 'acr'
     */
    ClientScopeModel addOIDCAcrClientScope(RealmModel realm);

    /**
     * 添加 {@code basic} OIDC 客户端范围，已存在则直接返回。
     *
     * @param realm
     * @return created or already existing client scope 'basic'
     */
    ClientScopeModel addOIDCBasicClientScope(RealmModel realm);

    /**
     * 添加 {@code service_account} OIDC 客户端范围，已存在则直接返回。
     *
     * @param realm
     * @return created or already existing client scope 'service_account'
     */
    ClientScopeModel addOIDCServiceAccountClientScope(RealmModel realm);

    /**
     * 为领域添加 SAML 步进认证 {@code AuthnContextClassRef} 映射器客户端范围。
     * @param realm
     * @return created, already existing client scope or null if not step-up not enabled
     */
    ClientScopeModel addSamlAuthnContextClassRefClientScope(RealmModel realm);
}
