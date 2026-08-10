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
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for ObjectType complex type.
 * XML 数字签名 Object 元素，封装待签名或已签名的外部数据，可指定 MIME 类型与编码方式。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ObjectType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="MimeType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Encoding" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class ObjectType {

    /** 对象内容元素列表。 */
    protected List<Object> content = new ArrayList<>();
    /** 对象标识符（Id）。 */
    protected String id;
    /** MIME 类型（MimeType）。 */
    protected String mimeType;
    /** 内容编码 URI（Encoding）。 */
    protected String encoding;

    /** 添加一条内容元素。 */
    public void add(Object obj) {
        this.content.add(obj);
    }

    /** 移除一条内容元素。 */
    public void remove(Object obj) {
        this.content.remove(obj);
    }

    /**
     * 获取对象内容列表（只读）。
     *
     * Gets the value of the content property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link org.w3c.dom.Element } {@link Object } {@link String }
     */
    public List<Object> getContent() {
        return Collections.unmodifiableList(this.content);
    }

    /**
     * 获取 对象标识符（Id） 属性的值。
     *
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * 设置 对象标识符（Id） 属性的值。
     *
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * 获取 MIME 类型（MimeType） 属性的值。
     *
     * Gets the value of the mimeType property.
     *
     * @return possible object is {@link String }
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * 设置 MIME 类型（MimeType） 属性的值。
     *
     * Sets the value of the mimeType property.
     *
     * @param value allowed object is {@link String }
     */
    public void setMimeType(String value) {
        this.mimeType = value;
    }

    /**
     * 获取 编码 URI（Encoding） 属性的值。
     *
     * Gets the value of the encoding property.
     *
     * @return possible object is {@link String }
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * 设置 编码 URI（Encoding） 属性的值。
     *
     * Sets the value of the encoding property.
     *
     * @param value allowed object is {@link String }
     */
    public void setEncoding(String value) {
        this.encoding = value;
    }
}