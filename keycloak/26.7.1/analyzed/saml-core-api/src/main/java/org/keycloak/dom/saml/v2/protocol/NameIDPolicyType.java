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

/**
 * <p>
 * Java class for NameIDPolicyType complex type.
 * SAML 2.0 NameID 策略，指定服务提供者对主体标识格式与创建行为的要求。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="NameIDPolicyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="Format" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="SPNameQualifier" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="AllowCreate" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class NameIDPolicyType {

    /** NameID 格式 URI（Format）。 */
    protected URI format;
    /** 服务提供者名称限定符（SPNameQualifier）。 */
    protected String spNameQualifier;
    /** 是否允许 IdP 创建新的 NameID，默认为 false。 */
    protected Boolean allowCreate = Boolean.FALSE;

    /**
     * 获取 Format 格式属性的值。
     *
     * Gets the value of the format property.
     *
     * @return possible object is {@link String }
     */
    public URI getFormat() {
        return format;
    }

    /**
     * 设置 Format 格式属性的值。
     *
     * Sets the value of the format property.
     *
     * @param value allowed object is {@link String }
     */
    public void setFormat(URI value) {
        this.format = value;
    }

    /**
     * 获取 SPNameQualifier 属性的值。
     *
     * Gets the value of the spNameQualifier property.
     *
     * @return possible object is {@link String }
     */
    public String getSPNameQualifier() {
        return spNameQualifier;
    }

    /**
     * 设置 SPNameQualifier 属性的值。
     *
     * Sets the value of the spNameQualifier property.
     *
     * @param value allowed object is {@link String }
     */
    public void setSPNameQualifier(String value) {
        this.spNameQualifier = value;
    }

    /**
     * 获取 AllowCreate 是否允许创建属性的值。
     *
     * Gets the value of the allowCreate property.
     *
     * @return possible object is {@link Boolean }
     */
    public Boolean isAllowCreate() {
        return allowCreate;
    }

    /**
     * 设置 AllowCreate 是否允许创建属性的值。
     *
     * Sets the value of the allowCreate property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setAllowCreate(Boolean value) {
        this.allowCreate = value;
    }
}
