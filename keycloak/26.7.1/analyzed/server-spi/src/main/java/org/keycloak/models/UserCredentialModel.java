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

package org.keycloak.models;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.common.util.SecretGenerator;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.credential.PasswordUserCredentialModel;
import org.keycloak.models.credential.RecoveryAuthnCodesCredentialModel;

/**
 * 用户凭据输入模型：实现 {@link CredentialInput}，封装登录/更新时的凭据数据。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserCredentialModel implements CredentialInput {

    /** @deprecated 请使用 {@link PasswordCredentialModel#TYPE} */
    @Deprecated /** Use PasswordCredentialModel.TYPE instead **/
    public static final String PASSWORD = PasswordCredentialModel.TYPE;

    /** @deprecated 请使用 {@link PasswordCredentialModel#PASSWORD_HISTORY} */
    @Deprecated /** Use PasswordCredentialModel.PASSWORD_HISTORY instead **/
    public static final String PASSWORD_HISTORY = PasswordCredentialModel.PASSWORD_HISTORY;

    /** @deprecated 请使用 {@link OTPCredentialModel#TOTP} */
    @Deprecated /**  Use OTPCredentialModel.TOTP instead **/
    public static final String TOTP = OTPCredentialModel.TOTP;

    /** @deprecated 请使用 {@link OTPCredentialModel#HOTP} */
    @Deprecated /**  Use OTPCredentialModel.TOTP instead **/
    public static final String HOTP = OTPCredentialModel.HOTP;

    /** @deprecated 遗留常量，Keycloak 已不再使用 */
    @Deprecated /** Legacy stuff. Not used in Keycloak anymore **/
    public static final String PASSWORD_TOKEN = CredentialModel.PASSWORD_TOKEN;

    /** 通用密钥凭据类型常量。 */
    public static final String SECRET = CredentialModel.SECRET;
    /** Kerberos 凭据类型常量。 */
    public static final String KERBEROS = CredentialModel.KERBEROS;
    /** 客户端证书凭据类型常量。 */
    public static final String CLIENT_CERT = CredentialModel.CLIENT_CERT;

    private String credentialId;
    private String type;
    private String challengeResponse;
    private String device;
    private String algorithm;
    private boolean adminRequest;

    // Additional context informations
    protected Map<String, Object> notes = new HashMap<>();

    /** 无参构造。 */
    public UserCredentialModel() {
    }

    /** @param credentialId 凭据 ID
     * @param type 凭据类型
     * @param challengeResponse 挑战响应值 */
    public UserCredentialModel(String credentialId, String type, String challengeResponse) {
        this.credentialId = credentialId;
        this.type = type;
        this.challengeResponse = challengeResponse;
        this.adminRequest = false;
    }

    /** @param credentialId 凭据 ID
     * @param type 凭据类型
     * @param challengeResponse 挑战响应值
     * @param adminRequest 是否为管理员请求 */
    public UserCredentialModel(String credentialId, String type, String challengeResponse, boolean adminRequest) {
        this.credentialId = credentialId;
        this.type = type;
        this.challengeResponse = challengeResponse;
        this.adminRequest = adminRequest;
    }

    /** @param password 密码
     * @return 密码凭据模型 */
    public static PasswordUserCredentialModel password(String password) {
        return password(password, false);
    }

    /** @param password 密码
     * @param adminRequest 是否为管理员请求
     * @return 密码凭据模型 */
    public static PasswordUserCredentialModel password(String password, boolean adminRequest) {
        // It uses PasswordUserCredentialModel for backwards compatibility. Some UserStorage providers can check for that type
        return new PasswordUserCredentialModel("", PasswordCredentialModel.TYPE, password, adminRequest);
    }

    /** @deprecated 遗留 passwordToken（已不再使用） */
    @Deprecated /** passwordToken is legacy stuff. Not used in Keycloak anymore **/
    public static UserCredentialModel passwordToken(String passwordToken) {
        return new UserCredentialModel("", PASSWORD_TOKEN, passwordToken);
    }

    /**
     * 构建 OTP 凭据；type 须为 totp 或 hotp。
     * @param type must be "totp" or "hotp"
     * @param key
     * @return
     */
    public static UserCredentialModel otp(String type, String key) {
        if (type.equals(HOTP)) return hotp(key);
        if (type.equals(TOTP)) return totp(key);
        throw new RuntimeException("Unknown OTP type");
    }


    /** @param key TOTP 密钥
     * @return TOTP 凭据模型 */
    public static UserCredentialModel totp(String key) {
        return new UserCredentialModel("", TOTP, key);
    }

    
    /** @param key HOTP 密钥
     * @return HOTP 凭据模型 */
    public static UserCredentialModel hotp(String key) {
        return new UserCredentialModel("", HOTP, key);
    }

    /** @param password 密钥值
     * @return SECRET 类型凭据模型 */
    public static UserCredentialModel secret(String password) {
        return new UserCredentialModel("", SECRET, password);
    }

    /** @param token Kerberos 令牌
     * @return Kerberos 凭据模型 */
    public static UserCredentialModel kerberos(String token) {
        return new UserCredentialModel("", KERBEROS, token);
    }

    /** @return 随机生成的 SECRET 凭据模型 */
    public static UserCredentialModel generateSecret() {
        return new UserCredentialModel("", SECRET, SecretGenerator.getInstance().randomString());
    }

    /** @param backupAuthnCodeInput 备份认证码输入
     * @return 备份认证码凭据模型 */
    public static UserCredentialModel buildFromBackupAuthnCode(String backupAuthnCodeInput) {
        return buildFromBackupAuthnCode("", backupAuthnCodeInput);
    }

    /** @param credentialId 凭据 ID
     * @param backupAuthnCodeInput 备份认证码输入
     * @return 备份认证码凭据模型 */
    public static UserCredentialModel buildFromBackupAuthnCode(String credentialId, String backupAuthnCodeInput) {
        return new UserCredentialModel(credentialId, RecoveryAuthnCodesCredentialModel.TYPE, backupAuthnCodeInput);
    }

    @Override
    public String getCredentialId() {
        return credentialId;
    }

    @Override
    public String getType() {
        return type;
    }

    /** @param type 凭据类型 */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getChallengeResponse() {
        return challengeResponse;
    }

    /** @return 是否为管理员发起的凭据请求 */
    public boolean isAdminRequest() {
        return adminRequest;
    }

    /**
     * 仅为向后兼容保留。
     * This method exists only because of the backwards compatibility
     */
    @Deprecated
    public static boolean isOtp(String type) {
        return TOTP.equals(type) || HOTP.equals(type);
    }

    /**
     * This method exists only because of the backwards compatibility. It is recommended to use {@link #getChallengeResponse()} instead
     */
    /** @return 凭据值（兼容 {@link #getChallengeResponse()}） */
    public String getValue() {
        return getChallengeResponse();
    }

    /** @param value 凭据值 */
    public void setValue(String value) {
        this.challengeResponse = value;
    }

    /** @return OTP 等设备标识 */
    public String getDevice() {
        return device;
    }

    /** @param device OTP 等设备标识 */
    public void setDevice(String device) {
        this.device = device;
    }

    /** @return 哈希/OTP 算法名称 */
    public String getAlgorithm() {
        return algorithm;
    }

    /** @param algorithm 哈希/OTP 算法名称 */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /** @param key note 键
     * @param value note 值 */
    public void setNote(String key, Object value) {
        this.notes.put(key, value);
    }

    /** @param key 待移除 note 键 */
    public void removeNote(String key) {
        this.notes.remove(key);
    }

    /** @param key note 键
     * @return note 值 */
    public Object getNote(String key) {
        return this.notes.get(key);
    }

}
