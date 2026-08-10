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
import javax.xml.namespace.QName;

import org.keycloak.dom.saml.common.CommonRequestAbstractType;

/**
 * SAML 1.1 请求抽象基类：包含版本号、RespondWith 列表及请求公共属性。
 *
 * <complexType name="RequestAbstractType" abstract="true">
 *
 * <sequence> <element ref="samlp:RespondWith" minOccurs="0" maxOccurs="unbounded"/> <element ref="ds:Signature"
 * minOccurs="0"/>
 * </sequence> <attribute name="RequestID" type="ID" use="required"/> <attribute name="MajorVersion" type="integer"
 * use="required"/> <attribute name="MinorVersion" type="integer" use="required"/> <attribute name="IssueInstant"
 * type="dateTime" use="required"/> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public abstract class SAML11RequestAbstractType extends CommonRequestAbstractType {

    /** 主版本号（SAML 1.1 默认为 1）。 */
    protected int majorVersion = 1;

    /** 次版本号（SAML 1.1 默认为 1）。 */
    protected int minorVersion = 1;

    /** 期望响应元素类型的 QName 列表。 */
    protected List<QName> respondWith = new ArrayList<>();

    /**
     * 构造 SAML 1.1 请求基类。
     *
     * @param id 请求 ID
     * @param issueInstant 签发时间
     */
    public SAML11RequestAbstractType(String id, XMLGregorianCalendar issueInstant) {
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

    /** 添加 RespondWith QName。 */
    public void add(QName rw) {
        this.respondWith.add(rw);
    }

    /** 批量添加 RespondWith QName。 */
    public void addAllConditions(List<QName> rw) {
        this.respondWith.addAll(rw);
    }

    /** 移除 RespondWith QName。 */
    public boolean remove(QName rw) {
        return this.respondWith.remove(rw);
    }

    /** 返回不可修改的 RespondWith 列表。 */
    public List<QName> getRespondWith() {
        return Collections.unmodifiableList(respondWith);
    }
}
