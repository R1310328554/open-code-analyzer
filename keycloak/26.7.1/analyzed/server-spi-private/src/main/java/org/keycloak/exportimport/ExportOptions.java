/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.exportimport;

/**
 * Realm 导出选项：控制是否包含用户、客户端、组/角色及服务账号子集。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class ExportOptions {

    private boolean usersIncluded = true;
    private boolean clientsIncluded = true;
    private boolean groupsAndRolesIncluded = true;
    private boolean onlyServiceAccountsIncluded = false;
    private boolean partial;

    /** 默认导出全部用户、客户端与组/角色。 */
    public ExportOptions() {
    }

    /**
     * 指定各导出维度。
     *
     * @param users 是否包含用户
     * @param clients 是否包含客户端
     * @param groupsAndRoles 是否包含组与角色
     * @param onlyServiceAccounts 是否仅导出服务账号用户
     * @param partial 是否为部分导出
     */
    public ExportOptions(boolean users, boolean clients, boolean groupsAndRoles, boolean onlyServiceAccounts, boolean partial) {
        usersIncluded = users;
        clientsIncluded = clients;
        groupsAndRolesIncluded = groupsAndRoles;
        onlyServiceAccountsIncluded = onlyServiceAccounts;
        this.partial = partial;
    }

    /** @return 导出是否包含用户 */
    public boolean isUsersIncluded() {
        return usersIncluded;
    }

    /** @return 导出是否包含客户端 */
    public boolean isClientsIncluded() {
        return clientsIncluded;
    }

    /** @return 导出是否包含组与角色 */
    public boolean isGroupsAndRolesIncluded() {
        return groupsAndRolesIncluded;
    }

    /** @return 是否仅导出服务账号用户 */
    public boolean isOnlyServiceAccountsIncluded() {
        return onlyServiceAccountsIncluded;
    }

    public void setUsersIncluded(boolean value) {
        usersIncluded = value;
    }

    public void setClientsIncluded(boolean value) {
        clientsIncluded = value;
    }

    public void setGroupsAndRolesIncluded(boolean value) {
        groupsAndRolesIncluded = value;
    }

    public void setOnlyServiceAccountsIncluded(boolean value) {
        onlyServiceAccountsIncluded = value;
    }

    /** @return 是否为部分导出（非完整 realm 快照） */
    public boolean isPartial() {
        return partial;
    }
}
