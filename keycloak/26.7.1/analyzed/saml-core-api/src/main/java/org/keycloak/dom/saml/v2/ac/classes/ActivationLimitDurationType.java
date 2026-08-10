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

import javax.xml.datatype.Duration;

/**
 * SAML 2.0 认证电话类激活时限类型：以 XML Duration 限定激活凭证的有效时长。
 *
 * <p>
 * Java class for ActivationLimitDurationType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ActivationLimitDurationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="duration" use="required" type="{http://www.w3.org/2001/XMLSchema}duration" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class ActivationLimitDurationType {

    /** 激活有效时长。 */
    protected Duration duration;

    /**
     * 构造激活时限。
     *
     * @param theDuration 有效时长
     */
    public ActivationLimitDurationType(Duration theDuration) {
        this.duration = theDuration;
    }

    /**
     * 获取 duration 属性值。
     *
     * Gets the value of the duration property.
     *
     * @return possible object is {@link Duration }
     */
    public Duration getDuration() {
        return duration;
    }

}