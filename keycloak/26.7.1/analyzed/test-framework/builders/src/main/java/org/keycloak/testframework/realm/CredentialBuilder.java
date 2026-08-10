package org.keycloak.testframework.realm;

import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.utils.HmacOTP;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;

/**
 * {@link CredentialRepresentation} 的流式构建器，便于在测试中构造密码、TOTP、HOTP 等凭据。
 */
public class CredentialBuilder extends Builder<CredentialRepresentation> {

    /** 基于已有凭据表示对象构造构建器。 */
    private CredentialBuilder(CredentialRepresentation rep) {
        super(rep);
    }

    /** 创建空的凭据构建器。 */
    public static CredentialBuilder create() {
        return new CredentialBuilder(new CredentialRepresentation());
    }

    /** 快速创建密码类型凭据。 */
    public static CredentialBuilder password(String password) {
        return create().type(CredentialRepresentation.PASSWORD).value(password);
    }

    /** 基于 TOTP 密钥创建 OTP 凭据表示。 */
    public static CredentialBuilder totp(String totpSecret) {
        return update(ModelToRepresentation.toRepresentation(OTPCredentialModel.createTOTP(totpSecret, 6, 30, HmacOTP.HMAC_SHA1)));
    }

    /** 基于 HOTP 密钥创建 OTP 凭据表示。 */
    public static CredentialBuilder hotp(String hotpSecret) {
        return update(ModelToRepresentation.toRepresentation(OTPCredentialModel.createHOTP(hotpSecret, 6, 0, HmacOTP.HMAC_SHA1)));
    }

    /** 基于已有凭据表示对象创建更新用构建器。 */
    public static CredentialBuilder update(CredentialRepresentation rep) {
        return new CredentialBuilder(rep);
    }

    /** 设置凭据类型（如 password、otp）。 */
    public CredentialBuilder type(String type) {
        rep.setType(type);
        return this;
    }

    /** 设置凭据明文值（如密码）。 */
    public CredentialBuilder value(String value) {
        rep.setValue(value);
        return this;
    }

    /** 设置凭据密钥数据（序列化 JSON）。 */
    public CredentialBuilder secretData(String secretData) {
        rep.setSecretData(secretData);
        return this;
    }

}
