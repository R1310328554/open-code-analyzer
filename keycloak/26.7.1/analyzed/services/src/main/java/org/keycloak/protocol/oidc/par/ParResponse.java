/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.par;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 推送授权请求（PAR）成功响应体。
 * <p>包含 {@code request_uri} 与 {@code expires_in}，对应 RFC 9126 PAR 端点返回的 JSON 结构。</p>
 */
public class ParResponse {

    /** PAR 返回的请求 URI（urn:ietf:params:oauth:request_uri:…） */
    @JsonProperty("request_uri")
    private String requestUri;

    /** request_uri 有效时长（秒） */
    @JsonProperty("expires_in")
    private int expiresIn;

    /** @param requestUri 请求 URI @param expiresIn 过期秒数 */
    public ParResponse(String requestUri, int expiresIn) {
        this.requestUri = requestUri;
        this.expiresIn = expiresIn;
    }

    /** @return 请求 URI */
    public String getRequestUri() {
        return requestUri;
    }

    /** @param requestUri 请求 URI */
    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    /** @return 过期秒数 */
    public int getExpiresIn() {
        return expiresIn;
    }

    /** @param expiresIn 过期秒数 */
    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}
