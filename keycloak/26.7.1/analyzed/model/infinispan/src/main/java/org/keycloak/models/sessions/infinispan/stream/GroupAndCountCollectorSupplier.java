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
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.keycloak.marshalling.Marshalling;

import org.infinispan.commons.util.concurrent.CompletableFutures;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 提供用于分组并计数的 {@link Collector} 的 {@link Supplier}。
 * <p>
 * Infinispan 可通过 {@link SerializedLambda} 序列化 lambda，但效率不如 ProtoStream marshaller。
 *
 * @param <T> 元素类型。
 */
@ProtoTypeId(Marshalling.GROUP_AND_COUNT_COLLECTOR_SUPPLIER)
public class GroupAndCountCollectorSupplier<T> implements Supplier<Collector<T, ?, Map<T, Long>>> {

    /** 单例实例，避免在分布式流操作中重复创建。 */
    private static final GroupAndCountCollectorSupplier<?> INSTANCE = new GroupAndCountCollectorSupplier<>();

    private GroupAndCountCollectorSupplier() {
    }

    /** 返回可 ProtoStream 序列化的单例供应商。 */
    @ProtoFactory
    @SuppressWarnings("unchecked")
    public static <T1> GroupAndCountCollectorSupplier<T1> getInstance() {
        return (GroupAndCountCollectorSupplier<T1>) INSTANCE;
    }

    /** 按元素自身分组并统计各组出现次数。 */
    @Override
    public Collector<T, ?, Map<T, Long>> get() {
        return Collectors.groupingBy(CompletableFutures.identity(), Collectors.counting());
    }
}
