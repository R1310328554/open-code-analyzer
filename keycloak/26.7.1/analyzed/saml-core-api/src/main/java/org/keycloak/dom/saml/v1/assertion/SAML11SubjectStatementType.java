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

/**
 * <complexType name="SubjectStatementAbstractType"
 * SAML 1.1 含主体语句抽象基类：所有须绑定 {@link SAML11SubjectType} 的语句类型之父类。
 abstract="true"> <complexContent> <extension
 * base="saml:StatementAbstractType"> <sequence> <element ref="saml:Subject"/> </sequence>
 *
 * </extension> </complexContent> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11SubjectStatementType extends SAML11StatementAbstractType {

    /** 语句所描述的主体。 */
    protected SAML11SubjectType subject;

    /** 无参构造。 */
    public SAML11SubjectStatementType() {
    }

    /** 以给定主体构造语句。 */
    public SAML11SubjectStatementType(SAML11SubjectType subject) {
        this.subject = subject;
    }

    /** 返回关联主体。 */
    public SAML11SubjectType getSubject() {
        return subject;
    }

    /** 设置关联主体。 */
    public void setSubject(SAML11SubjectType subject) {
        this.subject = subject;
    }
}