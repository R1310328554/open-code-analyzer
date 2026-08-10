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
package org.keycloak.dom.saml.v2.metadata;

/**
 * <p>
 * Java class for KeyTypes.
 * SAML 2.0 密钥用途类型：encryption（加密）或 signing（签名）。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 *
 * <pre>
 * &lt;simpleType name="KeyTypes">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="encryption"/>
 *     &lt;enumeration value="signing"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
public enum KeyTypes {
    ENCRYPTION("encryption"), SIGNING("signing");
    private final String value;

    KeyTypes(String v) {
        value = v;
    }

    /** 返回枚举对应的 XML 字符串值。 */
    public String value() {
        return value;
    }

    /** 从 XML 字符串解析为 {@link KeyTypes} 枚举常量。 */
    public static KeyTypes fromValue(String v) {
        for (KeyTypes c : KeyTypes.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}