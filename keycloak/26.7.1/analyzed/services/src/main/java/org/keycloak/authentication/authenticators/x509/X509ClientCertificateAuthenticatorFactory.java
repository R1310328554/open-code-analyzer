/*
 * Copyright 2016 Analytical Graphics, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.authentication.authenticators.x509;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;

/**
 * @author <a href="mailto:pnalyvayko@agi.com">Peter Nalyvayko</a>
 * @version $Revision: 1 $
 *
 */
public class X509ClientCertificateAuthenticatorFactory  extends AbstractX509ClientCertificateAuthenticatorFactory {

    /** Provider ID：auth-x509-client-username-form。 */
    public static final String PROVIDER_ID = "auth-x509-client-username-form";
    /** 单例认证器实例。 */
    public static final X509ClientCertificateAuthenticator SINGLETON =
            new X509ClientCertificateAuthenticator();


    @Override
    /** @return 帮助说明：从双向 SSL 握手中的 X509 证书校验用户名与密码 */
    public String getHelpText() {
        return "Validates username and password from X509 client certificate received as a part of mutual SSL handshake.";
    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "X509/Validate Username Form";
    }

    @Override
    /** @return 执行要求选项（ALTERNATIVE/REQUIRED/DISABLED） */
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }


    @Override
    /** @return {@link X509ClientCertificateAuthenticator} 单例 */
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    /** @return Provider ID */
    public String getId() {
        return PROVIDER_ID;
    }
}
