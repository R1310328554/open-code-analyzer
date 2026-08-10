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
package org.keycloak.storage;

import org.keycloak.models.GroupModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.provider.Provider;

/**
 * Keycloak 用户存储 Provider 标记接口。
 * <p>
 * 本接口仅含基础生命周期回调；完整能力通过实现以下能力接口扩展：
 * <ul>
 *     <li>{@link org.keycloak.storage.user.UserLookupProvider UserLookupProvider} — 基础查找，实现后可从该存储登录。</li>
 *     <li>{@link org.keycloak.storage.user.UserQueryMethodsProvider UserQueryMethodsProvider} — 复杂查询，实现后可在管理控制台管理用户。</li>
 *     <li>{@link org.keycloak.storage.user.UserCountMethodsProvider UserCountMethodsProvider} — 计数优化，实现后可加速用户查询。</li>
 *     <li>{@link org.keycloak.storage.user.UserQueryProvider UserQueryProvider} — {@code UserQueryMethodsProvider} 与 {@code UserCountMethodsProvider} 的组合能力。</li>
 *     <li>{@link org.keycloak.storage.user.UserRegistrationProvider UserRegistrationProvider} — 用户注册写入外部存储。</li>
 *     <li>{@link org.keycloak.storage.user.UserBulkUpdateProvider UserBulkUpdateProvider} — 对存储内全部用户执行批量操作（如批量赋角色）。</li>
 *     <li>{@link org.keycloak.storage.user.ImportedUserValidation ImportedUserValidation} — 校验自存储导入到 Keycloak 本地库的用户。</li>
 * </ul>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserStorageProvider extends Provider {


    /**
     * realm 删除前的回调；可在此清理外部存储中的关联数据。
     *
     * @param realm 待删除 realm
     */
    default void preRemove(RealmModel realm) {

    }

    /**
     * 组删除前的回调；可同步移除外部存储中的用户-组映射。
     *
     * @param realm 所属 realm
     * @param group 待删除组
     */
    default void preRemove(RealmModel realm, GroupModel group) {

    }

    /**
     * 角色删除前的回调；可同步移除外部存储中的用户-角色映射。
     *
     * @param realm 所属 realm
     * @param role 待删除角色
     */
    default void preRemove(RealmModel realm, RoleModel role) {

    }

    /**
     * 用户存储编辑模式枚举，供实现类描述用户数据的可写性。
     */
    enum EditMode {
        /**
         * 只读：用户存储不可修改。
         */
        READ_ONLY,
        /**
         * 可写：用户存储支持更新。
         */
        WRITABLE,
        /**
         * 不同步：更新仅保存在 Keycloak 本地，不回写用户存储。
         */
        UNSYNCED
    }
}
