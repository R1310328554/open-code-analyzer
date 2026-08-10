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
 * Java class for EncryptedKeyType complex type.
 * XML 加密 EncryptedKey 元素类型，表示经加密的对称密钥，可携带引用列表、密钥名称与接收方标识。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="EncryptedKeyType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.w3.org/2001/04/xmlenc#}EncryptedType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2001/04/xmlenc#}ReferenceList" minOccurs="0"/>
 *         &lt;element name="CarriedKeyName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Recipient" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class EncryptedKeyType extends EncryptedType {

    /** 指向被该密钥加密的数据或密钥的引用列表。 */
    protected ReferenceList referenceList;
    /** 携带的对称密钥名称，便于在密钥仓库中查找。 */
    protected String carriedKeyName;
    /** 密钥接收方标识，用于多接收方场景。 */
    protected String recipient;

    /**
     * 获取 referenceList 属性值。
     *
     * Gets the value of the referenceList property.
     *
     * @return possible object is {@link ReferenceList }
     */
    public ReferenceList getReferenceList() {
        return referenceList;
    }

    /**
     * 设置 referenceList 属性值。
     *
     * Sets the value of the referenceList property.
     *
     * @param value allowed object is {@link ReferenceList }
     */
    public void setReferenceList(ReferenceList value) {
        this.referenceList = value;
    }

    /**
     * 获取 carriedKeyName 属性值。
     *
     * Gets the value of the carriedKeyName property.
     *
     * @return possible object is {@link String }
     */
    public String getCarriedKeyName() {
        return carriedKeyName;
    }

    /**
     * 设置 carriedKeyName 属性值。
     *
     * Sets the value of the carriedKeyName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setCarriedKeyName(String value) {
        this.carriedKeyName = value;
    }

    /**
     * 获取 recipient 属性值。
     *
     * Gets the value of the recipient property.
     *
     * @return possible object is {@link String }
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * 设置 recipient 属性值。
     *
     * Sets the value of the recipient property.
     *
     * @param value allowed object is {@link String }
     */
    public void setRecipient(String value) {
        this.recipient = value;
    }

}
