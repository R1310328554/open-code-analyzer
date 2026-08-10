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
package org.keycloak.dom.saml.v2.assertion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for SubjectType complex type.
 * SAML 2.0 断言主体：可含 BaseID/NameID/EncryptedID 及零个或多个 SubjectConfirmation。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SubjectType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;sequence>
 *           &lt;choice>
 *             &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}BaseID"/>
 *             &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}NameID"/>
 *             &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}EncryptedID"/>
 *           &lt;/choice>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}SubjectConfirmation" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}SubjectConfirmation" maxOccurs="unbounded"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class SubjectType implements Serializable {

    protected List<SubjectConfirmationType> subjectConfirmation = new ArrayList<>();

    protected STSubType subType;

    /**
     * 获取主体子类型（含标识符与确认列表）。
     *
     * Get the {@link STSubType}
     *
     * @return
     */
    public STSubType getSubType() {
        return subType;
    }

    /**
     * 设置主体子类型。
     *
     * Set the {@link STSubType}
     *
     * @param subType
     */
    public void setSubType(STSubType subType) {
        this.subType = subType;
    }

    /**
     * 获取主体确认条目数量。
     *
     * Get the size of subject confirmations
     *
     * @return
     */
    public int getCount() {
        return subjectConfirmation.size();
    }

    /**
     * 获取主体确认列表（只读）。
     *
     * Get a list of subject confirmations
     *
     * @return {@link} read only list of subject confirmation
     */
    public List<SubjectConfirmationType> getConfirmation() {
        return Collections.unmodifiableList(subjectConfirmation);
    }

    /**
     * 添加一条主体确认。
     *
     * Add a subject confirmation
     *
     * @param con
     */
    public void addConfirmation(SubjectConfirmationType con) {
        subjectConfirmation.add(con);
    }

    /**
     * 移除一条主体确认。
     *
     * Remove a subject confirmation
     *
     * @param con
     */
    public void removeConfirmation(SubjectConfirmationType con) {
        subjectConfirmation.remove(con);
    }

    /** 主体子类型：承载 BaseID/EncryptedID 及关联的 SubjectConfirmation 列表。 */
    public static class STSubType implements Serializable {

        private BaseIDAbstractType baseID;

        private EncryptedElementType encryptedID;

        protected List<SubjectConfirmationType> subjectConfirmation = new ArrayList<SubjectConfirmationType>();

        /** 设置 BaseID 标识符。 */
        public void addBaseID(BaseIDAbstractType base) {
            this.baseID = base;
        }

        /** 获取 BaseID 标识符。 */
        public BaseIDAbstractType getBaseID() {
            return baseID;
        }

        /** 获取加密标识符。 */
        public EncryptedElementType getEncryptedID() {
            return encryptedID;
        }

        /** 设置加密标识符。 */
        public void setEncryptedID(EncryptedElementType encryptedID) {
            this.encryptedID = encryptedID;
        }

        /** 添加一条主体确认。 */
        public void addConfirmation(SubjectConfirmationType con) {
            subjectConfirmation.add(con);
        }

        /** 获取主体确认数量。 */
        public int getCount() {
            return subjectConfirmation.size();
        }

        /** 获取主体确认列表（只读）。 */
        public List<SubjectConfirmationType> getConfirmation() {
            return Collections.unmodifiableList(subjectConfirmation);
        }
    }
}