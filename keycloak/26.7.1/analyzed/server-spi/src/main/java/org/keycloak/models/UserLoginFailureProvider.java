/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.provider.Provider;

/**
 * 用户登录失败 Provider：管理 realm 内用户暴力破解/登录失败计数与锁定状态。
 *
 * @author <a href="mailto:mkanis@redhat.com">Martin Kanis</a>
 */
public interface UserLoginFailureProvider extends Provider {

    /**
     * 返回指定 realm 与用户 ID 的 {@link UserLoginFailureModel}。
     * Returns the {@link UserLoginFailureModel} for the given realm and user id.
     * @param realm {@link RealmModel}
     * @param userId {@link String} Id of the user.
     * @return Returns the {@link UserLoginFailureModel} for the given realm and user id.
     */
    UserLoginFailureModel getUserLoginFailure(RealmModel realm, String userId);

    /**
     * 为指定 realm 与用户 ID 新增 {@link UserLoginFailureModel}。
     * Adds a {@link UserLoginFailureModel} for the given realm and user id.
     * @param realm {@link RealmModel}
     * @param userId {@link String} Id of the user.
     * @return Returns newly created {@link UserLoginFailureModel}.
     */
    UserLoginFailureModel addUserLoginFailure(RealmModel realm, String userId);

    /**
     * 移除指定 realm 与用户 ID 的 {@link UserLoginFailureModel}。
     * Removes a {@link UserLoginFailureModel} for the given realm and user id.
     * @param realm {@link RealmModel}
     * @param userId {@link String} Id of the user.
     */
    void removeUserLoginFailure(RealmModel realm, String userId);

    /**
     * 移除指定 realm 的全部 {@link UserLoginFailureModel}。
     * Removes all the {@link UserLoginFailureModel} for the given realm.
     * @param realm {@link RealmModel}
     */
    void removeAllUserLoginFailures(RealmModel realm);

    /**
     * 当 realm 暴力破解超时相关设置变更时调用。
     * This is called when the realm settings change in relation to the brute force timeouts.
     */
    default void updateWithLatestRealmSettings(RealmModel realm) {};

}
