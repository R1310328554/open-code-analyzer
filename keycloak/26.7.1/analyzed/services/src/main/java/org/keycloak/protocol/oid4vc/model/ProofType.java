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

package org.keycloak.protocol.oid4vc.model;


/**
 * 持有者绑定（holder-binding）proof 类型常量。
 * <p>与凭证颁发者元数据及 {@link ProofValidator} 实现中的类型键一致。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public final class ProofType {

    /** JWT 密钥持有证明类型键 {@code jwt}。 */
    public static final String JWT = "jwt";
    /** Data Integrity VP 证明类型键 {@code di_vp}。 */
    public static final String DI_PROOF = "di_vp";
    /** 密钥证明（attestation）类型键 {@code attestation}。 */
    public static final String ATTESTATION = "attestation";

}
