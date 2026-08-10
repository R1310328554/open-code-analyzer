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
package org.keycloak.dom.saml.v2.metadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 * 可携带额外 XML 属性的类型抽象基类。
 *
 * Abstract base class for types that can have extra attributes
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 10, 2010
 */
public abstract class TypeWithOtherAttributes {

    /** 未绑定到强类型属性的扩展属性映射。 */
    protected Map<QName, String> otherAttributes = new HashMap<>();

    /**
     * 添加扩展属性。
     *
     * Add other attribute
     *
     * @param qame
     * @param value
     */
    public void addOtherAttribute(QName qame, String value) {
        otherAttributes.put(qame, value);
    }

    /**
     * 移除扩展属性。
     *
     * Remove other attribute
     *
     * @param qame
     * @param value
     */
    public void removeOtherAttribute(QName qame) {
        otherAttributes.remove(qame);
    }

    /**
     * 获取未绑定到本类强类型属性的扩展属性映射。
     *
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     *
     * @return always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return Collections.unmodifiableMap(otherAttributes);
    }
}
