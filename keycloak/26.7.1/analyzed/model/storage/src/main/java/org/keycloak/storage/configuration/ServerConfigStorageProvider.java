/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.storage.configuration;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.keycloak.provider.Provider;

/**
 * 用于在 Keycloak 集群实例间共享服务器配置的 {@link Provider}。
 * <p>
 * 本 Provider 为键值存储，键与值均为 {@link String} 类型。
 */
public interface ServerConfigStorageProvider extends Provider {

    /**
     * 返回指定 {@code key} 关联的值。
     *
     * @param key 待查询的键
     * @return 键对应的值；不存在时返回空 {@link Optional}
     * @throws NullPointerException 若 {@code key} 为 {@code null}
     */
    Optional<String> find(String key);

    /**
     * 以指定 {@code key} 存储 {@code value}。
     * <p>
     * 若键已存在，则更新其值。
     *
     * @param key   存储键
     * @param value 存储值
     * @throws NullPointerException 若 {@code key} 或 {@code value} 为 {@code null}
     */
    void store(String key, String value);

    /**
     * 移除指定 {@code key} 对应的值。
     *
     * @param key 待移除的键
     * @throws NullPointerException 若 {@code key} 为 {@code null}
     */
    void remove(String key);

    /**
     * 返回指定 {@code key} 的值；若不存在则调用 {@code valueGenerator} 生成并存储后返回。
     *
     * @param key            查询或写入的键
     * @param valueGenerator 键不存在时用于生成值的 {@link Supplier}
     * @return 已存储或新生成的值
     * @throws NullPointerException 若 {@code key}、{@code valueGenerator} 或其返回值为 {@code null}
     */
    String loadOrCreate(String key, Supplier<String> valueGenerator);

    /**
     * 等价于 {@code loadOrCreate(key, () -> value)}。
     *
     * @see #loadOrCreate(String, Supplier)
     */
    default String loadOrCreate(String key, String value) {
        return loadOrCreate(key, () -> value);
    }

    /**
     * 等价于 {@code replace(key, Objects.requireNonNull(expected)::equals, () -> Objects.requireNonNull(newValue))}。
     *
     * @see #replace(String, Predicate, Supplier)
     */
    default boolean replace(String key, String expected, String newValue) {
        return replace(key, Objects.requireNonNull(expected)::equals, () -> Objects.requireNonNull(newValue));
    }

    /**
     * 当 {@link Predicate} 对当前值返回 {@code true} 时，用 {@code valueGenerator} 生成的新值替换 {@code key} 对应的值。
     *
     * @param key              待替换的键
     * @param replacePredicate 判断是否应执行替换的条件
     * @param valueGenerator   生成新值的 {@link Supplier}
     * @return 成功替换返回 {@code true}，否则返回 {@code false}
     */
    boolean replace(String key, Predicate<String> replacePredicate, Supplier<String> valueGenerator);

    /**
     * @return 当前已存储的全部键的 {@link Stream}
     */
    Stream<String> keys();

}
