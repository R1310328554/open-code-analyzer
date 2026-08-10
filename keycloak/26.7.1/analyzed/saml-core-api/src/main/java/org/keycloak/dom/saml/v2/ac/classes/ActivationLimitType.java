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

/**
 * <p>
 * Java class for ActivationLimitType complex type.
 * SAML 2.0 认证电话类激活限制类型：在时长、使用次数或会话维度约束凭证激活。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ActivationLimitType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}ActivationLimitDuration"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}ActivationLimitUsages"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}ActivationLimitSession"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class ActivationLimitType {

    protected ActivationLimitDurationType activationLimitDuration;
    protected ActivationLimitUsagesType activationLimitUsages;
    protected ActivationLimitSessionType activationLimitSession;

    /**
     * 获取 激活时限（时长） 属性的值。
     *
     * Gets the value of the activationLimitDuration property.
     *
     * @return possible object is {@link ActivationLimitDurationType }
     */
    public ActivationLimitDurationType getActivationLimitDuration() {
        return activationLimitDuration;
    }

    /**
     * 设置 激活时限（时长） 属性的值。
     *
     * Sets the value of the activationLimitDuration property.
     *
     * @param value allowed object is {@link ActivationLimitDurationType }
     */
    public void setActivationLimitDuration(ActivationLimitDurationType value) {
        this.activationLimitDuration = value;
    }

    /**
     * 获取 激活使用次数上限 属性的值。
     *
     * Gets the value of the activationLimitUsages property.
     *
     * @return possible object is {@link ActivationLimitUsagesType }
     */
    public ActivationLimitUsagesType getActivationLimitUsages() {
        return activationLimitUsages;
    }

    /**
     * 设置 激活使用次数上限 属性的值。
     *
     * Sets the value of the activationLimitUsages property.
     *
     * @param value allowed object is {@link ActivationLimitUsagesType }
     */
    public void setActivationLimitUsages(ActivationLimitUsagesType value) {
        this.activationLimitUsages = value;
    }

    /**
     * 获取 激活会话限制 属性的值。
     *
     * Gets the value of the activationLimitSession property.
     *
     * @return possible object is {@link ActivationLimitSessionType }
     */
    public ActivationLimitSessionType getActivationLimitSession() {
        return activationLimitSession;
    }

    /**
     * 设置 激活会话限制 属性的值。
     *
     * Sets the value of the activationLimitSession property.
     *
     * @param value allowed object is {@link ActivationLimitSessionType }
     */
    public void setActivationLimitSession(ActivationLimitSessionType value) {
        this.activationLimitSession = value;
    }

}
