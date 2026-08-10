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
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import org.keycloak.marshalling.Marshalling;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 将 {@link Collection} 转为 {@link Stream} 的 {@link Function}。
 * <p>
 * 等价于 {@code Collection::stream}，但以 ProtoStream 序列化，
 * 比 Infinispan 基于 {@link SerializedLambda} 的 lambda 编组更高效。
 *
 * @param <T> 集合元素类型。
 */
@ProtoTypeId(Marshalling.COLLECTION_TO_STREAM_MAPPER)
public class CollectionToStreamMapper<T> implements Function<Collection<T>, Stream<T>> {

    private static final CollectionToStreamMapper<?> INSTANCE = new CollectionToStreamMapper<>();

    private CollectionToStreamMapper() {
    }

    /** ProtoStream 工厂方法，返回泛型单例。 */
    @ProtoFactory
    @SuppressWarnings("unchecked")
    public static <T1> CollectionToStreamMapper<T1> getInstance() {
        return (CollectionToStreamMapper<T1>) INSTANCE;
    }

    /** 对集合调用 {@link Collection#stream()}。 */
    @Override
    public Stream<T> apply(Collection<T> collection) {
        return collection.stream();
    }
}
