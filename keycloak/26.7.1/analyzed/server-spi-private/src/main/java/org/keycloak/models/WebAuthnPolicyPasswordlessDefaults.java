/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models;

/**
 * 无密码（Passwordless）场景下的 WebAuthn 策略默认值。
 * <p>在 {@link WebAuthnPolicyTwoFactorDefaults} 基础上强制 resident key 与用户验证。</p>
 *
 * @author rmartinc
 */
public class WebAuthnPolicyPasswordlessDefaults extends WebAuthnPolicyTwoFactorDefaults {

    /** @return 无密码 WebAuthn 默认策略实例 */
    public static WebAuthnPolicy get() {
        return new WebAuthnPolicyPasswordlessDefaults();
    }

    /** 构造无密码默认策略：resident key 与 user verification 均为 required。 */
    WebAuthnPolicyPasswordlessDefaults() {
        super();
        this.residentKey = Constants.WEBAUTHN_POLICY_OPTION_REQUIRED;
        this.userVerificationRequirement = Constants.WEBAUTHN_POLICY_OPTION_REQUIRED;
    }
}
