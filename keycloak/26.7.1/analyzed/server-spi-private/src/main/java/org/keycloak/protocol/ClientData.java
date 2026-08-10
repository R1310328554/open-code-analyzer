/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.protocol;

import java.io.IOException;

import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.logging.Logger;

/**
 * 封装客户端登录请求（OIDC 或 SAML）的必要数据。
 * <p>当 {@code authenticationSession} 已过期时，可用于携带 redirect-uri、state 等信息，以便将错误正确回传给客户端。</p>
 * <p>Encapsulates necessary data about client login request (OIDC or SAML request). Can be useful for cases when authenticationSession
 * expired and we need to redirect back to the client with the error due to this.</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientData {

    protected static final Logger logger = Logger.getLogger(ClientData.class);

    /** 重定向 URI（JSON 字段 {@code ru}）。 */
    @JsonProperty("ru")
    private String redirectUri;

    /** 响应类型（JSON 字段 {@code rt}）。 */
    @JsonProperty("rt")
    private String responseType;

    /** 响应模式（JSON 字段 {@code rm}）。 */
    @JsonProperty("rm")
    private String responseMode;

    /** OAuth state 参数（JSON 字段 {@code st}）。 */
    @JsonProperty("st")
    private String state;

    public ClientData() {
    }

    /** @param redirectUri 重定向 URI
     * @param responseType 响应类型
     * @param responseMode 响应模式
     * @param state OAuth state
     */
    public ClientData(String redirectUri, String responseType, String responseMode, String state) {
        this.redirectUri = redirectUri;
        this.responseType = responseType;
        this.responseMode = responseMode;
        this.state = state;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseMode() {
        return responseMode;
    }

    public void setResponseMode(String responseMode) {
        this.responseMode = responseMode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return String.format("ClientData [ redirectUri=%s, responseType=%s, responseMode=%s, state=%s ]", redirectUri, responseType, responseMode, state);
    }

    /**
     * 从 Base64Url 编码的 {@code clientData} 请求参数解码。
     * @param clientDataParam 编码后的 clientData 字符串
     * @return 解码后的 {@link ClientData}，参数为空时返回 {@code null}
     */
    public static ClientData decodeClientDataFromParameter(String clientDataParam) throws IOException {
        if (ObjectUtil.isBlank(clientDataParam)) {
            return null;
        } else {
            byte[] cdataJson = Base64Url.decode(clientDataParam);
            return JsonSerialization.readValue(cdataJson, ClientData.class);
        }
    }

    /** 将本对象序列化为 Base64Url 编码 JSON 字符串。 */
    public String encode() {
        try {
            return Base64Url.encode(JsonSerialization.writeValueAsBytes(this));
        } catch (IOException ioe) {
            throw new RuntimeException("Not possible to serialize clientData");
        }
    }
}
