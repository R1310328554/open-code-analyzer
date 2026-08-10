/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.services.clientpolicy.executor;

import javax.security.auth.x500.X500Principal;

import org.keycloak.OAuthErrorException;
import org.keycloak.authentication.authenticators.client.X509ClientAuthenticator;
import org.keycloak.models.ClientModel;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.ClientCRUDContext;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TLS 客户端证书认证 CA 主题 DN 执行器。
 * <p>在客户端注册/更新时，为使用 X509（{@code tls_client_auth}）认证的客户端设置默认证书颁发机构（CA）主题 DN，并可强制拒绝与配置不符的 CA 名称。</p>
 *
 * @author rmartinc
 */
public class TlsClientAuthCASubjectDNExecutor implements ClientPolicyExecutorProvider<TlsClientAuthCASubjectDNExecutor.Configuration> {

    /** 执行器运行时配置 */
    private Configuration configuration;

    /** TLS CA 主题 DN 执行器配置 */
    public static class Configuration extends ClientPolicyExecutorConfigurationRepresentation {

        /** 是否强制 CA 主题 DN 必须与配置一致 */
        @JsonProperty(TlsClientAuthCASubjectDNExecutorFactory.ENFORCED)
        protected Boolean enforced;
        /** 默认 CA 主题 DN（RFC4514 或 RFC1779 格式） */
        @JsonProperty(TlsClientAuthCASubjectDNExecutorFactory.CA_SUBJECT_DN)
        protected String caSubjectDn;

        /** @return 是否启用强制校验 */
        public Boolean isEnforced() {
            return enforced;
        }

        /** @param enforced 是否启用强制校验 */
        public void setEnforced(Boolean enforced) {
            this.enforced = enforced;
        }

        /** @return 配置的 CA 主题 DN */
        public String getCaSubjectDn() {
            return caSubjectDn;
        }

        /** @param caSubjectDn CA 主题 DN */
        public void setCaSubjectDn(String caSubjectDn) {
            this.caSubjectDn = caSubjectDn;
        }
    }

    /** {@inheritDoc} 返回执行器 Provider ID */
    @Override
    public String getProviderId() {
        return TlsClientAuthCASubjectDNExecutorFactory.PROVIDER_ID;
    }

    /** {@inheritDoc} 保存运行时配置 */
    @Override
    public void setupConfiguration(Configuration config) {
        this.configuration = config;
    }

    /** {@inheritDoc} 返回配置类类型 */
    @Override
    public Class<Configuration> getExecutorConfigurationClass() {
        return Configuration.class;
    }

    /** 在客户端注册/更新事件中校验或设置 CA 主题 DN */
    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        switch (context.getEvent()) {
            case REGISTERED, UPDATED -> check(((ClientCRUDContext)context).getTargetClient());
        }
    }

    /** 校验 X509 客户端的 CA 主题 DN，未设置时写入默认值 */
    private void check(ClientModel clientModel) throws ClientPolicyException {
        OIDCAdvancedConfigWrapper oidcClient = OIDCAdvancedConfigWrapper.fromClientModel(clientModel);
        if (X509ClientAuthenticator.PROVIDER_ID.equals(clientModel.getClientAuthenticatorType())) {
            final String dn = configuration.getCaSubjectDn();
            if (oidcClient.getTlsClientAuthCASubjectDn() == null) {
                oidcClient.setTlsClientAuthCASubjectDn(dn);
            } else if (Boolean.TRUE.equals(configuration.isEnforced())) {
                try {
                    X500Principal forcedDn = X509ClientAuthenticator.constructX500Principal(dn);
                    X500Principal passedDn = X509ClientAuthenticator.constructX500Principal(oidcClient.getTlsClientAuthCASubjectDn());
                    if (!forcedDn.equals(passedDn)) {
                        throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Certificate Authority subject DN must be " + dn);
                    }
                } catch (IllegalArgumentException e) {
                    throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Certificate Authority subject DN must be " + dn);
                }
            }
        }
    }
}
