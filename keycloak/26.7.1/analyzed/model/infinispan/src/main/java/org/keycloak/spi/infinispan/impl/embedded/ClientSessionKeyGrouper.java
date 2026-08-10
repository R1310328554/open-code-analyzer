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

package org.keycloak.spi.infinispan.impl.embedded;

import org.keycloak.models.sessions.infinispan.entities.EmbeddedClientSessionKey;

import org.infinispan.distribution.group.Grouper;

/**
 * 基于用户会话 ID 对客户端会话进行分组的 {@link Grouper} 实现。
 * <p>
 * 同一用户会话下的所有客户端会话会被路由到与用户会话相同的缓存节点，避免跨节点访问。
 */
public enum ClientSessionKeyGrouper implements Grouper<EmbeddedClientSessionKey> {

    INSTANCE;

    // Infinispan 解析器要求存在构造函数或静态 getInstance 方法；修复 ClusterConfigKeepAliveDistTest。
    public static ClientSessionKeyGrouper getInstance() {
        return INSTANCE;
    }

    /** 以用户会话 ID 作为分组键，使客户端会话与用户会话共置。 */
    @Override
    public Object computeGroup(EmbeddedClientSessionKey key, Object group) {
        return key.userSessionId();
    }

    @Override
    public Class<EmbeddedClientSessionKey> getKeyType() {
        return EmbeddedClientSessionKey.class;
    }
}
