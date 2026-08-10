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
 * Java class for TimeSyncTokenType complex type.
 * SAML 2.0 时间同步令牌类型：描述 OTP 设备类型、种子长度及是否手持设备。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TimeSyncTokenType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="DeviceType" use="required" type="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}DeviceTypeType"
 * />
 *       &lt;attribute name="SeedLength" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="DeviceInHand" use="required" type="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}booleanType"
 * />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class TimeSyncTokenType {

    protected DeviceTypeType deviceType;
    protected BigInteger seedLength;
    protected Boolean deviceInHand = Boolean.FALSE;

    /**
     * 获取 设备类型 属性的值。
     *
     * Gets the value of the deviceType property.
     *
     * @return possible object is {@link DeviceTypeType }
     */
    public DeviceTypeType getDeviceType() {
        return deviceType;
    }

    /**
     * 设置 设备类型 属性的值。
     *
     * Sets the value of the deviceType property.
     *
     * @param value allowed object is {@link DeviceTypeType }
     */
    public void setDeviceType(DeviceTypeType value) {
        this.deviceType = value;
    }

    /**
     * 获取 种子长度 属性的值。
     *
     * Gets the value of the seedLength property.
     *
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getSeedLength() {
        return seedLength;
    }

    /**
     * 设置 种子长度 属性的值。
     *
     * Sets the value of the seedLength property.
     *
     * @param value allowed object is {@link BigInteger }
     */
    public void setSeedLength(BigInteger value) {
        this.seedLength = value;
    }

    /**
     * 获取 是否手持设备 属性的值。
     *
     * Gets the value of the deviceInHand property.
     *
     * @return possible object is {@link BooleanType }
     */
    public Boolean getDeviceInHand() {
        return deviceInHand;
    }

    /**
     * 设置 是否手持设备 属性的值。
     *
     * Sets the value of the deviceInHand property.
     *
     * @param value allowed object is {@link BooleanType }
     */
    public void setDeviceInHand(Boolean value) {
        this.deviceInHand = value;
    }
}