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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.EncryptedAssertionType;

/**
 * <p>
 * Java class for ResponseType complex type.
 * SAML 2.0 响应消息，在 {@link StatusResponseType} 基础上携带一个或多个断言。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ResponseType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}StatusResponseType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Assertion"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}EncryptedAssertion"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class ResponseType extends StatusResponseType {

    /** 断言列表（明文或加密断言的二选一封装）。 */
    protected List<RTChoiceType> assertions = new ArrayList<>();

    /**
     * 构造 SAML 响应。
     *
     * @param id 响应标识符
     * @param issueInstant 签发时间
     */
    public ResponseType(String id, XMLGregorianCalendar issueInstant) {
        super(id, issueInstant);
    }

    /**
     * 从已有状态响应复制构造。
     *
     * @param srt 源状态响应
     */
    public ResponseType(StatusResponseType srt) {
        super(srt);
    }

    /**
     * 添加断言。
     *
     * Add an assertion
     *
     * @param choice
     */
    public void addAssertion(RTChoiceType choice) {
        assertions.add(choice);
    }

    /**
     * 移除断言。
     *
     * Remove an assertion
     *
     * @param choice
     */
    public void removeAssertion(RTChoiceType choice) {
        assertions.remove(choice);
    }

    /**
     * 按 ID 替换第一个匹配的断言。
     *
     * Replace the first assertion with the passed assertion
     *
     * @param id id of the old assertion
     * @param newAssertion
     */
    public void replaceAssertion(String id, RTChoiceType newAssertion) {
        int index = 0;
        if (id != null && !id.isEmpty()) {
            for (RTChoiceType assertion : assertions) {
                if (assertion.getID().equals(id)) {
                    break;
                }
                index++;
            }
        }
        assertions.remove(index);
        assertions.add(index, newAssertion);
    }

    /**
     * 获取断言只读列表。
     *
     * Gets a read only list of assertions
     */
    public List<RTChoiceType> getAssertions() {
        return Collections.unmodifiableList(assertions);
    }

    /**
     * 断言选择封装：明文 {@link AssertionType} 或 {@link EncryptedAssertionType} 二选一。
     */
    public static class RTChoiceType {

        /** 明文断言。 */
        private AssertionType assertion;

        /** 加密断言。 */
        private EncryptedAssertionType encryptedAssertion;

        /** 断言标识符。 */
        private String id;

        /**
         * 以明文断言构造选择项。
         *
         * @param assertion 断言对象
         */
        public RTChoiceType(AssertionType assertion) {
            this.assertion = assertion;
            this.id = assertion.getID();
        }

        /**
         * 以加密断言构造选择项。
         *
         * @param encryptedAssertion 加密断言对象
         */
        public RTChoiceType(EncryptedAssertionType encryptedAssertion) {
            this.encryptedAssertion = encryptedAssertion;

        }

        /** 获取明文断言。 */
        public AssertionType getAssertion() {
            return assertion;
        }

        /** 获取加密断言。 */
        public EncryptedAssertionType getEncryptedAssertion() {
            return encryptedAssertion;
        }

        /** 获取断言 ID。 */
        public String getID() {
            return id;
        }
    }
}
