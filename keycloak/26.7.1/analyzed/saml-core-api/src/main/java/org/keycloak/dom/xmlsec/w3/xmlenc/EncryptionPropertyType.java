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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 * <p>
 * Java class for EncryptionPropertyType complex type.
 * XML 加密单条 EncryptionProperty，用于携带与加密过程相关的自定义元数据。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="EncryptionPropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;any/>
 *       &lt;/choice>
 *       &lt;attribute name="Target" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class EncryptionPropertyType {

    /** 本属性所关联的加密元素 URI。 */
    protected URI target;
    /** 属性元素唯一标识。 */
    protected String id;
    /** 未绑定到已知属性的其他 XML 属性。 */
    private Map<QName, String> otherAttributes = new HashMap<>();

    /**
     * 获取 target 属性值。
     *
     * Gets the value of the target property.
     *
     * @return possible object is {@link URI }
     */
    public URI getTarget() {
        return target;
    }

    /**
     * 设置 target 属性值。
     *
     * Sets the value of the target property.
     *
     * @param value allowed object is {@link URI }
     */
    public void setTarget(URI value) {
        this.target = value;
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

    /** 添加一条未建模的 XML 属性。 */
    public void addOtherAttribute(QName key, String val) {
        this.otherAttributes.put(key, val);
    }

    /** 批量添加未建模的 XML 属性。 */
    public void addOtherAttributes(Map<QName, String> otherMap) {
        this.otherAttributes.putAll(otherMap);
    }

    /** 移除指定 QName 的未建模属性。 */
    public void removeOtherAttribute(QName key) {
        this.otherAttributes.remove(key);
    }

    /**
     * 获取未绑定到本类已知属性的 XML 属性映射（只读，永不为 null）。
     *
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     *
     * @return always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return Collections.unmodifiableMap(otherAttributes);
    }
}
