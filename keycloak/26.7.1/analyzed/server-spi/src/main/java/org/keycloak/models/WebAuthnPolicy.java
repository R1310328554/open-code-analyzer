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

package org.keycloak.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keycloak.crypto.Algorithm;

import org.jboss.logging.Logger;

/**
 * WebAuthn 策略：配置 RP 名称、签名算法、认证器附件、resident key 与用户验证等 WebAuthn 注册/认证参数。
 */
public class WebAuthnPolicy implements Serializable {

    protected static final Logger logger = Logger.getLogger(WebAuthnPolicy.class);
    // 必填字段
    // required
    protected String rpEntityName;
    protected List<String> signatureAlgorithms;
    // 可选字段
    // optional
    protected String rpId;
    protected String attestationConveyancePreference;
    protected String authenticatorAttachment;
    protected String requireResidentKey;
    protected String residentKey;
    protected String userVerificationRequirement;
    protected int createTimeout = 0; // 未作为配置项暴露
    // not specified as option
    protected boolean avoidSameAuthenticatorRegister = false;
    protected List<String> acceptableAaguids;
    protected List<String> extraOrigins;
    protected Boolean passkeysEnabled; // 仅用于无密码场景
    // only used for passwordless
    protected String mediation; // 仅用于无密码场景
    // only used for passwordless

    /** 默认构造 WebAuthn 策略。 */
    public WebAuthnPolicy() {
    }

    /** @param signatureAlgorithms 支持的签名算法列表 */
    public WebAuthnPolicy(List<String> signatureAlgorithms) {
        this.signatureAlgorithms = signatureAlgorithms;
    }

    // TODO：签名算法列表需线程安全
    // TODO : must be thread safe list
    /** 默认 WebAuthn 策略（ES256 + RS256）。 */
    public static WebAuthnPolicy DEFAULT_POLICY = new WebAuthnPolicy(new ArrayList<>(Arrays.asList(Algorithm.ES256,Algorithm.RS256)));

    /** @return RP 显示名称 */
    public String getRpEntityName() {
        return rpEntityName;
    }

    /** @param rpEntityName RP 显示名称 */
    public void setRpEntityName(String rpEntityName) {
        this.rpEntityName = rpEntityName;
    }

    /** @return 签名算法列表 */
    public List<String> getSignatureAlgorithm() {
        return signatureAlgorithms;
    }

    /** @param signatureAlgorithms 签名算法列表 */
    public void setSignatureAlgorithm(List<String> signatureAlgorithms) {
        this.signatureAlgorithms = signatureAlgorithms;
    }

    /** @return RP ID（通常为域名） */
    public String getRpId() {
        return rpId;
    }

    /** @param rpId RP ID */
    public void setRpId(String rpId) {
        this.rpId = rpId;
    }

    /** @return 认证声明传达偏好 */
    public String getAttestationConveyancePreference() {
        return attestationConveyancePreference;
    }

    /** @param attestationConveyancePreference 认证声明传达偏好 */
    public void setAttestationConveyancePreference(String attestationConveyancePreference) {
        this.attestationConveyancePreference = attestationConveyancePreference;
    }

    /** @return 认证器附件类型（platform/cross-platform） */
    public String getAuthenticatorAttachment() {
        return authenticatorAttachment;
    }

    /** @param authenticatorAttachment 认证器附件类型 */
    public void setAuthenticatorAttachment(String authenticatorAttachment) {
        this.authenticatorAttachment = authenticatorAttachment;
    }

    /**
     * 已弃用：请改用 {@link #getResidentKey()}。
     * @deprecated Use {@link #getResidentKey()} instead. Kept for backwards compatibility and planned to be removed in the future.
     */
    @Deprecated
    public String getRequireResidentKey() {
        return requireResidentKey;
    }

    /**
     * @deprecated Use {@link #setResidentKey(String)} instead. Kept for backwards compatibility and planned to be removed in the future.
     */
    @Deprecated
    public void setRequireResidentKey(String requireResidentKey) {
        if ("Yes".equals(requireResidentKey) || "No".equals(requireResidentKey)) {
            logger.warn("The WebAuthn policy option 'requireResidentKey' is deprecated and will be removed in the future. Use the 'residentKey' option instead.");
        }
        this.requireResidentKey = requireResidentKey;
    }

    /** @return resident key 要求 */
    public String getResidentKey() {
        return residentKey;
    }

    /** @param residentKey resident key 要求 */
    public void setResidentKey(String residentKey) {
        this.residentKey = residentKey;
    }

    /** @return 用户验证要求 */
    public String getUserVerificationRequirement() {
        return userVerificationRequirement;
    }

    /** @param userVerificationRequirement 用户验证要求 */
    public void setUserVerificationRequirement(String userVerificationRequirement) {
        this.userVerificationRequirement = userVerificationRequirement;
    }

    /** @return 注册超时（毫秒） */
    public int getCreateTimeout() {
        return createTimeout;
    }

    /** @param createTimeout 注册超时 */
    public void setCreateTimeout(int createTimeout) {
        this.createTimeout = createTimeout;
    }

    /** @return 是否禁止同一认证器重复注册 */
    public boolean isAvoidSameAuthenticatorRegister() {
        return avoidSameAuthenticatorRegister;
    }

    /** @param avoidSameAuthenticatorRegister 是否禁止重复注册 */
    public void setAvoidSameAuthenticatorRegister(boolean avoidSameAuthenticatorRegister) {
        this.avoidSameAuthenticatorRegister = avoidSameAuthenticatorRegister;
    }

    /** @return 允许的 AAGUID 列表 */
    public List<String> getAcceptableAaguids() {
        return acceptableAaguids;
    }

    /** @param acceptableAaguids 允许的 AAGUID 列表 */
    public void setAcceptableAaguids(List<String> acceptableAaguids) {
        this.acceptableAaguids = acceptableAaguids;
    }

    /** @return 额外允许的 origin 列表 */
    public List<String> getExtraOrigins(){
        return extraOrigins;
    }

    /** @param extraOrigins 额外 origin 列表 */
    public void setExtraOrigins(List<String> extraOrigins) {
        this.extraOrigins = extraOrigins;
    }

    /** @return 是否启用 Passkeys（无密码） */
    public Boolean isPasskeysEnabled() {
        return passkeysEnabled;
    }

    /** @param passkeysEnabled 是否启用 Passkeys */
    public void setPasskeysEnabled(Boolean passkeysEnabled) {
        this.passkeysEnabled = passkeysEnabled;
    }

    /** @return 条件式 UI 中介模式 */
    public String getMediation() {
        return mediation;
    }

    /** @param mediation 中介模式 */
    public void setMediation(String mediation) {
        this.mediation = mediation;
    }
}
