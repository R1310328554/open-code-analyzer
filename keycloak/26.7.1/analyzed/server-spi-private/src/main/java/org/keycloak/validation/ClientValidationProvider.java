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

import org.keycloak.models.ClientModel;

/**
 * 客户端校验提供者：对 {@link ClientModel} 执行创建/更新校验。
 * <p>扩展 {@link Validator}，支持标准校验与 OIDC 动态注册场景。</p>
 */
public interface ClientValidationProvider extends Validator<ClientModel> {

    // OIDC 动态客户端注册专用校验入口
    // for a special case when performing OIDC client registration
    /** @param validationContext 含 OIDC 表示的校验上下文
     * @return 校验结果 */
    ValidationResult validate(ClientValidationContext.OIDCContext validationContext);

    @Override
    default void close() {
    }

}
