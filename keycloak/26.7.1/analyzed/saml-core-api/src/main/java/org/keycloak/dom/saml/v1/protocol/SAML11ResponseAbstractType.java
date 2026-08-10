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

import java.net.URI;
import javax.xml.datatype.XMLGregorianCalendar;

import org.keycloak.dom.saml.common.CommonResponseType;

/**
 * SAML 1.1 响应抽象基类：包含版本号、可选收件人及响应公共属性。
 *
 * <complexType name="ResponseAbstractType" abstract="true"> <sequence>
 *
 * <element ref="ds:Signature" minOccurs="0"/> </sequence> <attribute name="ResponseID" type="ID" use="required"/>
 * <attribute
 * name="InResponseTo" type="NCName" use="optional"/> <attribute name="MajorVersion" type="integer" use="required"/>
 * <attribute
 * name="MinorVersion" type="integer" use="required"/> <attribute name="IssueInstant" type="dateTime" use="required"/>
 * <attribute name="Recipient" type="anyURI" use="optional"/> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public abstract class SAML11ResponseAbstractType extends CommonResponseType {

    /** 主版本号（SAML 1.1 默认为 1）。 */
    protected int majorVersion = 1;

    /** 次版本号（SAML 1.1 默认为 1）。 */
    protected int minorVersion = 1;

    /** 可选的响应收件人 URI。 */
    protected URI recipient;

    /**
     * 构造 SAML 1.1 响应基类。
     *
     * @param id 响应 ID
     * @param issueInstant 签发时间
     */
    public SAML11ResponseAbstractType(String id, XMLGregorianCalendar issueInstant) {
        super(id, issueInstant);
    }

    /** 返回主版本号。 */
    public int getMajorVersion() {
        return majorVersion;
    }

    /** 返回次版本号。 */
    public int getMinorVersion() {
        return minorVersion;
    }

    /** 返回收件人 URI。 */
    public URI getRecipient() {
        return recipient;
    }

    /** 设置收件人 URI。 */
    public void setRecipient(URI recipient) {
        this.recipient = recipient;
    }
}
