/*
 *
 *  * Copyright 2021  Red Hat, Inc. and/or its affiliates
 *  * and other contributors as indicated by the @author tags.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.keycloak.userprofile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import jakarta.ws.rs.core.Response;

import org.keycloak.validate.ValidationError;

/**
 * 用户配置校验异常：封装属性校验错误集合。
 * <p>提供按类型或属性名查询错误、构建轻量级异常及获取 HTTP 状态码的能力。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public final class ValidationException extends RuntimeException {

    private final Map<String, List<Error>> errors;

    /** @param error 单个校验错误 */
    public ValidationException(ValidationError error) {
        errors = Map.of(error.getMessage(), List.of(new Error(error)));
    }

    private ValidationException(Map<String, List<Error>> errors) {
        this.errors = errors;
    }

    /** @return 所有校验错误的扁平列表 */
    public List<Error> getErrors() {
        return errors.values().stream().reduce(new ArrayList<>(), (l, r) -> {
            l.addAll(r);
            return l;
        }, (l, r) -> l);
    }

    /** @param types 错误消息类型；为空时检查是否存在任意错误
     * @return 存在匹配错误时返回 {@code true} */
    public boolean hasError(String... types) {
        if (types.length == 0) {
            return !errors.isEmpty();
        }

        for (String type : types) {
            if (errors.containsKey(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否存在与给定属性名相关的校验错误。
     *
     * @param name 属性名
     * @return 存在相关错误时返回 {@code true}
     */
    public boolean isAttributeOnError(String... name) {
        if (name.length == 0) {
            return !errors.isEmpty();
        }

        List<String> names = Arrays.asList(name);

        return errors.values().stream().flatMap(Collection::stream).anyMatch(error -> names.contains(error.getAttribute()));
    }

    @Override
    public String toString() {
        return "ValidationException [errors=" + errors + "]";
    }

    @Override
    public String getMessage() {
        return toString();
    }

    /**
     * 轻量级校验错误收集器，避免创建带昂贵堆栈跟踪的空异常。
     * Creating a light-weight consumer of validation errors to avoid creating an empty exception which has an expensive stack trace without having the need for it.
     */
    public static class ValidationExceptionBuilder implements Consumer<ValidationError> {

        private final Map<String, List<Error>> errors = new HashMap<>();

        @Override
        public void accept(ValidationError error) {
            addError(error);
        }

        void addError(ValidationError error) {
            List<Error> errors = this.errors.computeIfAbsent(error.getMessage(), (k) -> new ArrayList<>());
            errors.add(new Error(error));
        }

        /** @return 是否已收集到校验错误 */
        public boolean hasError() {
            return !errors.isEmpty();
        }

        /** @return 基于已收集错误构建 {@link ValidationException} */
        public ValidationException build() {
            return new ValidationException(errors);
        }
    }

    /** @return 错误对应的 HTTP 状态码；默认 {@link Response.Status#BAD_REQUEST} */
    public Response.Status getStatusCode() {
        for (Map.Entry<String, List<Error>> entry : errors.entrySet()) {
            for (Error error : entry.getValue()) {
                if (!Response.Status.BAD_REQUEST.equals(error.getStatusCode())) {
                    return error.getStatusCode();
                }
            }
        }
        return Response.Status.BAD_REQUEST;
    }

    /** 单个校验错误的不可变包装。 */
    public static class Error implements Serializable {

        private final ValidationError error;

        /** @param error 底层 {@link ValidationError} */
        public Error(ValidationError error) {
            this.error = error;
        }

        /** @return 出错属性名（输入提示） */
        public String getAttribute() {
            return error.getInputHint();
        }

        public String getMessage() {
            return error.getMessage();
        }

        public Object[] getMessageParameters() {
            return error.getInputHintWithMessageParameters();
        }

        @Override
        public String toString() {
            return "Error [error=" + error + "]";
        }

        /** @param messageFormatter 消息格式化函数
         * @return 格式化后的错误消息 */
        public String getFormattedMessage(BiFunction<String, Object[], String>  messageFormatter) {
            return messageFormatter.apply(getMessage(), getMessageParameters());
        }

        public Response.Status getStatusCode() {
            return error.getStatusCode();
        }
    }

}
