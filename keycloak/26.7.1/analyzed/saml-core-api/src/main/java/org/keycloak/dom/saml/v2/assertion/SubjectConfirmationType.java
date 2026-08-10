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

package org.keycloak.dom.saml.v2.assertion;

import java.io.Serializable;

/**
 * <p>
 * Java class for SubjectConfirmationType complex type.
 * SAML 2.0 主体确认：通过 Method URI 描述确认方式，可携带 BaseID/NameID/EncryptedID 及确认数据。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SubjectConfirmationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}BaseID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}NameID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}EncryptedID"/>
 *         &lt;/choice>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}SubjectConfirmationData" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Method" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class SubjectConfirmationType implements Serializable {

    protected BaseIDAbstractType baseID;
    protected NameIDType nameID;
    protected EncryptedElementType encryptedID;
    protected SubjectConfirmationDataType subjectConfirmationData;
    protected String method;

    /**
     * 获取 基础标识符（BaseID） 属性的值。
     *
     * Gets the value of the baseID property.
     *
     * @return possible object is {@link BaseIDAbstractType }
     */
    public BaseIDAbstractType getBaseID() {
        return baseID;
    }

    /**
     * 设置 基础标识符（BaseID） 属性的值。
     *
     * Sets the value of the baseID property.
     *
     * @param value allowed object is {@link BaseIDAbstractType }
     */
    public void setBaseID(BaseIDAbstractType value) {
        this.baseID = value;
    }

    /**
     * 获取 名称标识符（NameID） 属性的值。
     *
     * Gets the value of the nameID property.
     *
     * @return possible object is {@link NameIDType }
     */
    public NameIDType getNameID() {
        return nameID;
    }

    /**
     * 设置 名称标识符（NameID） 属性的值。
     *
     * Sets the value of the nameID property.
     *
     * @param value allowed object is {@link NameIDType }
     */
    public void setNameID(NameIDType value) {
        this.nameID = value;
    }

    /**
     * 获取 加密标识符（EncryptedID） 属性的值。
     *
     * Gets the value of the encryptedID property.
     *
     * @return possible object is {@link EncryptedElementType }
     */
    public EncryptedElementType getEncryptedID() {
        return encryptedID;
    }

    /**
     * 设置 加密标识符（EncryptedID） 属性的值。
     *
     * Sets the value of the encryptedID property.
     *
     * @param value allowed object is {@link EncryptedElementType }
     */
    public void setEncryptedID(EncryptedElementType value) {
        this.encryptedID = value;
    }

    /**
     * 获取 主体确认数据 属性的值。
     *
     * Gets the value of the subjectConfirmationData property.
     *
     * @return possible object is {@link SubjectConfirmationDataType }
     */
    public SubjectConfirmationDataType getSubjectConfirmationData() {
        return subjectConfirmationData;
    }

    /**
     * 设置 主体确认数据 属性的值。
     *
     * Sets the value of the subjectConfirmationData property.
     *
     * @param value allowed object is {@link SubjectConfirmationDataType }
     */
    public void setSubjectConfirmationData(SubjectConfirmationDataType value) {
        this.subjectConfirmationData = value;
    }

    /**
     * 获取 确认方法 URI（Method） 属性的值。
     *
     * Gets the value of the method property.
     *
     * @return possible object is {@link String }
     */
    public String getMethod() {
        return method;
    }

    /**
     * 设置 确认方法 URI（Method） 属性的值。
     *
     * Sets the value of the method property.
     *
     * @param value allowed object is {@link String }
     */
    public void setMethod(String value) {
        this.method = value;
    }
}