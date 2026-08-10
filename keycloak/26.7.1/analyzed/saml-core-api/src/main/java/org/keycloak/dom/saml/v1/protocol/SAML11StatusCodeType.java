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
package org.keycloak.dom.saml.v1.protocol;

import java.io.Serializable;
import javax.xml.namespace.QName;

/**
 * SAML 1.1 状态码（StatusCode）类型：标识 SAML 响应的处理结果，可嵌套子状态码。
 *
 * <complexType name="StatusCodeType"> <sequence> <element ref="samlp:StatusCode" minOccurs="0"/> </sequence>
 * <attribute
 * name="Value" type="QName" use="required"/> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11StatusCodeType implements Serializable {

    /** 表示成功的预定义状态码。 */
    public static final SAML11StatusCodeType SUCCESS = new SAML11StatusCodeType(new QName("samlp:Success"));

    /** 可选的嵌套子状态码。 */
    protected SAML11StatusCodeType statusCode;

    /** 状态码值（QName）。 */
    protected QName value;

    /**
     * 构造状态码。
     *
     * @param theValue 状态码 QName 值
     */
    public SAML11StatusCodeType(QName theValue) {
        value = theValue;
    }

    /**
     * 获取嵌套状态码属性值。
     *
     * @return 可能的值为 {@link StatusCodeType }
     */
    public SAML11StatusCodeType getStatusCode() {
        return statusCode;
    }

    /**
     * 设置嵌套状态码属性值。
     *
     * @param value 允许的值为 {@link StatusCodeType }
     */
    public void setStatusCode(SAML11StatusCodeType value) {
        this.statusCode = value;
    }

    /**
     * 获取状态码 Value 属性值。
     *
     * @return 可能的值为 {@link String }
     */
    public QName getValue() {
        return value;
    }
}
