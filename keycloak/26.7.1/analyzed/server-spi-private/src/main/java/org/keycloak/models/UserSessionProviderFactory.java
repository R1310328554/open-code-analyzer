/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models;

import org.keycloak.provider.ProviderFactory;

/**
 * {@link UserSessionProvider} 的 {@link org.keycloak.provider.ProviderFactory} 工厂接口。
 * <p>按存储后端（Infinispan、JPA 等）实例化用户/客户端会话提供者。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserSessionProviderFactory<T extends UserSessionProvider> extends ProviderFactory<T> {

    // 曾用于从 userSessionPersister 预填充 userSession Infinispan/内存存储
    // 已废弃：不再支持离线会话预加载
    /**
     * 从持久层批量加载会话到内存缓存（已废弃，空实现）。
     *
     * @param sessionFactory 会话工厂
     * @param maxErrors 最大错误数
     * @param sessionsPerSegment 每段加载的会话数
     */
    @Deprecated
    default void loadPersistentSessions(KeycloakSessionFactory sessionFactory, final int maxErrors, final int sessionsPerSegment) {}

}
