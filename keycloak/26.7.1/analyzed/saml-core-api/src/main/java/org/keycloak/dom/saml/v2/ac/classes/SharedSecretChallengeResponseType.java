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
 * SAML 2.0 共享密钥挑战-应答类型：表示主体已通过共享密钥与对称密码的挑战-应答协议完成认证。
 *
 * This element indicates that the Principal has been authenticated by a challenge-response protocol utilizing shared
 * secret
 * keys and symmetric cryptography.
 *
 *
 * <p>
 * Java class for SharedSecretChallengeResponseType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SharedSecretChallengeResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Extension"
 * maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="method" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class SharedSecretChallengeResponseType extends ExtensionListType {

    protected String method;

    /**
     * 获取 挑战-应答方法 URI 属性的值。
     *
     * Gets the value of the method property.
     *
     * @return possible object is {@link String }
     */
    public String getMethod() {
        return method;
    }

    /**
     * 设置 挑战-应答方法 URI 属性的值。
     *
     * Sets the value of the method property.
     *
     * @param value allowed object is {@link String }
     */
    public void setMethod(String value) {
        this.method = value;
    }

}
