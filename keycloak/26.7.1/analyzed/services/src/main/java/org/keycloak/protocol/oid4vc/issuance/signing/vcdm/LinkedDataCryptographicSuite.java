/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oid4vc.issuance.signing.vcdm;

import org.keycloak.protocol.oid4vc.model.VerifiableCredential;

/**
 * Linked Data 密码学签名套件（LD-Signature Suite）实现接口。
 * <p>各实现按 {@see https://w3c-ccg.github.io/ld-cryptosuite-registry/} 定义算法，为 {@link VerifiableCredential} 生成符合 VCDM 的 Linked Data 证明签名。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public interface LinkedDataCryptographicSuite {

    /**
     * 按本套件规则为给定可验证凭证生成签名。
     *
     * @param verifiableCredential 待签名的可验证凭证
     * @return 签名字节数组
     */
    byte[] getSignature(VerifiableCredential verifiableCredential);

    /**
     * 本套件定义的 proof 类型标识。
     *
     * @return proof 类型字符串
     */
    String getProofType();

}