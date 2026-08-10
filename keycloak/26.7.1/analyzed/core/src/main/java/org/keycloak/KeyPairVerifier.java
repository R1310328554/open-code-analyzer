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

package org.keycloak;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.keycloak.common.VerificationException;
import org.keycloak.common.util.PemUtils;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.crypto.RSAProvider;

/**
 * 通过 RSA 签名与验签验证 PEM 格式公私钥是否匹配的实用工具。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeyPairVerifier {

    /**
     * 解码 PEM 公私钥，用私钥签发 JWS 并用公钥验签，以确认密钥对一致。
     *
     * @param privateKeyPem PEM 编码的 RSA 私钥
     * @param publicKeyPem PEM 编码的 RSA 公钥
     * @throws VerificationException 解码失败或密钥不匹配时抛出
     */
    public static void verify(String privateKeyPem, String publicKeyPem) throws VerificationException {
        PrivateKey privateKey;
        try {
            privateKey = PemUtils.decodePrivateKey(privateKeyPem);
        } catch (Exception e) {
            throw new VerificationException("Failed to decode private key", e);
        }

        PublicKey publicKey;
        try {
            publicKey = PemUtils.decodePublicKey(publicKeyPem);
        } catch (Exception e) {
            throw new VerificationException("Failed to decode public key", e);
        }

        try {
            String jws = new JWSBuilder().content("content".getBytes()).rsa256(privateKey);
            if (!RSAProvider.verify(new JWSInput(jws), publicKey)) {
                throw new VerificationException("Keys don't match");
            }
        } catch (Exception e) {
            throw new VerificationException("Keys don't match");
        }
    }

}
