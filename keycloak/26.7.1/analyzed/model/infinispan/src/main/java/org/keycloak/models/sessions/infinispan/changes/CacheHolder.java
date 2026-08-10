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

package org.keycloak.models.sessions.infinispan.changes;

import java.util.function.Supplier;

import org.keycloak.models.sessions.infinispan.SessionFunction;
import org.keycloak.models.sessions.infinispan.entities.SessionEntity;

import org.infinispan.Cache;
import org.infinispan.util.concurrent.ActionSequencer;

/**
 * 聚合会话变更事务所需的缓存相关依赖。
 * <p>
 * 包含 {@link Cache}、用于 replace 操作的 {@link ActionSequencer}、
 * 计算 lifespan 与 max-idle 的 {@link SessionFunction}，以及可选的亲和键生成器。
 */
public record CacheHolder<K, V extends SessionEntity>(Cache<K, SessionEntityWrapper<V>> cache,
                                                      ActionSequencer sequencer,
                                                      SessionFunction<V> lifespanFunction,
                                                      SessionFunction<V> maxIdleFunction,
                                                      Supplier<K> keyGenerator) {
}
