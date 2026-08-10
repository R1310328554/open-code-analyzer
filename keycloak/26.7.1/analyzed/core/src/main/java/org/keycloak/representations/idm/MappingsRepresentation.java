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
 * 用户或组的有效角色映射汇总表示，包含 realm 级与客户端级映射。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class MappingsRepresentation {
    /** realm 级角色映射列表。 */
    protected List<RoleRepresentation> realmMappings;
    /** 客户端 ID 到客户端角色映射的映射表。 */
    protected Map<String, ClientMappingsRepresentation> clientMappings;

    /** @return realm 级角色映射 */
    public List<RoleRepresentation> getRealmMappings() {
        return realmMappings;
    }

    /** @param realmMappings realm 级角色映射 */
    public void setRealmMappings(List<RoleRepresentation> realmMappings) {
        this.realmMappings = realmMappings;
    }

    /** @return 客户端级角色映射 */
    public Map<String, ClientMappingsRepresentation> getClientMappings() {
        return clientMappings;
    }

    /** @param clientMappings 客户端级角色映射 */
    public void setClientMappings(Map<String, ClientMappingsRepresentation> clientMappings) {
        this.clientMappings = clientMappings;
    }
}
