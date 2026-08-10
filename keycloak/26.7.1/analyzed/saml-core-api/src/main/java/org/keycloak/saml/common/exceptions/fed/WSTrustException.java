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
package org.keycloak.saml.common.exceptions.fed;

import java.security.GeneralSecurityException;

/**
 * <p>
 * 处理 WS-Trust 请求消息时发生错误时抛出的异常。
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class WSTrustException extends GeneralSecurityException {

    /**
     * <p>
     * 使用指定错误消息创建 {@code WSTrustException} 实例。
     * </p>
     *
     * @param message 错误消息
     */
    public WSTrustException(String message) {
        super(message);
    }

    /**
     * <p>
     * 使用指定错误消息及根因创建 {@code WSTrustException} 实例。
     * </p>
     *
     * @param message 错误消息
     * @param cause 表示错误根因的 {@code Throwable}
     */
    public WSTrustException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * <p>
     * 使用指定 {@link Throwable} 创建 {@code WSTrustException} 实例。
     * </p>
     *
     * @param message 错误消息
     */
    public WSTrustException(Throwable t) {
        super(t);
    }

}
