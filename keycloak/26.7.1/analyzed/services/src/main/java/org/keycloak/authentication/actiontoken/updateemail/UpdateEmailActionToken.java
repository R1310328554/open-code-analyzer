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

package org.keycloak.authentication.actiontoken.updateemail;

import org.keycloak.authentication.actiontoken.DefaultActionToken;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 更新邮箱操作令牌，携带旧/新邮箱、是否注销其他会话及可选重定向 URI。
 */
public class UpdateEmailActionToken extends DefaultActionToken {

	/** 令牌类型 update-email。 */
	public static final String TOKEN_TYPE = "update-email";

	/** 变更前的邮箱地址。 */
	@JsonProperty("oldEmail")
	private String oldEmail;
	/** 待验证的新邮箱地址。 */
	@JsonProperty("newEmail")
	private String newEmail;
    /** 确认更新后是否注销其他会话（仅 true 时序列化）。 */
    @JsonProperty("logoutSessions")
    private Boolean logoutSessions;
    /** 更新完成后的重定向 URI。 */
    @JsonProperty("reduri")
    private String redirectUri;

	/** 构造更新邮箱令牌（默认不注销其他会话）。 */
    public UpdateEmailActionToken(String userId, int absoluteExpirationInSecs, String oldEmail, String newEmail, String clientId) {
          this(userId, absoluteExpirationInSecs, oldEmail, newEmail, clientId, null);
    }

	public UpdateEmailActionToken(String userId, int absoluteExpirationInSecs, String oldEmail, String newEmail, String clientId, Boolean logoutSessions){
		this(userId, absoluteExpirationInSecs, oldEmail, newEmail, clientId, logoutSessions, null);
	}

	public UpdateEmailActionToken(String userId, int absoluteExpirationInSecs, String oldEmail, String newEmail, String clientId, Boolean logoutSessions, String redirectUri){
		super(userId, TOKEN_TYPE, absoluteExpirationInSecs, null);
		this.oldEmail = oldEmail;
		this.newEmail = newEmail;
		this.issuedFor = clientId;
		this.logoutSessions = Boolean.TRUE.equals(logoutSessions)? true : null;
		this.redirectUri = redirectUri;
	}

	private UpdateEmailActionToken(){

	}

	/** @return 旧邮箱 */
	public String getOldEmail() {
		return oldEmail;
	}

	public void setOldEmail(String oldEmail) {
		this.oldEmail = oldEmail;
	}

	/** @return 新邮箱 */
	public String getNewEmail() {
		return newEmail;
	}

	public void setNewEmail(String newEmail) {
		this.newEmail = newEmail;
	}

    /** @return 是否在确认后注销其他会话 */
    public Boolean getLogoutSessions() {
        return this.logoutSessions;
    }

    public void setLogoutSessions(Boolean logoutSessions) {
        this.logoutSessions = Boolean.TRUE.equals(logoutSessions)? true : null;
    }

    /** @return 重定向 URI */
    public String getRedirectUri() {
        return redirectUri;
	}

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
}
