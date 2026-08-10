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
 * <complexType name="EvidenceType">
 * SAML 1.1 证据（Evidence）DOM 类型：通过断言 ID 引用或嵌套断言为授权决策提供佐证。
 <choice maxOccurs="unbounded"> <element ref="saml:AssertionIDReference"/>
 *
 * <element ref="saml:Assertion"/> </choice> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11EvidenceType {

    /** 引用的断言 ID 列表。 */
    protected List<String> assertionIDReference = new ArrayList<>();

    /** 嵌套的完整断言列表。 */
    protected List<SAML11AssertionType> assertions = new ArrayList<>();

    /** 添加一条断言 ID 引用。 */
    public void add(String condition) {
        this.assertionIDReference.add(condition);
    }

    /** 批量添加断言 ID 引用。 */
    public void addAllAssertionIDReference(List<String> theassertionIDReference) {
        this.assertionIDReference.addAll(theassertionIDReference);
    }

    /** 移除指定断言 ID 引用。 */
    public boolean remove(String assertionIDReference) {
        return this.assertionIDReference.remove(assertionIDReference);
    }

    /** 返回不可修改的断言 ID 引用列表。 */
    public List<String> getAssertionIDReference() {
        return Collections.unmodifiableList(assertionIDReference);
    }

    /** 添加一条嵌套断言。 */
    public void add(SAML11AssertionType condition) {
        this.assertions.add(condition);
    }

    /** 批量添加嵌套断言。 */
    public void addAllAssertionType(List<SAML11AssertionType> theassertions) {
        this.assertions.addAll(theassertions);
    }

    /** 移除指定嵌套断言。 */
    public boolean remove(SAML11AssertionType assertion) {
        return this.assertions.remove(assertion);
    }

    /** 返回不可修改的嵌套断言列表。 */
    public List<SAML11AssertionType> getAssertions() {
        return Collections.unmodifiableList(assertions);
    }
}