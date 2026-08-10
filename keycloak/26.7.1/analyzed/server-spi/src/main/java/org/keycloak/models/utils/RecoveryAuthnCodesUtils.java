package org.keycloak.models.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.common.util.SecretGenerator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.crypto.JavaAlgorithm;
import org.keycloak.jose.jws.crypto.HashUtils;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.RecoveryAuthnCodesCredentialModel;

import org.jboss.logging.Logger;


/**
 * 恢复认证码工具类：生成、哈希、验证恢复码及查询用户凭据。
 */
public class RecoveryAuthnCodesUtils {

    private static final Logger logger = Logger.getLogger(RecoveryAuthnCodesUtils.class);

    /** 默认生成的恢复码数量。 */
    public static final int QUANTITY_OF_CODES_TO_GENERATE = 12;
    private static final int CODE_LENGTH = 12;
    /** 恢复码字符集（大写字母与数字，排除易混淆字符）。 */
    public static final char[] UPPERNUM = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789".toCharArray();
    private static final SecretGenerator SECRET_GENERATOR = SecretGenerator.getInstance();
    /** 恢复码哈希算法（SHA-512）。 */
    public static final String NOM_ALGORITHM_TO_HASH = JavaAlgorithm.SHA512;
    /** 浏览器流中恢复码输入默认错误消息键。 */
    public static final String RECOVERY_AUTHN_CODES_INPUT_DEFAULT_ERROR_MESSAGE = "recovery-codes-error-invalid";
    /** 浏览器认证流中恢复码输入字段名。 */
    public static final String FIELD_RECOVERY_CODE_IN_BROWSER_FLOW = "recoveryCodeInput";

    /** 对原始恢复码进行 SHA-512 哈希。
     * @param rawGeneratedCode 明文恢复码 */
    public static byte[] hashRawCode(String rawGeneratedCode) {
        Objects.requireNonNull(rawGeneratedCode, "rawGeneratedCode cannot be null");
        // 若算法可配置，需兼容 SHA-512 与旧版 RS512
        // If we allow the algorithm to be truly configurable, we should make sure that it works
        // with both `SHA-512` as well as `RS512` (which was used in the versions prior to 26.2)
        return HashUtils.hash(NOM_ALGORITHM_TO_HASH, rawGeneratedCode.getBytes(StandardCharsets.UTF_8));
    }

    /** 验证用户输入的恢复码是否与存储的哈希匹配。 */
    public static boolean verifyRecoveryCodeInput(String rawInputRecoveryCode, String hashedSavedRecoveryCode) {
        byte[] hashedInputBackupCode = hashRawCode(rawInputRecoveryCode);
        try {
            byte[] savedCode = Base64.getMimeDecoder().decode(hashedSavedRecoveryCode);
            return MessageDigest.isEqual(hashedInputBackupCode, savedCode);
        } catch (IllegalArgumentException iae) {
            logger.warnf("Error when decoding saved recovery code", iae);
            return false;
        }
    }

    /** 生成默认数量的随机明文恢复码列表。 */
    public static List<String> generateRawCodes() {
        Supplier<String> code = () -> SECRET_GENERATOR.randomString(CODE_LENGTH,UPPERNUM);
        return Stream.generate(code).limit(QUANTITY_OF_CODES_TO_GENERATE).collect(Collectors.toList());
    }

    /**
     * 查询用户凭据：先查联邦存储，再查本地存储。
     * Checks the user storage for the credential. If not found it will look for the credential in the local storage
     *
     * @param user - User model
     * @return - a optional  credential model
     */
    public static Optional<CredentialModel> getCredential(UserModel user) {
        return user.credentialManager()
                .getFederatedCredentialsStream()
                .filter(c -> RecoveryAuthnCodesCredentialModel.TYPE.equals(c.getType()))
                .findFirst()
                .or(() -> user.credentialManager().getStoredCredentialsByTypeStream(RecoveryAuthnCodesCredentialModel.TYPE).findFirst());
    }
}
