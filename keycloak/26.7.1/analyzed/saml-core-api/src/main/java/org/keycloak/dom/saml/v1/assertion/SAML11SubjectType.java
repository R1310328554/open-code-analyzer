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

/**
 * SAML 1.1 主体（Subject）类型：标识断言所描述的主体，可包含名称标识符与/或主体确认信息。
 *
 * <complexType name="SubjectType"> <choice> <sequence> <element ref="saml:NameIdentifier"/> <element
 * ref="saml:SubjectConfirmation" minOccurs="0"/>
 *
 * </sequence> <element ref="saml:SubjectConfirmation"/> </choice> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11SubjectType {

    /** SubjectType 的 choice 分支：名称标识符或主体确认。 */
    public static class SAML11SubjectTypeChoice {

        protected SAML11NameIdentifierType nameID;

        protected SAML11SubjectConfirmationType subjectConfirmation;

        /** 以名称标识符构造 choice 分支。 */
        public SAML11SubjectTypeChoice(SAML11NameIdentifierType nameID) {
            this.nameID = nameID;
        }

        /** 以主体确认构造 choice 分支。 */
        public SAML11SubjectTypeChoice(SAML11SubjectConfirmationType subConfirms) {
            this.subjectConfirmation = subConfirms;
        }

        /** 返回名称标识符。 */
        public SAML11NameIdentifierType getNameID() {
            return nameID;
        }

        /** 返回主体确认。 */
        public SAML11SubjectConfirmationType getSubjectConfirmation() {
            return subjectConfirmation;
        }
    }

    /** 可选的主体确认（与 choice 分支互斥或并存，取决于序列化形态）。 */
    protected SAML11SubjectConfirmationType subjectConfirmation;

    /** choice 分支：名称标识符序列或单独的主体确认。 */
    protected SAML11SubjectTypeChoice choice;

    /** 返回主体确认。 */
    public SAML11SubjectConfirmationType getSubjectConfirmation() {
        return subjectConfirmation;
    }

    /** 设置主体确认。 */
    public void setSubjectConfirmation(SAML11SubjectConfirmationType subjectConfirmation) {
        this.subjectConfirmation = subjectConfirmation;
    }

    /** 返回 choice 分支。 */
    public SAML11SubjectTypeChoice getChoice() {
        return choice;
    }

    /** 设置 choice 分支。 */
    public void setChoice(SAML11SubjectTypeChoice choice) {
        this.choice = choice;
    }
}
