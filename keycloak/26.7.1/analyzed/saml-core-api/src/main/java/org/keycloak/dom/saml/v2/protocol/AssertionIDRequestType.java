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
package org.keycloak.dom.saml.v2.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>
 * Java class for AssertionIDRequestType complex type.
 * SAML 2.0 断言 ID 请求：按断言 ID 引用批量查询断言。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AssertionIDRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}RequestAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}AssertionIDRef" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class AssertionIDRequestType extends RequestAbstractType {

    /** 待查询的断言 ID 引用列表。 */
    protected List<String> assertionIDRef = new ArrayList<>();

    /** 构造断言 ID 请求。 */
    public AssertionIDRequestType(String id, XMLGregorianCalendar instant) {
        super(id, instant);
    }

    /**
     * 添加断言 ID 引用。
     *
     * Add assertion id reference
     *
     * @param id
     */
    public void addAssertionIDRef(String id) {
        assertionIDRef.add(id);
    }

    /**
     * 移除断言 ID 引用。
     *
     * remove assertion id reference
     *
     * @param id
     */
    public void removeAssertionIDRef(String id) {
        assertionIDRef.remove(id);
    }

    /**
     * 获取 assertionIDRef 属性的值。
     *
     * Gets the value of the assertionIDRef property.
     */
    public List<String> getAssertionIDRef() {
        return Collections.unmodifiableList(this.assertionIDRef);
    }
}
