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
 * Java class for PGPDataType complex type.
 * XML 数字签名 PGP 密钥数据，可携带 PGPKeyID 与 PGPKeyPacket 等 OpenPGP 密钥材料。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PGPDataType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;sequence>
 *           &lt;element name="PGPKeyID" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *           &lt;element name="PGPKeyPacket" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *           &lt;any/>
 *         &lt;/sequence>
 *         &lt;sequence>
 *           &lt;element name="PGPKeyPacket" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *           &lt;any/>
 *         &lt;/sequence>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class PGPDataType {

    /** PGP 密钥数据子元素列表。 */
    protected List<Object> content = new ArrayList<>();

    /** 添加一条 PGP 数据子元素。 */
    public void add(Object obj) {
        this.content.add(obj);
    }

    /** 移除一条 PGP 数据子元素。 */
    public void remove(Object obj) {
        this.content.remove(obj);
    }

    /**
     * 获取 PGP 数据子元素列表（只读）。
     *
     * Gets the value of the content property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link org.w3c.dom.Element } {@link Object } {@link String }
     */
    public List<Object> getContent() {
        return Collections.unmodifiableList(this.content);
    }
}