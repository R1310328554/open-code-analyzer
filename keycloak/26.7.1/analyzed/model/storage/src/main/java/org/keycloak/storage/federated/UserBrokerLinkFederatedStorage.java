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
package org.keycloak.storage.federated;

import java.util.stream.Stream;

import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.RealmModel;

/**
 * 联邦用户身份代理（Broker）链接存储接口：管理外部 IdP 与联邦用户的关联关系。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserBrokerLinkFederatedStorage {

    /**
     * 根据联邦身份查找对应用户 ID。
     *
     * @param socialLink 联邦身份模型
     * @param realm      所属 realm
     * @return 关联的用户 ID，未找到时返回 {@code null}
     */
    String getUserByFederatedIdentity(FederatedIdentityModel socialLink, RealmModel realm);

    /** 为联邦用户添加身份代理链接。 */
    void addFederatedIdentity(RealmModel realm, String userId, FederatedIdentityModel socialLink);

    /**
     * 移除联邦用户与指定身份提供商的链接。
     *
     * @return 成功移除返回 {@code true}
     */
    boolean removeFederatedIdentity(RealmModel realm, String userId, String socialProvider);

    /** 身份提供商删除前的回调，可清理相关联邦链接。 */
    void preRemove(RealmModel realm, IdentityProviderModel provider);

    /** 更新联邦用户的身份代理链接信息。 */
    void updateFederatedIdentity(RealmModel realm, String userId, FederatedIdentityModel federatedIdentityModel);

    /**
     * 获取指定联邦用户的全部身份代理链接。
     *
     * @param userId the user identifier.
     * @param realm a reference to the realm.
     * @return a non-null {@link Stream} of federated identities associated with the user.
     */
    Stream<FederatedIdentityModel> getFederatedIdentitiesStream(String userId, RealmModel realm);

    /**
     * 获取联邦用户与指定身份提供商的单条链接。
     *
     * @param userId         用户 ID
     * @param socialProvider 身份提供商标识
     * @param realm          所属 realm
     * @return 联邦身份模型，不存在时返回 {@code null}
     */
    FederatedIdentityModel getFederatedIdentity(String userId, String socialProvider, RealmModel realm);

    /**
     * @deprecated 父接口已移除基于集合的方法，可直接使用本接口，无需再继承此 Streams 子接口。
     */
    @Deprecated
    interface Streams extends UserBrokerLinkFederatedStorage {
    }
}
