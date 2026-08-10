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
package org.keycloak.storage.user;

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * 已导入用户校验能力接口（可选）。
 *
 * <p>由支持用户校验的 {@link org.keycloak.storage.UserStorageProvider UserStorageProvider} 实现。
 * 若存储将用户导入 Keycloak 本地库并需与外部源保持同步，必须实现此接口。
 * 当 Keycloak 查询已导入用户时会调用 {@link #validate(RealmModel, UserModel) validate()}；
 * 若返回 {@code null}，该用户将从本地存储移除并重新从外部存储加载。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ImportedUserValidation {

    /**
     * 校验已导入用户是否仍在外部存储中有效。
     *
     * <p>返回 {@code null} 时，本地存储中的对应用户将被移除。
     *
     * @param realm
     * @param user
     * @return null if user no longer valid
     */
    UserModel validate(RealmModel realm, UserModel user);
}
