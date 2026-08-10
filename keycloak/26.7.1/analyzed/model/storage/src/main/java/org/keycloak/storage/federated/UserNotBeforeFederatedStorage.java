/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.models.RealmModel;

/**
 * 联邦用户 {@code notBefore} 时间戳存储接口。
 *
 * <p>{@code notBefore} 用于令牌撤销：在此时间之前签发的令牌将被视为无效。
 * 外部用户存储通常不维护该字段，故由联邦存储单独持久化。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface UserNotBeforeFederatedStorage {

    /** 设置联邦用户的 notBefore 时间戳（Unix 秒）。 */
    void setNotBeforeForUser(RealmModel realm, String userId, int notBefore);

    /** 获取联邦用户的 notBefore 时间戳；未设置时返回 0。 */
    int getNotBeforeOfUser(RealmModel realm, String userId);
}
