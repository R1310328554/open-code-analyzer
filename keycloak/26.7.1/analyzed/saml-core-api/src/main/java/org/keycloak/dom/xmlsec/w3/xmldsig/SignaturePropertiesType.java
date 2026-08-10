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
 * Java class for SignaturePropertiesType complex type.
 * W3C XML Signature 签名属性集合容器，可包含一个或多个 {@link SignaturePropertyType}。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SignaturePropertiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}SignatureProperty" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class SignaturePropertiesType {

    /** 签名属性列表。 */
    protected List<SignaturePropertyType> signatureProperty = new ArrayList<>();
    /** 元素标识符（可选属性 Id）。 */
    protected String id;

    /** 添加一条签名属性。 */
    public void addSignatureProperty(SignaturePropertyType sig) {
        this.signatureProperty.add(sig);
    }

    /** 移除一条签名属性。 */
    public void removeSignatureProperty(SignaturePropertyType sig) {
        this.signatureProperty.remove(sig);
    }

    /**
     * 获取签名属性列表（只读视图）。
     *
     * Gets the value of the signatureProperty property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link SignaturePropertyType }
     */
    public List<SignaturePropertyType> getSignatureProperty() {
        return Collections.unmodifiableList(this.signatureProperty);
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
