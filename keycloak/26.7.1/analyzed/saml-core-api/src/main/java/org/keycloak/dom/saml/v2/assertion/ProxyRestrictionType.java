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

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for ProxyRestrictionType complex type.
 * SAML 2.0 代理限制条件：限定断言可经代理转发的次数与受众。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ProxyRestrictionType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:assertion}ConditionAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Audience" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Count" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class ProxyRestrictionType extends ConditionAbstractType {

    protected List<URI> audience = new ArrayList<>();

    protected BigInteger count;

    /** 添加允许的代理受众 URI。
     *
     * Add an audience
     *
     * @param a
     */
    public void addAudience(URI a) {
        this.audience.add(a);
    }

    /** 获取只读代理受众 URI 列表。
     *
     * Gets the value of the audience property.
     */
    public List<URI> getAudience() {
        return Collections.unmodifiableList(audience);
    }

    /** 移除代理受众 URI。
     *
     * Remove an audience
     *
     * @param a
     */
    public void removeAudience(URI a) {
        this.audience.remove(a);
    }

    /**
     * 获取 允许代理次数 属性的值。
     *
     * Gets the value of the count property.
     *
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getCount() {
        return count;
    }

    /**
     * 设置 允许代理次数 属性的值。
     *
     * Sets the value of the count property.
     *
     * @param value allowed object is {@link BigInteger }
     */
    public void setCount(BigInteger value) {
        this.count = value;
    }
}