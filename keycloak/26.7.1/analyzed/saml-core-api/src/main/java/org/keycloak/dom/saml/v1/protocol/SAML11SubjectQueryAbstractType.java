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

import org.keycloak.dom.saml.v1.assertion.SAML11SubjectType;

/**
 * SAML 1.1 主体查询（SubjectQuery）抽象基类：所有以主体为目标的查询的公共父类型。
 *
 * <complexType name="SubjectQueryAbstractType" abstract="true"> <complexContent> <extension
 * base="samlp:QueryAbstractType">
 * <sequence> <element ref="saml:Subject"/> </sequence>
 *
 * </extension> </complexContent> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11SubjectQueryAbstractType extends SAML11QueryAbstractType {

    /** 查询目标主体。 */
    protected SAML11SubjectType subject;

    /** 返回查询主体。 */
    public SAML11SubjectType getSubject() {
        return subject;
    }

    /** 设置查询主体。 */
    public void setSubject(SAML11SubjectType subject) {
        this.subject = subject;
    }
}
