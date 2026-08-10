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
 * Java class for ManageNameIDRequestType complex type.
 * SAML 2.0 NameID 管理请求，用于更新、加密或终止主体的持久标识符。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ManageNameIDRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}RequestAbstractType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}NameID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}EncryptedID"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}NewID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}NewEncryptedID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}Terminate"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class ManageNameIDRequestType extends RequestAbstractType {

    /** 当前主体的 NameID 标识。 */
    protected NameIDType nameID;

    /** 加密的主体标识。 */
    protected EncryptedElementType encryptedID;

    /** 新的 NameID 值（明文）。 */
    protected String newID;

    /** 新的加密 NameID。 */
    protected EncryptedElementType newEncryptedID;

    /** 终止标识符操作标记。 */
    protected TerminateType terminate;

    /**
     * 构造 NameID 管理请求。
     *
     * @param id 请求标识符
     * @param instant 签发时间
     */
    public ManageNameIDRequestType(String id, XMLGregorianCalendar instant) {
        super(id, instant);
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

    /**
     * 获取 NewID 新标识符属性的值。
     *
     * Gets the value of the newID property.
     *
     * @return possible object is {@link String }
     */
    public String getNewID() {
        return newID;
    }

    /**
     * 设置 NewID 新标识符属性的值。
     *
     * Sets the value of the newID property.
     *
     * @param value allowed object is {@link String }
     */
    public void setNewID(String value) {
        this.newID = value;
    }

    /**
     * 获取 NewEncryptedID 新加密标识属性的值。
     *
     * Gets the value of the newEncryptedID property.
     *
     * @return possible object is {@link EncryptedElementType }
     */
    public EncryptedElementType getNewEncryptedID() {
        return newEncryptedID;
    }

    /**
     * 设置 NewEncryptedID 新加密标识属性的值。
     *
     * Sets the value of the newEncryptedID property.
     *
     * @param value allowed object is {@link EncryptedElementType }
     */
    public void setNewEncryptedID(EncryptedElementType value) {
        this.newEncryptedID = value;
    }

    /**
     * 获取 Terminate 终止操作属性的值。
     *
     * Gets the value of the terminate property.
     *
     * @return possible object is {@link TerminateType }
     */
    public TerminateType getTerminate() {
        return terminate;
    }

    /**
     * 设置 Terminate 终止操作属性的值。
     *
     * Sets the value of the terminate property.
     *
     * @param value allowed object is {@link TerminateType }
     */
    public void setTerminate(TerminateType value) {
        this.terminate = value;
    }

}
