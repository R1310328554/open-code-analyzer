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
package org.keycloak.dom.xmlsec.w3.xmldsig;

/**
 * W3C XML Signature X509Certificate 元素对应的 Java 类型，承载 DER 编码的 X.509 证书字节。
 */
public class X509CertificateType {

    /** DER 编码的 X.509 证书字节。 */
    private byte[] encodedCertificate;

    /**
     * 获取 DER 编码的证书字节。
     *
     * @return 证书字节数组
     */
    public byte[] getEncodedCertificate() {
        return this.encodedCertificate;
    }

    /**
     * 设置 DER 编码的证书字节。
     *
     * @param encodedCertificate 证书字节数组
     */
    public void setEncodedCertificate(byte[] encodedCertificate) {
        this.encodedCertificate = encodedCertificate;
    }
}
