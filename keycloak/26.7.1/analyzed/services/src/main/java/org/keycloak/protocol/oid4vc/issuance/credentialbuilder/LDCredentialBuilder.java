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

import org.keycloak.VCFormat;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.oid4vc.model.CredentialBuildConfig;
import org.keycloak.protocol.oid4vc.model.CredentialDefinition;
import org.keycloak.protocol.oid4vc.model.SupportedCredentialConfiguration;
import org.keycloak.protocol.oid4vc.model.VerifiableCredential;


/**
 * LDP-VC（{@code ldp_vc}）格式可验证凭证构建器。
 * <p>内部表示本身即 LDP 格式，主要设置 issuer 后包装为 {@link LDCredentialBody}。</p>
 * {@see https://www.w3.org/TR/vc-data-model/}
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public class LDCredentialBuilder implements CredentialBuilder {

    /** 默认构造。 */
    public LDCredentialBuilder() {
    }

    /** {@inheritDoc} 返回 {@link VCFormat#LDP_VC}。 */
    @Override
    public String getSupportedFormat() {
        return VCFormat.LDP_VC;
    }

    /** {@inheritDoc} 设置含 {@code @context} 的 {@link CredentialDefinition}。 */
    @Override
    public void contributeToMetadata(SupportedCredentialConfiguration credentialConfig, CredentialScopeModel credentialScope) {
        CredentialDefinition credentialDefinition = CredentialDefinition.parse(credentialScope);
        credentialConfig.setCredentialDefinition(credentialDefinition);
    }

    @Override
    public LDCredentialBody buildCredentialBody(
            VerifiableCredential verifiableCredential,
            CredentialBuildConfig credentialBuildConfig
    ) throws CredentialBuilderException {
        // 默认内部表示即 LDP 格式，仅需设置 issuer
        verifiableCredential.setIssuer(credentialBuildConfig.getCredentialIssuer());
        return new LDCredentialBody(verifiableCredential);
    }
}
