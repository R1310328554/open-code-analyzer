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
package org.keycloak.dom.saml.v1.assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SAML 1.1 属性声明（AttributeStatement），关联主体并携带一组属性。
 *
 * <complexType name="AttributeStatementType"> <complexContent> <extension base="saml:SubjectStatementAbstractType">
 * <sequence>
 * <element ref="saml:Attribute" maxOccurs="unbounded"/>
 *
 * </sequence> </extension> </complexContent> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11AttributeStatementType extends SAML11SubjectStatementType {

    protected List<SAML11AttributeType> attribute = new ArrayList<>();

    /** 添加一条属性。 */
    public void add(SAML11AttributeType aAttribute) {
        this.attribute.add(aAttribute);
    }

    /** 批量添加属性。 */
    public void addAllAttributes(List<SAML11AttributeType> attribList) {
        this.attribute.addAll(attribList);
    }

    /** 移除一条属性。 */
    public boolean remove(SAML11AttributeType anAttrib) {
        return this.attribute.remove(anAttrib);
    }

    /** 获取属性列表（只读）。 */
    public List<SAML11AttributeType> get() {
        return Collections.unmodifiableList(attribute);
    }
}