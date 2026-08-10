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
package org.keycloak.saml.processing.core.saml.v2.holders;

import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;

/**
 * 创建 SAML 消息所需的 IdP（身份提供者）上下文信息容器。
 * <p>包含主体确认方式、NameID 格式及断言有效期等默认配置。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 10, 2008
 */
public class IDPInfoHolder {

    /** 主体确认方法 URI，默认为 Bearer。 */
    private String subjectConfirmationMethod = JBossSAMLURIConstants.SUBJECT_CONFIRMATION_BEARER.get();
    /** NameID 格式 URI，默认为 Transient。 */
    private String nameIDFormat = JBossSAMLURIConstants.NAMEID_FORMAT_TRANSIENT.get();
    /** NameID 的实际值。 */
    private String nameIDFormatValue;

    /** 关联的 SAML 断言对象。 */
    private AssertionType assertion;

    /** 断言有效时长（分钟），默认 5 分钟。 */
    private int assertionValidityDuration = 5; // 5 Minutes

    /** 返回断言有效时长（分钟）。 */
    public int getAssertionValidityDuration() {
        return assertionValidityDuration;
    }

    /** 设置断言有效时长（分钟）。 */
    public void setAssertionValidityDuration(int assertionValidityDuration) {
        this.assertionValidityDuration = assertionValidityDuration;
    }

    /** 返回主体确认方法 URI。 */
    public String getSubjectConfirmationMethod() {
        return subjectConfirmationMethod;
    }

    /** 设置主体确认方法 URI。 */
    public void setSubjectConfirmationMethod(String subjectConfirmationMethod) {
        this.subjectConfirmationMethod = subjectConfirmationMethod;
    }

    /** 返回 NameID 格式 URI。 */
    public String getNameIDFormat() {
        return nameIDFormat;
    }

    /** 设置 NameID 格式 URI。 */
    public void setNameIDFormat(String nameIDFormat) {
        this.nameIDFormat = nameIDFormat;
    }

    /** 返回 NameID 值。 */
    public String getNameIDFormatValue() {
        return nameIDFormatValue;
    }

    /** 设置 NameID 值。 */
    public void setNameIDFormatValue(String nameIDFormatValue) {
        this.nameIDFormatValue = nameIDFormatValue;
    }

    /** 返回关联的断言对象。 */
    public AssertionType getAssertion() {
        return assertion;
    }

    /** 设置关联的断言对象。 */
    public void setAssertion(AssertionType assertion) {
        this.assertion = assertion;
    }
}