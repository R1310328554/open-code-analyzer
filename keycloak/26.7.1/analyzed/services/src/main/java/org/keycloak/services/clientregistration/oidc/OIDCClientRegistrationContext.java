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

package org.keycloak.services.clientregistration.oidc;

import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.oidc.OIDCClientRepresentation;
import org.keycloak.services.clientregistration.AbstractClientRegistrationContext;
import org.keycloak.services.clientregistration.ClientRegistrationProvider;

/**
 * OIDC 动态客户端注册上下文。
 * <p>在通用注册上下文基础上保留原始 {@link OIDCClientRepresentation}，供策略与 Provider 使用。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OIDCClientRegistrationContext extends AbstractClientRegistrationContext {

    /** 原始 OIDC 客户端元数据表示 */
    private final OIDCClientRepresentation oidcRep;

    /**
     * @param session Keycloak 会话
     * @param client 转换后的内部客户端表示
     * @param provider 当前 OIDC 注册 Provider
     * @param oidcRep 原始 OIDC 客户端元数据
     */
    public OIDCClientRegistrationContext(KeycloakSession session, ClientRepresentation client, ClientRegistrationProvider provider, OIDCClientRepresentation oidcRep) {
        super(session, client, provider);
        this.oidcRep = oidcRep;
    }

}
