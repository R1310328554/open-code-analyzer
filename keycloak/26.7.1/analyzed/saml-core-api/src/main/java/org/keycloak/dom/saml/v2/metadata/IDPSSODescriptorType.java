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
package org.keycloak.dom.saml.v2.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.keycloak.dom.saml.v2.assertion.AttributeType;

/**
 * <p>
 * Java class for IDPSSODescriptorType complex type.
 * SAML 2.0 IdP SSO 描述符：单点登录、NameID 映射及断言 ID 请求服务端点。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="IDPSSODescriptorType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:metadata}SSODescriptorType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}SingleSignOnService" maxOccurs="unbounded"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}NameIDMappingService" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}AssertionIDRequestService" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}AttributeProfile" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Attribute" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="WantAuthnRequestsSigned" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class IDPSSODescriptorType extends SSODescriptorType {

    protected List<EndpointType> singleSignOnService = new ArrayList<>();

    protected List<EndpointType> nameIDMappingService = new ArrayList<>();

    protected List<EndpointType> assertionIDRequestService = new ArrayList<>();

    protected List<String> attributeProfile = new ArrayList<>();

    protected List<AttributeType> attribute = new ArrayList<>();

    protected Boolean wantAuthnRequestsSigned = false;

    /** 以协议支持列表构造 IdP SSO 描述符。 */
    public IDPSSODescriptorType(List<String> protocolSupport) {
        super(protocolSupport);
    }

    /**
     * 添加单点登录服务端点。
     *
     * Add a SSO service
     *
     * @param endpt
     */
    public void addSingleSignOnService(EndpointType endpt) {
        this.singleSignOnService.add(endpt);
    }

    /**
     * 添加 NameID 映射服务端点。
     *
     * Add name id mapping service
     *
     * @param endpt
     */
    public void addNameIDMappingService(EndpointType endpt) {
        this.nameIDMappingService.add(endpt);
    }

    /**
     * 添加断言 ID 请求服务端点。
     *
     * Add assertion id request service
     *
     * @param endpt
     */
    public void addAssertionIDRequestService(EndpointType endpt) {
        this.assertionIDRequestService.add(endpt);
    }

    /**
     * 添加属性配置文件 URI。
     *
     * Add attribute profile
     *
     * @param str
     */
    public void addAttributeProfile(String str) {
        this.attributeProfile.add(str);
    }

    /**
     * 添加 IdP 支持的属性。
     *
     * Add attribute
     *
     * @param att
     */
    public void addAttribute(AttributeType att) {
        this.attribute.add(att);
    }

    /**
     * 移除单点登录服务端点。
     *
     * Remove a SSO service
     *
     * @param endpt
     */
    public void removeSingleSignOnService(EndpointType endpt) {
        this.singleSignOnService.remove(endpt);
    }

    /**
     * 移除 NameID 映射服务端点。
     *
     * remove name id mapping service
     *
     * @param endpt
     */
    public void removeNameIDMappingService(EndpointType endpt) {
        this.nameIDMappingService.remove(endpt);
    }

    /**
     * 移除断言 ID 请求服务端点。
     *
     * remove assertion id request service
     *
     * @param endpt
     */
    public void removeAssertionIDRequestService(EndpointType endpt) {
        this.assertionIDRequestService.remove(endpt);
    }

    /** 移除属性配置文件 URI。
     *
     * Add attribute profile
     *
     * @param str
     */
    public void removeAttributeProfile(String str) {
        this.attributeProfile.remove(str);
    }

    /** 移除 IdP 支持的属性。
     *
     * Add attribute
     *
     * @param att
     */
    public void removeAttribute(AttributeType att) {
        this.attribute.remove(att);
    }

    /** 获取只读单点登录服务端点列表。
     *
     * Gets the value of the singleSignOnService property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link EndpointType }
     */
    public List<EndpointType> getSingleSignOnService() {
        return Collections.unmodifiableList(this.singleSignOnService);
    }

    /** 获取只读 NameID 映射服务端点列表。
     *
     * Gets the value of the nameIDMappingService property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link EndpointType }
     */
    public List<EndpointType> getNameIDMappingService() {
        return Collections.unmodifiableList(this.nameIDMappingService);
    }

    /** 获取只读断言 ID 请求服务端点列表。
     *
     * Gets the value of the assertionIDRequestService property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link EndpointType }
     */
    public List<EndpointType> getAssertionIDRequestService() {
        return Collections.unmodifiableList(this.assertionIDRequestService);
    }

    /** 获取只读属性配置文件 URI 列表。
     *
     * Gets the value of the attributeProfile property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the
     * returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the
     * attributeProfile property.
     *
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getAttributeProfile() {
        return Collections.unmodifiableList(this.attributeProfile);
    }

    /** 获取只读 IdP 属性列表。
     *
     * Gets the value of the attribute property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link AttributeType }
     */
    public List<AttributeType> getAttribute() {
        return Collections.unmodifiableList(this.attribute);
    }

    /**
     * 获取 是否要求 AuthnRequest 签名 属性的值。
     *
     * Gets the value of the wantAuthnRequestsSigned property.
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isWantAuthnRequestsSigned() {
        return wantAuthnRequestsSigned;
    }

    /**
     * 设置 是否要求 AuthnRequest 签名 属性的值。
     *
     * Sets the value of the wantAuthnRequestsSigned property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setWantAuthnRequestsSigned(Boolean value) {
        this.wantAuthnRequestsSigned = value;
    }
}