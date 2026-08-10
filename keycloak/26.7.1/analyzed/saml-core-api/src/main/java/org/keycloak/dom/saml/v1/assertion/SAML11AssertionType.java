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
import javax.xml.datatype.XMLGregorianCalendar;

import org.keycloak.dom.saml.common.CommonAssertionType;

import org.w3c.dom.Element;

/**
 * SAML 1.1 断言类型，包含版本号、条件、Advice、各类 Statement、签名与签发者。
 *
 * <complexType name="AssertionType"> <sequence> <element ref="saml:Conditions" minOccurs="0"/> <element
 * ref="saml:Advice"
 * minOccurs="0"/> <choice maxOccurs="unbounded"> <element ref="saml:Statement"/> <element
 * ref="saml:SubjectStatement"/>
 * <element ref="saml:AuthenticationStatement"/> <element ref="saml:AuthorizationDecisionStatement"/> <element
 * ref="saml:AttributeStatement"/> </choice>
 *
 * <element ref="ds:Signature" minOccurs="0"/> </sequence> <attribute name="MajorVersion" type="integer"
 * use="required"/>
 * <attribute name="MinorVersion" type="integer" use="required"/> <attribute name="AssertionID" type="ID"
 * use="required"/>
 * <attribute name="Issuer" type="string" use="required"/> <attribute name="IssueInstant" type="dateTime"
 * use="required"/>
 * </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 21, 2011
 */
public class SAML11AssertionType extends CommonAssertionType {

    protected int majorVersion = 1;

    protected int minorVersion = 1;

    protected SAML11ConditionsType conditions;

    protected SAML11AdviceType advice;

    protected List<SAML11StatementAbstractType> statements = new ArrayList<>();

    protected Element signature;

    protected String issuer;

    /**
     * 构造 SAML 1.1 断言。
     *
     * @param iD 断言 ID
     * @param issueInstant 签发时间
     */
    public SAML11AssertionType(String iD, XMLGregorianCalendar issueInstant) {
        super(iD, issueInstant);
    }

    /** 获取主版本号（SAML 1.1 默认为 1）。 */
    public int getMajorVersion() {
        return majorVersion;
    }

    /** 获取次版本号（SAML 1.1 默认为 1）。 */
    public int getMinorVersion() {
        return minorVersion;
    }

    /** 添加一条 Statement。 */
    public void add(SAML11StatementAbstractType statement) {
        this.statements.add(statement);
    }

    /** 批量添加 Statement。 */
    public void addAllStatements(List<SAML11StatementAbstractType> statement) {
        this.statements.addAll(statement);
    }

    /** 移除一条 Statement。 */
    public boolean remove(SAML11StatementAbstractType statement) {
        return this.statements.remove(statement);
    }

    /** 获取 Statement 列表（只读）。 */
    public List<SAML11StatementAbstractType> getStatements() {
        return Collections.unmodifiableList(statements);
    }

    /** 获取断言条件。 */
    public SAML11ConditionsType getConditions() {
        return conditions;
    }

    /** 设置断言条件。 */
    public void setConditions(SAML11ConditionsType conditions) {
        this.conditions = conditions;
    }

    /** 获取 Advice 元素。 */
    public SAML11AdviceType getAdvice() {
        return advice;
    }

    /** 设置 Advice 元素。 */
    public void setAdvice(SAML11AdviceType advice) {
        this.advice = advice;
    }

    /** 获取 XML 数字签名元素。 */
    public Element getSignature() {
        return signature;
    }

    /** 设置 XML 数字签名元素。 */
    public void setSignature(Element signature) {
        this.signature = signature;
    }

    /** 获取断言签发者标识。 */
    public String getIssuer() {
        return issuer;
    }

    /** 设置断言签发者标识。 */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}