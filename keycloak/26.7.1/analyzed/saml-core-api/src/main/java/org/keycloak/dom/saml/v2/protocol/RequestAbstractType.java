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

import java.net.URI;
import javax.xml.datatype.XMLGregorianCalendar;

import org.keycloak.dom.saml.common.CommonRequestAbstractType;
import org.keycloak.dom.saml.v2.SAML2Object;
import org.keycloak.dom.saml.v2.assertion.NameIDType;

/**
 * <p>
 * Java class for RequestAbstractType complex type.
 * SAML 2.0 请求抽象基类，定义所有 SAML 协议请求的公共结构与属性。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="RequestAbstractType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Issuer" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Signature" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}Extensions" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="Version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="IssueInstant" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="Destination" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="Consent" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public abstract class RequestAbstractType extends CommonRequestAbstractType implements SAML2Object {

    /** 请求签发者（Issuer）。 */
    protected NameIDType issuer;

    /** 协议扩展元素。 */
    protected ExtensionsType extensions;

    /** SAML 协议版本，默认为 "2.0"。 */
    protected String version = "2.0";

    /** 请求的目标 URI（Destination）。 */
    protected URI destination;

    /** 用户同意声明 URI（Consent）。 */
    protected String consent;

    /**
     * 构造 SAML 请求。
     *
     * @param id 请求标识符
     * @param instant 签发时间
     */
    public RequestAbstractType(String id, XMLGregorianCalendar instant) {
        super(id, instant);
    }

    /**
     * 获取 Issuer 签发者属性的值。
     *
     * Gets the value of the issuer property.
     *
     * @return possible object is {@link NameIDType }
     */
    public NameIDType getIssuer() {
        return issuer;
    }

    /**
     * 设置 Issuer 签发者属性的值。
     *
     * Sets the value of the issuer property.
     *
     * @param value allowed object is {@link NameIDType }
     */
    public void setIssuer(NameIDType value) {
        this.issuer = value;
    }

    /**
     * 获取 Extensions 扩展属性的值。
     *
     * Gets the value of the extensions property.
     *
     * @return possible object is {@link ExtensionsType }
     */
    public ExtensionsType getExtensions() {
        return extensions;
    }

    /**
     * 设置 Extensions 扩展属性的值。
     *
     * Sets the value of the extensions property.
     *
     * @param value allowed object is {@link ExtensionsType }
     */
    public void setExtensions(ExtensionsType value) {
        this.extensions = value;
    }

    /**
     * 获取 Version 版本属性的值。
     *
     * Gets the value of the version property.
     *
     * @return possible object is {@link String }
     */
    public String getVersion() {
        return version;
    }

    /**
     * 获取 Destination 目标 URI 属性的值。
     *
     * Gets the value of the destination property.
     *
     * @return possible object is {@link String }
     */
    public URI getDestination() {
        return destination;
    }

    /**
     * 设置 Destination 目标 URI 属性的值。
     *
     * Sets the value of the destination property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDestination(URI value) {
        this.destination = value;
    }

    /**
     * 获取 Consent 同意声明属性的值。
     *
     * Gets the value of the consent property.
     *
     * @return possible object is {@link String }
     */
    public String getConsent() {
        return consent;
    }

    /**
     * 设置 Consent 同意声明属性的值。
     *
     * Sets the value of the consent property.
     *
     * @param value allowed object is {@link String }
     */
    public void setConsent(String value) {
        this.consent = value;
    }
}
