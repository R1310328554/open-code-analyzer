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
package org.keycloak.dom.xmlsec.w3.xmlenc;

import java.net.URI;

/**
 * <p>
 * Java class for CipherReferenceType complex type.
 * W3C XML Encryption 密文引用元素，通过 URI 指向外部密文并可附带转换算法。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CipherReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="Transforms" type="{http://www.w3.org/2001/04/xmlenc#}TransformsType" minOccurs="0"/>
 *       &lt;/choice>
 *       &lt;attribute name="URI" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class CipherReferenceType {

    /** 应用于外部密文的转换算法列表（可选 Transforms 子元素）。 */
    protected TransformsType transforms;
    /** 外部密文 URI（必填属性 URI）。 */
    protected URI uri;

    /**
     * 构造指定 URI 的密文引用。
     *
     * @param uri 外部密文 URI
     */
    public CipherReferenceType(URI uri) {

    }

    /**
     * 获取转换算法列表。
     *
     * Gets the value of the transforms property.
     *
     * @return possible object is {@link TransformsType }
     */
    public TransformsType getTransforms() {
        return transforms;
    }

    /**
     * 设置转换算法列表。
     *
     * Sets the value of the transforms property.
     *
     * @param value allowed object is {@link TransformsType }
     */
    public void setTransforms(TransformsType value) {
        this.transforms = value;
    }

    /**
     * 获取外部密文 URI。
     *
     * Gets the value of the uri property.
     *
     * @return possible object is {@link String }
     */
    public URI getURI() {
        return uri;
    }

}
