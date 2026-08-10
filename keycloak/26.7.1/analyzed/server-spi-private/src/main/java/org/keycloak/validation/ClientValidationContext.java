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

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.oidc.OIDCClientRepresentation;

/**
 * 客户端校验上下文：封装 {@link ClientModel} 的创建/更新校验场景。
 * <p>继承 {@link DefaultValidationContext}，提供标准客户端校验所需的事件与会话信息。</p>
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class ClientValidationContext extends DefaultValidationContext<ClientModel> {
    /** @param event 校验事件（创建或更新）
     * @param session Keycloak 会话
     * @param objectToValidate 待校验客户端 */
    public ClientValidationContext(Event event, KeycloakSession session, ClientModel objectToValidate) {
        super(event, session, objectToValidate);
    }

    /** OIDC 动态客户端注册专用校验上下文，附加 {@link OIDCClientRepresentation}。 */
    public static class OIDCContext extends ClientValidationContext {
        private final OIDCClientRepresentation oidcClient;

        /** @param oidcClient OIDC 客户端注册表示 */
        public OIDCContext(Event event, KeycloakSession session, ClientModel objectToValidate, OIDCClientRepresentation oidcClient) {
            super(event, session, objectToValidate);
            this.oidcClient = oidcClient;
        }

        /** @return OIDC 客户端注册表示 */
        public OIDCClientRepresentation getOIDCClient() {
            return oidcClient;
        }
    }
}
