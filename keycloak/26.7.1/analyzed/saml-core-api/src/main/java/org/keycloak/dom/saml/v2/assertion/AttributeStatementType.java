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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for AttributeStatementType complex type.
 * SAML 2.0 属性声明（AttributeStatement）：携带明文或加密属性元素。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AttributeStatementType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:assertion}StatementAbstractType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Attribute"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}EncryptedAttribute"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class AttributeStatementType extends StatementAbstractType {

    protected List<ASTChoiceType> attributes = new ArrayList<>();

    /**
     * 添加一条属性（明文或加密）。
     *
     * Add an attribute
     *
     * @param attribute
     */
    public void addAttribute(ASTChoiceType attribute) {
        attributes.add(attribute);
    }

    /**
     * 移除一条属性。
     *
     * Remove an attribute
     *
     * @param attribute
     */
    public void removeAttribute(ASTChoiceType attribute) {
        attributes.remove(attribute);
    }

    /**
     * 获取属性列表（只读）。
     *
     * Gets the attributes.
     *
     * @return a read only {@link List}
     */
    public List<ASTChoiceType> getAttributes() {
        return Collections.unmodifiableList(this.attributes);
    }

    /** 批量添加属性。 */
    public void addAttributes(List<ASTChoiceType> attributes) {
        this.attributes.addAll(attributes);
    }

    /** 属性声明中的选择项：明文 {@link AttributeType} 或加密 {@link EncryptedElementType}。 */
    public static class ASTChoiceType implements Serializable {

        private AttributeType attribute;
        private EncryptedElementType encryptedAssertion;

        /** 构造明文属性选择项。 */
        public ASTChoiceType(AttributeType attribute) {
            super();
            this.attribute = attribute;
        }

        /** 构造加密属性选择项。 */
        public ASTChoiceType(EncryptedElementType encryptedAssertion) {
            super();
            this.encryptedAssertion = encryptedAssertion;
        }

        /** 获取明文属性。 */
        public AttributeType getAttribute() {
            return attribute;
        }

        /** 获取加密属性元素。 */
        public EncryptedElementType getEncryptedAssertion() {
            return encryptedAssertion;
        }
    }
}