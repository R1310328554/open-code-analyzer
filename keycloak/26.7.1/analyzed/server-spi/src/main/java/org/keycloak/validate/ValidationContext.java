/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.validate;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.keycloak.models.KeycloakSession;

/**
 * 校验上下文：保存校验过程中的会话、错误集合与扩展属性。
 * Holds information about the validation state.
 */
public class ValidationContext {

    /** 执行校验时所在的 {@link KeycloakSession}。 */
    /**
     * Holds the {@link KeycloakSession} in which the validation is performed.
     */
    private final KeycloakSession session;

    /** 校验过程中发现的 {@link ValidationError} 集合。 */
    /**
     * Holds the {@link ValidationError} found during validation.
     */
    private Set<ValidationError> errors;

    /** 供 {@link Validator} 实现访问的可选扩展属性。 */
    /**
     * Holds optional attributes that should be available to {@link Validator} implementations.
     */
    private final Map<String, Object> attributes;

    /**
     * 创建不含 {@link KeycloakSession} 的校验上下文。
     * Creates a new {@link ValidationContext} without a {@link KeycloakSession}.
     */
    public ValidationContext() {
        this(null, null);
    }

    /**
     * 创建带 {@link KeycloakSession} 的校验上下文。
     * Creates a new {@link ValidationContext} with a {@link KeycloakSession}.
     *
     * @param session
     */
    public ValidationContext(KeycloakSession session) {
        // 使用 LinkedHashSet 保留错误顺序
        // we deliberately use a LinkedHashSet here to retain the order of errors.
        this(session, null);
    }

    /**
     * 创建校验上下文。
     * Creates a new {@link ValidationContext}.
     *
     * @param session
     * @param errors
     */
    protected ValidationContext(KeycloakSession session, Set<ValidationError> errors) {
        this.session = session;
        this.errors = errors;
        this.attributes = new HashMap<>();
    }

    /**
     * 便捷获取 {@link Validator}，用于嵌套校验。
     * Eases access to {@link Validator Validator's} for nested validation.
     *
     * @param validatorId
     * @return
     */
    public Validator validator(String validatorId) {
        return Validators.validator(session, validatorId);
    }

    /**
     * 添加一条 {@link ValidationError}。
     * Adds an {@link ValidationError}.
     *
     * @param error
     */
    public void addError(ValidationError error) {
        if (errors == null)
            errors = new LinkedHashSet<>();
        errors.add(error);
    }

    /**
     * 便捷判断当前上下文是否校验通过（等价于 {@code toResult().isValid()}）。
     * Convenience method for checking the validation status of the current {@link ValidationContext}.
     * <p>
     * This is an alternative to {@code toResult().isValid()} for brief validations.
     *
     * @return
     */
    public boolean isValid() {
        return errors == null || errors.isEmpty();
    }

    /** @return 扩展属性映射 */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /** @return 关联的 Keycloak 会话 */
    public KeycloakSession getSession() {
        return session;
    }

    /** @return 校验错误集合（无错误时返回空集） */
    public Set<ValidationError> getErrors() {
        return errors != null ? errors : Collections.emptySet();
    }

    /**
     * 根据当前错误集合生成 {@link ValidationResult}。
     * Creates a {@link ValidationResult} based on the current errors;
     *
     * @return
     */
    public ValidationResult toResult() {
        return new ValidationResult(getErrors());
    }

    @Override
    public String toString() {
        return "ValidationContext{" + "valid=" + isValid() + ", errors=" + errors + ", attributes=" + attributes + '}';
    }
}