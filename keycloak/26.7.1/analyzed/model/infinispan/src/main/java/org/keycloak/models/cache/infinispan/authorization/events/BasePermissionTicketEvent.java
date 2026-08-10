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
 * 权限票据（Permission Ticket）集群失效事件的抽象基类。
 *
 * <p>封装票据关联的所有者、资源、作用域、请求方等字段，
 * 供 {@code PermissionTicketUpdatedEvent} 与 {@code PermissionTicketRemovedEvent} 复用。
 * 实现 {@link AuthorizationCacheInvalidationEvent} 以参与授权缓存失效广播。
 */
abstract class BasePermissionTicketEvent extends InvalidationEvent implements AuthorizationCacheInvalidationEvent {

    /** 权限票据所有者（资源拥有者）ID。 */
    private final String owner;
    /** 关联资源 ID。 */
    private final String resource;
    /** 关联作用域 ID。 */
    private final String scope;
    /** 所属资源服务器 ID。 */
    private final String serverId;
    /** 权限请求方 ID。 */
    private final String requester;
    /** 资源名称（用于日志与调试）。 */
    private final String resourceName;

    /** 构造权限票据失效事件基类实例。 */
    BasePermissionTicketEvent(String id, String owner, String resource, String scope, String serverId, String requester, String resourceName) {
        super(id);
        this.owner = owner;
        this.resource = resource;
        this.scope = scope;
        this.serverId = serverId;
        this.requester = requester;
        this.resourceName = resourceName;
    }

    /** 返回权限票据所有者 ID。 */
    @ProtoField(2)
    public String getOwner() {
        return owner;
    }

    /** 返回权限请求方 ID。 */
    @ProtoField(3)
    public String getRequester() {
        return requester;
    }

    /** 返回关联资源 ID。 */
    @ProtoField(4)
    public String getResource() {
        return resource;
    }

    /** 返回资源名称。 */
    @ProtoField(5)
    public String getResourceName() {
        return resourceName;
    }

    /** 返回关联作用域 ID。 */
    @ProtoField(6)
    public String getScope() {
        return scope;
    }

    /** 返回所属资源服务器 ID。 */
    @ProtoField(7)
    public String getServerId() {
        return serverId;
    }

    /** 返回便于调试的事件字符串表示。 */
    @Override
    public String toString() {
        return "%s [ id=%s, name=%s]".formatted(getClass().getName(), getId(), resource);
    }

    /** 基于资源 ID 与资源服务器 ID 判断事件是否相等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BasePermissionTicketEvent that = (BasePermissionTicketEvent) o;
        return Objects.equals(resource, that.resource) && Objects.equals(serverId, that.serverId);
    }

    /** 计算哈希码，用于集群事件去重。 */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), resource, serverId);
    }

}
