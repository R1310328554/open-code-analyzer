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
package org.keycloak.dom.saml.v2.protocol;

import javax.xml.datatype.XMLGregorianCalendar;

import org.keycloak.dom.saml.v2.assertion.EncryptedElementType;
import org.keycloak.dom.saml.v2.assertion.NameIDType;

/**
 * <p>
 * Java class for NameIDMappingResponseType complex type.
 * SAML 2.0 NameID 映射响应，返回映射后的主体标识（明文或加密形式）。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="NameIDMappingResponseType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}StatusResponseType">
 *       &lt;choice>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}NameID"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}EncryptedID"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class NameIDMappingResponseType extends StatusResponseType {

    /** 映射后的 NameID 标识。 */
    protected NameIDType nameID;

    /** 映射后的加密 NameID 标识。 */
    protected EncryptedElementType encryptedID;

    /**
     * 构造 NameID 映射响应。
     *
     * @param id 响应标识符
     * @param issueInstant 签发时间
     */
    public NameIDMappingResponseType(String id, XMLGregorianCalendar issueInstant) {
        super(id, issueInstant);
    }

    /**
     * 获取 NameID 属性的值。
     *
     * Gets the value of the nameID property.
     *
     * @return possible object is {@link NameIDType }
     */
    public NameIDType getNameID() {
        return nameID;
    }

    /**
     * 设置 NameID 属性的值。
     *
     * Sets the value of the nameID property.
     *
     * @param value allowed object is {@link NameIDType }
     */
    public void setNameID(NameIDType value) {
        this.nameID = value;
    }

    /**
     * 获取 EncryptedID 属性的值。
     *
     * Gets the value of the encryptedID property.
     *
     * @return possible object is {@link EncryptedElementType }
     */
    public EncryptedElementType getEncryptedID() {
        return encryptedID;
    }

    /**
     * 设置 EncryptedID 属性的值。
     *
     * Sets the value of the encryptedID property.
     *
     * @param value allowed object is {@link EncryptedElementType }
     */
    public void setEncryptedID(EncryptedElementType value) {
        this.encryptedID = value;
    }
}
