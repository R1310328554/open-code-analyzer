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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * SAML 1.1 认证声明，记录认证方法、认证时间、主体本地性及权威绑定信息。
 *
 * <complexType name="AuthenticationStatementType"> <complexContent> <extension base="saml:SubjectStatementAbstractType">
 *
 * <sequence> <element ref="saml:SubjectLocality" minOccurs="0"/> <element ref="saml:AuthorityBinding" minOccurs="0"
 * maxOccurs="unbounded"/> </sequence> <attribute name="AuthenticationMethod" type="anyURI" use="required"/> <attribute
 * name="AuthenticationInstant" type="dateTime" use="required"/> </extension> </complexContent> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11AuthenticationStatementType extends SAML11SubjectStatementType {

    protected URI authenticationMethod;

    protected XMLGregorianCalendar authenticationInstant;

    protected SAML11SubjectLocalityType subjectLocality;

    protected List<SAML11AuthorityBindingType> authorityBinding = new ArrayList<>();

    /**
     * 构造认证声明。
     *
     * @param authenticationMethod 认证方法 URI
     * @param authenticationInstant 认证发生时间
     */
    public SAML11AuthenticationStatementType(URI authenticationMethod, XMLGregorianCalendar authenticationInstant) {
        this.authenticationMethod = authenticationMethod;
        this.authenticationInstant = authenticationInstant;
    }

    /** 获取认证方法 URI。 */
    public URI getAuthenticationMethod() {
        return authenticationMethod;
    }

    /** 获取认证发生时间。 */
    public XMLGregorianCalendar getAuthenticationInstant() {
        return authenticationInstant;
    }

    /** 获取主体本地性（如 IP、DNS 名）。 */
    public SAML11SubjectLocalityType getSubjectLocality() {
        return subjectLocality;
    }

    /** 设置主体本地性。 */
    public void setSubjectLocality(SAML11SubjectLocalityType subjectLocality) {
        this.subjectLocality = subjectLocality;
    }

    /** 添加一条权威绑定。 */
    public void add(SAML11AuthorityBindingType advice) {
        this.authorityBinding.add(advice);
    }

    /** 批量添加权威绑定。 */
    public void addAllAuthorityBindingType(List<SAML11AuthorityBindingType> advice) {
        this.authorityBinding.addAll(advice);
    }

    /** 移除一条权威绑定。 */
    public boolean remove(SAML11AuthorityBindingType advice) {
        return this.authorityBinding.remove(advice);
    }

    /** 获取权威绑定列表（只读）。 */
    public List<SAML11AuthorityBindingType> getAuthorityBindingType() {
        return Collections.unmodifiableList(authorityBinding);
    }
}