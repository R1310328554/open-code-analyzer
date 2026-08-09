/*
 * Copyright 2023 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.http;

import io.netty.handler.codec.DefaultHeaders.NameValidator;
import io.netty.handler.codec.DefaultHeaders.ValueValidator;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * {@link HttpHeadersFactory} 的不可变构建器，自身也实现工厂接口。
 * <p>
每个 {@code with-} 方法返回新实例；推荐从 {@link DefaultHttpHeadersFactory#headersFactory()} 开始。
可配置名称/值校验、多值头合并及 trailer 专用规则。
 */
public final class DefaultHttpHeadersFactory implements HttpHeadersFactory {
    private static final NameValidator<CharSequence> DEFAULT_NAME_VALIDATOR = new NameValidator<CharSequence>() {
        @Override
        public void validateName(CharSequence name) {
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("empty headers are not allowed [" + name + ']');
            }
            int index = HttpHeaderValidationUtil.validateToken(name);
            if (index != -1) {
                throw new IllegalArgumentException("a header name can only contain \"token\" characters, " +
                        "but found invalid character 0x" + Integer.toHexString(name.charAt(index)) +
                        " at index " + index + " of header '" + name + "'.");
            }
        }
    };
    private static final ValueValidator<CharSequence> DEFAULT_VALUE_VALIDATOR = new ValueValidator<CharSequence>() {
        @Override
        public void validate(CharSequence value) {
            int index = HttpHeaderValidationUtil.validateValidHeaderValue(value);
            if (index != -1) {
                throw new IllegalArgumentException("a header value contains prohibited character 0x" +
                        Integer.toHexString(value.charAt(index)) + " at index " + index + '.');
            }
        }
    };
    private static final NameValidator<CharSequence> DEFAULT_TRAILER_NAME_VALIDATOR =
            new NameValidator<CharSequence>() {
                @Override
                public void validateName(CharSequence name) {
                    DEFAULT_NAME_VALIDATOR.validateName(name);
                    if (HttpHeaderNames.CONTENT_LENGTH.contentEqualsIgnoreCase(name)
                            || HttpHeaderNames.TRANSFER_ENCODING.contentEqualsIgnoreCase(name)
                            || HttpHeaderNames.TRAILER.contentEqualsIgnoreCase(name)) {
                        throw new IllegalArgumentException("prohibited trailing header: " + name);
                    }
                }
            };

    @SuppressWarnings("unchecked")
    private static final NameValidator<CharSequence> NO_NAME_VALIDATOR = NameValidator.NOT_NULL;
    @SuppressWarnings("unchecked")
    private static final ValueValidator<CharSequence> NO_VALUE_VALIDATOR =
            (ValueValidator<CharSequence>) ValueValidator.NO_VALIDATION;

    private static final DefaultHttpHeadersFactory DEFAULT =
            new DefaultHttpHeadersFactory(DEFAULT_NAME_VALIDATOR, DEFAULT_VALUE_VALIDATOR, false);
    private static final DefaultHttpHeadersFactory DEFAULT_TRAILER =
            new DefaultHttpHeadersFactory(DEFAULT_TRAILER_NAME_VALIDATOR, DEFAULT_VALUE_VALIDATOR, false);
    private static final DefaultHttpHeadersFactory DEFAULT_COMBINING =
            new DefaultHttpHeadersFactory(DEFAULT.nameValidator, DEFAULT.valueValidator, true);
    private static final DefaultHttpHeadersFactory DEFAULT_NO_VALIDATION =
            new DefaultHttpHeadersFactory(NO_NAME_VALIDATOR, NO_VALUE_VALIDATOR, false);

    /** 头部名称校验器。 */
    private final NameValidator<CharSequence> nameValidator;
    /** 头部值校验器。 */
    private final ValueValidator<CharSequence> valueValidator;
    /** 是否将同名多值头合并为逗号分隔的单行。 */
    private final boolean combiningHeaders;

    /**
     * Create a header builder with the given settings.
     *
     * @param nameValidator The name validator to use, not null.
     * @param valueValidator The value validator to use, not null.
     * @param combiningHeaders {@code true} if multi-valued headers should be combined into single lines.
     */
    private DefaultHttpHeadersFactory(
            NameValidator<CharSequence> nameValidator,
            ValueValidator<CharSequence> valueValidator,
            boolean combiningHeaders) {
        this.nameValidator = checkNotNull(nameValidator, "nameValidator");
        this.valueValidator = checkNotNull(valueValidator, "valueValidator");
        this.combiningHeaders = combiningHeaders;
    }

    /**
     * 获取创建普通 HTTP 头的默认工厂，启用推荐校验。
     * <p>
     * 创建的 {@link HttpHeaders} 实例会校验 token 字符与非法值。
     */
    public static DefaultHttpHeadersFactory headersFactory() {
        return DEFAULT;
    }

    /**
     * 获取创建 trailing headers 的默认工厂。
     * <p>
     * 额外禁止 Content-Length、Transfer-Encoding、Trailer 等不允许出现在 trailer 中的头。
     */
    public static DefaultHttpHeadersFactory trailersFactory() {
        return DEFAULT_TRAILER;
    }

    /** 根据当前配置创建新的 {@link HttpHeaders} 实例。 */
    @Override
    public HttpHeaders newHeaders() {
        if (isCombiningHeaders()) {
            return new CombinedHttpHeaders(getNameValidator(), getValueValidator());
        }
        return new DefaultHttpHeaders(getNameValidator(), getValueValidator());
    }

    /** 创建容量为 2 的空头部实例（适合预期条目很少的场景）。 */
    @Override
    public HttpHeaders newEmptyHeaders() {
        if (isCombiningHeaders()) {
            return new CombinedHttpHeaders(getNameValidator(), getValueValidator(), 2);
        }
        return new DefaultHttpHeaders(getNameValidator(), getValueValidator(), 2);
    }

    /**
     * Create a new builder that has HTTP header name validation enabled or disabled.
     * <p>
     * <b>Warning!</b> Setting {@code validation} to {@code false} will mean that Netty won't
     * validate & protect against user-supplied headers that are malicious.
     * This can leave your server implementation vulnerable to
     * <a href="https://cwe.mitre.org/data/definitions/113.html">
     *     CWE-113: Improper Neutralization of CRLF Sequences in HTTP Headers ('HTTP Response Splitting')
     * </a>.
     * When disabling this validation, it is the responsibility of the caller to ensure that the values supplied
     * do not contain a non-url-escaped carriage return (CR) and/or line feed (LF) characters.
     *
     * @param validation If validation should be enabled or disabled.
     * @return The new builder.
     */
    public DefaultHttpHeadersFactory withNameValidation(boolean validation) {
        return withNameValidator(validation ? DEFAULT_NAME_VALIDATOR : NO_NAME_VALIDATOR);
    }

    /**
     * Create a new builder that with the given {@link NameValidator}.
     * <p>
     * <b>Warning!</b> If the given validator does not check that the header names are standards compliant, Netty won't
     * validate & protect against user-supplied headers that are malicious.
     * This can leave your server implementation vulnerable to
     * <a href="https://cwe.mitre.org/data/definitions/113.html">
     *     CWE-113: Improper Neutralization of CRLF Sequences in HTTP Headers ('HTTP Response Splitting')
     * </a>.
     * When disabling this validation, it is the responsibility of the caller to ensure that the values supplied
     * do not contain a non-url-escaped carriage return (CR) and/or line feed (LF) characters.
     *
     * @param validator The HTTP header name validator to use.
     * @return The new builder.
     */
    public DefaultHttpHeadersFactory withNameValidator(NameValidator<CharSequence> validator) {
        if (nameValidator == checkNotNull(validator, "validator")) {
            return this;
        }
        if (validator == DEFAULT_NAME_VALIDATOR && valueValidator == DEFAULT_VALUE_VALIDATOR) {
            return combiningHeaders ? DEFAULT_COMBINING : DEFAULT;
        }
        return new DefaultHttpHeadersFactory(validator, valueValidator, combiningHeaders);
    }

    /**
     * Create a new builder that has HTTP header value validation enabled or disabled.
     * <p>
     * <b>Warning!</b> Setting {@code validation} to {@code false} will mean that Netty won't
     * validate & protect against user-supplied headers that are malicious.
     * This can leave your server implementation vulnerable to
     * <a href="https://cwe.mitre.org/data/definitions/113.html">
     *     CWE-113: Improper Neutralization of CRLF Sequences in HTTP Headers ('HTTP Response Splitting')
     * </a>.
     * When disabling this validation, it is the responsibility of the caller to ensure that the values supplied
     * do not contain a non-url-escaped carriage return (CR) and/or line feed (LF) characters.
     *
     * @param validation If validation should be enabled or disabled.
     * @return The new builder.
     */
    public DefaultHttpHeadersFactory withValueValidation(boolean validation) {
        return withValueValidator(validation ? DEFAULT_VALUE_VALIDATOR : NO_VALUE_VALIDATOR);
    }

    /**
     * Create a new builder that with the given {@link ValueValidator}.
     * <p>
     * <b>Warning!</b> If the given validator does not check that the header values are standards compliant, Netty won't
     * validate & protect against user-supplied headers that are malicious.
     * This can leave your server implementation vulnerable to
     * <a href="https://cwe.mitre.org/data/definitions/113.html">
     *     CWE-113: Improper Neutralization of CRLF Sequences in HTTP Headers ('HTTP Response Splitting')
     * </a>.
     * When disabling this validation, it is the responsibility of the caller to ensure that the values supplied
     * do not contain a non-url-escaped carriage return (CR) and/or line feed (LF) characters.
     *
     * @param validator The HTTP header name validator to use.
     * @return The new builder.
     */
    public DefaultHttpHeadersFactory withValueValidator(ValueValidator<CharSequence> validator) {
        if (valueValidator == checkNotNull(validator, "validator")) {
            return this;
        }
        if (nameValidator == DEFAULT_NAME_VALIDATOR && validator == DEFAULT_VALUE_VALIDATOR) {
            return combiningHeaders ? DEFAULT_COMBINING : DEFAULT;
        }
        return new DefaultHttpHeadersFactory(nameValidator, validator, combiningHeaders);
    }

    /**
     * Create a new builder that has HTTP header validation enabled or disabled.
     * <p>
     * <b>Warning!</b> Setting {@code validation} to {@code false} will mean that Netty won't
     * validate & protect against user-supplied headers that are malicious.
     * This can leave your server implementation vulnerable to
     * <a href="https://cwe.mitre.org/data/definitions/113.html">
     *     CWE-113: Improper Neutralization of CRLF Sequences in HTTP Headers ('HTTP Response Splitting')
     * </a>.
     * When disabling this validation, it is the responsibility of the caller to ensure that the values supplied
     * do not contain a non-url-escaped carriage return (CR) and/or line feed (LF) characters.
     *
     * @param validation If validation should be enabled or disabled.
     * @return The new builder.
     */
    public DefaultHttpHeadersFactory withValidation(boolean validation) {
        if (this == DEFAULT && !validation) {
            return DEFAULT_NO_VALIDATION;
        }
        if (this == DEFAULT_NO_VALIDATION && validation) {
            return DEFAULT;
        }
        return withNameValidation(validation).withValueValidation(validation);
    }

    /**
     * Create a new builder that will build {@link HttpHeaders} objects that either combine
     * multi-valued headers, or not.
     *
     * @param combiningHeaders {@code true} if multi-valued headers should be combined, otherwise {@code false}.
     * @return The new builder.
     */
    public DefaultHttpHeadersFactory withCombiningHeaders(boolean combiningHeaders) {
        if (this.combiningHeaders == combiningHeaders) {
            return this;
        }
        return new DefaultHttpHeadersFactory(nameValidator, valueValidator, combiningHeaders);
    }

    /**
     * Get the currently configured {@link NameValidator}.
     * <p>
     * This method will be used by the {@link #newHeaders()} method.
     *
     * @return The configured name validator.
     */
    public NameValidator<CharSequence> getNameValidator() {
        return nameValidator;
    }

    /**
     * Get the currently configured {@link ValueValidator}.
     * <p>
     * This method will be used by the {@link #newHeaders()} method.
     *
     * @return The configured value validator.
     */
    public ValueValidator<CharSequence> getValueValidator() {
        return valueValidator;
    }

    /**
     * Check whether header combining is enabled or not.
     *
     * @return {@code true} if header value combining is enabled, otherwise {@code false}.
     */
    public boolean isCombiningHeaders() {
        return combiningHeaders;
    }

    /**
     * Check whether header name validation is enabled.
     *
     * @return {@code true} if header name validation is enabled, otherwise {@code false}.
     */
    public boolean isValidatingHeaderNames() {
        return nameValidator != NO_NAME_VALIDATOR;
    }

    /**
     * Check whether header value validation is enabled.
     *
     * @return {@code true} if header value validation is enabled, otherwise {@code false}.
     */
    public boolean isValidatingHeaderValues() {
        return valueValidator != NO_VALUE_VALIDATOR;
    }
}
