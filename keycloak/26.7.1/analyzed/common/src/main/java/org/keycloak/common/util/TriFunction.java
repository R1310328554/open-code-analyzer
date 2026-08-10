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

package org.keycloak.common.util;

/**
 * 接受三个参数并产生结果的函数式接口（三元 {@link java.util.function.Function}）。
 *
 * @param <T> 第一个参数类型
 * @param <U> 第二个参数类型
 * @param <V> 第三个参数类型
 * @param <R> 返回值类型
 */
@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    /**
     * 将此函数应用于给定参数。
     *
     * @param t the first function argument
     * @param u the second function argument
     * @param v the third function argument
     * @return the function result
     */
    R apply(T t, U u, V v);

}
