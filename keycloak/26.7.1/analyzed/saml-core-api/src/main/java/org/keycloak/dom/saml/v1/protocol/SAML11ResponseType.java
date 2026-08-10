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
package org.keycloak.dom.saml.v1.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

import org.keycloak.dom.saml.v1.assertion.SAML11AssertionType;

/**
 * SAML 1.1 响应（Response）类型：包含处理状态及零个或多个断言。
 *
 * <complexType name="ResponseType"> <complexContent> <extension base="samlp:ResponseAbstractType"> <sequence> <element
 * ref="samlp:Status"/> <element ref="saml:Assertion" minOccurs="0" maxOccurs="unbounded"/> </sequence> </extension>
 *
 * </complexContent> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11ResponseType extends SAML11ResponseAbstractType {

    /** 响应携带的断言列表。 */
    protected List<SAML11AssertionType> assertions = new ArrayList<>();

    /** 响应处理状态。 */
    protected SAML11StatusType status;

    /**
     * 构造 SAML 1.1 响应。
     *
     * @param id 响应 ID
     * @param issueInstant 签发时间
     */
    public SAML11ResponseType(String id, XMLGregorianCalendar issueInstant) {
        super(id, issueInstant);
    }

    /** 添加断言。 */
    public void add(SAML11AssertionType assertion) {
        this.assertions.add(assertion);
    }

    /** 移除断言。 */
    public boolean remove(SAML11AssertionType assertion) {
        return this.assertions.remove(assertion);
    }

    /** 返回不可修改的断言列表。 */
    public List<SAML11AssertionType> get() {
        return Collections.unmodifiableList(assertions);
    }

    /** 返回处理状态。 */
    public SAML11StatusType getStatus() {
        return status;
    }

    /** 设置处理状态。 */
    public void setStatus(SAML11StatusType status) {
        this.status = status;
    }
}
