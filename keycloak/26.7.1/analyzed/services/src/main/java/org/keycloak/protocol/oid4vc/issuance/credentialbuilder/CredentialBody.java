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

/**
 * 特定格式凭证的未完成表示，签名前可继续绑定密钥等操作。
 * <p>由 {@link CredentialBuilder} 构建，后续由签发流程完成签名。</p>
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public interface CredentialBody {

    /**
     * 在签名前将凭证绑定到持有者公钥（密钥绑定 / holder binding）。
     * @param jwk 持有者 JWK 公钥
     * @throws CredentialBuilderException 绑定失败时
     */
    void addKeyBinding(JWK jwk) throws CredentialBuilderException;
}
