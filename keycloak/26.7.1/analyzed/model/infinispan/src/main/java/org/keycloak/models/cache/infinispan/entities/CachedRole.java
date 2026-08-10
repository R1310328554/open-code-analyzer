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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.cache.infinispan.DefaultLazyLoader;
import org.keycloak.models.cache.infinispan.LazyLoader;

/**
 * 角色（Role）的 Infinispan 缓存快照实体。
 * <p>
 * 缓存角色名称、描述与所属领域；组合角色与属性通过 {@link LazyLoader} 按需加载。
 * 实现 {@link InRealm}。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CachedRole extends AbstractRevisioned implements InRealm {

    /** 组合角色记录：客户端容器 ID 集合与子角色 ID 集合。 */
    public record CompositeRolesRecord (Set<String> clientContainerIds, Set<String> ids) {}

    /** 角色名称。 */
    final protected String name;
    /** 所属领域 ID。 */
    final protected String realm;
    /** 角色描述。 */
    final protected String description;
    /** 组合角色懒加载器。 */
    final protected LazyLoader<RoleModel, CompositeRolesRecord> composites;
    /**
     * 供缓存失效逻辑读取先前已缓存的组合角色映射，以判定本条目是否应被驱逐。
     */
    private volatile CompositeRolesRecord cachedComposites = new CompositeRolesRecord(Set.of(), Set.of());
    /** 角色属性懒加载器。 */
    private final LazyLoader<RoleModel, MultivaluedHashMap<String, String>> attributes;

    /** 从角色模型构造缓存快照。 */
    public CachedRole(long revision, RoleModel model, RealmModel realm) {
        super(revision, model.getId());
        description = model.getDescription();
        name = model.getName();
        this.realm = realm.getId();
        composites = new DefaultLazyLoader<>(roleModel -> {
            Set<String> ids = new HashSet<>();
            Set<String> clientContainerIds = new HashSet<>();
            roleModel.getCompositesStream().forEach(r -> {
                ids.add(r.getId());
                if (r.isClientRole()) {
                    clientContainerIds.add(r.getContainerId());
                }
            });
            return new CompositeRolesRecord(Collections.unmodifiableSet(clientContainerIds), Collections.unmodifiableSet(ids));
        }, null);
        attributes = new DefaultLazyLoader<>(roleModel -> new MultivaluedHashMap<>(roleModel.getAttributes()), MultivaluedHashMap::new);
    }

    /** 返回角色名称。 */
    public String getName() {
        return name;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    /** 返回角色描述。 */
    public String getDescription() {
        return description;
    }

    /** 判断角色是否为组合角色（含至少一个子角色）。 */
    public boolean isComposite(KeycloakSession session, Supplier<RoleModel> roleModel) {
        return !getComposites(session, roleModel).ids().isEmpty();
    }

    /** 按需加载并返回组合角色记录，同时更新 {@link #cachedComposites}。 */
    public CompositeRolesRecord getComposites(KeycloakSession session, Supplier<RoleModel> roleModel) {
        cachedComposites = composites.get(session, roleModel);
        return cachedComposites;
    }

    /**
     * 供缓存失效逻辑读取先前已缓存的组合角色映射，以判定本条目是否应被驱逐。
     * 若尚未加载则返回空集合（此时无需失效）。
     */
    public CompositeRolesRecord getCachedComposites() {
        return cachedComposites;
    }

    /** 按需加载并返回角色属性映射。 */
    public MultivaluedHashMap<String, String> getAttributes(KeycloakSession session, Supplier<RoleModel> roleModel) {
        return attributes.get(session, roleModel);
    }
}
