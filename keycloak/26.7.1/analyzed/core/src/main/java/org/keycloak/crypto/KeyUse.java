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
package org.keycloak.crypto;

/**
 * JWK {@code use}（密钥用途）枚举，映射 JOSE 规范中的 {@code sig}、{@code enc} 等取值。
 */
public enum KeyUse {

    /** 签名用途（对应规范值 {@code sig}）。 */
    SIG("sig"),
    /** 加密用途（对应规范值 {@code enc}）。 */
    ENC("enc"),
    /** SPIFFE JWT-SVID 用途。 */
    JWT_SVID("jwt-svid");

    /** JOSE/JWK 规范中的字符串表示。 */
    private String specName;

    KeyUse(String specName) {
        this.specName = specName;
    }

    /**
     * @return JOSE 规范中的 {@code use} 字符串
     */
    public String getSpecName() {
        return specName;
    }

}
