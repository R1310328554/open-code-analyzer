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

import java.math.BigInteger;

/**
 * <p>
 * Java class for X509IssuerSerialType complex type.
 * W3C XML Signature X509IssuerSerial 元素，以颁发者名称与序列号标识 X.509 证书。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="X509IssuerSerialType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="X509IssuerName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="X509SerialNumber" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class X509IssuerSerialType {

    /** X509 证书颁发者名称（X509IssuerName 子元素）。 */
    protected String x509IssuerName;
    /** X509 证书序列号（X509SerialNumber 子元素）。 */
    protected BigInteger x509SerialNumber;

    /**
     * 获取证书颁发者名称。
     *
     * Gets the value of the x509IssuerName property.
     *
     * @return possible object is {@link String }
     */
    public String getX509IssuerName() {
        return x509IssuerName;
    }

    /**
     * 设置证书颁发者名称。
     *
     * Sets the value of the x509IssuerName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setX509IssuerName(String value) {
        this.x509IssuerName = value;
    }

    /**
     * 获取证书序列号。
     *
     * Gets the value of the x509SerialNumber property.
     *
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getX509SerialNumber() {
        return x509SerialNumber;
    }

    /**
     * 设置证书序列号。
     *
     * Sets the value of the x509SerialNumber property.
     *
     * @param value allowed object is {@link BigInteger }
     */
    public void setX509SerialNumber(BigInteger value) {
        this.x509SerialNumber = value;
    }
}
