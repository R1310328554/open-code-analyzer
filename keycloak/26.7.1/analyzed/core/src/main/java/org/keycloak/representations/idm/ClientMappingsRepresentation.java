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

/**
 * 单个客户端的角色映射 REST 表示，将客户端 ID 与其可访问的角色列表关联。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClientMappingsRepresentation {
    /** 映射记录 ID。 */
    protected String id;
    /** 客户端 ID 或 clientId 字符串。 */
    protected String client;

    /** 该客户端映射的角色列表。 */
    protected List<RoleRepresentation> mappings;

    /** @return 映射记录 ID */
    public String getId() {
        return id;
    }

    /** @param id 映射记录 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 客户端标识 */
    public String getClient() {
        return client;
    }

    /** @param client 客户端标识 */
    public void setClient(String client) {
        this.client = client;
    }

    /** @return 角色映射列表 */
    public List<RoleRepresentation> getMappings() {
        return mappings;
    }

    /** @param mappings 角色映射列表 */
    public void setMappings(List<RoleRepresentation> mappings) {
        this.mappings = mappings;
    }
}
