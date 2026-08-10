/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.util;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 实现此接口的类型保证：每个实例对应一个随时间稳定、互不重复的整数索引，
 * 且该索引始终标识同一逻辑实例。
 * 索引可用于持久化，因此特定条目的索引不可变更。
 * 主要用于 {@code enum} 类型。
 */
public interface EnumWithStableIndex {
    /**
     * @return 随时间稳定、用于标识实例的唯一数字索引。
     *   同一类中不得为两个不同条目复用同一索引，
     *   即使它们不能同时存在（例如一个删除后才引入另一个）。
     */
    public int getStableIndex();   

    /**
     * 根据稳定索引构建反向查找映射（索引 → 枚举实例）。
     *
     * @param values 枚举常量数组
     * @return 索引到实例的不可变映射视图
     */
    public static <E extends EnumWithStableIndex> Map<Integer, E> getReverseIndex(E[] values) {
        return Stream.of(values).collect(Collectors.toMap(EnumWithStableIndex::getStableIndex, Function.identity()));
    }
}
