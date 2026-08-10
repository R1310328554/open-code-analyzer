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

package org.keycloak.dom.saml.v2.ac.classes;

/**
 * <p>
 * Java class for mediumType.
 * SAML 2.0 存储介质枚举（mediumType）：memory、smartcard、token、MobileDevice、MobileAuthCard。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 *
 * <pre>
 * &lt;simpleType name="mediumType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="memory"/>
 *     &lt;enumeration value="smartcard"/>
 *     &lt;enumeration value="token"/>
 *     &lt;enumeration value="MobileDevice"/>
 *     &lt;enumeration value="MobileAuthCard"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
public enum MediumType {

    MEMORY("memory"), SMARTCARD("smartcard"), TOKEN("token"), MOBILE_DEVICE("MobileDevice"), MOBILE_AUTH_CARD("MobileAuthCard");
    private final String value;

    MediumType(String v) {
        value = v;
    }

    /** 返回枚举对应的 XML 字符串值。 */
    public String value() {
        return value;
    }

    /** 从 XML 字符串解析为 {@link MediumType} 枚举常量。 */
    public static MediumType fromValue(String v) {
        for (MediumType c : MediumType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
