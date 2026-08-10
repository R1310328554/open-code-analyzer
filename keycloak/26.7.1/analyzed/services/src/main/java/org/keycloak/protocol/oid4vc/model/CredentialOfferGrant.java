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

package org.keycloak.protocol.oid4vc.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 凭证发放（Credential Offer）中 {@code grants} 条目的公共接口。
 * <p>具体实现包括 {@link AuthorizationCodeGrant} 与 {@link PreAuthorizedCodeGrant}，规范见 {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-offer}。</p>
 *
 * @author <a href="mailto:tdiesler@ibm.com">Thomas Diesler</a>
 */
public interface CredentialOfferGrant {

    /**
     * 返回授权类型键名（如 {@code authorization_code}、{@code urn:ietf:params:oauth:grant-type:pre-authorized_code}）。
     * @return 授权类型标识
     */
    @JsonIgnore
    String getGrantType();
}
