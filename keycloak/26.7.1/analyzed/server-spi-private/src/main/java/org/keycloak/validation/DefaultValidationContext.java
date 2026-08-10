/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.validation;

import java.util.HashSet;
import java.util.Set;

import org.keycloak.models.KeycloakSession;

/**
 * 校验上下文默认实现：收集 {@link ValidationError} 并构建 {@link ValidationResult}。
 * <p>子类可扩展特定领域（如客户端）的校验上下文。</p>
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public abstract class DefaultValidationContext<T> implements ValidationContext<T> {

    private final Event event;
    private final KeycloakSession session;
    private final T objectToValidate;
    private final Set<ValidationError> errors;

    /** @param event 校验事件
     * @param session Keycloak 会话
     * @param objectToValidate 待校验对象 */
    public DefaultValidationContext(Event event, KeycloakSession session, T objectToValidate) {
        this.event = event;
        this.session = session;
        this.objectToValidate = objectToValidate;
        this.errors = new HashSet<>();
    }

    /** @return 校验事件（创建或更新） */
    @Override
    public Event getEvent() {
        return event;
    }

    /** @return 当前 Keycloak 会话 */
    @Override
    public KeycloakSession getSession() {
        return session;
    }

    /** @return 待校验对象 */
    @Override
    public T getObjectToValidate() {
        return objectToValidate;
    }

    /** @param message 全局错误消息（无字段 ID）
     * @return 当前上下文（链式调用） */
    @Override
    public ValidationContext<T> addError(String message) {
        return addError(null, message, null);
    }

    /** @param fieldId 出错字段标识
     * @param message 错误消息
     * @return 当前上下文（链式调用） */
    @Override
    public ValidationContext<T> addError(String fieldId, String message) {
        return addError(fieldId, message, null);
    }

    /** @param localizedMessageKey 国际化消息键
     * @param localizedMessageParams 国际化消息参数
     * @return 当前上下文（链式调用） */
    @Override
    public ValidationContext<T> addError(String fieldId, String message, String localizedMessageKey, Object... localizedMessageParams) {
        errors.add(new ValidationError(fieldId, message, localizedMessageKey, localizedMessageParams));
        return this;
    }

    /** @return 基于已收集错误构建的校验结果 */
    @Override
    public ValidationResult toResult() {
        return new ValidationResult(new HashSet<>(errors));
    }
}
