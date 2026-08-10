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
 * Java class for ManifestType complex type.
 * XML 数字签名清单，包含一组 {@link ReferenceType} 引用，用于批量描述待签名/验证的数据对象。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ManifestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Reference" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class ManifestType {

    /** 引用列表（Reference）。 */
    protected List<ReferenceType> reference = new ArrayList<>();
    /** 清单标识符（Id）。 */
    protected String id;

    /** 添加一条引用。 */
    public void addReference(ReferenceType ref) {
        this.reference.add(ref);
    }

    /** 移除一条引用。 */
    public void removeReference(ReferenceType ref) {
        this.reference.remove(ref);
    }

    /**
     * 获取引用列表（只读）。
     *
     * Gets the value of the reference property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link ReferenceType }
     */
    public List<ReferenceType> getReference() {
        return Collections.unmodifiableList(this.reference);
    }

    /**
     * 获取 清单标识符（Id） 属性的值。
     *
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * 设置 清单标识符（Id） 属性的值。
     *
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }
}