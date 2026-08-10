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
package org.keycloak.dom.saml.v2.assertion;

import java.io.Serializable;

import org.w3c.dom.Element;

/**
 * Represents an element that is encrypted
 * 加密元素类型：持有 EncryptedData 及可选 EncryptedKey 的 DOM 表示。
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 24, 2010
 */
public class EncryptedElementType implements Serializable {

    /**
     * <complexType name="EncryptedElementType"> <sequence> <element ref="xenc:EncryptedData"/> <element
     * ref="xenc:EncryptedKey"
     * minOccurs="0" maxOccurs="unbounded"/> </sequence> </complexType>
     */

    protected Element encryptedElement;

    /** 默认构造加密元素。 */
    public EncryptedElementType() {
    }

    /** 以 DOM 元素构造加密元素。 */
    public EncryptedElementType(Element el) {
        this.encryptedElement = el;
    }

    /** 获取加密 DOM 元素。 */
    public Element getEncryptedElement() {
        return encryptedElement;
    }

    /** 设置加密 DOM 元素。 */
    public void setEncryptedElement(Element encryptedElement) {
        this.encryptedElement = encryptedElement;
    }
}