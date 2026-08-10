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

import jakarta.ws.rs.BadRequestException;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.oidc.OIDCClientRepresentation;

/**
 * 客户端校验工具类：委托 {@link ClientValidationProvider} 执行创建/更新校验。
 * <p>校验失败时通过 {@link ErrorHandler} 回调处理结果。</p>
 */
public class ValidationUtil {

    /** 校验客户端（无 OIDC 表示）。
     * @param create {@code true} 表示创建场景
     * @param errorHandler 校验失败回调 */
    public static void validateClient(KeycloakSession session, ClientModel client, boolean create, ErrorHandler errorHandler) throws BadRequestException {
        validateClient(session, client, null, create, errorHandler);
    }

    /** 校验客户端，可选附带 OIDC 动态注册表示。
     * @param oidcClient 非 {@code null} 时使用 {@link ClientValidationContext.OIDCContext}
     * @param errorHandler 校验失败回调 */
    public static void validateClient(KeycloakSession session, ClientModel client, OIDCClientRepresentation oidcClient, boolean create, ErrorHandler errorHandler) throws BadRequestException {
        ClientValidationProvider provider = session.getProvider(ClientValidationProvider.class);
        if (provider != null) {
            ValidationContext.Event event = create ? ValidationContext.Event.CREATE : ValidationContext.Event.UPDATE;
            ValidationResult result;

            if (oidcClient != null) {
                result = provider.validate(new ClientValidationContext.OIDCContext(event, session, client, oidcClient));
            }
            else {
                result = provider.validate(new ClientValidationContext(event, session, client));
            }

            if (!result.isValid()) {
                errorHandler.onError(result);
            }
        }
    }

    /** 校验失败时的错误处理回调。 */
    public interface ErrorHandler {

        /** @param context 包含错误的校验结果 */
        void onError(ValidationResult context);

    }

}
