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

package org.keycloak.models;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.keycloak.common.util.MultivaluedHashMap;

/**
 * 用户同意模型：记录用户对客户端/作用域的 OAuth 授权同意。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UserConsentModel {

    private final ClientModel client;
    private final Set<ClientScopeModel> clientScopes = new HashSet<>();
    private final MultivaluedHashMap<String, String> parameters = new MultivaluedHashMap<>();
    private Long createdDate;
    private Long lastUpdatedDate;

    /** @param client 关联客户端 */
    public UserConsentModel(ClientModel client) {
        this.client = client;
    }

    /** @return 关联客户端 */
    public ClientModel getClient() {
        return client;
    }

    /** @param clientScope 已授权客户端作用域 */
    public void addGrantedClientScope(ClientScopeModel clientScope) {
        addGrantedClientScope(clientScope, null);
    }

    /** @param clientScope 已授权客户端作用域
     * @param parameter 参数化作用域的参数值 */
    public void addGrantedClientScope(ClientScopeModel clientScope, String parameter) {
        if (clientScope.isAlwaysConsent()) {
            // always consent scopes are skipped
            return;
        }
        clientScopes.add(clientScope);
        if (ClientScopeModel.isParameterizedScope(clientScope)) {
            if (parameter == null) {
                throw new IllegalArgumentException("Parameter value is compulsory for Parameterized Scope " + clientScope.getName());
            }
            parameters.add(clientScope.getId(), parameter);
        }
    }

    /** @return 已授权的客户端作用域集合 */
    public Set<ClientScopeModel> getGrantedClientScopes() {
        return clientScopes;
    }

    /** @param clientScope 客户端作用域
     * @return 参数化作用域的参数值列表 */
    public List<String> getParameters(ClientScopeModel clientScope) {
        if (ClientScopeModel.isParameterizedScope(clientScope)) {
            return parameters.getList(clientScope.getId());
        }
        return Collections.emptyList();
    }

    /** @param clientScope 待检查作用域
     * @return 是否已授权 */
    public boolean isClientScopeGranted(ClientScopeModel clientScope) {
        return isClientScopeGranted(clientScope, null);
    }

    /** @param clientScope 待检查作用域
     * @param parameter 参数化作用域参数
     * @return 是否已授权 */
    public boolean isClientScopeGranted(ClientScopeModel clientScope, String parameter) {
        for (ClientScopeModel apprClientScope : clientScopes) {
            if (apprClientScope.getId().equals(clientScope.getId())) {
                if (ClientScopeModel.isParameterizedScope(clientScope)) {
                    return parameter != null && parameters.getList(apprClientScope.getId()).contains(parameter);
                } else {
                    return parameter == null && parameters.getList(apprClientScope.getId()).isEmpty();
                }
            }
        }
        return false;
    }

    /** @return 同意创建时间戳 */
    public Long getCreatedDate() {
        return createdDate;
    }

    /** @param createdDate 同意创建时间戳 */
    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    /** @return 同意最后更新时间戳 */
    public Long getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    /** @param lastUpdatedDate 同意最后更新时间戳 */
    public void setLastUpdatedDate(Long lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
}
