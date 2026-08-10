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
 * Java class for X509DataType complex type.
 * W3C XML Signature X509Data 元素，封装 X.509 证书相关的多种标识信息。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="X509DataType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded">
 *         &lt;choice>
 *           &lt;element name="X509IssuerSerial" type="{http://www.w3.org/2000/09/xmldsig#}X509IssuerSerialType"/>
 *           &lt;element name="X509SKI" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *           &lt;element name="X509SubjectName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="X509Certificate" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *           &lt;element name="X509CRL" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *           &lt;any/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class X509DataType {

    /** X509 数据对象列表（颁发者序列号、SKI、主题名、证书或 CRL 等）。 */
    protected List<Object> x509IssuerSerialOrX509SKIOrX509SubjectName = new ArrayList<>();

    /** 添加 X509 数据对象。 */
    public void add(Object obj) {
        this.x509IssuerSerialOrX509SKIOrX509SubjectName.add(obj);
    }

    /** 移除 X509 数据对象。 */
    public void remove(Object obj) {
        this.x509IssuerSerialOrX509SKIOrX509SubjectName.remove(obj);
    }

    /**
     * 获取 X509 数据对象列表（只读视图）。
     *
     * Gets the value of the x509IssuerSerialOrX509SKIOrX509SubjectName property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link org.w3c.dom.Element } {@link Object }
     */
    public List<Object> getDataObjects() {
        return Collections.unmodifiableList(this.x509IssuerSerialOrX509SKIOrX509SubjectName);
    }
}
