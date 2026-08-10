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

package org.keycloak.models.cache.infinispan.authorization.events;

import java.util.Set;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.cache.infinispan.authorization.StoreFactoryCacheManager;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 授权作用域（Scope）更新时的集群缓存失效事件。
 * <p>
 * 通过 ProtoStream 序列化后在集群节点间广播，
 * 触发 {@link StoreFactoryCacheManager#scopeUpdated} 失效关联授权缓存条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.SCOPE_UPDATED_EVENT)
public class ScopeUpdatedEvent extends BaseScopeEvent {

    /** ProtoStream 工厂方法，从作用域字段反序列化事件实例。 */
    @ProtoFactory
    ScopeUpdatedEvent(String id, String name, String serverId) {
        super(id, name, serverId);
    }

    /** 创建作用域更新失效事件。 */
    public static ScopeUpdatedEvent create(String id, String name, String serverId) {
        return new ScopeUpdatedEvent(id, name, serverId);
    }

    /** 向失效集合追加因作用域更新而需刷新的授权缓存键。 */
    @Override
    public void addInvalidations(StoreFactoryCacheManager cache, Set<String> invalidations) {
        cache.scopeUpdated(getId(), name, serverId, invalidations);
    }
}
