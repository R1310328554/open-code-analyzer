/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientregistration;

import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientRepresentation;

/**
 * 默认客户端注册上下文。
 * <p>用于通用 JSON 客户端表示（{@link ClientRepresentation}）的注册操作。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultClientRegistrationContext extends AbstractClientRegistrationContext {

    /**
     * @param session Keycloak 会话
     * @param client 待注册/更新的客户端表示
     * @param provider 当前注册 Provider
     */
    public DefaultClientRegistrationContext(KeycloakSession session, ClientRepresentation client, ClientRegistrationProvider provider) {
        super(session, client, provider);
    }

}
