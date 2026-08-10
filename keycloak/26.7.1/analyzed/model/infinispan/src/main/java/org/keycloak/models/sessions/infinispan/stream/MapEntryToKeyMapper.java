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

import java.lang.invoke.SerializedLambda;
import java.util.Map;
import java.util.function.Function;

import org.keycloak.marshalling.Marshalling;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 从 {@link Map.Entry} 提取键的 {@link Function}。
 * <p>
 * 等价于 {@code Map.Entry::getKey}。
 * <p>
 * Infinispan 可通过 {@link SerializedLambda} 序列化 lambda，但效率不如 ProtoStream marshaller。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
@ProtoTypeId(Marshalling.MAP_ENTRY_TO_KEY_FUNCTION)
public class MapEntryToKeyMapper<K, V> implements Function<Map.Entry<K, V>, K> {

    /** 单例实例，供分布式流映射复用。 */
    private static final MapEntryToKeyMapper<?, ?> INSTANCE = new MapEntryToKeyMapper<>();

    private MapEntryToKeyMapper() {
    }

    /** 返回可 ProtoStream 序列化的单例映射器。 */
    @ProtoFactory
    @SuppressWarnings("unchecked")
    public static <K1, V1> MapEntryToKeyMapper<K1, V1> getInstance() {
        return (MapEntryToKeyMapper<K1, V1>) INSTANCE;
    }

    /** 返回 Map 条目的键部分。 */
    @Override
    public K apply(Map.Entry<K, V> entry) {
        return entry.getKey();
    }
}
