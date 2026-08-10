package org.keycloak.models.credential;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.OTPPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.credential.dto.OTPCredentialData;
import org.keycloak.models.credential.dto.OTPSecretData;
import org.keycloak.models.utils.Base32;
import org.keycloak.util.JsonSerialization;

/**
 * OTP 凭据模型：封装 TOTP/HOTP 凭据的公开数据与密钥。
 */
public class OTPCredentialModel extends CredentialModel {

    /** OTP 凭据类型标识。 */
    public static final String TYPE = "otp";
    /** 基于时间的一次性密码子类型。 */
    public static final String TOTP = "totp";
    /** 基于计数器的一次性密码子类型。 */
    public static final String HOTP = "hotp";

    /**
     * 从存储读取原始密钥时支持的编码方式。
     * The supported encodings when reading the raw secret from the storage
     */
    public enum SecretEncoding {
        /** Base32 编码 */ BASE32
    }

    private final OTPCredentialData credentialData;
    private final OTPSecretData secretData;

    private OTPCredentialModel(String secretValue, String subType, int digits, int counter, int period, String algorithm) {
        this(secretValue, subType, digits, counter, period, algorithm, null);
    }

    private OTPCredentialModel(String secretValue, String subType, int digits, int counter, int period, String algorithm, String secretEncoding) {
        credentialData = new OTPCredentialData(subType, digits, counter, period, algorithm, secretEncoding);
        secretData = new OTPSecretData(secretValue);
    }

    private OTPCredentialModel(OTPCredentialData credentialData, OTPSecretData secretData) {
        this.credentialData = credentialData;
        this.secretData = secretData;
    }

    /** 创建 TOTP 凭据（默认编码）。 */
    public static OTPCredentialModel createTOTP(String secretValue, int digits, int period, String algorithm){
        return createTOTP(secretValue, digits, period, algorithm, null);
    }

    public static OTPCredentialModel createTOTP(String secretValue, int digits, int period, String algorithm, String encoding){
        OTPCredentialModel credentialModel = new OTPCredentialModel(secretValue, TOTP, digits, 0, period, algorithm, encoding);
        credentialModel.fillCredentialModelFields();
        return credentialModel;
    }

    /** 创建 HOTP 凭据。 */
    public static OTPCredentialModel createHOTP(String secretValue, int digits, int counter, String algorithm) {
        OTPCredentialModel credentialModel = new OTPCredentialModel(secretValue, HOTP, digits, counter, 0, algorithm);
        credentialModel.fillCredentialModelFields();
        return credentialModel;
    }

    /** 按 realm OTP 策略创建凭据。 */
    public static OTPCredentialModel createFromPolicy(RealmModel realm, String secretValue) {
        return createFromPolicy(realm, secretValue, "");
    }

    public static OTPCredentialModel createFromPolicy(RealmModel realm, String secretValue, String userLabel) {
        OTPPolicy policy = realm.getOTPPolicy();

        OTPCredentialModel credentialModel = new OTPCredentialModel(secretValue, policy.getType(), policy.getDigits(),
                policy.getInitialCounter(), policy.getPeriod(), policy.getAlgorithm());
        credentialModel.fillCredentialModelFields();
        credentialModel.setUserLabel(userLabel);
        return credentialModel;
    }

    /** 从通用 {@link CredentialModel} 反序列化 OTP 凭据。 */
    public static OTPCredentialModel createFromCredentialModel(CredentialModel credentialModel) {
        try {
            OTPCredentialData credentialData = JsonSerialization.readValue(credentialModel.getCredentialData(), OTPCredentialData.class);
            OTPSecretData secretData = JsonSerialization.readValue(credentialModel.getSecretData(), OTPSecretData.class);

            OTPCredentialModel otpCredentialModel = new OTPCredentialModel(credentialData, secretData);
            otpCredentialModel.setUserLabel(credentialModel.getUserLabel());
            otpCredentialModel.setCreatedDate(credentialModel.getCreatedDate());
            otpCredentialModel.setType(TYPE);
            otpCredentialModel.setId(credentialModel.getId());
            otpCredentialModel.setSecretData(credentialModel.getSecretData());
            otpCredentialModel.setCredentialData(credentialModel.getCredentialData());
            return otpCredentialModel;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /** 更新 HOTP 计数器。 */
    public void updateCounter(int counter) {
        credentialData.setCounter(counter);
        try {
            setCredentialData(JsonSerialization.writeValueAsString(credentialData));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** @return OTP 公开凭据数据 */
    public OTPCredentialData getOTPCredentialData() {
        return credentialData;
    }

    /** @return OTP 密钥数据 */
    public OTPSecretData getOTPSecretData() {
        return secretData;
    }

    /** @return 解码后的原始密钥字节 */
    public byte[] getDecodedSecret() {
        String encoding = credentialData.getSecretEncoding();

        if (encoding == null) {
            return secretData.getValue().getBytes(StandardCharsets.UTF_8);
        }

        try {
            if (SecretEncoding.BASE32.equals(SecretEncoding.valueOf(encoding.toUpperCase()))) {
                return Base32.decode(secretData.getValue());
            }

            throw new RuntimeException("Unsupported secret encoding: " + encoding);
        } catch (Exception cause) {
            throw new RuntimeException("Failed to decode otp secret using encoding [" + encoding + "]", cause);
        }
    }

    private void fillCredentialModelFields(){
        try {
            setCredentialData(JsonSerialization.writeValueAsString(credentialData));
            setSecretData(JsonSerialization.writeValueAsString(secretData));
            setType(TYPE);
            setCreatedDate(Time.currentTimeMillis());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
