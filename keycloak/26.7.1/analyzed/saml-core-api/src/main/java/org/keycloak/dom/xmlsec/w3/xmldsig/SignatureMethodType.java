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
 * Java class for SignatureMethodType complex type.
 * W3C XML Signature 签名方法元素，指定签名算法及可选的 HMAC 输出长度等扩展内容。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SignatureMethodType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="HMACOutputLength" type="{http://www.w3.org/2000/09/xmldsig#}HMACOutputLengthType"
 * minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="Algorithm" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class SignatureMethodType {

    /** 签名方法内容列表（可含 HMACOutputLength 等扩展元素）。 */
    protected List<Object> content = new ArrayList<>();
    /** 签名算法 URI（必填属性 Algorithm）。 */
    protected String algorithm;

    /** 向内容列表添加元素。 */
    public void add(Object obj) {
        this.content.add(obj);
    }

    /** 从内容列表移除元素。 */
    public void remove(Object obj) {
        this.content.remove(obj);
    }

    /**
     * 获取签名方法内容列表（只读视图）。
     *
     * Gets the value of the content property.
     *
     * {@link Object } {@link String }
     */
    public List<Object> getContent() {
        return Collections.unmodifiableList(this.content);
    }

    /**
     * 获取签名算法 URI。
     *
     * Gets the value of the algorithm property.
     *
     * @return possible object is {@link String }
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * 设置签名算法 URI。
     *
     * Sets the value of the algorithm property.
     *
     * @param value allowed object is {@link String }
     */
    public void setAlgorithm(String value) {
        this.algorithm = value;
    }

}
