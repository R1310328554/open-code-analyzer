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
package org.keycloak.dom.xmlsec.w3.xmldsig;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Java class for SignaturePropertyType complex type.
 * W3C XML Signature 单条签名属性，通过 Target 属性关联被签名的对象。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SignaturePropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;any/>
 *       &lt;/choice>
 *       &lt;attribute name="Target" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class SignaturePropertyType {

    /** 属性内容列表（可含任意 XML 元素或文本）。 */
    protected List<Object> content = new ArrayList<>();
    /** 目标 URI（必填属性 Target），指向关联的签名对象。 */
    protected String target;
    /** 元素标识符（可选属性 Id）。 */
    protected String id;

    /**
     * 获取属性内容列表。
     *
     * Gets the value of the content property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link org.w3c.dom.Element } {@link Object } {@link String }
     */
    public List<Object> getContent() {
        return this.content;
    }

    /**
     * 获取目标 URI。
     *
     * Gets the value of the target property.
     *
     * @return possible object is {@link String }
     */
    public String getTarget() {
        return target;
    }

    /**
     * 设置目标 URI。
     *
     * Sets the value of the target property.
     *
     * @param value allowed object is {@link String }
     */
    public void setTarget(String value) {
        this.target = value;
    }

    /**
     * 获取元素标识符。
     *
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * 设置元素标识符。
     *
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }
}
