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
 * Java class for AttributeAuthorityDescriptorType complex type.
 * SAML 2.0 属性权威描述符：声明属性服务、断言 ID 请求服务、NameID 格式及支持的属性配置。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AttributeAuthorityDescriptorType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:metadata}RoleDescriptorType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}AttributeService" maxOccurs="unbounded"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}AssertionIDRequestService" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}NameIDFormat" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}AttributeProfile" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Attribute" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */

public class AttributeAuthorityDescriptorType extends RoleDescriptorType {

    protected List<EndpointType> attributeService = new ArrayList<>();

    protected List<EndpointType> assertionIDRequestService = new ArrayList<>();

    protected List<String> nameIDFormat = new ArrayList<>();

    protected List<String> attributeProfile = new ArrayList<>();

    protected List<AttributeType> attribute = new ArrayList<>();

    /** 以支持的协议列表构造属性权威描述符。 */
    public AttributeAuthorityDescriptorType(List<String> protocolSupport) {
        super(protocolSupport);
    }

    /**
     * 添加属性服务端点。
     *
     * Add an attribute service
     *
     * @param endpoint
     */
    public void addAttributeService(EndpointType endpoint) {
        this.attributeService.add(endpoint);
    }

    /**
     * 添加断言 ID 请求服务端点。
     *
     * Add an assertion id request service
     *
     * @param endpoint
     */
    public void addAssertionIDRequestService(EndpointType endpoint) {
        this.assertionIDRequestService.add(endpoint);
    }

    /**
     * 添加支持的 NameID 格式 URI。
     *
     * Add a name id
     *
     * @param str
     */
    public void addNameIDFormat(String str) {
        this.nameIDFormat.add(str);
    }

    /**
     * 添加属性配置文件 URI。
     *
     * Add an attribute profile
     *
     * @param str
     */
    public void addAttributeProfile(String str) {
        this.attributeProfile.add(str);
    }

    /**
     * 添加一条支持的属性。
     *
     * Add an attribute
     *
     * @param attribute
     */
    public void addAttribute(AttributeType attribute) {
        this.attribute.add(attribute);
    }

    /**
     * 移除属性服务端点。
     *
     * Remove an attribute service
     *
     * @param endpoint
     */
    public void removeAttributeService(EndpointType endpoint) {
        this.attributeService.remove(endpoint);
    }

    /**
     * 移除断言 ID 请求服务端点。
     *
     * Remove assertion id request service
     *
     * @param endpoint
     */
    public void removeAssertionIDRequestService(EndpointType endpoint) {
        this.assertionIDRequestService.remove(endpoint);
    }

    /**
     * 移除 NameID 格式 URI。
     *
     * Remove Name ID
     *
     * @param str
     */
    public void removeNameIDFormat(String str) {
        this.nameIDFormat.remove(str);
    }

    /**
     * 移除属性配置文件 URI。
     *
     * Remove attribute profile
     *
     * @param str
     */
    public void removeAttributeProfile(String str) {
        this.attributeProfile.remove(str);
    }

    /**
     * 移除一条支持的属性。
     *
     * Remove attribute
     *
     * @param attribute
     */
    public void removeAttribute(AttributeType attribute) {
        this.attribute.remove(attribute);
    }

    /**
     * 获取属性服务端点列表（只读）。
     *
     * Gets the value of the attributeService property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link EndpointType }
     */
    public List<EndpointType> getAttributeService() {
        return Collections.unmodifiableList(this.attributeService);
    }

    /**
     * 获取断言 ID 请求服务端点列表（只读）。
     *
     * Gets the value of the assertionIDRequestService property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link EndpointType }
     */
    public List<EndpointType> getAssertionIDRequestService() {
        return Collections.unmodifiableList(this.assertionIDRequestService);
    }

    /**
     * 获取 NameID 格式 URI 列表（只读）。
     *
     * Gets the value of the nameIDFormat property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getNameIDFormat() {
        return Collections.unmodifiableList(this.nameIDFormat);
    }

    /**
     * 获取属性配置文件 URI 列表（只读）。
     *
     * Gets the value of the attributeProfile property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getAttributeProfile() {
        return Collections.unmodifiableList(this.attributeProfile);
    }

    /**
     * 获取支持的属性列表（只读）。
     *
     * Gets the value of the attribute property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link AttributeType }
     */
    public List<AttributeType> getAttribute() {
        return Collections.unmodifiableList(this.attribute);
    }
}