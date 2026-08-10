/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.representations;

import org.keycloak.OAuth2Constants;

import com.fasterxml.jackson.annotation.JsonProperty;

import static org.keycloak.OAuth2Constants.EXPIRES_IN;
import static org.keycloak.OAuth2Constants.INTERVAL;

/**
 * OAuth 2.0 设备授权流程中设备授权端点的 JSON 响应（RFC 8628 §3.2）。
 *
 * @author <a href="mailto:h2-wada@nri.co.jp">Hiroyuki Wada</a>
 * @see <a href="https://tools.ietf.org/html/rfc8628#section-3.2">Device Authorization Response</a>
 */
public class OAuth2DeviceAuthorizationResponse {

    /** 设备码（必填）。 */
    @JsonProperty("device_code")
    protected String deviceCode;

    /** 用户码（必填，供用户在授权设备上输入）。 */
    @JsonProperty(OAuth2Constants.USER_CODE)
    protected String userCode;

    /** 用户验证 URI（必填）。 */
    @JsonProperty("verification_uri")
    protected String verificationUri;

    /** 含预填用户码的完整验证 URI（可选）。 */
    @JsonProperty("verification_uri_complete")
    protected String verificationUriComplete;

    /** 设备码与用户码有效期（秒，必填）。 */
    @JsonProperty(EXPIRES_IN)
    protected long expiresIn;

    /** 轮询令牌端点的建议间隔（秒，可选）。 */
    @JsonProperty(INTERVAL)
    protected long interval;

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getVerificationUri() {
        return verificationUri;
    }

    public void setVerificationUri(String verificationUri) {
        this.verificationUri = verificationUri;
    }

    public String getVerificationUriComplete() {
        return verificationUriComplete;
    }

    public void setVerificationUriComplete(String verificationUriComplete) {
        this.verificationUriComplete = verificationUriComplete;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }
}
