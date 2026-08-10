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

package org.keycloak.dom.saml.v2.profiles.attribute.dce;

/**
 * <p>
 * Java class for DCEValueType complex type.
 * SAML 2.0 DCE 属性值类型：含 Realm 与 FriendlyName 的 URI 值。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="DCEValueType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anyURI">
 *       &lt;attribute ref="{urn:oasis:names:tc:SAML:2.0:profiles:attribute:DCE}Realm"/>
 *       &lt;attribute ref="{urn:oasis:names:tc:SAML:2.0:profiles:attribute:DCE}FriendlyName"/>
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 */
public class DCEValueType {

    /** 属性值 URI。 */
    protected String value;
    /** DCE 域（Realm）。 */
    protected String realm;
    /** 友好名称。 */
    protected String friendlyName;

    /**
     * 获取 value 属性的值。
     *
     * Gets the value of the value property.
     *
     * @return possible object is {@link String }
     */
    public String getValue() {
        return value;
    }

    /**
     * 设置 value 属性的值。
     *
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link String }
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 获取 realm 属性的值。
     *
     * Gets the value of the realm property.
     *
     * @return possible object is {@link String }
     */
    public String getRealm() {
        return realm;
    }

    /**
     * 设置 realm 属性的值。
     *
     * Sets the value of the realm property.
     *
     * @param value allowed object is {@link String }
     */
    public void setRealm(String value) {
        this.realm = value;
    }

    /**
     * 获取 friendlyName 属性的值。
     *
     * Gets the value of the friendlyName property.
     *
     * @return possible object is {@link String }
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * 设置 friendlyName 属性的值。
     *
     * Sets the value of the friendlyName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setFriendlyName(String value) {
        this.friendlyName = value;
    }

}
