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
 * 授权策略（Policy）删除时的集群缓存失效事件。
 * <p>
 * 通过 ProtoStream 序列化后在集群节点间广播，
 * 触发 {@link StoreFactoryCacheManager#policyRemoval} 失效关联授权缓存条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.POLICY_REMOVED_EVENT)
public class PolicyRemovedEvent extends BasePolicyEvent {

    /** ProtoStream 工厂方法，从策略字段反序列化事件实例。 */
    @ProtoFactory
    PolicyRemovedEvent(String id, String name, Set<String> resources, Set<String> resourceTypes, Set<String> scopes, String serverId) {
        super(id, name, resources, resourceTypes, scopes, serverId);
    }

    /** 创建策略删除失效事件。 */
    public static PolicyRemovedEvent create(String id, String name, Set<String> resources, Set<String> resourceTypes, Set<String> scopes, String serverId) {
        return new PolicyRemovedEvent(id, name, resources, resourceTypes, scopes, serverId);
    }

    /** 向失效集合追加因策略删除而需刷新的授权缓存键。 */
    @Override
    public void addInvalidations(StoreFactoryCacheManager cache, Set<String> invalidations) {
        cache.policyRemoval(getId(), name, resources, resourceTypes, scopes, serverId, invalidations);
    }
}
