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

package org.keycloak.partialimport;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.connections.jpa.support.EntityManagers;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.PartialImportRepresentation;

/**
 * 部分导入管理器：按固定顺序编排各资源类型处理器的 prepare、删除覆盖与导入。
 * <p>顺序为客户端 → 角色 → IdP → IdP 映射器 → 群组 → 用户。</p>
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
public class PartialImportManager {
    /** 按依赖顺序注册的部分导入处理器列表。 */
    private final List<PartialImport> partialImports = new ArrayList<>();

    private final PartialImportRepresentation rep;
    private final KeycloakSession session;
    private final RealmModel realm;

    /**
     * 初始化管理器并注册各资源处理器（顺序不可变更）。
     * @param rep 部分导入表示
     * @param session Keycloak 会话
     * @param realm 目标 Realm
     */
    public PartialImportManager(PartialImportRepresentation rep, KeycloakSession session,
                                RealmModel realm) {
        this.rep = rep;
        this.session = session;
        this.realm = realm;

        // 切勿更改以下处理器的注册顺序！！！
        partialImports.add(new ClientsPartialImport());
        partialImports.add(new RolesPartialImport());
        partialImports.add(new IdentityProvidersPartialImport());
        partialImports.add(new IdentityProviderMappersPartialImport());
        partialImports.add(new GroupsPartialImport());
        partialImports.add(new UsersPartialImport());
    }

    /** 执行完整部分导入流程并返回汇总结果。 */
    public PartialImportResults saveResources() {
        PartialImportResults results = new PartialImportResults();

        for (PartialImport partialImport : partialImports) {
            partialImport.prepare(rep, realm, session);
        }

        for (PartialImport partialImport : partialImports) {
            partialImport.removeOverwrites(realm, session);
            EntityManagers.flush(session, false);
            results.addAllResults(partialImport.doImport(rep, realm, session));
        }

        return results;
    }

}
