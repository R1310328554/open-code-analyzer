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
package org.keycloak.exceptions;

import org.keycloak.common.VerificationException;
import org.keycloak.representations.JsonWebToken;

/**
 * 令牌验证失败时抛出的基类异常，携带关联的 {@link JsonWebToken} 实例。
 *
 * @author hmlnarik
 */
public class TokenVerificationException extends VerificationException {

    /** 验证失败的令牌对象。 */
    private final JsonWebToken token;

    /**
     * @param token 验证失败的令牌
     */
    public TokenVerificationException(JsonWebToken token) {
        this.token = token;
    }

    /**
     * @param token 验证失败的令牌
     * @param message 错误描述
     */
    public TokenVerificationException(JsonWebToken token, String message) {
        super(message);
        this.token = token;
    }

    /**
     * @param token 验证失败的令牌
     * @param message 错误描述
     * @param cause 原始异常
     */
    public TokenVerificationException(JsonWebToken token, String message, Throwable cause) {
        super(message, cause);
        this.token = token;
    }

    /**
     * @param token 验证失败的令牌
     * @param cause 原始异常
     */
    public TokenVerificationException(JsonWebToken token, Throwable cause) {
        super(cause);
        this.token = token;
    }

    /** @return 验证失败的令牌对象 */
    public JsonWebToken getToken() {
        return token;
    }

}
