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

/**
 * 联邦用户必需操作（Required Actions）存储接口。
 *
 * <p>管理外部用户在下次登录时必须完成的操作（如更新密码、配置 OTP 等）。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserRequiredActionsFederatedStorage {

    /**
     * 获取 {@code userId} 标识的联邦用户关联的全部必需操作名称。
     *
     * @param realm a reference to the realm.
     * @param userId the user identifier.
     * @return a non-null {@link Stream} of required action names.
     */
    Stream<String> getRequiredActionsStream(RealmModel realm, String userId);

    /** 为联邦用户添加必需操作。 */
    void addRequiredAction(RealmModel realm, String userId, String action);

    /** 移除联邦用户的指定必需操作。 */
    void removeRequiredAction(RealmModel realm, String userId, String action);

    /**
     * @deprecated This interface is no longer necessary; collection-based methods were removed from the parent interface
     * and therefore the parent interface can be used directly
     */
    @Deprecated
    interface Streams extends UserRequiredActionsFederatedStorage {
    }
}
