/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oid4vc.issuance.credentialbuilder;

import org.keycloak.jose.jwk.JWK;
import org.keycloak.protocol.oid4vc.model.VerifiableCredential;

import org.jboss.logging.Logger;

/**
 * LDP-VC 格式的未完成凭证体，直接持有 {@link VerifiableCredential} 对象。
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public class LDCredentialBody implements CredentialBody {

    private static final Logger LOGGER = Logger.getLogger(LDCredentialBody.class);
    /** 待签名的可验证凭证对象。 */
    private final VerifiableCredential verifiableCredential;

    /** @param verifiableCredential 可验证凭证 */
    public LDCredentialBody(VerifiableCredential verifiableCredential) {
        this.verifiableCredential = verifiableCredential;
    }

    /** {@inheritDoc} LDP-VC 密钥绑定尚未实现，仅记录警告。 */
    public void addKeyBinding(JWK jwk) throws CredentialBuilderException {
        LOGGER.warnf("Key binding is not yet implemented for LDP credentials");
    }

    /** @return 内部可验证凭证对象 */
    public VerifiableCredential getVerifiableCredential() {
        return verifiableCredential;
    }
}
