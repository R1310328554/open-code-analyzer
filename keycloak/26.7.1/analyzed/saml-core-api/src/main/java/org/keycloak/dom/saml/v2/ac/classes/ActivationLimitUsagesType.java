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

import java.math.BigInteger;

/**
 * <p>
 * Java class for ActivationLimitUsagesType complex type.
 * SAML 2.0 认证电话类激活使用次数限制类型：以整数属性 number 指定允许的最大使用次数。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ActivationLimitUsagesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="number" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */

public class ActivationLimitUsagesType {

    /** 允许使用次数。 */
    protected BigInteger number;

    /**
     * 构造激活使用次数限制。
     *
     * @param theNumber 允许的最大使用次数
     */
    public ActivationLimitUsagesType(BigInteger theNumber) {
        this.number = theNumber;
    }

    /**
     * 获取 number 属性值。
     *
     * Gets the value of the number property.
     *
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getNumber() {
        return number;
    }

}