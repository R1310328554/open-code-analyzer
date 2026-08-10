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

import java.security.Principal;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.adapters.saml.SamlPrincipal;

import org.wildfly.security.auth.SupportLevel;
import org.wildfly.security.auth.server.RealmIdentity;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SecurityRealm;
import org.wildfly.security.authz.AuthorizationIdentity;
import org.wildfly.security.authz.MapAttributes;
import org.wildfly.security.credential.Credential;
import org.wildfly.security.evidence.BearerTokenEvidence;
import org.wildfly.security.evidence.Evidence;

/**
 * 将 {@link SamlPrincipal} 映射为 Elytron {@link SecurityRealm} 身份的安全域实现。
 *
 * <p>支持 Bearer 令牌证据校验，并从 SAML 主体属性构建 {@link AuthorizationIdentity}。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class KeycloakSecurityRealm implements SecurityRealm {

    /**
     * 根据主体类型返回域身份；非 {@link SamlPrincipal} 返回 {@link RealmIdentity#NON_EXISTENT}。
     *
     * @param principal 待解析主体
     * @return 域身份
     * @throws RealmUnavailableException 域不可用时抛出
     */
    @Override
    public RealmIdentity getRealmIdentity(Principal principal) throws RealmUnavailableException {
        if (principal instanceof SamlPrincipal) {
            return createRealmIdentity((SamlPrincipal) principal);
        }
        return RealmIdentity.NON_EXISTENT;
    }

    /** 为 SAML 主体创建 Elytron 域身份包装。 */
    private RealmIdentity createRealmIdentity(SamlPrincipal principal) {
        return new RealmIdentity() {
            @Override
            public Principal getRealmIdentityPrincipal() {
                return principal;
            }

            @Override
            public SupportLevel getCredentialAcquireSupport(Class<? extends Credential> aClass, String s, AlgorithmParameterSpec algorithmParameterSpec) throws RealmUnavailableException {
                return SupportLevel.UNSUPPORTED;
            }

            @Override
            public <C extends Credential> C getCredential(Class<C> credentialType) throws RealmUnavailableException {
                return null;
            }

            @Override
            public SupportLevel getEvidenceVerifySupport(Class<? extends Evidence> evidenceType, String algorithmName) throws RealmUnavailableException {
                if (isBearerTokenEvidence(evidenceType)) {
                    return SupportLevel.SUPPORTED;
                }

                return SupportLevel.UNSUPPORTED;
            }

            @Override
            public boolean verifyEvidence(Evidence evidence) throws RealmUnavailableException {
                return principal != null;
            }

            @Override
            public boolean exists() throws RealmUnavailableException {
                return principal != null;
            }

            @Override
            public AuthorizationIdentity getAuthorizationIdentity() throws RealmUnavailableException {
                Map<String, List<String>> attributes = new HashMap<>(principal.getAttributes());
                return AuthorizationIdentity.basicIdentity(new MapAttributes(attributes));
            }
        };
    }

    /** 本域不支持凭据获取。 */
    @Override
    public SupportLevel getCredentialAcquireSupport(Class<? extends Credential> credentialType, String algorithmName, AlgorithmParameterSpec parameterSpec) throws RealmUnavailableException {
        return SupportLevel.UNSUPPORTED;
    }

    /** 域级别证据校验：仅 {@link BearerTokenEvidence} 可能受支持。 */
    @Override
    public SupportLevel getEvidenceVerifySupport(Class<? extends Evidence> evidenceType, String algorithmName) throws RealmUnavailableException {
        if (isBearerTokenEvidence(evidenceType)) {
            return SupportLevel.POSSIBLY_SUPPORTED;
        }

        return SupportLevel.UNSUPPORTED;
    }

    /** 判断证据类型是否为 {@link BearerTokenEvidence}。 */
    private boolean isBearerTokenEvidence(Class<?> evidenceType) {
        return evidenceType != null && evidenceType.equals(BearerTokenEvidence.class);
    }
}
