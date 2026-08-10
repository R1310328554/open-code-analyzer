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

import org.keycloak.representations.JsonWebToken;

/**
 * 令牌因时间约束无效时抛出（已过期或尚未生效）。
 * 参见 {@link JsonWebToken#isActive()}。
 *
 * @author hmlnarik
 */
public class TokenNotActiveException extends TokenVerificationException {

    /**
     * @param token 验证失败的令牌
     */
    public TokenNotActiveException(JsonWebToken token) {
        super(token);
    }

    /**
     * @param token 验证失败的令牌
     * @param message 错误描述
     */
    public TokenNotActiveException(JsonWebToken token, String message) {
        super(token, message);
    }

    /**
     * @param token 验证失败的令牌
     * @param message 错误描述
     * @param cause 原始异常
     */
    public TokenNotActiveException(JsonWebToken token, String message, Throwable cause) {
        super(token, message, cause);
    }

    /**
     * @param token 验证失败的令牌
     * @param cause 原始异常
     */
    public TokenNotActiveException(JsonWebToken token, Throwable cause) {
        super(token, cause);
    }

}
