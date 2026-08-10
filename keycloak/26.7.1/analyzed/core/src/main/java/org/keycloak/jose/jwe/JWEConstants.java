/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.jose.jwe;

import org.keycloak.common.crypto.CryptoConstants;

/**
 * JWE（JSON Web Encryption）算法与加密方式常量，对应 RFC 7518 注册的 {@code alg} 与 {@code enc} 标识。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class JWEConstants {

    /** 直接密钥模式（{@code dir}），内容加密密钥由共享密钥直接提供。 */
    public static final String DIRECT = "dir";
    /** AES-128 密钥包装算法。 */
    public static final String A128KW = CryptoConstants.A128KW;
    /** RSA PKCS#1 v1.5 密钥加密算法。 */
    public static final String RSA1_5 = CryptoConstants.RSA1_5;
    /** RSA-OAEP 密钥加密算法。 */
    public static final String RSA_OAEP = CryptoConstants.RSA_OAEP;
    /** RSA-OAEP（SHA-256）密钥加密算法。 */
    public static final String RSA_OAEP_256 = CryptoConstants.RSA_OAEP_256;
    /** ECDH-ES 直接密钥协商算法。 */
    public static final String ECDH_ES = CryptoConstants.ECDH_ES;
    /** ECDH-ES + AES-128 密钥包装。 */
    public static final String ECDH_ES_A128KW = CryptoConstants.ECDH_ES_A128KW;
    /** ECDH-ES + AES-192 密钥包装。 */
    public static final String ECDH_ES_A192KW = CryptoConstants.ECDH_ES_A192KW;
    /** ECDH-ES + AES-256 密钥包装。 */
    public static final String ECDH_ES_A256KW = CryptoConstants.ECDH_ES_A256KW;

    /** AES-128-CBC + HMAC-SHA-256 内容加密算法。 */
    public static final String A128CBC_HS256 = "A128CBC-HS256";
    /** AES-192-CBC + HMAC-SHA-384 内容加密算法。 */
    public static final String A192CBC_HS384 = "A192CBC-HS384";
    /** AES-256-CBC + HMAC-SHA-512 内容加密算法。 */
    public static final String A256CBC_HS512 = "A256CBC-HS512";
    /** AES-128-GCM 内容加密算法。 */
    public static final String A128GCM = "A128GCM";
    /** AES-192-GCM 内容加密算法。 */
    public static final String A192GCM = "A192GCM";
    /** AES-256-GCM 内容加密算法。 */
    public static final String A256GCM = "A256GCM";

}
