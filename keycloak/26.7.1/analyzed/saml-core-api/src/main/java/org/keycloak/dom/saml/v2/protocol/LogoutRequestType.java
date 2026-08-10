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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

import org.keycloak.dom.saml.v2.assertion.BaseIDAbstractType;
import org.keycloak.dom.saml.v2.assertion.EncryptedElementType;
import org.keycloak.dom.saml.v2.assertion.NameIDType;

/**
 * <p>
 * Java class for LogoutRequestType complex type.
 * SAML 2.0 登出请求，用于终止指定主体的一个或多个 SSO 会话。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="LogoutRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}RequestAbstractType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}BaseID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}NameID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}EncryptedID"/>
 *         &lt;/choice>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}SessionIndex" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Reason" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="NotOnOrAfter" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class LogoutRequestType extends RequestAbstractType {

    /** 主体的 BaseID 标识（与 NameID、EncryptedID 三选一）。 */
    protected BaseIDAbstractType baseID;

    /** 主体的 NameID 标识。 */
    protected NameIDType nameID;

    /** 加密的主体标识。 */
    protected EncryptedElementType encryptedID;

    /** 待终止的会话索引列表。 */
    protected List<String> sessionIndex = new ArrayList<>();

    /** 登出原因说明。 */
    protected String reason;

    /** 登出请求的有效截止时间（NotOnOrAfter）。 */
    protected XMLGregorianCalendar notOnOrAfter;

    /**
     * 构造登出请求。
     *
     * @param id 请求标识符
     * @param instant 签发时间
     */
    public LogoutRequestType(String id, XMLGregorianCalendar instant) {
        super(id, instant);
    }

    /**
     * 获取 BaseID 属性的值。
     *
     * Gets the value of the baseID property.
     *
     * @return possible object is {@link BaseIDAbstractType }
     */
    public BaseIDAbstractType getBaseID() {
        return baseID;
    }

    /**
     * 设置 BaseID 属性的值。
     *
     * Sets the value of the baseID property.
     *
     * @param value allowed object is {@link BaseIDAbstractType }
     */
    public void setBaseID(BaseIDAbstractType value) {
        this.baseID = value;
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
     * 添加会话索引。
     *
     * Add session index
     *
     * @param index
     */
    public void addSessionIndex(String index) {
        this.sessionIndex.add(index);
    }

    /**
     * 移除会话索引。
     *
     * Remove session index
     *
     * @param index
     */
    public void removeSessionIndex(String index) {
        this.sessionIndex.remove(index);
    }

    /**
     * 获取会话索引列表（只读视图）。
     *
     * Gets the value of the sessionIndex property.
     */
    public List<String> getSessionIndex() {
        return Collections.unmodifiableList(this.sessionIndex);
    }

    /**
     * 获取 Reason 登出原因属性的值。
     *
     * Gets the value of the reason property.
     *
     * @return possible object is {@link String }
     */
    public String getReason() {
        return reason;
    }

    /**
     * 设置 Reason 登出原因属性的值。
     *
     * Sets the value of the reason property.
     *
     * @param value allowed object is {@link String }
     */
    public void setReason(String value) {
        this.reason = value;
    }

    /**
     * 获取 NotOnOrAfter 有效截止时间属性的值。
     *
     * Gets the value of the notOnOrAfter property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getNotOnOrAfter() {
        return notOnOrAfter;
    }

    /**
     * 设置 NotOnOrAfter 有效截止时间属性的值。
     *
     * Sets the value of the notOnOrAfter property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     */
    public void setNotOnOrAfter(XMLGregorianCalendar value) {
        this.notOnOrAfter = value;
    }

}
