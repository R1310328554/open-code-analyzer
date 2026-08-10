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

package org.keycloak.models.cache.infinispan.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.UserConsentModel;

/**
 * 单条用户授权同意（User Consent）的缓存值对象。
 * <p>
 * 缓存客户端 ID、已授权作用域、参数化作用域参数及创建/更新时间；
 * 亦可通过 {@link #notExistent} 标记表示该客户端无同意记录。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CachedUserConsent {

    /** 客户端数据库 ID。 */
    private final String clientDbId;
    /** 已授权的客户端作用域 ID 集合。 */
    private final Set<String> clientScopeIds = new HashSet<>();
    /** 参数化作用域的参数（按作用域 ID 分组）。 */
    private final MultivaluedHashMap<String,String> parameters = new MultivaluedHashMap<>();
    /** 同意创建时间戳。 */
    private final Long createdDate;
    /** 同意最后更新时间戳。 */
    private final Long lastUpdatedDate;
    /** 标记该客户端不存在同意记录（负缓存）。 */
    private boolean notExistent;

    /** 从用户同意模型构造缓存值对象。 */
    public CachedUserConsent(UserConsentModel consentModel) {
        this.clientDbId = consentModel.getClient().getId();
        for (ClientScopeModel clientScope : consentModel.getGrantedClientScopes()) {
            this.clientScopeIds.add(clientScope.getId());
            if (ClientScopeModel.isParameterizedScope(clientScope)) {
                this.parameters.addAll(clientScope.getId(), consentModel.getParameters(clientScope));
            }
        }
        this.createdDate = consentModel.getCreatedDate();
        this.lastUpdatedDate = consentModel.getLastUpdatedDate();
    }

    /** 构造表示"无同意记录"的负缓存条目。 */
    public CachedUserConsent(String clientDbId) {
        this.clientDbId = clientDbId;
        this.createdDate = null;
        this.lastUpdatedDate = null;
        this.notExistent = true;
    }

    /** 返回客户端数据库 ID。 */
    public String getClientDbId() {
        return clientDbId;
    }

    /** 返回已授权的客户端作用域 ID 集合。 */
    public Set<String> getClientScopeIds() {
        return clientScopeIds;
    }

    /** 返回指定作用域的参数列表。 */
    public List<String> getParameters(String scopeId) {
        return parameters.getList(scopeId);
    }

    /** 返回同意创建时间戳。 */
    public Long getCreatedDate() {
        return createdDate;
    }

    /** 返回同意最后更新时间戳。 */
    public Long getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    /** 返回是否为"无同意记录"的负缓存条目。 */
    public boolean isNotExistent() {
        return notExistent;
    }
}
