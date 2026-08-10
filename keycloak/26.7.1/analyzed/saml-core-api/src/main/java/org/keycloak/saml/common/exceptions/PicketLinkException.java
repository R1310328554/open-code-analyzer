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

/**
 * <p>安全模块抛出的任何异常均继承自此运行时异常类，便于其他模块与扩展在需要时用单个 catch 块捕获全部安全相关异常。</p>
 *
 * <p>此类作为根异常而非 {@link SecurityException}，以避免与 JEE 容器等框架对 {@link SecurityException} 的特殊处理产生混淆或冲突。</p>
 */
public class PicketLinkException extends RuntimeException {

    /** 构造无消息的 PicketLink 异常。 */
    public PicketLinkException() {
        super();
    }

    /**
     * 构造带消息及根因的异常。
     *
     * @param message 错误描述
     * @param cause 根因
     */
    public PicketLinkException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造带消息的异常。
     *
     * @param message 错误描述
     */
    public PicketLinkException(String message) {
        super(message);
    }

    /**
     * 构造以给定异常为根因的异常。
     *
     * @param cause 根因
     */
    public PicketLinkException(Throwable cause) {
        super(cause);
    }
}
