/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authentication.actiontoken.inviteorg;

import org.keycloak.authentication.actiontoken.DefaultActionToken;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of a token that represents a time-limited verify e-mail action.
 *
 * @author hmlnarik
 */
public class InviteOrgActionToken extends DefaultActionToken {

    /** 令牌类型 ORGIVT（组织邀请）。 */
    public static final String TOKEN_TYPE = "ORGIVT";

    /** JSON 字段：接受邀请后的重定向 URI。 */
    private static final String JSON_FIELD_REDIRECT_URI = "reduri";
    /** JSON 字段：目标组织 ID。 */
    private static final String JSON_ORG_ID = "org_id";

    @JsonProperty(JSON_FIELD_REDIRECT_URI)
    /** 接受邀请后的重定向地址。 */
    private String redirectUri;


    @JsonProperty(JSON_ORG_ID)
    /** 被邀请加入的组织 ID。 */
    private String orgId;

    /** 构造组织邀请操作令牌。 */
    public InviteOrgActionToken(String userId, int absoluteExpirationInSecs, String email, String clientId) {
        super(userId, TOKEN_TYPE, absoluteExpirationInSecs, null);
        setEmail(email);
        this.issuedFor = clientId;
    }

    private InviteOrgActionToken() {
    }

    /** @return 重定向 URI */
    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    /** @return 组织 ID */
    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}
