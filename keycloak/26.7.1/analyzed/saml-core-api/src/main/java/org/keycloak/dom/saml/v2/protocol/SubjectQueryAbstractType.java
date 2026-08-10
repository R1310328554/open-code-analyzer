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

import javax.xml.datatype.XMLGregorianCalendar;

import org.keycloak.dom.saml.v2.assertion.SubjectType;

/**
 * <p>
 * Java class for SubjectQueryAbstractType complex type.
 * SAML 2.0 主体查询抽象基类，扩展 {@link RequestAbstractType} 并携带 {@link SubjectType}。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SubjectQueryAbstractType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}RequestAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Subject"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public abstract class SubjectQueryAbstractType extends RequestAbstractType {

    /** 查询目标主体（Subject）。 */
    protected SubjectType subject;

    /**
     * 构造主体查询请求。
     *
     * @param id 请求标识符
     * @param instant 请求时间
     */
    public SubjectQueryAbstractType(String id, XMLGregorianCalendar instant) {
        super(id, instant);
    }

    /** 设置查询目标主体。 */
    public void setSubject(SubjectType subject) {
        this.subject = subject;
    }

    /**
     * 获取主体（Subject）属性的值。
     *
     * Gets the value of the subject property.
     *
     * @return possible object is {@link SubjectType }
     */
    public SubjectType getSubject() {
        return subject;
    }
}