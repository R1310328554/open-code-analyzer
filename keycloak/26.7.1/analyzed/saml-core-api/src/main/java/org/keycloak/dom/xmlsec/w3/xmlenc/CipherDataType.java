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
package org.keycloak.dom.xmlsec.w3.xmlenc;

/**
 * <p>
 * Java class for CipherDataType complex type.
 * W3C XML Encryption 密文数据元素，以 CipherValue 或 CipherReference 二选一承载密文。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CipherDataType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="CipherValue" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element ref="{http://www.w3.org/2001/04/xmlenc#}CipherReference"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class CipherDataType {

    /** 内联密文值（CipherValue 子元素，Base64 解码后的字节）。 */
    protected byte[] cipherValue;
    /** 外部密文引用（CipherReference 子元素）。 */
    protected CipherReferenceType cipherReference;

    /**
     * 获取内联密文值字节数组。
     *
     * Gets the value of the cipherValue property.
     *
     * @return possible object is byte[]
     */
    public byte[] getCipherValue() {
        return cipherValue;
    }

    /**
     * 设置内联密文值字节数组。
     *
     * Sets the value of the cipherValue property.
     *
     * @param value allowed object is byte[]
     */
    public void setCipherValue(byte[] value) {
        this.cipherValue = ((byte[]) value);
    }

    /**
     * 获取外部密文引用。
     *
     * Gets the value of the cipherReference property.
     *
     * @return possible object is {@link CipherReferenceType }
     */
    public CipherReferenceType getCipherReference() {
        return cipherReference;
    }

    /**
     * 设置外部密文引用。
     *
     * Sets the value of the cipherReference property.
     *
     * @param value allowed object is {@link CipherReferenceType }
     */
    public void setCipherReference(CipherReferenceType value) {
        this.cipherReference = value;
    }
}
