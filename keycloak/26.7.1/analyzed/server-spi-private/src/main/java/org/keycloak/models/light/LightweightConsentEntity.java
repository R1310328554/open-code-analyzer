/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models.light;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.keycloak.common.util.CollectionUtil;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.common.util.Time;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserConsentModel;
import org.keycloak.models.utils.KeycloakModelUtils;

/**
 * 轻量用户 OAuth 同意记录的内存实体。
 * <p>序列化存储已授权客户端范围 ID 及参数化 scope 参数，可在 {@link UserConsentModel} 与 JSON 之间转换。</p>
 *
 * @author hmlnarik
 */
class LightweightConsentEntity {

    private String clientId;
    private Long createdDate;
    private Set<String> grantedClientScopesIds;
    private final MultivaluedHashMap<String, String> parameters = new MultivaluedHashMap<>();
    private Long lastUpdatedDate;

    /** 从 {@link UserConsentModel} 构建轻量同意实体（含创建/更新时间戳）。 */
    public static LightweightConsentEntity fromModel(UserConsentModel model) {
        long currentTime = Time.currentTimeMillis();

        LightweightConsentEntity consentEntity = new LightweightConsentEntity();
        consentEntity.setClientId(model.getClient().getId());
        consentEntity.setCreatedDate(currentTime);
        consentEntity.setLastUpdatedDate(currentTime);

        model.getGrantedClientScopes()
          .stream()
          .forEach(m -> consentEntity.addGrantedClientScopesId(m.getId(),
                ClientScopeModel.isParameterizedScope(m) ? model.getParameters(m) : null));

        return consentEntity;
    }

    /** 将轻量同意实体还原为 {@link UserConsentModel}；客户端不存在时抛出 {@link ModelException}。 */
    public static UserConsentModel toModel(RealmModel realm, LightweightConsentEntity entity) {
        if (entity == null) {
            return null;
        }

        ClientModel client = realm.getClientById(entity.getClientId());
        if (client == null) {
            throw new ModelException("Client with id " + entity.getClientId() + " is not available");
        }
        UserConsentModel model = new UserConsentModel(client);
        model.setCreatedDate(entity.getCreatedDate());
        model.setLastUpdatedDate(entity.getLastUpdatedDate());

        Set<String> grantedClientScopesIds = entity.getGrantedClientScopesIds();

        if (grantedClientScopesIds != null && !grantedClientScopesIds.isEmpty()) {
            grantedClientScopesIds.stream()
                    .map(scopeId -> KeycloakModelUtils.findClientScopeById(realm, client, scopeId))
                    .filter(Objects::nonNull)
                    .forEach(m -> {
                        List<String> parameters = entity.parameters.getList(m.getId());
                        if (parameters.isEmpty()) {
                            model.addGrantedClientScope(m);
                        } else {
                            parameters.stream().forEach(p -> model.addGrantedClientScope(m, p));
                        }
                    });
        }

        return model;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, grantedClientScopesIds, lastUpdatedDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LightweightConsentEntity other = (LightweightConsentEntity) obj;
        return Objects.equals(this.clientId, other.clientId)
          && Objects.equals(this.lastUpdatedDate, other.lastUpdatedDate)
          && Objects.equals(this.grantedClientScopesIds, other.grantedClientScopesIds);
    }

    @Override
    public String toString() {
        return String.format("%s@%08x", "LightweightConsentEntity", System.identityHashCode(this));
    }

    /** 关联客户端的内部 ID。 */
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public Set<String> getGrantedClientScopesIds() {
        return grantedClientScopesIds;
    }

    /** 添加已授权客户端范围 ID，可选附带参数化 scope 的参数列表。 */
    public void addGrantedClientScopesId(String clientScopeId, List<String> parameters) {
        if (clientScopeId == null) {
            return;
        }
        if (grantedClientScopesIds == null) {
            grantedClientScopesIds = new HashSet<>();
        }
        grantedClientScopesIds.add(clientScopeId);
        if (CollectionUtil.isNotEmpty(parameters)) {
            this.parameters.addAll(clientScopeId, parameters);
        }
        this.lastUpdatedDate = Time.currentTimeMillis();
    }

    public void addGrantedClientScopesId(String clientScopeId) {
        addGrantedClientScopesId(clientScopeId, null);
    }

    public Long getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Long lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

}
