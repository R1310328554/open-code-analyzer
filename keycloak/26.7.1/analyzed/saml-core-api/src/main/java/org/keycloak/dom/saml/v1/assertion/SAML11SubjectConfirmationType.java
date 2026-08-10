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

import org.w3c.dom.Element;

/**
 * <complexType name="SubjectConfirmationType">
 * SAML 1.1 主体确认（SubjectConfirmation）DOM 类型：描述如何确认断言主体身份（确认方法、数据与可选密钥信息）。
 <sequence> <element ref="saml:ConfirmationMethod"
 * maxOccurs="unbounded"/>
 * <element ref="saml:SubjectConfirmationData" minOccurs="0"/>
 *
 * <element ref="ds:KeyInfo" minOccurs="0"/> </sequence> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11SubjectConfirmationType {

    /** 主体确认方法 URI 列表。 */
    protected List<URI> confirmationMethod = new ArrayList<>();

    /** 主体确认附加数据（类型由确认方法决定）。 */
    protected Object subjectConfirmationData;

    /** 可选 XML 签名 KeyInfo 元素。 */
    protected Element keyInfo;

    /** 添加一条确认方法 URI。 */
    public void addConfirmationMethod(URI confirmation) {
        this.confirmationMethod.add(confirmation);
    }

    /** 批量添加确认方法 URI。 */
    public void addAllConfirmationMethod(List<URI> confirmation) {
        this.confirmationMethod.addAll(confirmation);
    }

    /** 移除指定确认方法 URI。 */
    public boolean removeConfirmationMethod(URI confirmation) {
        return this.confirmationMethod.remove(confirmation);
    }

    /** 返回不可修改的确认方法列表。 */
    public List<URI> getConfirmationMethod() {
        return Collections.unmodifiableList(confirmationMethod);
    }

    /** 设置主体确认数据。 */
    public void setSubjectConfirmationData(Object subjectConfirmation) {
        this.subjectConfirmationData = subjectConfirmation;
    }

    /** 返回 KeyInfo 元素。 */
    public Element getKeyInfo() {
        return keyInfo;
    }

    /** 设置 KeyInfo 元素。 */
    public void setKeyInfo(Element keyInfo) {
        this.keyInfo = keyInfo;
    }

    /** 返回主体确认数据。 */
    public Object getSubjectConfirmationData() {
        return subjectConfirmationData;
    }
}