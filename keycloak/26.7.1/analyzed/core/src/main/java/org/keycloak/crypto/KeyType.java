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
 * JWK {@code kty}（密钥类型）字段的取值常量，对应 RFC 7517 定义的类型标识。
 */
public interface KeyType {

    /** 椭圆曲线密钥（EC）。 */
    String EC = "EC";
    /** RSA 密钥。 */
    String RSA = "RSA";
    /** 对称/octet 序列密钥（OCT）。 */
    String OCT = "OCT";
    /** Octet Key Pair（OKP，如 Ed25519）。 */
    String OKP = "OKP";
    /** Asymmetric Key Pair（后量子等非传统类型）。 */
    String AKP = "AKP";

}
