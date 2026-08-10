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

import java.util.Objects;

import org.keycloak.models.cache.infinispan.events.InvalidationEvent;

import org.infinispan.protostream.annotations.ProtoField;

/**
 * 授权作用域（Scope）集群失效事件的抽象基类。
 *
 * <p>封装作用域名称与所属资源服务器 ID，
 * 供 {@code ScopeUpdatedEvent} 与 {@code ScopeRemovedEvent} 复用。
 * 实现 {@link AuthorizationCacheInvalidationEvent} 以参与授权缓存失效广播。
 */
abstract class BaseScopeEvent extends InvalidationEvent implements AuthorizationCacheInvalidationEvent {

    /** 作用域名称。 */
    @ProtoField(2)
    final String name;
    /** 所属资源服务器 ID。 */
    @ProtoField(3)
    final String serverId;

    /** 构造作用域失效事件基类实例。 */
    BaseScopeEvent(String id, String name, String serverId) {
        super(id);
        this.name = Objects.requireNonNull(name);
        this.serverId = Objects.requireNonNull(serverId);
    }

    /** 基于作用域元数据判断事件是否相等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseScopeEvent that = (BaseScopeEvent) o;
        return name.equals(that.name) &&
                serverId.equals(that.serverId);
    }

    /** 计算哈希码，用于集群事件去重。 */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + serverId.hashCode();
        return result;
    }

    /** 返回便于调试的事件字符串表示。 */
    @Override
    public String toString() {
        return String.format("%s [ id=%s, name=%s ]", getClass(), getId(), name);
    }
}
