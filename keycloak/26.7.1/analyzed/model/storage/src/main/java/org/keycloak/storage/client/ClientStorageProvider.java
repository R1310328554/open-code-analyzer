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
package org.keycloak.storage.client;

import org.keycloak.models.GroupModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.provider.Provider;

/**
 * 客户端存储 Provider 的基础接口，为客户端提供替代存储机制。
 * <p>
 * 当前为私有且不完整的 SPI。如需完善或自行实现，请在开发邮件列表讨论。
 * 相关工作见 JIRA KEYCLOAK-6408。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ClientStorageProvider extends Provider, ClientLookupProvider {


    /**
     * realm 删除前的回调；可在外部存储中清理与该 realm 关联的客户端数据。
     *
     * @param realm 待删除 realm
     */
    default
    void preRemove(RealmModel realm) {

    }

    /**
     * 组删除前的回调；可同步移除外部存储中的客户端-组映射（如适用）。
     *
     * @param realm 所属 realm
     * @param group 待删除组
     */
    default
    void preRemove(RealmModel realm, GroupModel group) {

    }

    /**
     * 角色删除前的回调；可同步移除外部存储中的客户端-角色映射（如适用）。
     *
     * @param realm 所属 realm
     * @param role 待删除角色
     */
    default
    void preRemove(RealmModel realm, RoleModel role) {

    }
}
