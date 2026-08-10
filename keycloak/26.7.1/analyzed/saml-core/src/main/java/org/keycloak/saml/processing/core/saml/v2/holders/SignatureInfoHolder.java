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
package org.keycloak.saml.processing.core.saml.v2.holders;

/**
 * 保存 SAML 数字签名值与算法信息的容器。
 * <p>用于 HTTP-Redirect 绑定等需要单独传递签名参数的场景。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 24, 2009
 */
public class SignatureInfoHolder {

    /** 签名二进制值。 */
    private byte[] signatureValue;

    /** 签名算法 URI 或标识。 */
    private String sigAlg;

    /**
     * 构造签名信息容器。
     *
     * @param signatureValue 签名值字节数组
     * @param sigAlg 签名算法标识
     */
    public SignatureInfoHolder(byte[] signatureValue, String sigAlg) {
        super();
        this.signatureValue = signatureValue;
        this.sigAlg = sigAlg;
    }

    /** 返回签名值。 */
    public byte[] getSignatureValue() {
        return signatureValue;
    }

    /** 返回签名算法标识。 */
    public String getSigAlg() {
        return sigAlg;
    }
}