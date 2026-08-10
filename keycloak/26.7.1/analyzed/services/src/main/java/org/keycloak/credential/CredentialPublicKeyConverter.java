/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.credential;

import org.keycloak.common.util.Base64Url;

import com.webauthn4j.converter.util.CborConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.attestation.authenticator.COSEKey;

/**
 * WebAuthn 凭证公钥（{@link COSEKey}）与数据库字符串的 JPA 转换器。
 * <p>使用 CBOR 编码后以 Base64Url 存储。</p>
 */
public class CredentialPublicKeyConverter {

    private CborConverter cborConverter;

    /** @param objectConverter 提供 CBOR 转换能力 */
    public CredentialPublicKeyConverter(ObjectConverter objectConverter) {
        this.cborConverter = objectConverter.getCborConverter();
    }

    /** @param credentialPublicKey COSE 公钥 @return Base64Url(CBOR) 字符串 */
    public String convertToDatabaseColumn(COSEKey credentialPublicKey) {
        return Base64Url.encode(cborConverter.writeValueAsBytes(credentialPublicKey));
    }

    /** @param s 数据库存储值 @return 解码后的 {@link COSEKey} */
    public COSEKey convertToEntityAttribute(String s) {
        return cborConverter.readValue(Base64Url.decode(s), COSEKey.class);
    }
}
