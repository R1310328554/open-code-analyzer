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

/**
 * SAML 1.1 请求（Request）类型：可携带查询、断言 ID 引用或断言工件（Artifact）。
 *
 * <complexType name="RequestType"> <complexContent> <extension base="samlp:RequestAbstractType"> <choice> <element
 * ref="samlp:Query"/> <element ref="samlp:SubjectQuery"/> <element ref="samlp:AuthenticationQuery"/>
 *
 * <element ref="samlp:AttributeQuery"/> <element ref="samlp:AuthorizationDecisionQuery"/> <element
 * ref="saml:AssertionIDReference" maxOccurs="unbounded"/> <element ref="samlp:AssertionArtifact"
 * maxOccurs="unbounded"/>
 * </choice> </extension> </complexContent> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11RequestType extends SAML11RequestAbstractType {

    /** 请求内嵌的查询对象（choice 分支之一）。 */
    protected SAML11QueryAbstractType query;

    /** 断言 ID 引用列表。 */
    protected List<String> assertionIDRef = new ArrayList<>();

    /** 断言工件（Artifact）列表。 */
    protected List<String> assertionArtifact = new ArrayList<>();

    /**
     * 构造 SAML 1.1 请求。
     *
     * @param id 请求 ID
     * @param issueInstant 签发时间
     */
    public SAML11RequestType(String id, XMLGregorianCalendar issueInstant) {
        super(id, issueInstant);
    }

    /** 添加断言 ID 引用。 */
    public void addAssertionIDRef(String sadt) {
        this.assertionIDRef.add(sadt);
    }

    /** 移除断言 ID 引用。 */
    public boolean removeAssertionIDRef(String sadt) {
        return this.assertionIDRef.remove(sadt);
    }

    /** 返回不可修改的断言 ID 引用列表。 */
    public List<String> getAssertionIDRef() {
        return Collections.unmodifiableList(assertionIDRef);
    }

    /** 添加断言工件。 */
    public void addAssertionArtifact(String sadt) {
        this.assertionArtifact.add(sadt);
    }

    /** 移除断言工件。 */
    public boolean removeAssertionArtifact(String sadt) {
        return this.assertionArtifact.remove(sadt);
    }

    /** 返回不可修改的断言工件列表。 */
    public List<String> getAssertionArtifact() {
        return Collections.unmodifiableList(assertionArtifact);
    }

    /** 返回内嵌查询。 */
    public SAML11QueryAbstractType getQuery() {
        return query;
    }

    /** 设置内嵌查询。 */
    public void setQuery(SAML11QueryAbstractType query) {
        this.query = query;
    }
}
