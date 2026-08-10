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
 * Java class for PasswordType complex type.
 * SAML 2.0 口令类型：描述长度、字符集、生成方式及外部验证 URI。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PasswordType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Length" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Alphabet" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Generation" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Extension"
 * maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ExternalVerification" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class PasswordType extends ExtensionListType {

    protected LengthType length;
    protected AlphabetType alphabet;
    protected Generation generation;
    protected String externalVerification;

    /**
     * 获取 口令长度 属性的值。
     *
     * Gets the value of the length property.
     *
     * @return possible object is {@link LengthType }
     */
    public LengthType getLength() {
        return length;
    }

    /**
     * 设置 口令长度 属性的值。
     *
     * Sets the value of the length property.
     *
     * @param value allowed object is {@link LengthType }
     */
    public void setLength(LengthType value) {
        this.length = value;
    }

    /**
     * 获取 字符集 属性的值。
     *
     * Gets the value of the alphabet property.
     *
     * @return possible object is {@link AlphabetType }
     */
    public AlphabetType getAlphabet() {
        return alphabet;
    }

    /**
     * 设置 字符集 属性的值。
     *
     * Sets the value of the alphabet property.
     *
     * @param value allowed object is {@link AlphabetType }
     */
    public void setAlphabet(AlphabetType value) {
        this.alphabet = value;
    }

    /**
     * 获取 生成方式 属性的值。
     *
     * Gets the value of the generation property.
     *
     * @return possible object is {@link Generation }
     */
    public Generation getGeneration() {
        return generation;
    }

    /**
     * 设置 生成方式 属性的值。
     *
     * Sets the value of the generation property.
     *
     * @param value allowed object is {@link Generation }
     */
    public void setGeneration(Generation value) {
        this.generation = value;
    }

    /**
     * 获取 外部验证 URI 属性的值。
     *
     * Gets the value of the externalVerification property.
     *
     * @return possible object is {@link String }
     */
    public String getExternalVerification() {
        return externalVerification;
    }

    /**
     * 设置 外部验证 URI 属性的值。
     *
     * Sets the value of the externalVerification property.
     *
     * @param value allowed object is {@link String }
     */
    public void setExternalVerification(String value) {
        this.externalVerification = value;
    }

}
