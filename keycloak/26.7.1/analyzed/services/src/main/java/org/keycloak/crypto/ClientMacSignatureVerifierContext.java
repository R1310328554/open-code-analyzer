/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.crypto;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.keycloak.common.VerificationException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;

/**
 * 客户端 HMAC JWS 签名验证上下文。
 * <p>从 Vault 或客户端配置读取共享密钥，构建对称 {@link KeyWrapper} 用于 HS256/384/512 验签。</p>
 */
public class ClientMacSignatureVerifierContext extends MacSignatureVerifierContext {

    /** @param session 当前会话 @param client 客户端 @param algorithm HMAC 算法，null 时默认 HS256 */
    public ClientMacSignatureVerifierContext(KeycloakSession session, ClientModel client, String algorithm) throws VerificationException {
        super(getKey(session, client, algorithm));
    }

    private static KeyWrapper getKey(KeycloakSession session, ClientModel client, String algorithm) throws VerificationException {
        if (algorithm == null) algorithm = Algorithm.HS256;
        String secretRef = client.getSecret();
        String clientSecretString = session.vault().getStringSecret(secretRef).get().orElse(secretRef);
        SecretKey clientSecret = new SecretKeySpec(clientSecretString.getBytes(StandardCharsets.UTF_8), JavaAlgorithm.getJavaAlgorithm(algorithm));
        KeyWrapper key = new KeyWrapper();
        key.setSecretKey(clientSecret);
        key.setUse(KeyUse.SIG);
        key.setType(KeyType.OCT);
        key.setAlgorithm(algorithm);
        return key;
    }

}
