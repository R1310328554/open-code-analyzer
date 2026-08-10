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
 * Java class for ReferenceType complex type.
 * XML 加密引用类型，通过 URI 指向 EncryptedData 或 EncryptedKey 元素。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="URI" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class ReferenceType {

    /** 被引用元素的 URI（通常为片段标识符）。 */
    protected URI uri;

    /** 解析后的引用对象，运行时填充。 */
    public Object reference;

    /** 使用 URI 构造引用。 */
    public ReferenceType(URI uri) {
        this.uri = uri;
    }

    /** 获取解析后的引用对象。 */
    public Object getReference() {
        return reference;
    }

    /** 设置解析后的引用对象。 */
    public void setReference(Object reference) {
        this.reference = reference;
    }

    /**
     * 获取 uri 属性值。
     *
     * Gets the value of the uri property.
     *
     * @return possible object is {@link URI }
     */
    public URI getURI() {
        return uri;
    }
}
