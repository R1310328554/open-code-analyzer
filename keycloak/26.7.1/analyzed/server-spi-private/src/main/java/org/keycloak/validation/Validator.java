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

import org.keycloak.provider.Provider;

/**
 * 通用校验器接口：对给定 {@link ValidationContext} 执行校验并返回 {@link ValidationResult}。
 * <p>作为 Keycloak {@link Provider} 扩展点，可由 SPI 注册实现。</p>
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public interface Validator<T> extends Provider {
    /** @param validationContext 校验上下文
     * @return 校验结果 */
    ValidationResult validate(ValidationContext<T> validationContext);
}
