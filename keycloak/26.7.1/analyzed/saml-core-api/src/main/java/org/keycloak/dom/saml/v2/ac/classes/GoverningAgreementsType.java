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

package org.keycloak.dom.saml.v2.ac.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for GoverningAgreementsType complex type.
 * SAML 2.0 治理协议集合类型：包含一个或多个 {@link GoverningAgreementRefType} 引用。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GoverningAgreementsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}GoverningAgreementRef"
 * maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class GoverningAgreementsType {

    protected List<GoverningAgreementRefType> governingAgreementRef = new ArrayList<>();

    /** 添加治理协议引用。 */
    public void add(GoverningAgreementRefType gov) {
        this.governingAgreementRef.add(gov);
    }

    /** 移除治理协议引用。 */
    public void remove(GoverningAgreementRefType gov) {
        this.governingAgreementRef.remove(gov);
    }

    /**
     * 获取治理协议引用列表。
     *
     * Gets the value of the governingAgreementRef property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link GoverningAgreementRefType }
     */
    public List<GoverningAgreementRefType> getGoverningAgreementRef() {
        return Collections.unmodifiableList(this.governingAgreementRef);
    }

}
