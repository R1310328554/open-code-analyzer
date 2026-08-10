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

package org.keycloak.broker.provider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;

/**
 * 身份提供方映射器 SPI：将外部 IdP 用户属性映射到 Keycloak 用户。
 * <p>同时作为 {@link Provider}、{@link ProviderFactory} 与 {@link ConfiguredProvider}，在首次登录、导入与后续同步阶段更新联邦用户。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface IdentityProviderMapper extends Provider, ProviderFactory<IdentityProviderMapper>,ConfiguredProvider {
    /** 表示映射器兼容任意身份提供方类型的通配符。 */
    String ANY_PROVIDER = "*";
    /** 映射器默认支持的 IdP 同步模式集合（LEGACY 与 IMPORT）。 */
    Set<IdentityProviderSyncMode> DEFAULT_IDENTITY_PROVIDER_MAPPER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.LEGACY, IdentityProviderSyncMode.IMPORT));

    /** 返回此映射器兼容的身份提供方类型 ID 列表。 */
    String[] getCompatibleProviders();
    /** 管理控制台中映射器的显示分类。 */
    String getDisplayCategory();
    /** 管理控制台中映射器的显示类型名称。 */
    String getDisplayType();

    /** 判断映射器是否显式支持给定 {@link IdentityProviderSyncMode}。 */
    default boolean supportsSyncMode(IdentityProviderSyncMode syncMode) {
        return DEFAULT_IDENTITY_PROVIDER_MAPPER_SYNC_MODES.contains(syncMode);
    }

    /**
     * 在 FirstBrokerLogin 之前预处理联邦身份：确定用户名/邮箱并将属性写入 {@link BrokeredIdentityContext}。
     *
     * Called to determine what keycloak username and email to use to process the login request from the external IDP.
     * It's called before "FirstBrokerLogin" flow, so can be used to map attributes to BrokeredIdentityContext ( BrokeredIdentityContext.setUserAttribute ),
     * which will be available on "Review Profile" page and in authenticators during FirstBrokerLogin flow
     *
     *
     * @param session
     * @param realm
     * @param mapperModel
     * @param context
     */
    void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context);

    /**
     * 首次创建本地用户并完成 FirstBrokerLogin 流程后调用，用于导入联邦用户属性。
     *
     * Called after UserModel is created for first time for this user. Called after "FirstBrokerLogin" flow
     *
     * @param session
     * @param realm
     * @param user
     * @param mapperModel
     * @param context
     */
    void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context);

    /**
     * 用户已导入时的遗留同步逻辑；新实现应迁移至 {@link #updateBrokeredUser}。
     *
     * 用户已存在时按当前同步模式更新联邦用户属性。
     *
     * Called when this user has logged in before and has already been imported. Legacy behaviour. When updating the mapper to correctly update brokered users
     * in all sync modes, move the old behavior into this method.
     *
     * @param session
     * @param realm
     * @param user
     * @param mapperModel
     * @param context
     */
    void updateBrokeredUserLegacy(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context);

    /**
     * Called when this user has logged in before and has already been imported.
     *
     * @param session
     * @param realm
     * @param user
     * @param mapperModel
     * @param context
     */
    void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context);
}
