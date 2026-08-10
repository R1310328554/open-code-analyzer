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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;

import jakarta.ws.rs.core.Response;

/**
 * 校验错误：记录校验器 ID、输入提示、国际化消息键及参数。
 * Denotes an error found during validation.
 */
public class ValidationError implements Serializable {

    private static final long serialVersionUID = 4950708316675951914L;

    /** 通用无效值消息键。 */
    /**
     * A generic invalid value message.
     */
    public static final String MESSAGE_INVALID_VALUE = "error-invalid-value";

    /** 空消息参数的共享常量。 */
    /**
     * Empty message parameters fly-weight.
     */
    private static final Object[] EMPTY_PARAMETERS = {};

    /** 报告此错误的校验器 ID。 */
    /**
     * Holds the name of the validator that reported the {@link ValidationError}.
     */
    private final String validatorId;

    /** 输入提示：可为属性名、嵌套字段路径或逻辑键。 */
    /**
     * Holds an inputHint.
     * <p>
     * This could be a attribute name, a nested field path or a logical key.
     */
    private final String inputHint;

    /** 用于国际化的消息键。 */
    /**
     * Holds the message key for translation.
     */
    private final String message;

    /** 消息翻译的可选参数。 */
    /**
     * Optional parameters for the message translation.
     */
    private final Object[] messageParameters;

    /** 与此错误关联的 HTTP 状态码提示；调用方可选择是否采用。
     *
     * The status code associated with this error. This information serves as a hint so that
     * callers can choose whether they want to respect the status defined for the error.
     *
     * TODO: Should be better to refactor {@code Messages} to bing messages to status code as well as any other metadata that might be associated with the message.
     */
    private Response.Status statusCode = Response.Status.BAD_REQUEST;

    /** 构造校验错误（无额外消息参数）。 */
    public ValidationError(String validatorId, String inputHint, String message) {
        this(validatorId, inputHint, message, EMPTY_PARAMETERS);
    }

    /** 构造校验错误（含消息参数）。 */
    public ValidationError(String validatorId, String inputHint, String message, Object... messageParameters) {
        this.validatorId = validatorId;
        this.inputHint = inputHint;
        this.message = message;
        this.messageParameters = messageParameters == null ? EMPTY_PARAMETERS : messageParameters.clone();
    }

    public String getValidatorId() {
        return validatorId;
    }

    public String getInputHint() {
        return inputHint;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 返回原始消息参数（如实际提交待校验的值）。
     * Returns the raw message parameters, e.g. the actual input that was given for validation.
     *
     * @return
     * @see #getInputHintWithMessageParameters()
     */
    public Object[] getMessageParameters() {
        return messageParameters;
    }

    /**
     * 使用给定格式化函数渲染错误消息。
     * Formats the current {@link ValidationError} with the given formatter {@link java.util.function.Function}.
     * <p>
     * The formatter {@link java.util.function.Function} will be called with the {@link #message} and
     * {@link #getInputHintWithMessageParameters()} to render the error message.
     *
     * @param formatter
     * @return
     */
    public String formatMessage(BiFunction<String, Object[], String> formatter) {
        Objects.requireNonNull(formatter, "formatter must not be null");
        return formatter.apply(message, getInputHintWithMessageParameters());
    }

    /**
     * 返回数组：首元素为 {@link #inputHint}，其后为 {@link #messageParameters}。
     * Returns an array where the first element is the {@link #inputHint} followed by the {@link #messageParameters}.
     *
     * @return
     */
    public Object[] getInputHintWithMessageParameters() {

        // 将 inputHint 插入消息参数首位
        // insert to current input hint into the message
        Object[] args = new Object[messageParameters.length + 1];
        args[0] = getInputHint();
        System.arraycopy(messageParameters, 0, args, 1, messageParameters.length);

        return args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValidationError)) {
            return false;
        }
        ValidationError that = (ValidationError) o;
        return Objects.equals(validatorId, that.validatorId) && Objects.equals(inputHint, that.inputHint) && Objects.equals(message, that.message) && Arrays.equals(messageParameters, that.messageParameters);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(validatorId, inputHint, message);
        result = 31 * result + Arrays.hashCode(messageParameters);
        return result;
    }

    @Override
    public String toString() {
        return "ValidationError{" + "validatorId='" + validatorId + '\'' + ", inputHint='" + inputHint + '\'' + ", message='" + message + '\'' + ", messageParameters=" + Arrays.toString(messageParameters) + '}';
    }

    /** 设置 HTTP 状态码并返回自身（链式调用）。 */
    public ValidationError setStatusCode(Response.Status statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /** @return 关联的 HTTP 状态码 */
    public Response.Status getStatusCode() {
        return statusCode;
    }
}
