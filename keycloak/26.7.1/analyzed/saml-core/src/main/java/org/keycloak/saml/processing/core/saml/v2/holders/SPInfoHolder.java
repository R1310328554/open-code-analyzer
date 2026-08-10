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
package org.keycloak.saml.processing.core.saml.v2.holders;

/**
 * <p>
 * 保存服务提供者（SP）相关信息的容器。
 * </p>
 * <p>
 * 用于生成 SAML 请求与响应时携带请求 ID、响应目标及 Issuer。
 * </p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 10, 2008
 */
public class SPInfoHolder {

    /** SAML 请求 ID（InResponseTo 对应值）。 */
    private String requestID;
    /** 响应 Destination URI。 */
    private String responseDestinationURI;
    /** SP 的 Issuer 标识。 */
    private String issuer;

    /** 返回请求 ID。 */
    public String getRequestID() {
        return requestID;
    }

    /** 设置请求 ID。 */
    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    /** 返回响应 Destination URI。 */
    public String getResponseDestinationURI() {
        return responseDestinationURI;
    }

    /** 设置响应 Destination URI。 */
    public void setResponseDestinationURI(String responseDestinationURI) {
        this.responseDestinationURI = responseDestinationURI;
    }

    /** 返回 SP Issuer 标识。 */
    public String getIssuer() {
        return issuer;
    }

    /** 设置 SP Issuer 标识。 */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}