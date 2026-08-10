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
 * Java class for SubjectLocalityType complex type.
 * SAML 2.0 主体本地性：描述认证发生时的网络地址或 DNS 名称。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SubjectLocalityType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="Address" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="DNSName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class SubjectLocalityType implements Serializable {

    protected String address;

    protected String dnsName;

    /**
     * 获取 网络地址（Address） 属性的值。
     *
     * Gets the value of the address property.
     *
     * @return possible object is {@link String }
     */
    public String getAddress() {
        return address;
    }

    /**
     * 设置 网络地址（Address） 属性的值。
     *
     * Sets the value of the address property.
     *
     * @param value allowed object is {@link String }
     */
    public void setAddress(String value) {
        this.address = value;
    }

    /**
     * 获取 DNS 名称（DNSName） 属性的值。
     *
     * Gets the value of the dnsName property.
     *
     * @return possible object is {@link String }
     */
    public String getDNSName() {
        return dnsName;
    }

    /**
     * 设置 DNS 名称（DNSName） 属性的值。
     *
     * Sets the value of the dnsName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDNSName(String value) {
        this.dnsName = value;
    }
}