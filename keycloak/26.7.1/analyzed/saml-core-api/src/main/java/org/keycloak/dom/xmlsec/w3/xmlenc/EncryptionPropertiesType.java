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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for EncryptionPropertiesType complex type.
 * XML 加密 EncryptionProperties 容器，可包含多条 {@link EncryptionPropertyType} 扩展属性。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="EncryptionPropertiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2001/04/xmlenc#}EncryptionProperty" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class EncryptionPropertiesType {

    /** 加密属性条目列表。 */
    protected List<EncryptionPropertyType> encryptionProperty = new ArrayList<>();
    /** 属性集合的唯一标识。 */
    protected String id;

    /** 添加一条加密属性。 */
    public void addEncryptionProperty(EncryptionPropertyType enc) {
        this.encryptionProperty.add(enc);
    }

    /** 移除一条加密属性。 */
    public void removeEncryptionProperty(EncryptionPropertyType enc) {
        this.encryptionProperty.remove(enc);
    }

    /**
     * 获取 encryptionProperty 属性列表（只读）。
     *
     * Gets the value of the encryptionProperty property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link EncryptionPropertyType }
     */
    public List<EncryptionPropertyType> getEncryptionProperty() {
        return Collections.unmodifiableList(this.encryptionProperty);
    }

    /**
     * 获取 id 属性值。
     *
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * 设置 id 属性值。
     *
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }
}
