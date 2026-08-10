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
 * 授权资源（Resource）删除时的集群缓存失效事件。
 * <p>
 * 通过 ProtoStream 序列化后在集群节点间广播，
 * 触发 {@link StoreFactoryCacheManager#resourceRemoval} 失效关联授权缓存条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.RESOURCE_REMOVED_EVENT)
public class ResourceRemovedEvent extends BaseResourceEvent {

    /** ProtoStream 工厂方法，从资源字段反序列化事件实例。 */
    @ProtoFactory
    static ResourceRemovedEvent protoFactory(String id, String name, String owner, String serverId, String type, Set<String> uris, Set<String> scopes) {
        return new ResourceRemovedEvent(id, name, owner, serverId, type, uris, scopes);
    }

    /** 私有构造，通过工厂方法或 {@link #create} 获取实例。 */
    private ResourceRemovedEvent(String id, String name, String owner, String serverId, String type, Set<String> uris, Set<String> scopes) {
        super(id, name, owner, serverId, type, uris, scopes);
    }

    /** 创建资源删除失效事件。 */
    public static ResourceRemovedEvent create(String id, String name, String type, Set<String> uris, String owner, Set<String> scopes, String serverId) {
        return new ResourceRemovedEvent(id, name, owner, serverId, type, uris, scopes);
    }

    /** 向失效集合追加因资源删除而需刷新的授权缓存键。 */
    @Override
    public void addInvalidations(StoreFactoryCacheManager cache, Set<String> invalidations) {
        cache.resourceRemoval(getId(), name, type, uris, owner, scopes, serverId, invalidations);
    }
}
