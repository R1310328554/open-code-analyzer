/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authentication.actiontoken;

import jakarta.ws.rs.core.Response;

import org.keycloak.TokenVerifier.Predicate;
import org.keycloak.common.VerificationException;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.provider.Provider;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 操作令牌处理器 SPI 接口，定义校验、预处理与业务处理契约。
 *  Handler of the action token.
 *
 * @param <T> Class implementing the action token
 *
 *  @author hmlnarik
 */
public interface ActionTokenHandler<T extends JsonWebToken> extends Provider {

    /**
     * 令牌初始校验通过后、正式处理前的预处理钩子。
     * This method allows to parse the token and extract information from it after initial verification.
     * @param token Token.
     * @param tokenContext Token context.
     * @return Error response if the initial verification fails, {@code null} otherwise.
     */
    default Response preHandleToken(T token, ActionTokenContext<T> tokenContext) {
        return null;
    }

    /**
     * 在校验全部通过后执行令牌对应的业务操作。
     * Performs the action as per the token details. This method is only called if all verifiers
     * returned in {@link #handleToken} succeed.
     *
     * @param token
     * @param tokenContext
     * @return
     */
    Response handleToken(T token, ActionTokenContext<T> tokenContext);

    /**
     * 返回用于反序列化的令牌 Java 类型。
     * Returns the Java token class for use with deserialization.
     * @return
     */
    Class<T> getTokenClass();

    /**
     * 返回处理前必须全部通过的校验谓词数组。
     * Returns an array of verifiers that are tested prior to handling the token. All verifiers have to pass successfully
     * for token to be handled. The returned array must not be {@code null}.
     * @param tokenContext
     * @return Verifiers or an empty array. The returned array must not be {@code null}.
     */
    default Predicate<? super T>[] getVerifiers(ActionTokenContext<T> tokenContext) {
        return new Predicate[] {};
    }

    /**
     * 从令牌中读取应加入的复合认证会话 ID。
     * Returns a compound authentication session ID requested from within the given token that the handler should attempt to join.
     * @param token Token. Can be {@code null}
     * @param tokenContext
     * @param currentAuthSession Authentication session that is currently in progress, {@code null} if no authentication session is not set
     * @see AuthenticationSessionCompoundId
     * @return Authentication session ID (can be {@code null} if the token does not contain authentication session ID)
     */
    String getAuthenticationSessionIdFromToken(T token, ActionTokenContext<T> tokenContext, AuthenticationSessionModel currentAuthSession);

    /**
     * 返回审计日志使用的事件类型。
     * Returns a event type logged with {@link EventBuilder} class.
     * @return
     */
    EventType eventType();

    /**
     * 令牌处理失败且无更具体错误时写入事件的默认错误码。
     * Returns an error to be shown in the {@link EventBuilder} detail when token handling fails and
     * no more specific error is provided.
     * @return
     */
    String getDefaultEventError();

    /**
     * 令牌处理失败且无更具体消息时返回给用户的默认错误键。
     * Returns an error to be shown in the response when token handling fails and no more specific
     * error message is provided.
     * @return
     */
    String getDefaultErrorMessage();

    /**
     * 根据令牌信息创建新的认证会话。
     * Creates a fresh authentication session according to the information from the token. The default
     * implementation creates a new authentication session that requests termination after required actions.
     * @param token
     * @param tokenContext
     * @return
     */
    AuthenticationSessionModel startFreshAuthenticationSession(T token, ActionTokenContext<T> tokenContext) throws VerificationException;

    /**
     * 指示令牌是否可重复使用（false 表示一次性）。
     * Returns {@code true} when the token can be used repeatedly to invoke the action, {@code false} when the token
     * is intended to be for single use only.
     * @return see above
     */
    boolean canUseTokenRepeatedly(T token, ActionTokenContext<T> tokenContext);
}
