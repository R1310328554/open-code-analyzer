/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.models.cache.infinispan.authorization.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.Scope;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.cache.infinispan.DefaultLazyLoader;
import org.keycloak.models.cache.infinispan.LazyLoader;
import org.keycloak.models.cache.infinispan.entities.AbstractRevisioned;

/**
 * 授权资源（Resource）的 Infinispan 缓存快照实体。
 * <p>
 * 核心字段在构造时固化；URI、作用域 ID 与属性通过 {@link LazyLoader} 懒加载，
 * 避免缓存条目过大。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class CachedResource extends AbstractRevisioned implements InResourceServer {

    /** 所属资源服务器 ID。 */
    private final String resourceServerId;
    /** 资源图标 URI。 */
    private final String iconUri;
    /** 资源所有者 ID。 */
    private final String owner;
    /** 资源类型标识。 */
    private final String type;
    /** 资源名称。 */
    private final String name;
    /** 资源显示名称。 */
    private final String displayName;
    /** 是否启用所有者托管访问（UMA）。 */
    private final boolean ownerManagedAccess;
    /** 关联作用域 ID 集合的懒加载器。 */
    private LazyLoader<Resource, Set<String>> scopesIds;
    /** 资源 URI 集合的懒加载器。 */
    private LazyLoader<Resource, Set<String>> uris;
    /** 资源属性映射的懒加载器。 */
    private LazyLoader<Resource, MultivaluedHashMap<String, String>> attributes;

    /** 从 Resource 模型构造缓存快照。 */
    public CachedResource(long revision, Resource resource) {
        super(revision, resource.getId());
        this.name = resource.getName();
        this.displayName = resource.getDisplayName();
        this.type = resource.getType();
        this.owner = resource.getOwner();
        this.iconUri = resource.getIconUri();
        this.resourceServerId = resource.getResourceServer().getId();
        ownerManagedAccess = resource.isOwnerManagedAccess();

        this.uris = new DefaultLazyLoader<>(source -> new HashSet<>(source.getUris()), Collections::emptySet);

        this.scopesIds = new DefaultLazyLoader<>(source -> source.getScopes().stream().map(Scope::getId).collect(Collectors.toSet()), Collections::emptySet);

        this.attributes = new DefaultLazyLoader<>(source -> new MultivaluedHashMap<>(source.getAttributes()), MultivaluedHashMap::new);
    }


    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    /** 懒加载资源 URI 集合。 */
    public Set<String> getUris(KeycloakSession session, Supplier<Resource> source) {
        return this.uris.get(session, source);
    }

    public String getType() {
        return this.type;
    }

    public String getIconUri() {
        return this.iconUri;
    }

    public String getOwner() {
        return this.owner;
    }

    public boolean isOwnerManagedAccess() {
        return ownerManagedAccess;
    }

    public String getResourceServerId() {
        return this.resourceServerId;
    }

    /** 懒加载关联作用域 ID 集合。 */
    public Set<String> getScopesIds(KeycloakSession session, Supplier<Resource> source) {
        return this.scopesIds.get(session, source);
    }

    /** 懒加载资源属性映射。 */
    public Map<String, List<String>> getAttributes(KeycloakSession session, Supplier<Resource> source) {
        return attributes.get(session, source);
    }
}
