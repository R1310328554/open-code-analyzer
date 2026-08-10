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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 用户授权同意（User Consent）的 REST 表示，记录用户对客户端 Scope 的授权及时间戳。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class UserConsentRepresentation {

    /** 客户端 ID。 */
    protected String clientId;

    /** 用户已授权的客户端 Scope 名称列表。 */
    protected List<String> grantedClientScopes;

    /** 同意记录创建时间（Unix 毫秒时间戳）。 */
    private Long createdDate;

    /** 同意记录最后更新时间（Unix 毫秒时间戳）。 */
    private Long lastUpdatedDate;

    /** @deprecated 已授权的 realm 角色列表（已废弃）。 */
    @Deprecated
    protected List<String> grantedRealmRoles;

    /** @return 客户端 ID */
    public String getClientId() {
        return clientId;
    }

    /** @param clientId 客户端 ID */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** @return 已授权的客户端 Scope 列表 */
    public List<String> getGrantedClientScopes() {
        return grantedClientScopes;
    }

    /** @param grantedClientScopes 已授权的客户端 Scope 列表 */
    public void setGrantedClientScopes(List<String> grantedClientScopes) {
        this.grantedClientScopes = grantedClientScopes;
    }

    /** @param createdDate 创建时间戳 */
    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    /** @return 创建时间戳 */
    public Long getCreatedDate() {
        return createdDate;
    }

    /** @param lastUpdatedDate 最后更新时间戳 */
    public void setLastUpdatedDate(Long lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    /** @return 最后更新时间戳 */
    public Long getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    /** @return 已授权的 realm 角色列表（已废弃） */
    @Deprecated
    public List<String> getGrantedRealmRoles() {
        return grantedRealmRoles;
    }
}
