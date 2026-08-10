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

package org.keycloak.models.cache.infinispan.events;

import java.util.Objects;

import org.keycloak.cluster.ClusterEvent;

import org.infinispan.protostream.annotations.ProtoField;

/**
 * 缓存失效集群事件的抽象基类。
 * <p>
 * 实现 {@link ClusterEvent}，携带被失效实体的 ID，
 * 供领域、用户、组、角色等各类缓存失效事件继承。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class InvalidationEvent implements ClusterEvent {

    /** 被失效实体的 ID。 */
    private final String id;

    /** 以实体 ID 构造失效事件。 */
    protected InvalidationEvent(String id) {
        this.id = Objects.requireNonNull(id);
    }

    /** 返回被失效实体的 ID。 */
    @ProtoField(1)
    public final String getId() {
        return id;
    }

    /** 返回基于类与实体 ID 的哈希值。 */
    @Override
    public int hashCode() {
        return getClass().hashCode() * 13 + getId().hashCode();
    }

    /** 比较是否为同类型且实体 ID 相同。 */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!obj.getClass().equals(this.getClass())) return false;

        InvalidationEvent that = (InvalidationEvent) obj;
        return that.getId().equals(getId());
    }
}
