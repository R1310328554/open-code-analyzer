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
 * Java class for AlphabetType complex type.
 * SAML 2.0 认证电话类字符集类型：定义 PIN/口令的必选字符、排除字符及大小写规则。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AlphabetType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="requiredChars" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="excludedChars" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="case" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class AlphabetType {

    protected String requiredChars;
    protected String excludedChars;
    protected String _case;

    /**
     * 获取 必选字符 属性的值。
     *
     * Gets the value of the requiredChars property.
     *
     * @return possible object is {@link String }
     */
    public String getRequiredChars() {
        return requiredChars;
    }

    /**
     * 设置 必选字符 属性的值。
     *
     * Sets the value of the requiredChars property.
     *
     * @param value allowed object is {@link String }
     */
    public void setRequiredChars(String value) {
        this.requiredChars = value;
    }

    /**
     * 获取 排除字符 属性的值。
     *
     * Gets the value of the excludedChars property.
     *
     * @return possible object is {@link String }
     */
    public String getExcludedChars() {
        return excludedChars;
    }

    /**
     * 设置 排除字符 属性的值。
     *
     * Sets the value of the excludedChars property.
     *
     * @param value allowed object is {@link String }
     */
    public void setExcludedChars(String value) {
        this.excludedChars = value;
    }

    /**
     * 获取 大小写规则 属性的值。
     *
     * Gets the value of the case property.
     *
     * @return possible object is {@link String }
     */
    public String getCase() {
        return _case;
    }

    /**
     * 设置 大小写规则 属性的值。
     *
     * Sets the value of the case property.
     *
     * @param value allowed object is {@link String }
     */
    public void setCase(String value) {
        this._case = value;
    }

}
