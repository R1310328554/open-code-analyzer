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

package org.keycloak.keys.infinispan;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.cache.infinispan.events.InvalidationEvent;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 公钥存储缓存的单键失效集群事件。
 * <p>
 * 通过 ProtoStream 序列化后在集群节点间广播，指示移除指定缓存键对应的公钥条目。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoTypeId(Marshalling.PUBLIC_KEY_INVALIDATION_EVENT)
public class PublicKeyStorageInvalidationEvent extends InvalidationEvent {

    /** 构造指定缓存键的失效事件。 */
    private PublicKeyStorageInvalidationEvent(String cacheKey) {
        super(cacheKey);
    }

    /** ProtoStream 工厂方法，从缓存键创建失效事件。 */
    @ProtoFactory
    public static PublicKeyStorageInvalidationEvent create(String id) {
        return new PublicKeyStorageInvalidationEvent(id);
    }

    /** 返回待失效的公钥缓存键。 */
    public String getCacheKey() {
        return getId();
    }

    @Override
    public String toString() {
        return "PublicKeyStorageInvalidationEvent [ " + getId() + " ]";
    }

}
