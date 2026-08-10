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
package org.keycloak.saml.common.exceptions;

import java.security.GeneralSecurityException;

/**
 * 表示服务端处理过程中发生错误的异常。
 *
 * @author Anil.Saldhana@redhat.com
 * @since May 22, 2009
 */
public class ProcessingException extends GeneralSecurityException {

    /** 构造无消息的处理异常。 */
    public ProcessingException() {
        super();
    }

    /**
     * 构造带消息及根因的处理异常。
     *
     * @param message 错误描述
     * @param cause 根因
     */
    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造带消息的处理异常。
     *
     * @param message 错误描述
     */
    public ProcessingException(String message) {
        super(message);
    }

    /**
     * 构造以给定异常为根因的处理异常。
     *
     * @param cause 根因
     */
    public ProcessingException(Throwable cause) {
        super(cause);
    }
}
