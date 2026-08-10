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
import com.webauthn4j.data.attestation.statement.AttestationStatement;

/**
 * WebAuthn 认证声明（AttestationStatement）的 JPA 属性转换器。
 * <p>序列化为 CBOR 容器后 Base64Url 编码存入数据库。</p>
 */
public class AttestationStatementConverter {

    /** CBOR 编解码器，用于 attestation 结构化数据。 */
    private CborConverter cborConverter;

    /** @param objectConverter WebAuthn4J 对象转换器工厂 */
    public AttestationStatementConverter(ObjectConverter objectConverter) {
        this.cborConverter = objectConverter.getCborConverter();
    }

    /** 将 attestation 包装为容器后 CBOR+Base64Url 编码。 */
    public String convertToDatabaseColumn(AttestationStatement attribute) {
        AttestationStatementSerializationContainer container = new AttestationStatementSerializationContainer(attribute);
        return Base64Url.encode(cborConverter.writeValueAsBytes(container));
    }

    /** 从 Base64Url CBOR 容器还原 {@link AttestationStatement}。 */
    public AttestationStatement convertToEntityAttribute(String dbData) {
        byte[] data = Base64Url.decode(dbData);
        AttestationStatementSerializationContainer container = cborConverter.readValue(data, AttestationStatementSerializationContainer.class);
        return container.getAttestationStatement();
    }
}
