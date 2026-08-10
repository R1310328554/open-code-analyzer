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

package org.keycloak.dom.saml.v2.profiles.sso.ecp;

import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.dom.saml.v2.protocol.IDPListType;

/**
 * <p>
 * Java class for RequestType complex type.
 * ECP SSO 请求类型：SOAP 封装的身份验证请求，含 Issuer、IDP 列表及被动模式标志。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="RequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Issuer"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}IDPList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://schemas.xmlsoap.org/soap/envelope/}mustUnderstand use="required""/>
 *       &lt;attribute ref="{http://schemas.xmlsoap.org/soap/envelope/}actor use="required""/>
 *       &lt;attribute name="ProviderName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="IsPassive" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class RequestType {

    /** 请求签发者。 */
    protected NameIDType issuer;
    /** 可选的 IdP 列表。 */
    protected IDPListType idpList;
    /** SOAP mustUnderstand 标志。 */
    protected Boolean mustUnderstand = Boolean.FALSE;
    /** SOAP actor URI。 */
    protected String actor;
    /** 服务提供者名称。 */
    protected String providerName;
    /** 是否为被动（不交互）认证。 */
    protected Boolean isPassive = Boolean.FALSE;

    /**
     * 获取 issuer 属性的值。
     *
     * Gets the value of the issuer property.
     *
     * @return possible object is {@link NameIDType }
     */
    public NameIDType getIssuer() {
        return issuer;
    }

    /**
     * 设置 issuer 属性的值。
     *
     * Sets the value of the issuer property.
     *
     * @param value allowed object is {@link NameIDType }
     */
    public void setIssuer(NameIDType value) {
        this.issuer = value;
    }

    /**
     * 获取 idpList 属性的值。
     *
     * Gets the value of the idpList property.
     *
     * @return possible object is {@link IDPListType }
     */
    public IDPListType getIDPList() {
        return idpList;
    }

    /**
     * 设置 idpList 属性的值。
     *
     * Sets the value of the idpList property.
     *
     * @param value allowed object is {@link IDPListType }
     */
    public void setIDPList(IDPListType value) {
        this.idpList = value;
    }

    /**
     * 获取 mustUnderstand 属性的值。
     *
     * Gets the value of the mustUnderstand property.
     *
     * @return possible object is {@link String }
     */
    public Boolean isMustUnderstand() {
        return mustUnderstand;
    }

    /**
     * 设置 mustUnderstand 属性的值。
     *
     * Sets the value of the mustUnderstand property.
     *
     * @param value allowed object is {@link String }
     */
    public void setMustUnderstand(Boolean value) {
        this.mustUnderstand = value;
    }

    /**
     * 获取 actor 属性的值。
     *
     * Gets the value of the actor property.
     *
     * @return possible object is {@link String }
     */
    public String getActor() {
        return actor;
    }

    /**
     * 设置 actor 属性的值。
     *
     * Sets the value of the actor property.
     *
     * @param value allowed object is {@link String }
     */
    public void setActor(String value) {
        this.actor = value;
    }

    /**
     * 获取 providerName 属性的值。
     *
     * Gets the value of the providerName property.
     *
     * @return possible object is {@link String }
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * 设置 providerName 属性的值。
     *
     * Sets the value of the providerName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setProviderName(String value) {
        this.providerName = value;
    }

    /**
     * 获取 isPassive 属性的值。
     *
     * Gets the value of the isPassive property.
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isIsPassive() {
        return isPassive;
    }

    /**
     * 设置 isPassive 属性的值。
     *
     * Sets the value of the isPassive property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setIsPassive(Boolean value) {
        this.isPassive = value;
    }

}
