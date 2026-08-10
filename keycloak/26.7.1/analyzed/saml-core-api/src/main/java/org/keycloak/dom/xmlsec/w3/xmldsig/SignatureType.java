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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for SignatureType complex type.
 * W3C XML Signature 根元素，封装待签名信息、签名值及可选的密钥信息与对象。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SignatureType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}SignedInfo"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}SignatureValue"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}KeyInfo" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Object" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class SignatureType {

    /** 待签名信息（SignedInfo 子元素）。 */
    protected SignedInfoType signedInfo;
    /** 签名值（SignatureValue 子元素）。 */
    protected SignatureValueType signatureValue;
    /** 密钥信息（可选 KeyInfo 子元素）。 */
    protected KeyInfoType keyInfo;
    /** 附加对象列表（可选 Object 子元素）。 */
    protected List<ObjectType> object = new ArrayList<>();
    /** 元素标识符（可选属性 Id）。 */
    protected String id;

    /**
     * 获取待签名信息。
     *
     * Gets the value of the signedInfo property.
     *
     * @return possible object is {@link SignedInfoType }
     */
    public SignedInfoType getSignedInfo() {
        return signedInfo;
    }

    /**
     * 设置待签名信息。
     *
     * Sets the value of the signedInfo property.
     *
     * @param value allowed object is {@link SignedInfoType }
     */
    public void setSignedInfo(SignedInfoType value) {
        this.signedInfo = value;
    }

    /**
     * 获取签名值。
     *
     * Gets the value of the signatureValue property.
     *
     * @return possible object is {@link SignatureValueType }
     */
    public SignatureValueType getSignatureValue() {
        return signatureValue;
    }

    /**
     * 设置签名值。
     *
     * Sets the value of the signatureValue property.
     *
     * @param value allowed object is {@link SignatureValueType }
     */
    public void setSignatureValue(SignatureValueType value) {
        this.signatureValue = value;
    }

    /**
     * 获取密钥信息。
     *
     * Gets the value of the keyInfo property.
     *
     * @return possible object is {@link KeyInfoType }
     */
    public KeyInfoType getKeyInfo() {
        return keyInfo;
    }

    /**
     * 设置密钥信息。
     *
     * Sets the value of the keyInfo property.
     *
     * @param value allowed object is {@link KeyInfoType }
     */
    public void setKeyInfo(KeyInfoType value) {
        this.keyInfo = value;
    }

    /** 添加附加对象。 */
    public void addObject(ObjectType obj) {
        this.object.add(obj);
    }

    /** 移除附加对象。 */
    public void removeObject(ObjectType obj) {
        this.object.remove(obj);
    }

    /**
     * 获取附加对象列表（只读视图）。
     *
     * Gets the value of the object property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link ObjectType }
     */
    public List<ObjectType> getObject() {
        return Collections.unmodifiableList(this.object);
    }

    /**
     * 获取元素标识符。
     *
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * 设置元素标识符。
     *
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }
}
