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
package org.keycloak.adapters.saml.elytron;

import org.keycloak.adapters.saml.SamlAuthenticator;
import org.keycloak.adapters.saml.SamlDeployment;
import org.keycloak.adapters.saml.SamlSession;

/**
 * Elytron 环境下的 SAML 协议端点认证器。
 *
 * <p>用于处理 {@code /saml} 等 SAML 回调端点，认证成功后通过
 * {@link ElytronHttpFacade#authenticationComplete(SamlSession)} 绑定安全身份。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ElytronSamlEndpoint extends SamlAuthenticator {

    /** Elytron HTTP 门面。 */
    private final ElytronHttpFacade facade;

    /**
     * 创建 Elytron SAML 端点认证器。
     *
     * @param facade         HTTP 门面
     * @param samlDeployment SAML 部署配置
     */
    public ElytronSamlEndpoint(ElytronHttpFacade facade, SamlDeployment samlDeployment) {
        super(facade, samlDeployment, facade.getSessionStore());
        this.facade = facade;
    }

    /**
     * SAML 握手完成后通知 Elytron 门面建立已认证安全身份。
     *
     * @param samlSession 已建立的 SAML 会话
     */
    @Override
    protected void completeAuthentication(SamlSession samlSession) {
        facade.authenticationComplete(samlSession);
    }
}
