/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.protection.resource;

import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.authorization.model.Resource;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
/**
 * 扩展 {@link ResourceRepresentation}，适配 UMA 资源注册 JSON 字段命名。
 */
public class UmaResourceRepresentation extends ResourceRepresentation {

    /** 默认构造。 */
    public UmaResourceRepresentation() {

    }

    /** 从通用资源表示复制字段。 */
    public UmaResourceRepresentation(ResourceRepresentation resource) {
        setId(resource.getId());
        setName(resource.getName());
        setType(resource.getType());
        setUris(resource.getUris());
        setIconUri(resource.getIconUri());
        setOwner(resource.getOwner());
        setScopes(resource.getScopes());
        setDisplayName(resource.getDisplayName());
        setOwnerManagedAccess(resource.getOwnerManagedAccess());
    }

    /** 从授权模型 {@link Resource} 构建 UMA 表示。 */
    public UmaResourceRepresentation(Resource resource) {
        setId(resource.getId());
        setName(resource.getName());
        setType(resource.getType());
        setUris(resource.getUris());
        setIconUri(resource.getIconUri());
        setOwner(resource.getOwner());
        setScopes(resource.getScopes().stream().map(scope -> new ScopeRepresentation(scope.getName())).collect(Collectors.toSet()));
        setDisplayName(resource.getDisplayName());
        setOwnerManagedAccess(resource.isOwnerManagedAccess());
        setAttributes(resource.getAttributes());
    }

    /** @return UMA 字段 {@code resource_scopes} 对应的作用域集合 */
    @JsonProperty("resource_scopes")
    @Override
    public Set<ScopeRepresentation> getScopes() {
        return super.getScopes();
    }

    /** 设置 UMA {@code resource_scopes} 作用域。 */
    @JsonProperty("resource_scopes")
    @Override
    public void setScopes(Set<ScopeRepresentation> scopes) {
        super.setScopes(scopes);
    }
}
