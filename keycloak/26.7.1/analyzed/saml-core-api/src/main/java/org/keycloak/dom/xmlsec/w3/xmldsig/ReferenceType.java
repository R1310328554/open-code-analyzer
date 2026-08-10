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
 * Java class for ReferenceType complex type.
 * XML 数字签名引用，描述待摘要/验证的数据 URI、变换链、摘要算法及摘要值。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Transforms" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}DigestMethod"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}DigestValue"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="URI" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class ReferenceType {

    /** 变换链（Transforms）。 */
    protected TransformsType transforms;
    /** 摘要算法（DigestMethod）。 */
    protected DigestMethodType digestMethod;
    /** 摘要值（DigestValue）。 */
    protected byte[] digestValue;
    /** 引用标识符（Id）。 */
    protected String id;
    /** 被引用数据 URI。 */
    protected String uri;
    /** 引用类型 URI（Type）。 */
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
     * 获取 摘要算法（DigestMethod） 属性的值。
     *
     * Gets the value of the digestMethod property.
     *
     * @return possible object is {@link DigestMethodType }
     */
    public DigestMethodType getDigestMethod() {
        return digestMethod;
    }

    /**
     * 设置 摘要算法（DigestMethod） 属性的值。
     *
     * Sets the value of the digestMethod property.
     *
     * @param value allowed object is {@link DigestMethodType }
     */
    public void setDigestMethod(DigestMethodType value) {
        this.digestMethod = value;
    }

    /**
     * 获取 摘要值（DigestValue） 属性的值。
     *
     * Gets the value of the digestValue property.
     *
     * @return possible object is byte[]
     */
    public byte[] getDigestValue() {
        return digestValue;
    }

    /**
     * 设置 摘要值（DigestValue） 属性的值。
     *
     * Sets the value of the digestValue property.
     *
     * @param value allowed object is byte[]
     */
    public void setDigestValue(byte[] value) {
        this.digestValue = ((byte[]) value);
    }

    /**
     * 获取 引用标识符（Id） 属性的值。
     *
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * 设置 引用标识符（Id） 属性的值。
     *
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * 获取 被引用数据 URI 属性的值。
     *
     * Gets the value of the uri property.
     *
     * @return possible object is {@link String }
     */
    public String getURI() {
        return uri;
    }

    /**
     * 设置 被引用数据 URI 属性的值。
     *
     * Sets the value of the uri property.
     *
     * @param value allowed object is {@link String }
     */
    public void setURI(String value) {
        this.uri = value;
    }

    /**
     * 获取 引用类型 URI（Type） 属性的值。
     *
     * Gets the value of the type property.
     *
     * @return possible object is {@link String }
     */
    public String getType() {
        return type;
    }

    /**
     * 设置 引用类型 URI（Type） 属性的值。
     *
     * Sets the value of the type property.
     *
     * @param value allowed object is {@link String }
     */
    public void setType(String value) {
        this.type = value;
    }
}