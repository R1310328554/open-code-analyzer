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
package org.keycloak.services.resources.admin.fgap;

import org.keycloak.models.RealmModel;

/**
 * 跨领域（Realms）管理权限评估接口。
 * <p>用于判断管理员对指定领域的查看/管理权限，以及创建新领域的能力。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RealmsPermissionEvaluator {
    /** 是否可查看指定领域（含 view-realm 或 manage-realm 角色）。 */
    boolean canView(RealmModel realm);

    /** 指定领域上是否具备任一管理角色。 */
    boolean isAdmin(RealmModel realm);

    /** 当前调用者是否为管理员（master 或所在领域）。 */
    boolean isAdmin();

    /** 是否可创建新领域（master 领域的 create-realm 角色）。 */
    boolean canCreateRealm();

    /** 要求具备创建领域权限，否则抛出 403。 */
    void requireCreateRealm();
}
