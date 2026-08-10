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

package org.keycloak.jose.jws.crypto;

import org.keycloak.jose.jws.JWSInput;

/**
 * JWS 验签 SPI：按算法族（RSA、ECDSA、HMAC 等）验证 Compact JWS 签名。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface SignatureProvider {
    /**
     * 验证 JWS 签名。
     *
     * @param input 解析后的 JWS 输入
     * @param key 验签材料（如 PEM 证书或密钥字符串，具体语义由实现决定）
     * @return 验签是否通过
     */
    boolean verify(JWSInput input, String key);
}
