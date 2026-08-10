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

package org.keycloak.models.cache.infinispan.events;

import java.util.Objects;

import org.infinispan.protostream.annotations.ProtoField;

/**
 * 角色相关缓存失效事件的抽象基类。
 * <p>
 * 继承 {@link InvalidationEvent} 并实现 {@link RealmCacheInvalidationEvent}，
 * 携带角色 ID 与容器 ID（领域或客户端），供角色增删改事件复用。
 */
abstract class BaseRoleEvent extends InvalidationEvent implements RealmCacheInvalidationEvent {

    /** 角色所属容器 ID（领域 ID 或客户端 UUID）。 */
    @ProtoField(2)
    final String containerId;

    /** 以角色 ID 与容器 ID 构造基类事件。 */
    BaseRoleEvent(String roleId, String containerId) {
        super(roleId);
        this.containerId = Objects.requireNonNull(containerId);
    }

    /** 比较角色 ID 与容器 ID 是否一致。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseRoleEvent that = (BaseRoleEvent) o;
        return containerId.equals(that.containerId);
    }

    /** 返回基于角色 ID 与容器 ID 的哈希值。 */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + containerId.hashCode();
        return result;
    }

    /** 返回便于调试的字符串表示。 */
    @Override
    public String toString() {
        return String.format("%s [ roleId=%s, containerId=%s ]", getClass().getSimpleName(), getId(), containerId);
    }
}
