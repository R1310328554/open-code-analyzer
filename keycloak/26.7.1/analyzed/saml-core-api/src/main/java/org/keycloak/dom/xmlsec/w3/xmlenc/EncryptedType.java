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

import org.keycloak.dom.xmlsec.w3.xmldsig.KeyInfoType;

/**
 * <p>
 * Java class for EncryptedType complex type.
 * XML 加密 EncryptedType 抽象基类，描述加密元素共有的算法、密钥信息、密文数据与属性。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="EncryptedType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="EncryptionMethod" type="{http://www.w3.org/2001/04/xmlenc#}EncryptionMethodType"
 * minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}KeyInfo" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2001/04/xmlenc#}CipherData"/>
 *         &lt;element ref="{http://www.w3.org/2001/04/xmlenc#}EncryptionProperties" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="MimeType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Encoding" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public abstract class EncryptedType {

    /** 加密算法及参数。 */
    protected EncryptionMethodType encryptionMethod;
    /** 用于解密的对称密钥或密钥引用信息。 */
    protected KeyInfoType keyInfo;
    /** 密文数据容器。 */
    protected CipherDataType cipherData;
    /** 加密过程相关的扩展属性集合。 */
    protected EncryptionPropertiesType encryptionProperties;
    /** 元素唯一标识。 */
    protected String id;
    /** 被加密数据的类型 URI。 */
    protected String type;
    /** 明文 MIME 类型。 */
    protected String mimeType;
    /** 明文编码方式 URI。 */
    protected String encoding;

    /**
     * 获取 encryptionMethod 属性值。
     *
     * Gets the value of the encryptionMethod property.
     *
     * @return possible object is {@link EncryptionMethodType }
     */
    public EncryptionMethodType getEncryptionMethod() {
        return encryptionMethod;
    }

    /**
     * 设置 encryptionMethod 属性值。
     *
     * Sets the value of the encryptionMethod property.
     *
     * @param value allowed object is {@link EncryptionMethodType }
     */
    public void setEncryptionMethod(EncryptionMethodType value) {
        this.encryptionMethod = value;
    }

    /**
     * 获取 keyInfo 属性值。
     *
     * Gets the value of the keyInfo property.
     *
     * @return possible object is {@link KeyInfoType }
     */
    public KeyInfoType getKeyInfo() {
        return keyInfo;
    }

    /**
     * 设置 keyInfo 属性值。
     *
     * Sets the value of the keyInfo property.
     *
     * @param value allowed object is {@link KeyInfoType }
     */
    public void setKeyInfo(KeyInfoType value) {
        this.keyInfo = value;
    }

    /**
     * 获取 cipherData 属性值。
     *
     * Gets the value of the cipherData property.
     *
     * @return possible object is {@link CipherDataType }
     */
    public CipherDataType getCipherData() {
        return cipherData;
    }

    /**
     * 设置 cipherData 属性值。
     *
     * Sets the value of the cipherData property.
     *
     * @param value allowed object is {@link CipherDataType }
     */
    public void setCipherData(CipherDataType value) {
        this.cipherData = value;
    }

    /**
     * 获取 encryptionProperties 属性值。
     *
     * Gets the value of the encryptionProperties property.
     *
     * @return possible object is {@link EncryptionPropertiesType }
     */
    public EncryptionPropertiesType getEncryptionProperties() {
        return encryptionProperties;
    }

    /**
     * 设置 encryptionProperties 属性值。
     *
     * Sets the value of the encryptionProperties property.
     *
     * @param value allowed object is {@link EncryptionPropertiesType }
     */
    public void setEncryptionProperties(EncryptionPropertiesType value) {
        this.encryptionProperties = value;
    }

    /**
     * 获取 id 属性值。
     *
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * 设置 id 属性值。
     *
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * 获取 type 属性值。
     *
     * Gets the value of the type property.
     *
     * @return possible object is {@link String }
     */
    public String getType() {
        return type;
    }

    /**
     * 设置 type 属性值。
     *
     * Sets the value of the type property.
     *
     * @param value allowed object is {@link String }
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * 获取 mimeType 属性值。
     *
     * Gets the value of the mimeType property.
     *
     * @return possible object is {@link String }
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * 设置 mimeType 属性值。
     *
     * Sets the value of the mimeType property.
     *
     * @param value allowed object is {@link String }
     */
    public void setMimeType(String value) {
        this.mimeType = value;
    }

    /**
     * 获取 encoding 属性值。
     *
     * Gets the value of the encoding property.
     *
     * @return possible object is {@link String }
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * 设置 encoding 属性值。
     *
     * Sets the value of the encoding property.
     *
     * @param value allowed object is {@link String }
     */
    public void setEncoding(String value) {
        this.encoding = value;
    }

}
