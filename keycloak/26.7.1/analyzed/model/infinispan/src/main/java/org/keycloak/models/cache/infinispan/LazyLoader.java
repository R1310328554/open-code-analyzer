/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models.cache.infinispan;

import java.util.function.Supplier;

import org.keycloak.models.KeycloakSession;

/**
 * 惰性加载函数式接口：从数据源 {@code S} 获取数据 {@code D}，由实现决定何时拉取及如何缓存。
 * <p>
 * 数据源本身无需关心缓存策略，按需获取即可；具体缓存方式为实现细节。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 * @see DefaultLazyLoader
 */
public interface LazyLoader<S, D> {

    /**
     * 从 {@code source} 获取数据，仅在必要时拉取一次。
     * 实际触发加载的时机由实现类决定。
     *
     * @param session 当前 Keycloak 会话
     * @param source 数据源的惰性供应器
     * @return 从数据源获取的数据
     */
    D get(KeycloakSession session, Supplier<S> source);
}
