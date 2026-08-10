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
package org.keycloak.authorization.client.util;

import java.util.concurrent.Callable;

import org.keycloak.authorization.client.AuthorizationDeniedException;
import org.keycloak.authorization.client.ResourceNotFoundException;
import org.keycloak.authorization.client.representation.TokenIntrospectionResponse;

/**
 * <p>授权客户端异常处理与重试工具类。
 *
 * <p>将 {@link HttpResponseException} 映射为 {@link AuthorizationDeniedException} 等业务异常，
 * 并在 token 失效（403）时尝试刷新后重试。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public final class Throwables {

    /**
     * 处理 {@code cause} 并包装为 {@link RuntimeException}。
     * 若根因为 {@link HttpResponseException}，则提取更详细的 HTTP 错误信息。
     *
     * @param message 异常消息
     * @param cause 根因
     * @return 包装后的 {@link RuntimeException}
     */
    public static RuntimeException handleWrapException(String message, Throwable cause) {
        if (cause instanceof HttpResponseException) {
            throw handleAndWrapHttpResponseException(HttpResponseException.class.cast(cause));
        }

        return new RuntimeException(message, cause);
    }

    /**
     * <p>在从服务器获取新 {@code token} 后重试 {@code callable}。
     * 重试失败时按 {@link #handleWrapException(String, Throwable)} 处理。
     *
     * <p>仅当 {@code cause} 为状态码 403 的 {@link HttpResponseException} 时尝试重试——
     * 部分场景下 token 关联会话已失效，需重新签发。
     *
     * @param callable 待重试的可调用对象
     * @param token token 供应器
     * @param message 异常消息
     * @param cause 原始异常
     * @param <V> callable 返回值类型
     * @return callable 执行结果
     * @throws RuntimeException 重试仍失败时抛出
     */
    public static <V> V retryAndWrapExceptionIfNecessary(Callable<V> callable, TokenCallable token, String message, Throwable cause) throws RuntimeException {
        if (token == null || !token.isRetry()) {
            throw handleWrapException(message, cause);
        }

        if (cause instanceof HttpResponseException) {
            HttpResponseException httpe = HttpResponseException.class.cast(cause);

            if (httpe.getStatusCode() == 403) {
                TokenIntrospectionResponse response = token.getHttp().<TokenIntrospectionResponse>post(token.getServerConfiguration().getIntrospectionEndpoint())
                        .authentication()
                        .client()
                        .param("token", token.call())
                        .response().json(TokenIntrospectionResponse.class).execute();

                if (!response.getActive()) {
                    token.clearTokens();
                    try {
                        return callable.call();
                    } catch (Exception e) {
                        throw handleWrapException(message, e);
                    }
                }

                throw handleWrapException(message, cause);
            } else if (httpe.getStatusCode() == 400 && new String(httpe.getBytes()).contains("invalid_resource_id")) {
                throw new ResourceNotFoundException(message, cause);
            }
        }

        throw new RuntimeException(message, cause);
    }

    /** 将 403 映射为 {@link AuthorizationDeniedException}，其余 HTTP 错误包装为 {@link RuntimeException}。 */
    private static RuntimeException handleAndWrapHttpResponseException(HttpResponseException exception) {
        if (403 == exception.getStatusCode()) {
            throw new AuthorizationDeniedException(exception);
        }

        return new RuntimeException(exception);
    }
}
