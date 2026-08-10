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
 * Java class for SignedInfoType complex type.
 * W3C XML Signature 待签名信息，包含规范化方法、签名方法及一个或多个引用（Reference）。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SignedInfoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}CanonicalizationMethod"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}SignatureMethod"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Reference" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class SignedInfoType {

    /** 规范化方法（CanonicalizationMethod 子元素）。 */
    protected CanonicalizationMethodType canonicalizationMethod;
    /** 签名方法（SignatureMethod 子元素）。 */
    protected SignatureMethodType signatureMethod;
    /** 引用列表（Reference 子元素，至少一条）。 */
    protected List<ReferenceType> reference = new ArrayList<>();
    /** 元素标识符（可选属性 Id）。 */
    protected String id;

    /**
     * 获取规范化方法。
     *
     * Gets the value of the canonicalizationMethod property.
     *
     * @return possible object is {@link CanonicalizationMethodType }
     */
    public CanonicalizationMethodType getCanonicalizationMethod() {
        return canonicalizationMethod;
    }

    /**
     * 设置规范化方法。
     *
     * Sets the value of the canonicalizationMethod property.
     *
     * @param value allowed object is {@link CanonicalizationMethodType }
     */
    public void setCanonicalizationMethod(CanonicalizationMethodType value) {
        this.canonicalizationMethod = value;
    }

    /**
     * 获取签名方法。
     *
     * Gets the value of the signatureMethod property.
     *
     * @return possible object is {@link SignatureMethodType }
     */
    public SignatureMethodType getSignatureMethod() {
        return signatureMethod;
    }

    /**
     * 设置签名方法。
     *
     * Sets the value of the signatureMethod property.
     *
     * @param value allowed object is {@link SignatureMethodType }
     */
    public void setSignatureMethod(SignatureMethodType value) {
        this.signatureMethod = value;
    }

    /** 添加一条引用。 */
    public void add(ReferenceType ref) {
        this.reference.add(ref);
    }

    /** 移除一条引用。 */
    public void remove(ReferenceType ref) {
        this.reference.remove(ref);
    }

    /**
     * 获取引用列表（只读视图）。
     *
     * Gets the value of the reference property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link ReferenceType }
     */
    public List<ReferenceType> getReference() {
        return Collections.unmodifiableList(this.reference);
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
