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

package org.keycloak.representations.idm;

import java.util.List;
import java.util.Map;

/**
 * 角色集合的 REST 表示，按 realm 与客户端维度分组列出 {@link RoleRepresentation}。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RolesRepresentation {
    /** realm 级角色列表。 */
    protected List<RoleRepresentation> realm;
    /** 客户端 ID 到客户端角色列表的映射。 */
    protected Map<String, List<RoleRepresentation>> client;
    /** @deprecated 应用角色映射（已废弃，由 client 替代）。 */
    @Deprecated
    protected Map<String, List<RoleRepresentation>> application;

    /** @return realm 角色列表 */
    public List<RoleRepresentation> getRealm() {
        return realm;
    }

    /** @param realm realm 角色列表 */
    public void setRealm(List<RoleRepresentation> realm) {
        this.realm = realm;
    }

    /** @return 客户端角色映射 */
    public Map<String, List<RoleRepresentation>> getClient() {
        return client;
    }

    /** @param client 客户端角色映射 */
    public void setClient(Map<String, List<RoleRepresentation>> client) {
        this.client = client;
    }

    /** @return 应用角色映射（已废弃） */
    @Deprecated
    public Map<String, List<RoleRepresentation>> getApplication() {
        return application;
    }
}
