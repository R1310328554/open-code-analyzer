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

package org.keycloak.tracing;

import org.keycloak.models.KeycloakSession;
import org.keycloak.utils.KeycloakSessionUtil;

import org.jboss.logging.Logger;

/**
 * 追踪提供者工具类：从 {@link KeycloakSession} 或当前线程获取 {@link TracingProvider}。
 */
public class TracingProviderUtil {

    private static final Logger log = Logger.getLogger(TracingProviderUtil.class);
    /** 无会话时的 no-op 追踪提供者单例。 */
    private static TracingProvider NOOP_PROVIDER;

    /** 从会话获取 {@link TracingProvider}。 */
    public static TracingProvider getTracingProvider(KeycloakSession session) {
        return session.getProvider(TracingProvider.class);
    }

    // 仅 Quarkus 可用（事务处理器在线程中设置 session），Undertow 不支持
    // works only with Quarkus due to session set in Transaction Handler - not Undertow
    /** 从当前线程会话获取追踪提供者；无会话时返回 no-op 实现。 */
    public static TracingProvider getTracingProvider() {
        var session = KeycloakSessionUtil.getKeycloakSession();
        if (session == null) {
            log.warn("Cannot obtain session from thread to init TracingProvider. Return Noop provider.");
            if (NOOP_PROVIDER == null) {
                NOOP_PROVIDER = new NoopTracingProvider();
            }
            return NOOP_PROVIDER;
        }
        return getTracingProvider(session);
    }
}
