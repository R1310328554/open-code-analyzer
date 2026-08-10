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

import org.keycloak.authentication.ExplainedVerificationException;
import org.keycloak.exceptions.TokenVerificationException;
import org.keycloak.representations.JsonWebToken;

/**
 * 携带事件错误码与用户消息的令牌校验异常，可用于 {@code ErrorPage.error()} 展示。
 * Token verification exception that bears an error to be logged via event system
 * and a message to show to the user e.g. via {@code ErrorPage.error()}.
 * 
 * @author hmlnarik
 */
public class ExplainedTokenVerificationException extends TokenVerificationException {
    /** 写入事件系统的错误标识。 */
    private final String errorEvent;

    /** 从 {@link ExplainedVerificationException} 包装构造。 */
    public ExplainedTokenVerificationException(JsonWebToken token, ExplainedVerificationException cause) {
        super(token, cause.getMessage(), cause);
        this.errorEvent = cause.getErrorEvent();
    }

    public ExplainedTokenVerificationException(JsonWebToken token, String errorEvent) {
        super(token);
        this.errorEvent = errorEvent;
    }

    public ExplainedTokenVerificationException(JsonWebToken token, String errorEvent, String message) {
        super(token, message);
        this.errorEvent = errorEvent;
    }

    public ExplainedTokenVerificationException(JsonWebToken token, String errorEvent, String message, Throwable cause) {
        super(token, message);
        this.errorEvent = errorEvent;
    }

    public ExplainedTokenVerificationException(JsonWebToken token, String errorEvent, Throwable cause) {
        super(token, cause);
        this.errorEvent = errorEvent;
    }

    /** @return 事件错误码 */
    public String getErrorEvent() {
        return errorEvent;
    }
}
