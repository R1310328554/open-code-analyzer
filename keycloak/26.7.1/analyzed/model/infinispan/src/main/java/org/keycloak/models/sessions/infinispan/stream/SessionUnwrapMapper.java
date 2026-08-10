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

package org.keycloak.models.sessions.infinispan.stream;

import java.util.Map;
import java.util.function.Function;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.sessions.infinispan.changes.SessionEntityWrapper;
import org.keycloak.models.sessions.infinispan.entities.SessionEntity;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 从 {@link SessionEntityWrapper} 解包出 {@link SessionEntity} 的 {@link Function}。
 * <p>
 * {@link SessionEntityWrapper} 作为 {@link Map.Entry} 值的一部分存在于缓存条目中。
 *
 * @param <K> 键类型
 * @param <V> 会话实体类型
 */
@ProtoTypeId(Marshalling.SESSION_UNWRAP_MAPPER)
public class SessionUnwrapMapper<K, V extends SessionEntity> implements Function<Map.Entry<K, SessionEntityWrapper<V>>, V> {

    /** 单例实例，供分布式流映射复用。 */
    private static final SessionUnwrapMapper<?, ?> INSTANCE = new SessionUnwrapMapper<>();

    private SessionUnwrapMapper() {
    }

    /** 返回可 ProtoStream 序列化的单例解包映射器。 */
    @ProtoFactory
    @SuppressWarnings("unchecked")
    public static <K1, V1 extends SessionEntity> SessionUnwrapMapper<K1, V1> getInstance() {
        return (SessionUnwrapMapper<K1, V1>) INSTANCE;
    }

    /** 提取包装器内的会话实体。 */
    @Override
    public V apply(Map.Entry<K, SessionEntityWrapper<V>> entry) {
        return entry.getValue().getEntity();
    }
}
