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

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserConsentModel;

/**
 * 联邦用户 OAuth 同意书（Consent）存储接口：持久化客户端授权同意记录。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserConsentFederatedStorage {

    /** 为联邦用户添加客户端同意书记录。 */
    void addConsent(RealmModel realm, String userId, UserConsentModel consent);

    /**
     * 按客户端内部 ID 查询联邦用户的同意书。
     *
     * @param realm            所属 realm
     * @param userId           用户 ID
     * @param clientInternalId 客户端内部 ID
     * @return 同意书模型，不存在时返回 {@code null}
     */
    UserConsentModel getConsentByClient(RealmModel realm, String userId, String clientInternalId);

    /**
     * 获取指定联邦用户的全部客户端同意书。
     *
     * @param realm a reference to the realm.
     * @param userId the user identifier.
     * @return a non-null {@link Stream} of consents associated with the user.
     */
    Stream<UserConsentModel> getConsentsStream(RealmModel realm, String userId);

    /** 更新联邦用户的同意书记录。 */
    void updateConsent(RealmModel realm, String userId, UserConsentModel consent);

    /**
     * 撤销联邦用户对指定客户端的同意。
     *
     * @return 成功撤销返回 {@code true}
     */
    boolean revokeConsentForClient(RealmModel realm, String userId, String clientInternalId);

    /**
     * @deprecated 父接口已移除基于集合的方法，可直接使用本接口，无需再继承此 Streams 子接口。
     */
    @Deprecated
    interface Streams extends UserConsentFederatedStorage {
    }
}
