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
 *
 */

package org.keycloak.protocol.oidc.grants.ciba.endpoints;

import org.keycloak.protocol.oidc.grants.ciba.CibaGrantType;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 客户端通知端点请求体（ping 模式）。
 * <p>Keycloak 向客户端配置的 notification endpoint 发送此 JSON，携带 {@code auth_req_id}。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientNotificationEndpointRequest {

    /** 认证请求标识（auth_req_id） */
    @JsonProperty(CibaGrantType.AUTH_REQ_ID)
    private String authReqId;

    /** @return auth_req_id */
    public String getAuthReqId() {
        return authReqId;
    }

    /** @param authReqId 认证请求标识 */
    public void setAuthReqId(String authReqId) {
        this.authReqId = authReqId;
    }
}
