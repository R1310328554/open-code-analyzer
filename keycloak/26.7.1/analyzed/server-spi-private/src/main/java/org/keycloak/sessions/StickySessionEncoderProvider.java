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

package org.keycloak.sessions;

import java.util.Objects;

import org.keycloak.provider.Provider;

/**
 * 粘性会话编码提供者：在 AUTH_SESSION_ID Cookie 中编码/解码会话 ID 与路由信息。
 * <p>支持负载均衡场景下将会话绑定到缓存该会话的 Keycloak 实例。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface StickySessionEncoderProvider extends Provider {

    /** 会话 ID 与路由之间的默认分隔符。 */
    char DEFAULT_SEPARATOR = '.';

    /**
     * @return 用作粘性会话 Cookie（AUTH_SESSION_ID）的编码值
     * @deprecated 请改用 {@link #encodeSessionId(String, String)}。
     */
    @Deprecated(since = "26.5", forRemoval = true)
    default String encodeSessionId(String sessionId) {
        return encodeSessionId(sessionId, sessionId);
    }

    /**
     * 将路由信息编码进 {@code message}。
     * <p>路由由 {@code sessionId} 对应缓存该会话的 Keycloak 实例决定。</p>
     * Encodes the route into the {@code message}.
     * <p>
     * The route is computed by the {@code sessionId}, i.e., the Keycloak instance where it is cached.
     *
     * @param message   The message to encode with the route.
     * @param sessionId The session ID stored in the cache.
     * @return The encoded message with the route information.
     * @throws NullPointerException if any parameter is null.
     */
    String encodeSessionId(String message, String sessionId);

    /**
     * @param encodedSessionId 粘性会话 Cookie 的值
     * @return 解码后的 {@link AuthenticationSessionModel} 实际 ID
     * @deprecated 请改用 {@link #decodeSessionIdAndRoute(String)}。
     */
    @Deprecated(since = "26.5", forRemoval = true)
    default String decodeSessionId(String encodedSessionId) {
        return decodeSessionIdAndRoute(encodedSessionId).sessionId();
    }

    /**
     * 解码编码后的会话 ID，提取会话 ID 与路由组件。
     * <p>若编码不正确或粘性会话已禁用，路由组件可能为 {@code null}。</p>
     * Decodes the encoded session ID to extract its components, the session ID and the route.
     * <p>
     * The route component may be {@code null} if the session ID is not correctly encoded, or the sticky session is
     * disabled.
     *
     * @param encodedSessionId The encoded session ID.
     * @return The {@link SessionIdAndRoute} with the session ID and the route component. The route may be {@code null}.
     * @throws NullPointerException if {@code encodedSessionId} is {@code null}.
     */
    default SessionIdAndRoute decodeSessionIdAndRoute(String encodedSessionId) {
        // 默认格式：<session>.<route>
        // default implementation format: <session>.<route>
        var index = encodedSessionId.indexOf(DEFAULT_SEPARATOR);
        var length = encodedSessionId.length();
        if (index == -1 || index == (length - 1)) {
            // 路由信息不存在
            //route not present
            return new SessionIdAndRoute(encodedSessionId, null);
        }
        return new SessionIdAndRoute(encodedSessionId.substring(0, index), encodedSessionId.substring(index + 1, length));
    }

    /**
     * @return 是否由 Keycloak 在粘性会话 Cookie 中附加路由信息；否则可能由负载均衡器附加
     */
    boolean shouldAttachRoute();

    /**
     * @param sessionId 会话 ID
     * @return 该会话的路由；粘性会话禁用时返回 {@code null}
     * @throws NullPointerException 若 {@code sessionId} 为 {@code null}
     */
    String sessionIdRoute(String sessionId);

    /**
     * 会话 ID 与路由的配对记录。
     * @param sessionId 会话 ID
     * @param route     缓存该会话的 Keycloak 实例路由；粘性会话禁用时可为 {@code null}
     */
    record SessionIdAndRoute(String sessionId, String route) {

        public SessionIdAndRoute {
            Objects.requireNonNull(sessionId);
        }

        /** @param otherRoute 待比较的路由
         * @return 是否与当前路由相同 */
        public boolean isSameRoute(String otherRoute) {
            return Objects.equals(otherRoute, route);
        }
    }

}
