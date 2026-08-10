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
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 校验结果：封装一组 {@link ValidationError}，提供按校验器或 inputHint 查询的便捷方法。
 * Denotes the result of a validation.
 */
public class ValidationResult {

    /** 默认有效的空校验结果常量。 */
    /**
     * An empty ValidationResult that's valid by default.
     */
    public static final ValidationResult OK = new ValidationResult(Collections.emptySet());

    /** 由可变参数错误构造校验结果。 */
    public static ValidationResult of(ValidationError... errors) {
        return new ValidationResult(Set.of(errors));
    }

    /** 由错误集合构造校验结果。 */
    public static ValidationResult of(Set<ValidationError> errors) {
        return new ValidationResult(errors);
    }

    /** 校验过程中产生的错误集合。 */
    /**
     * Holds the {@link ValidationError ValidationError's} that occurred during validation.
     */
    private final Set<ValidationError> errors;

    /**
     * 根据给定错误集合创建校验结果；空集合表示有效。
     * Creates a new {@link ValidationResult} from the given errors.
     * <p>
     * The created {@link ValidationResult} is considered valid if the given {@code errors} are empty.
     *
     * @param errors
     */
    public ValidationResult(Set<ValidationError> errors) {
        this.errors = errors == null ? Collections.emptySet() : errors;
    }

    /**
     * 若校验无效则调用给定 {@link Consumer}。
     * Convenience method that accepts a {@link Consumer<ValidationResult>} if the result is not valid.
     *
     * @param consumer
     */
    public void ifNotValidAccept(Consumer<ValidationResult> consumer) {
        if (!isValid()) {
            consumer.accept(this);
        }
    }

    /**
     * 遍历每条错误并调用给定 {@link Consumer}。
     * Convenience method that accepts a {@link Consumer<ValidationError>}.
     *
     * @param consumer
     */
    public void forEachError(Consumer<ValidationError> consumer) {
        for (ValidationError error : getErrors()) {
            consumer.accept(error);
        }
    }

    /** @return 无错误时返回 {@code true} */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /** @return 全部校验错误 */
    public Set<ValidationError> getErrors() {
        return errors;
    }

    /**
     * 判断是否包含指定校验器 ID 的错误。
     * Checks if this {@link ValidationResult} contains {@link ValidationError ValidationError's} from the {@link Validator} with the given {@code id}.
     *
     * @param id
     * @return
     */
    public boolean hasErrorsForValidatorId(String id) {
        return getErrors().stream().anyMatch(e -> e.getValidatorId().equals(id));
    }

    /**
     * 返回指定校验器 ID 的错误集合；无则返回空集。
     * Returns a {@link Set} of {@link ValidationError ValidationError's} from the {@link Validator} with the given {@code id} if present, otherwise an empty {@link Set} is returned.
     * <p>
     *
     * @param id
     * @return
     */
    public Set<ValidationError> getErrorsForValidatorId(String id) {
        return getErrors().stream().filter(e -> e.getValidatorId().equals(id)).collect(Collectors.toSet());
    }

    /**
     * 判断是否包含指定 inputHint 的错误（如某属性路径）。
     * Checks if this {@link ValidationResult} contains {@link ValidationError ValidationError's} with the given {@code inputHint}.
     * <p>
     * This can be used to test if there are {@link ValidationError ValidationError's} for a specified attribute or attribute path.
     *
     * @param inputHint
     * @return
     */
    public boolean hasErrorsForInputHint(String inputHint) {
        return getErrors().stream().anyMatch(e -> e.getInputHint().equals(inputHint));
    }

    /**
     * 返回指定 inputHint 的错误集合；无则返回空集。
     * Returns a {@link Set} of {@link ValidationError ValidationError's} with the given {@code inputHint} if present, otherwise an empty {@link Set} is returned.
     * <p>
     *
     * @param inputHint
     * @return
     */
    public Set<ValidationError> getErrorsForInputHint(String inputHint) {
        return getErrors().stream().filter(e -> e.getInputHint().equals(inputHint)).collect(Collectors.toSet());
    }
}