/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.models.KeycloakSession;

/**
 * 通用校验上下文：在校验过程中收集错误并产出 {@link ValidationResult}。
 * <p>通过 {@link Event} 区分创建与更新场景。</p>
 */
public interface ValidationContext<T> {

    /** 校验触发事件。 */
    enum Event {
        /** 创建场景。 */ CREATE,
        /** 更新场景。 */ UPDATE
    }

    /** @return 当前校验事件 */
    Event getEvent();

    /** @return Keycloak 会话 */
    KeycloakSession getSession();

    /** @return 待校验对象 */
    T getObjectToValidate();

    /** 添加无字段 ID 的全局错误。
     * @param message 错误消息
     * @return 当前上下文 */
    ValidationContext<T> addError(String message);
    /** 添加与字段关联的错误。
     * @param fieldId 字段标识
     * @param message 错误消息
     * @return 当前上下文 */
    ValidationContext<T> addError(String fieldId, String message);
    /** 添加支持国际化的字段错误。
     * @param localizedMessageKey 消息资源键
     * @param localizedMessageParams 消息格式化参数
     * @return 当前上下文 */
    ValidationContext<T> addError(String fieldId, String message, String localizedMessageKey, Object... localizedMessageParams);

    /** @return 汇总所有错误的校验结果 */
    ValidationResult toResult();
}
