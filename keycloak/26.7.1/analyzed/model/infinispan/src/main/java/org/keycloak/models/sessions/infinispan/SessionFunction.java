/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.sessions.infinispan;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.sessions.infinispan.entities.SessionEntity;

/**
 * <p>Infinispan 会话实体的 lifespan 与 idle 超时计算函数。
 * 接收 realm、可选 client 及实体，返回毫秒级时间戳（lifespan、idle 等）。</p>
 *
 * @param <V> 适用的会话实体类型
 *
 * @author rmartinc
 */
@FunctionalInterface
public interface SessionFunction<V extends SessionEntity> {

    /** 根据 realm、client 与实体计算超时毫秒值。 */
    Long apply(RealmModel realm, ClientModel client, V entity);
}
