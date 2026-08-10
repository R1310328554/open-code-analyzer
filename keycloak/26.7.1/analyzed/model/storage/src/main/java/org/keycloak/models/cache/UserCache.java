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

package org.keycloak.models.cache;

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;

/**
 * 用户缓存 Provider 接口：扩展 {@link UserProvider}，支持集群范围的缓存驱逐。
 * <p>
 * 本接口所有方法均影响整个 Keycloak 集群实例。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserCache extends UserProvider {
    /**
     * 从缓存中驱逐指定用户。
     *
     * @param realm 用户所属 realm
     * @param user 待驱逐用户
     */
    void evict(RealmModel realm, UserModel user);

    /**
     * 驱逐指定 realm 的全部缓存用户。
     *
     * @param realm 目标 realm
     */
    void evict(RealmModel realm);

    /**
     * 清空全部用户缓存。
     */
    void clear();
}
