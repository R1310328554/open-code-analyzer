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

package org.keycloak.adapters.saml;

import java.util.Objects;

import org.keycloak.adapters.spi.AuthenticationError;
import org.keycloak.dom.saml.v2.protocol.StatusCodeType;
import org.keycloak.dom.saml.v2.protocol.StatusResponseType;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;

/**
 * 描述 SAML 认证过程中发生的错误。
 *
 * <p>实现 {@link AuthenticationError}，供适配器在 SAML 响应解析、签名校验或
 * 状态码异常时向上层报告失败原因。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SamlAuthenticationError implements AuthenticationError {

    /**
     * SAML 认证失败的具体原因枚举。
     */
    public static enum Reason {
        /** 从 HTTP 请求中提取 SAML 消息失败 */
        EXTRACTION_FAILURE,
        /** SAML 响应或断言签名无效 */
        INVALID_SIGNATURE,
        /** IdP 返回了错误状态码（StatusResponse 非 Success） */
        ERROR_STATUS
    }

    /** 失败原因 */
    private Reason reason;

    /** IdP 返回的 SAML 状态响应（可为 null） */
    private StatusResponseType status;

    /**
     * 仅指定失败原因构造错误对象。
     *
     * @param reason 失败原因
     */
    public SamlAuthenticationError(Reason reason) {
        this.reason = reason;
    }

    /**
     * 指定失败原因及 IdP 状态响应构造错误对象。
     *
     * @param reason 失败原因
     * @param status IdP 返回的状态响应
     */
    public SamlAuthenticationError(Reason reason, StatusResponseType status) {
        this.reason = reason;
        this.status = status;
    }

    /**
     * 从 IdP 状态响应构造错误对象（原因默认为 {@link Reason#ERROR_STATUS}）。
     *
     * @param statusType IdP 返回的状态响应
     */
    public SamlAuthenticationError(StatusResponseType statusType) {
        this.status = statusType;
    }

    /** @return 失败原因 */
    public Reason getReason() {
        return reason;
    }

    /** @return IdP 返回的 SAML 状态响应 */
    public StatusResponseType getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "SamlAuthenticationError [reason=" + reason + ", status=" 
          + ((status == null || status.getStatus() == null) ? "UNKNOWN" : extractStatusCode(status.getStatus().getStatusCode()))
          + "]";
    }
    
    /**
     * 递归提取 SAML 状态码字符串；遇到 Responder 中间码时继续向下解析。
     *
     * @param statusCode 状态码节点
     * @return 最终状态码 URI 字符串，未知时返回 "UNKNOWN"
     */
    private String extractStatusCode(StatusCodeType statusCode) {
        if (statusCode == null || statusCode.getValue() == null) {
            return "UNKNOWN";
        }
        if (Objects.equals(JBossSAMLURIConstants.STATUS_RESPONDER.get(), statusCode.getValue().toString())) {
            return extractStatusCode(statusCode.getStatusCode());
        }
        return statusCode.getValue().toString();
    }
}
