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
package org.keycloak.broker.provider;

/**
 * 身份联邦流程运行时异常，可选携带 {@link #messageCode} 供 UI 或事件使用。
 *
 * @author pedroigor
 */
public class IdentityBrokerException extends RuntimeException {
    private String messageCode;
    /** 以消息构造异常。 */
    public IdentityBrokerException(String message) {
        super(message);
    }

    /** 以消息与原因构造异常。 */
    public IdentityBrokerException(String message, Throwable t) {
        super(message, t);
    }

    /** 链式设置消息代码并返回自身。 */
    public IdentityBrokerException withMessageCode(String messageCode) {
        this.messageCode = messageCode;
        return this;
    }

    /** 返回可选的业务消息代码。 */
    public String getMessageCode() {
        return messageCode;
    }
}
