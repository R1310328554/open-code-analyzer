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

/**
 * <p>
 * Java class for SPSSODescriptorType complex type.
 * SAML 2.0 SP SSO 描述符：断言消费服务、属性消费服务及认证/断言签名要求。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SPSSODescriptorType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:metadata}SSODescriptorType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}AssertionConsumerService" maxOccurs="unbounded"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}AttributeConsumingService" maxOccurs="unbounded"
 * minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="AuthnRequestsSigned" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="WantAssertionsSigned" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class SPSSODescriptorType extends SSODescriptorType {

    protected List<IndexedEndpointType> assertionConsumerService = new ArrayList<>();

    protected List<AttributeConsumingServiceType> attributeConsumingService = new ArrayList<>();

    protected boolean authnRequestsSigned = false;

    protected boolean wantAssertionsSigned = false;

    /** 以协议支持列表构造 SP SSO 描述符。 */
    public SPSSODescriptorType(List<String> protocolSupport) {
        super(protocolSupport);
    }

    /**
     * 添加断言消费服务端点。
     *
     * Add an Assertion Consumer Service
     *
     * @param assertionConsumer an endpoint of type {@link IndexedEndpointType}
     */
    public void addAssertionConsumerService(IndexedEndpointType assertionConsumer) {
        this.assertionConsumerService.add(assertionConsumer);
    }

    /**
     * 添加属性消费服务。
     *
     * Add an attribute consumer
     *
     * @param attributeConsumer an instance of type {@link AttributeConsumingServiceType}
     */
    public void addAttributeConsumerService(AttributeConsumingServiceType attributeConsumer) {
        this.attributeConsumingService.add(attributeConsumer);
    }

    /**
     * 移除断言消费服务端点。
     *
     * Remove an Assertion Consumer Service
     *
     * @param assertionConsumer an endpoint of type {@link IndexedEndpointType}
     */
    public void removeAssertionConsumerService(IndexedEndpointType assertionConsumer) {
        this.assertionConsumerService.remove(assertionConsumer);
    }

    /**
     * 移除属性消费服务。
     *
     * Remove an attribute consumer
     *
     * @param attributeConsumer an instance of type {@link AttributeConsumingServiceType}
     */
    public void removeAttributeConsumerService(AttributeConsumingServiceType attributeConsumer) {
        this.attributeConsumingService.remove(attributeConsumer);
    }

    /**
     * 获取 assertionConsumerService 属性的值。
     * <p>
     * Objects of the following type(s) are allowed in the list {@link IndexedEndpointType }
     */
    public List<IndexedEndpointType> getAssertionConsumerService() {
        return Collections.unmodifiableList(this.assertionConsumerService);
    }

    /**
     * 获取 attributeConsumingService 属性的值。
     * <p>
     * Objects of the following type(s) are allowed in the list {@link AttributeConsumingServiceType }
     */
    public List<AttributeConsumingServiceType> getAttributeConsumingService() {
        return Collections.unmodifiableList(this.attributeConsumingService);
    }

    /**
     * 获取 authnRequestsSigned 属性的值。
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isAuthnRequestsSigned() {
        return authnRequestsSigned;
    }

    /**
     * 设置 authnRequestsSigned 属性的值。
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setAuthnRequestsSigned(Boolean value) {
        this.authnRequestsSigned = value;
    }

    /**
     * 获取 wantAssertionsSigned 属性的值。
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isWantAssertionsSigned() {
        return wantAssertionsSigned;
    }

    /**
     * 设置 wantAssertionsSigned 属性的值。
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setWantAssertionsSigned(Boolean value) {
        this.wantAssertionsSigned = value;
    }
}
