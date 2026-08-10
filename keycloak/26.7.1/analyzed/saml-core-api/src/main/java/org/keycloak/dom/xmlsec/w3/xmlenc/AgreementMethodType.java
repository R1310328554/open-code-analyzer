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
package org.keycloak.dom.xmlsec.w3.xmlenc;

import org.keycloak.dom.xmlsec.w3.xmldsig.KeyInfoType;

/**
 * <p>
 * Java class for AgreementMethodType complex type.
 * W3C XML Encryption 密钥协商方法元素，用于基于密钥协商算法派生加密密钥。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AgreementMethodType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="KA-Nonce" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;any/>
 *         &lt;element name="OriginatorKeyInfo" type="{http://www.w3.org/2000/09/xmldsig#}KeyInfoType" minOccurs="0"/>
 *         &lt;element name="RecipientKeyInfo" type="{http://www.w3.org/2000/09/xmldsig#}KeyInfoType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Algorithm" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class AgreementMethodType {

    /** 密钥协商算法 URI（必填属性 Algorithm）。 */
    protected String algorithm;

    /** 密钥协商方法的详细参数封装。 */
    public static class AggrementMethod {

        /** 密钥协商随机数（KA-Nonce 子元素）。 */
        protected byte[] kANonce;
        /** 发起方密钥信息（OriginatorKeyInfo 子元素）。 */
        protected KeyInfoType originatorKeyInfo;
        /** 接收方密钥信息（RecipientKeyInfo 子元素）。 */
        protected KeyInfoType recipientKeyInfo;

        /**
         * 构造密钥协商方法参数。
         *
         * @param kANonce 密钥协商随机数
         * @param originatorKeyInfo 发起方密钥信息
         * @param recipientKeyInfo 接收方密钥信息
         */
        public AggrementMethod(byte[] kANonce, KeyInfoType originatorKeyInfo, KeyInfoType recipientKeyInfo) {
            this.kANonce = kANonce;
            this.originatorKeyInfo = originatorKeyInfo;
            this.recipientKeyInfo = recipientKeyInfo;
        }

        /** 获取密钥协商随机数。 */
        public byte[] getkANonce() {
            return kANonce;
        }

        /** 获取发起方密钥信息。 */
        public KeyInfoType getOriginatorKeyInfo() {
            return originatorKeyInfo;
        }

        /** 获取接收方密钥信息。 */
        public KeyInfoType getRecipientKeyInfo() {
            return recipientKeyInfo;
        }
    }

    /**
     * 构造指定算法的密钥协商方法。
     *
     * @param algo 密钥协商算法 URI
     */
    public AgreementMethodType(String algo) {
        this.algorithm = algo;
    }

    /**
     * 获取密钥协商算法 URI。
     *
     * Gets the value of the algorithm property.
     *
     * @return possible object is {@link String }
     */
    public String getAlgorithm() {
        return algorithm;
    }

}
