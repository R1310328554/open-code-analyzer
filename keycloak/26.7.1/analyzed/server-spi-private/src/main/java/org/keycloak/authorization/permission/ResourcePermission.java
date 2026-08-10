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

package org.keycloak.authorization.permission;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;

/**
 * 资源权限：表示对特定资源（及可选范围）的访问许可。
 *
 * Represents a permission for a given resource.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ResourcePermission {

    private final Resource resource;
    private final Collection<Scope> scopes;
    private final String resourceType;
    private ResourceServer resourceServer;
    private Map<String, Set<String>> claims;
    private boolean granted;

    public ResourcePermission(Resource resource, Collection<Scope> scopes, ResourceServer resourceServer) {
        this(null, resource, scopes, resourceServer, null);
    }

    public ResourcePermission(String resourceType, Resource resource, Collection<Scope> scopes, ResourceServer resourceServer) {
        this(resourceType, resource, scopes, resourceServer, null);
    }

    public ResourcePermission(Resource resource, ResourceServer resourceServer, Map<String, ? extends Collection<String>> claims) {
        this(null, resource, new LinkedHashSet<>(), resourceServer, claims);
    }

    public ResourcePermission(Resource resource, Collection<Scope> scopes, ResourceServer resourceServer, Map<String, ? extends Collection<String>> claims) {
        this(null, resource, scopes, resourceServer, claims);
    }

    public ResourcePermission(String resourceType, Resource resource, Collection<Scope> scopes, ResourceServer resourceServer, Map<String, ? extends Collection<String>> claims) {
        this.resourceType = resourceType;
        this.resource = resource;
        this.scopes = scopes;
        this.resourceServer = resourceServer;
        if (claims != null) {
            this.claims = new HashMap<>();
            for (Entry<String, ? extends Collection<String>> entry : claims.entrySet()) {
                this.claims.computeIfAbsent(entry.getKey(), key -> new LinkedHashSet<>()).addAll(entry.getValue());
            }
        }
    }

    public String getResourceType() {
        return resourceType;
    }

    /**
     * 返回本权限适用的资源。
     *
     * Returns the resource to which this permission applies.
     *
     * @return the resource to which this permission applies
     */
    public Resource getResource() {
        return this.resource;
    }

    /**
     * 返回资源上被许可的范围集合。
     *
     * Returns a list of permitted scopes associated with the resource
     *
     * @return a lit of permitted scopes
     */
    public Collection<Scope> getScopes() {
        return this.scopes;
    }

    /**
     * 返回关联的资源服务器。
     *
     * Returns the resource server associated with this permission.
     *
     * @return the resource server
     */
    public ResourceServer getResourceServer() {
        return this.resourceServer;
    }

    /**
     * 返回权限相关的全部声明（claims）。
     *
     * Returns all permission claims.
     *
     * @return
     */
    public Map<String, Set<String>> getClaims() {
        if (claims == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(claims);
    }

    /**
     * 添加单值权限声明；若同名声明已存在则追加到值列表。
     *
     * <p>Adds a permission claim with the given name and a single value.
     *
     * <p>If a claim already exists, the value is added to list of values of the existing claim</p>
     *
     * @param name the name of the claim
     * @param value the value of the claim
     */
    public boolean addClaim(String name, String value) {
        if (claims == null) {
            claims = new HashMap<>();
        }
        return claims.computeIfAbsent(name, key -> new HashSet<>()).add(value);
    }

    /**
     * 移除指定名称的权限声明。
     *
     * <p>Removes a permission claim.
     *
     *
     * @param name the name of the claim
     */
    public void removeClaim(String name) {
        if (claims != null) {
            claims.remove(name);
        }
    }

    /** 向权限添加范围（须属于资源已有范围）。 */
    public void addScope(Scope scope) {
        if (resource != null) {
            if (!resource.getScopes().contains(scope)) {
                return;
            }
        }

        if (!scopes.contains(scope)) {
            scopes.add(scope);
        }
    }

    public void addClaims(Map<String, Set<String>> claims) {
        if (this.claims == null) {
            this.claims = new HashMap<>();
        }
        this.claims.putAll(claims);
    }

    /** 标记权限是否已通过 UMA 等方式直接授予。 */
    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    /** 是否已直接授予（跳过策略评估）。 */
    public boolean isGranted() {
        return granted;
    }
}
