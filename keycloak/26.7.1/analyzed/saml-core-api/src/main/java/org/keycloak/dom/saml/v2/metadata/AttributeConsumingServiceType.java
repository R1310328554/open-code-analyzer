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
 * Java class for AttributeConsumingServiceType complex type.
 * SAML 2.0 属性消费服务：描述 SP 期望的服务名称、描述及请求的 SAML 属性。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AttributeConsumingServiceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}ServiceName" maxOccurs="unbounded"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}ServiceDescription" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}RequestedAttribute" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="index" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" />
 *       &lt;attribute name="isDefault" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class AttributeConsumingServiceType {

    protected List<LocalizedNameType> serviceName = new ArrayList<>();

    protected List<LocalizedNameType> serviceDescription = new ArrayList<>();

    protected List<RequestedAttributeType> requestedAttribute = new ArrayList<>();

    protected int index;

    protected Boolean isDefault = Boolean.FALSE;

    /** 以索引号构造属性消费服务。 */
    public AttributeConsumingServiceType(int index) {
        this.index = index;
    }

    /**
     * 添加本地化服务名称。
     *
     * Add serviceName
     * <p>
     * Objects of the following type(s) are allowed in the list {@link LocalizedNameType }
     */
    public void addServiceName(LocalizedNameType service) {
        this.serviceName.add(service);
    }

    /**
     * 添加本地化服务描述。
     *
     * Add serviceDescription.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link LocalizedNameType }
     */
    public void addServiceDescription(LocalizedNameType desc) {
        this.serviceDescription.add(desc);
    }

    /**
     * 添加请求的属性。
     *
     * Add requestedAttribute
     * <p>
     * Objects of the following type(s) are allowed in the list {@link RequestedAttributeType }
     */
    public void addRequestedAttribute(RequestedAttributeType req) {
        this.requestedAttribute.add(req);
    }

    /**
     * 移除本地化服务名称。
     *
     * remove serviceName
     * <p>
     * Objects of the following type(s) are allowed in the list {@link LocalizedNameType }
     */
    public void removeServiceName(LocalizedNameType service) {
        this.serviceName.remove(service);
    }

    /**
     * 移除本地化服务描述。
     *
     * remove serviceDescription.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link LocalizedNameType }
     */
    public void removeServiceDescription(LocalizedNameType desc) {
        this.serviceDescription.remove(desc);
    }

    /**
     * 移除请求的属性。
     *
     * remove requestedAttribute
     * <p>
     * Objects of the following type(s) are allowed in the list {@link RequestedAttributeType }
     */
    public void removeRequestedAttribute(RequestedAttributeType req) {
        this.requestedAttribute.remove(req);
    }

    /**
     * 获取服务名称列表（只读）。
     *
     * Gets the value of the serviceName property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link LocalizedNameType }
     */
    public List<LocalizedNameType> getServiceName() {
        return Collections.unmodifiableList(this.serviceName);
    }

    /**
     * 获取服务描述列表（只读）。
     *
     * Gets the value of the serviceDescription property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link LocalizedNameType }
     */
    public List<LocalizedNameType> getServiceDescription() {
        return Collections.unmodifiableList(this.serviceDescription);
    }

    /**
     * 获取请求属性列表（只读）。
     *
     * Gets the value of the requestedAttribute property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link RequestedAttributeType }
     */
    public List<RequestedAttributeType> getRequestedAttribute() {
        return Collections.unmodifiableList(this.requestedAttribute);
    }

    /**
     * 获取服务索引号。
     *
     * Gets the value of the index property.
     */
    public int getIndex() {
        return index;
    }

    /**
     * 获取是否为默认属性消费服务。
     *
     * Gets the value of the isDefault property.
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isIsDefault() {
        return isDefault;
    }

    /**
     * 设置是否为默认属性消费服务。
     *
     * Sets the value of the isDefault property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setIsDefault(Boolean value) {
        this.isDefault = value;
    }
}