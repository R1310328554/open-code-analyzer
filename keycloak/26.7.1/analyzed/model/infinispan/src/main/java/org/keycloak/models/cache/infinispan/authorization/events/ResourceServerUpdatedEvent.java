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
 * 资源服务器（Resource Server）更新时的集群缓存失效事件。
 * <p>
 * 通过 ProtoStream 序列化后在集群节点间广播，
 * 触发 {@link StoreFactoryCacheManager#resourceServerUpdated} 失效关联授权缓存条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.RESOURCE_SERVER_UPDATED_EVENT)
public class ResourceServerUpdatedEvent extends BaseResourceServerEvent {

    /** ProtoStream 工厂方法，从资源服务器 ID 反序列化事件实例。 */
    @ProtoFactory
    ResourceServerUpdatedEvent(String id) {
        super(id);
    }

    /** 创建资源服务器更新失效事件。 */
    public static ResourceServerUpdatedEvent create(String id) {
        return new ResourceServerUpdatedEvent(id);
    }

    /** 向失效集合追加因资源服务器更新而需刷新的授权缓存键。 */
    @Override
    public void addInvalidations(StoreFactoryCacheManager cache, Set<String> invalidations) {
        cache.resourceServerUpdated(getId(), invalidations);
    }
}
