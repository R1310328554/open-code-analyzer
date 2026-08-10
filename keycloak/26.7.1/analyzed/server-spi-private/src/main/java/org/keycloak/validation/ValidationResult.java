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

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 校验结果：封装是否通过及错误集合。
 * <p>提供错误汇总、国际化消息拼接及按字段查询错误的能力。</p>
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class ValidationResult {
    private final boolean valid;
    private final Set<ValidationError> errors;

    /** @param errors 校验错误集合；为空时表示校验通过 */
    public ValidationResult(Set<ValidationError> errors) {
        this.valid = errors.size() == 0;
        this.errors = Collections.unmodifiableSet(errors);
    }

    /** @return 无错误时返回 {@code true} */
    public boolean isValid() {
        return valid;
    }

    /** @return 不可变的错误集合 */
    public Set<ValidationError> getErrors() {
        return errors;
    }

    /** @return 以分号连接所有默认错误消息 */
    public String getAllErrorsAsString() {
        return getAllErrorsAsString(ValidationError::getMessage);
    }

    /** @param messagesBundle 消息资源包
     * @return 以分号连接所有国际化错误消息 */
    public String getAllLocalizedErrorsAsString(Properties messagesBundle) {
        return getAllErrorsAsString(x -> x.getLocalizedMessage(messagesBundle));
    }

    /** @param function 错误消息提取函数
     * @return 拼接后的错误字符串 */
    protected String getAllErrorsAsString(Function<ValidationError, String> function) {
        return errors.stream().map(function).collect(Collectors.joining("; "));
    }

    /** @param fieldId 字段标识
     * @return 该字段存在错误时返回 {@code true} */
    public boolean fieldHasError(String fieldId) {
        if (fieldId == null) {
            return false;
        }
        for (ValidationError error : errors) {
            if (fieldId.equals(error.getFieldId())) {
                return true;
            }
        }
        return false;
    }
}
