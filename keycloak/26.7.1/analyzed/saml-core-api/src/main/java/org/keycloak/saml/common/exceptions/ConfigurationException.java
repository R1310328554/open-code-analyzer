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
 * 表示 SAML/PicketLink 配置错误的异常。
 * Exception indicating an issue with the configuration
 *
 * @author Anil.Saldhana@redhat.com
 * @since May 22, 2009
 */
public class ConfigurationException extends GeneralSecurityException {
    
    /** 构造无消息的 ConfigurationException。 */
    public ConfigurationException() {
        super();
    }

    /**
     * 构造带消息与原因的 ConfigurationException。
     *
     * @param message 异常描述
     * @param cause 原始异常
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造带消息的 ConfigurationException。
     *
     * @param message 异常描述
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * 构造仅包装原因的 ConfigurationException。
     *
     * @param cause 原始异常
     */
    public ConfigurationException(Throwable cause) {
        super(cause);
    }
}
