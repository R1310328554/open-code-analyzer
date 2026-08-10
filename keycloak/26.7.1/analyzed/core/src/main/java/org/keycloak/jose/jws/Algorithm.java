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

package org.keycloak.jose.jws;

import org.keycloak.jose.jws.crypto.RSAProvider;
import org.keycloak.jose.jws.crypto.SignatureProvider;

/**
 * JWS 签名算法枚举（已弃用，新代码请使用 {@link org.keycloak.crypto.Algorithm}）。
 * 部分常量关联 {@link SignatureProvider} 实现。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Deprecated
public enum Algorithm {

    /** 无签名。 */
    none(null, null),
    /** HMAC SHA-256。 */
    HS256(AlgorithmType.HMAC, null),
    /** HMAC SHA-384。 */
    HS384(AlgorithmType.HMAC, null),
    /** HMAC SHA-512。 */
    HS512(AlgorithmType.HMAC, null),
    /** RSA PKCS#1 v1.5 + SHA-256。 */
    RS256(AlgorithmType.RSA, new RSAProvider()),
    /** RSA PKCS#1 v1.5 + SHA-384。 */
    RS384(AlgorithmType.RSA, new RSAProvider()),
    /** RSA PKCS#1 v1.5 + SHA-512。 */
    RS512(AlgorithmType.RSA, new RSAProvider()),
    /** RSA PSS + SHA-256。 */
    PS256(AlgorithmType.RSA, null),
    /** RSA PSS + SHA-384。 */
    PS384(AlgorithmType.RSA, null),
    /** RSA PSS + SHA-512。 */
    PS512(AlgorithmType.RSA, null),
    /** ECDSA P-256 + SHA-256。 */
    ES256(AlgorithmType.ECDSA, null),
    /** ECDSA P-384 + SHA-384。 */
    ES384(AlgorithmType.ECDSA, null),
    /** ECDSA P-521 + SHA-512。 */
    ES512(AlgorithmType.ECDSA, null),
    /** EdDSA 通用标识。 */
    EdDSA(AlgorithmType.EDDSA, null),
    /** Ed25519。 */
    Ed25519(AlgorithmType.EDDSA, null),
    /** Ed448。 */
    Ed448(AlgorithmType.EDDSA, null)
    ;

    private AlgorithmType type;
    private SignatureProvider provider;

    Algorithm(AlgorithmType type, SignatureProvider provider) {
        this.type = type;
        this.provider = provider;
    }

    /** 返回算法族类型。 */
    public AlgorithmType getType() {
        return type;
    }

    /** 返回关联的签名提供者，可能为 {@code null}。 */
    public SignatureProvider getProvider() {
        return provider;
    }
}
