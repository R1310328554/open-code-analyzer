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

package org.keycloak.broker.provider;

import java.util.stream.Stream;

import org.keycloak.jose.jwk.JWK;
import org.keycloak.models.IdentityProviderModel;

/**
 * 可暴露可复用信任材料的身份提供方，用于客户端证明或 OID4VCI 密钥证明等流程。
 *
 * Identity providers that expose reusable trust material for flows such as
 * client attestation or OID4VCI key attestation.
 */
public interface TrustMaterialIdentityProvider<C extends IdentityProviderModel> extends IdentityProvider<C> {

    /** 按 {@link TrustMaterialRequest} 条件解析并返回 JWK 密钥流。 */
    Stream<JWK> resolveKeys(TrustMaterialRequest request);

}
