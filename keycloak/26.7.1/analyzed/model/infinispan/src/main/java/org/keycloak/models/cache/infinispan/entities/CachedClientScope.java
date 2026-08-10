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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;

/**
 * 客户端作用域（Client Scope）的 Infinispan 缓存快照实体。
 * <p>
 * 继承 {@link AbstractCachedClientScope}，缓存名称、协议、角色映射与属性等字段。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CachedClientScope extends AbstractCachedClientScope<ClientScopeModel> {

    /** 作用域名称。 */
    private String name;
    /** 作用域描述。 */
    private String description;
    /** 所属领域 ID。 */
    private String realm;
    /** 使用的协议（如 openid-connect）。 */
    private String protocol;
    /** 映射的角色 ID 集合。 */
    private Set<String> scope = new HashSet<>();
    /** 自定义属性键值对。 */
    private Map<String, String> attributes = new HashMap<>();

    /** 从客户端作用域模型构造缓存快照。 */
    public CachedClientScope(long revision, RealmModel realm, ClientScopeModel model) {
        super(revision, model);
        name = model.getName();
        description = model.getDescription();
        this.realm = realm.getId();
        protocol = model.getProtocol();
        scope.addAll(model.getScopeMappingsStream().map(RoleModel::getId).collect(Collectors.toSet()));
        attributes.putAll(model.getAttributes());
    }

    /** 返回作用域名称。 */
    public String getName() {
        return name;
    }

    /** 返回作用域描述。 */
    public String getDescription() { return description; }

    /** 设置作用域描述。 */
    public void setDescription(String description) { this.description = description; }

    /** 返回所属领域 ID。 */
    public String getRealm() {
        return realm;
    }

    /** 返回使用的协议。 */
    public String getProtocol() {
        return protocol;
    }

    /** 返回映射的角色 ID 集合。 */
    public Set<String> getScope() {
        return scope;
    }

    /** 返回自定义属性映射。 */
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
