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
 * 领域相关缓存失效事件的抽象基类。
 * <p>
 * 继承 {@link InvalidationEvent} 并实现 {@link RealmCacheInvalidationEvent}，
 * 携带领域 ID 与领域名称，供领域增删改事件复用。
 */
abstract class BaseRealmEvent extends InvalidationEvent implements RealmCacheInvalidationEvent {

    /** 领域名称。 */
    @ProtoField(2)
    final String realmName;

    /** 以领域 ID 与名称构造基类事件。 */
    BaseRealmEvent(String realmId, String realmName) {
        super(realmId);
        this.realmName = Objects.requireNonNull(realmName);
    }

    /** 比较领域 ID 与名称是否一致。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseRealmEvent that = (BaseRealmEvent) o;
        return realmName.equals(that.realmName);
    }

    /** 返回基于领域 ID 与名称的哈希值。 */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + realmName.hashCode();
        return result;
    }

    /** 返回便于调试的字符串表示。 */
    @Override
    public String toString() {
        return String.format("%s [ realmId=%s, realmName=%s ]", getClass().getSimpleName(), getId(), realmName);
    }
}
