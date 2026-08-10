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

/**
 * <p>
 * Java class for RetrievalMethodType complex type.
 * XML 数字签名密钥检索方法，通过 URI 指向外部密钥资源，可选携带变换链。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="RetrievalMethodType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Transforms" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="URI" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class RetrievalMethodType {

    /** 检索前变换链（Transforms）。 */
    protected TransformsType transforms;
    /** 密钥资源 URI。 */
    protected String uri;
    /** 检索资源类型 URI（Type）。 */
    protected String type;

    /**
     * 获取 变换链（Transforms） 属性的值。
     *
     * Gets the value of the transforms property.
     *
     * @return possible object is {@link TransformsType }
     */
    public TransformsType getTransforms() {
        return transforms;
    }

    /**
     * 设置 变换链（Transforms） 属性的值。
     *
     * Sets the value of the transforms property.
     *
     * @param value allowed object is {@link TransformsType }
     */
    public void setTransforms(TransformsType value) {
        this.transforms = value;
    }

    /**
     * 获取 密钥资源 URI 属性的值。
     *
     * Gets the value of the uri property.
     *
     * @return possible object is {@link String }
     */
    public String getURI() {
        return uri;
    }

    /**
     * 设置 密钥资源 URI 属性的值。
     *
     * Sets the value of the uri property.
     *
     * @param value allowed object is {@link String }
     */
    public void setURI(String value) {
        this.uri = value;
    }

    /**
     * 获取 资源类型 URI（Type） 属性的值。
     *
     * Gets the value of the type property.
     *
     * @return possible object is {@link String }
     */
    public String getType() {
        return type;
    }

    /**
     * 设置 资源类型 URI（Type） 属性的值。
     *
     * Sets the value of the type property.
     *
     * @param value allowed object is {@link String }
     */
    public void setType(String value) {
        this.type = value;
    }

}
