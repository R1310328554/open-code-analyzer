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

import java.util.Collection;

/**
 * 通用值类型校验器基类：支持单值与集合并校验；空值行为由 {@link #IGNORE_EMPTY_VALUE} 配置控制。
 * <ul>
 * <li>接受支持的类型及其集合。</li>
 * <li>默认对 null/空值报错；配置为 {@code true} 时跳过，逻辑在 {@link #skipValidation(Object, ValidatorConfig)} 实现。</li>
 * </ul>
 *
 * Base class for arbitrary value type validators. Functionality covered in this base class:
 * <ul>
 * <li>accepts supported type, collection of supported type.
 * <li>behavior around null and empty values is controlled by {@link #IGNORE_EMPTY_VALUE} configuration option which is
 * boolean. Error should be produced for them by default, but they should be ignored if that option is
 * <code>true</code>. Logic must be implemented in {@link #skipValidation(Object, ValidatorConfig)}.
 * </ul>
 * 
 * @author Vlastimil Elias <velias@redhat.com>
 *
 */
public abstract class AbstractSimpleValidator implements SimpleValidator {

    /**
     * 配置项：为 {@code true} 时忽略 null、空串及空白字符串，不报错。
     * 常用于 UserProfile 可选属性（必填由独立校验器负责）。
     *
     * Config option which allows to switch validator to ignore null, empty string and even blank string value - not to
     * produce error for them. Used eg. in UserProfile where we have optional attributes and required concern is checked
     * by separate validators.
     */
    public static final String IGNORE_EMPTY_VALUE = "ignore.empty.value";

    @Override
    public ValidationContext validate(Object input, String inputHint, ValidationContext context, ValidatorConfig config) {
        if (input instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Object> values = (Collection<Object>) input;

            for (Object value : values) {
                validate(value, inputHint, context, config);
            }

            return context;
        }

        if (skipValidation(input, config)) {
            return context;
        }

        doValidate(input, inputHint, context, config);

        return context;
    }

    /**
     * 校验值的类型、格式、范围等；须通过 {@link ValidationContext#addError(ValidationError)} 报告错误。
     * 输入为 Collection 时可能对每个元素多次调用。
     *
     * Validate type, format, range of the value etc. Always use {@link ValidationContext#addError(ValidationError)} to
     * report error to the user! Can be called multiple time for one validation if input is Collection.
     * 
     * @param value to be validated, never null
     * @param inputHint
     * @param context for the validation. Add errors into it.
     * @param config of the validation if provided
     * 
     * @see #skipValidation(Object, ValidatorConfig)
     */
    protected abstract void doValidate(Object value, String inputHint, ValidationContext context, ValidatorConfig config);

    /**
     * 判断是否跳过当前值的校验；通常依据 {@link #IGNORE_EMPTY_VALUE}，参见 {@link #isIgnoreEmptyValuesConfigured(ValidatorConfig)}。
     *
     * Decide if validation of individual value should be skipped or not. It should be controlled by
     * {@link #IGNORE_EMPTY_VALUE} configuration option, see {@link #isIgnoreEmptyValuesConfigured(ValidatorConfig)}.
     * 
     * @param value currently validated we make decision for
     * @param config to look for options in
     * @return true if validation should be skipped for this value -
     *         {@link #doValidate(Object, String, ValidationContext, ValidatorConfig)} is not called in this case.
     *         
     * @see #doValidate(Object, String, ValidationContext, ValidatorConfig)         
     */
    protected abstract boolean skipValidation(Object value, ValidatorConfig config);

    /**
     * 默认实现：读取 {@link #IGNORE_EMPTY_VALUE} 配置项。
     * Default implementation only looks for {@link #IGNORE_EMPTY_VALUE} configuration option.
     * 
     * @param config to get option from
     * @return
     */
    protected boolean isIgnoreEmptyValuesConfigured(ValidatorConfig config) {
        return config != null && config.getBooleanOrDefault(IGNORE_EMPTY_VALUE, false);
    }
}
