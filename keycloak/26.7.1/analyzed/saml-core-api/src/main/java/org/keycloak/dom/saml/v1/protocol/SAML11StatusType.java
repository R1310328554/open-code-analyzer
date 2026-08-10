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

import org.keycloak.dom.saml.common.CommonStatusDetailType;

/**
 * SAML 1.1 状态（Status）类型：描述 SAML 响应的处理结果，含状态码、消息与详情。
 *
 * <complexType name="StatusType"> <sequence> <element ref="samlp:StatusCode"/> <element ref="samlp:StatusMessage"
 * minOccurs="0"/> <element ref="samlp:StatusDetail" minOccurs="0"/> </sequence>
 *
 * </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11StatusType implements Serializable {

    /** 状态码。 */
    protected SAML11StatusCodeType statusCode;

    /** 可选的状态消息。 */
    protected String statusMessage;

    /** 可选的状态详情。 */
    protected CommonStatusDetailType statusDetail;

    /** 返回状态码。 */
    public SAML11StatusCodeType getStatusCode() {
        return statusCode;
    }

    /** 设置状态码。 */
    public void setStatusCode(SAML11StatusCodeType statusCode) {
        this.statusCode = statusCode;
    }

    /** 返回状态消息。 */
    public String getStatusMessage() {
        return statusMessage;
    }

    /** 设置状态消息。 */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    /** 返回状态详情。 */
    public CommonStatusDetailType getStatusDetail() {
        return statusDetail;
    }

    /** 设置状态详情。 */
    public void setStatusDetail(CommonStatusDetailType statusDetail) {
        this.statusDetail = statusDetail;
    }

    /** 构造表示成功的 Status 实例。 */
    public static SAML11StatusType successType() {
        SAML11StatusType success = new SAML11StatusType();
        success.setStatusCode(SAML11StatusCodeType.SUCCESS);
        return success;
    }
}
