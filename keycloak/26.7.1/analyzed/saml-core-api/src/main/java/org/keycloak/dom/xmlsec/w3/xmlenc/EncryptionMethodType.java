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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Java class for EncryptionMethodType complex type.
 * XML 加密 EncryptionMethod 元素类型，指定加密算法 URI 及可选的密钥长度、OAEP 参数等。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="EncryptionMethodType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="KeySize" type="{http://www.w3.org/2001/04/xmlenc#}KeySizeType" minOccurs="0"/>
 *         &lt;element name="OAEPparams" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="Algorithm" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class EncryptionMethodType {

    /** 加密算法 URI，必填属性。 */
    protected String algorithm;

    /** 加密方法子元素（密钥长度、OAEP 参数等）。 */
    protected EncryptionMethod encryptionMethod;

    /** 加密方法内部结构，对应 KeySize、OAEPparams 及扩展元素。 */
    public static class EncryptionMethod {

        /** 对称密钥长度（位）。 */
        protected BigInteger keySize;
        /** RSA-OAEP 加密使用的附加参数。 */
        protected byte[] OAEPparams;

        /** 其他未建模的扩展子元素。 */
        protected List<Object> any = new ArrayList<>();

        /** 使用指定密钥长度与 OAEP 参数构造。 */
        public EncryptionMethod(BigInteger bigInteger, byte[] oAEPparams) {
            this.keySize = bigInteger;
            OAEPparams = oAEPparams;
        }

        /** 创建空的加密方法子元素容器。 */
        public EncryptionMethod() {
            this.keySize = null;
            this.OAEPparams = null;
        }

        /** 获取密钥长度。 */
        public BigInteger getKeySize() {
            return keySize;
        }

        /** 获取 OAEP 参数。 */
        public byte[] getOAEPparams() {
            return OAEPparams;
        }

        /** 设置密钥长度。 */
        public void setKeySize(BigInteger keySize) {
            this.keySize = keySize;
        }

        /** 设置 OAEP 参数。 */
        public void setOAEPparams(byte[] OAEPparams) {
            this.OAEPparams = OAEPparams;
        }

        /** 获取扩展子元素列表。 */
        public List<Object> getAny() {
            return any;
        }

        /** 添加一条扩展子元素。 */
        public void addAny(Object e) {
            this.any.add(e);
        }
    }

    /** 使用算法 URI 构造加密方法类型。 */
    public EncryptionMethodType(String algo) {
        this.algorithm = algo;
    }

    /** 获取加密方法子元素。 */
    public EncryptionMethod getEncryptionMethod() {
        return encryptionMethod;
    }

    /** 设置加密方法子元素。 */
    public void setEncryptionMethod(EncryptionMethod encryptionMethod) {
        this.encryptionMethod = encryptionMethod;
    }

    /**
     * 获取 algorithm 属性值。
     *
     * Gets the value of the algorithm property.
     *
     * @return possible object is {@link String }
     */
    public String getAlgorithm() {
        return algorithm;
    }

}
