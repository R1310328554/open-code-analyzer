/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.cache.infinispan.authorization.events;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.keycloak.models.cache.infinispan.events.InvalidationEvent;

import org.infinispan.protostream.annotations.ProtoField;

/**
 * 授权资源（Resource）集群失效事件的抽象基类。
 *
 * <p>封装资源名称、所有者、类型、URI 及关联作用域等字段，
 * 供 {@code ResourceUpdatedEvent} 与 {@code ResourceRemovedEvent} 复用。
 * 实现 {@link AuthorizationCacheInvalidationEvent} 以参与授权缓存失效广播。
 */
abstract class BaseResourceEvent extends InvalidationEvent implements AuthorizationCacheInvalidationEvent {

    /** 资源名称。 */
    @ProtoField(2)
    final String name;
    /** 资源所有者 ID。 */
    @ProtoField(3)
    final String owner;
    /** 所属资源服务器 ID。 */
    @ProtoField(4)
    final String serverId;
    /** 资源类型标识。 */
    @ProtoField(5)
    final String type;
    /** 资源 URI 集合。 */
    @ProtoField(value = 6, collectionImplementation = HashSet.class)
    final Set<String> uris;
    /** 资源关联的作用域 ID 集合。 */
    @ProtoField(value = 7, collectionImplementation = HashSet.class)
    final Set<String> scopes;

    /** 构造资源失效事件基类实例。 */
    BaseResourceEvent(String id, String name, String owner, String serverId, String type, Set<String> uris, Set<String> scopes) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.owner = Objects.requireNonNull(owner);
        this.serverId = Objects.requireNonNull(serverId);
        this.type = type;
        this.uris = uris;
        this.scopes = scopes;
    }

    /** 基于资源元数据判断事件是否相等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseResourceEvent that = (BaseResourceEvent) o;
        return name.equals(that.name) &&
                owner.equals(that.owner) &&
                serverId.equals(that.serverId) &&
                Objects.equals(type, that.type) &&
                Objects.equals(uris, that.uris) &&
                Objects.equals(scopes, that.scopes);
    }

    /** 计算哈希码，用于集群事件去重。 */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + owner.hashCode();
        result = 31 * result + serverId.hashCode();
        result = 31 * result + Objects.hashCode(type);
        result = 31 * result + Objects.hashCode(uris);
        result = 31 * result + Objects.hashCode(scopes);
        return result;
    }

    /** 返回便于调试的事件字符串表示。 */
    @Override
    public String toString() {
        return String.format("%s [ id=%s, name=%s]", getClass().getSimpleName(), getId(), name);
    }
}
