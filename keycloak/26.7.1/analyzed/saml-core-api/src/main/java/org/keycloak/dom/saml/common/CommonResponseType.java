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
package org.keycloak.dom.saml.common;

import java.io.Serializable;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Element;

/**
 * SAML 响应类型，包含响应 ID、签发时间、关联请求 ID 与 XML 数字签名。
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class CommonResponseType implements Serializable {

    protected String id;

    protected XMLGregorianCalendar issueInstant;

    protected String inResponseTo;

    protected Element signature;

    /**
     * 构造 SAML 响应。
     *
     * @param id 响应 ID
     * @param issueInstant 签发时间
     */
    public CommonResponseType(String id, XMLGregorianCalendar issueInstant) {
        this.id = id;
        this.issueInstant = issueInstant;
    }

    /**
     * 获取响应 ID。
     *
     * @return 可能的值为 {@link String }
     */
    public String getID() {
        return id;
    }

    /**
     * 获取响应签发时间。
     *
     * @return 可能的值为 {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getIssueInstant() {
        return issueInstant;
    }

    /**
     * 获取所响应的请求 ID（InResponseTo）。
     *
     * @return 可能的值为 {@link String }
     */
    public String getInResponseTo() {
        return inResponseTo;
    }

    /**
     * 设置所响应的请求 ID（InResponseTo）。
     *
     * @param value 允许的值为 {@link String }
     */
    public void setInResponseTo(String value) {
        this.inResponseTo = value;
    }

    /**
     * 获取 XML 数字签名元素。
     *
     * @return 可能的值为 {@link org.keycloak.dom.xmlsec.w3.xmldsig.SignatureType }
     */
    public Element getSignature() {
        return signature;
    }

    /**
     * 设置 XML 数字签名元素。
     *
     * @param value 允许的值为 {@link org.keycloak.dom.xmlsec.w3.xmldsig.SignatureType }
     */
    public void setSignature(Element value) {
        this.signature = value;
    }
}