package org.keycloak.common.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 密码学安全随机 ID、字符串与字节序列生成器（单例）。
 */
public class SecretGenerator {

    /** 256 位（32 字节）密钥长度常量。 */
    public static final int SECRET_LENGTH_256_BITS = 32;
    /** 384 位（48 字节）密钥长度常量。 */
    public static final int SECRET_LENGTH_384_BITS = 48;
    /** 512 位（64 字节）密钥长度常量。 */
    public static final int SECRET_LENGTH_512_BITS = 64;
    /**
     * 会话 ID 字节长度。
     * <p />
     * NIST 与 ANSSI 均要求至少 128 位熵，见 <a href="https://github.com/keycloak/keycloak/issues/38663">#38663</a>。
     * 因各节点需按段过滤会话 ID（{@link org.keycloak.models.sessions.infinispan.SessionAffinityService}），
     * 额外增加熵以应对 256 段缓存等场景（预留 16 位）。
     * 适用于 keep-alive 连接固定到单节点生成 ID 的情况。
     */
    private static final int SESSION_ID_BYTES = 18;
    /** 生成 Base64 URL 安全会话 ID 的默认供应器。 */
    public static final Supplier<String> SECURE_ID_GENERATOR = () -> getInstance().generateBase64SecureId(SESSION_ID_BYTES);

    /** 大写字母字母表。 */
    public static final char[] UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    /** 数字字母表。 */
    public static final char[] DIGITS = "0123456789".toCharArray();

    /** 大小写字母与数字字母表。 */
    public static final char[] ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private static final SecretGenerator instance = new SecretGenerator();

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SecretGenerator() {
    }

    /** 返回单例实例。 */
    public static SecretGenerator getInstance() {
        return instance;
    }
    
    /** 生成标准 UUID 形式的随机 ID 字符串。 */
    public String generateSecureID() {
        return generateSecureUUID().toString();
    }

    /**
     * 生成 {@code nBytes} 随机字节经 Base64 URL 编码的 ID。
     * 断言结果不含 {@code .}（AuthenticationSessionManager 分隔符）与空格（OIDC session_state 约束）。
     */
    public String generateBase64SecureId(int nBytes) {
        assert nBytes > 0;
        byte[] data = new byte[nBytes];
        SECURE_RANDOM.nextBytes(data);
        String id = Base64.getUrlEncoder().encodeToString(data);

        // disallow a dot, as a dot is used as a separator in AuthenticationSessionManager.decodeBase64AndValidateSignature
        assert !id.contains(".");

        // disallow a space, as session_state must not contain a space (see https://openid.net/specs/openid-connect-session-1_0.html#CreatingUpdatingSessions)
        assert !id.contains(" ");

        return id;
    }

    /** 生成 {@code [0, bound)} 范围内的安全随机整数。 */
    public static int nextInt(int bound) {
        return SECURE_RANDOM.nextInt(bound);
    }

    /** 生成默认 256 位熵的 ALPHANUM 随机字符串。 */
    public String randomString() {
        return randomString(SECRET_LENGTH_256_BITS, ALPHANUM);
    }

    /** 从 ALPHANUM 字母表生成长度为 {@code length} 的随机字符串。 */
    public String randomString(int length) {
        return randomString(length, ALPHANUM);
    }

    /**
     * 从指定字母表生成长度为 {@code length} 的随机字符串。
     *
     * @param length 字符串长度，须 ≥ 1
     * @param symbols 字母表，至少 2 个符号
     */
    public String randomString(int length, char[] symbols) {
        if (length < 1) {
            throw new IllegalArgumentException();
        }
        if (symbols == null || symbols.length < 2) {
            throw new IllegalArgumentException();
        }

        char[] buf = new char[length];

        for (int idx = 0; idx < buf.length; ++idx) {
            buf[idx] = symbols[SECURE_RANDOM.nextInt(symbols.length)];
        }

        return new String(buf);
    }

    /** 生成默认 256 位随机字节。 */
    public byte[] randomBytes() {
        return randomBytes(SECRET_LENGTH_256_BITS);
    }

    /** 生成 {@code length} 字节的安全随机数组。 */
    public byte[] randomBytes(int length) {
        if (length < 1) {
            throw new IllegalArgumentException();
        }

        byte[] buf = new byte[length];
        SECURE_RANDOM.nextBytes(buf);
        return buf;
    }

    /** 生成随机字节并以十六进制字符串返回。 */
    public String randomBytesHex(int length) {
        final StringBuilder sb = new StringBuilder();
        for (byte b : randomBytes(length)) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit((b & 0xF), 16));
        }
        return sb.toString();
    }

    /**
     * 计算目标字母表中等熵长度，使与 {@code byteLengthEntropy} 字节随机熵相当。
     *
     * @param byteLengthEntropy The desired entropy in bytes
     * @param dstAlphabetLeng The length of the destination alphabet
     * @return The equivalent length in destination alphabet to have the same entropy bits
     */
    public static int equivalentEntropySize(int byteLengthEntropy, int dstAlphabetLeng) {
        return equivalentEntropySize(byteLengthEntropy, 256, dstAlphabetLeng);
    }

    /**
     * 计算源字母表字符串在目标字母表下的等熵长度。
     *
     * @param length The length of the string encoded in source alphabet
     * @param srcAlphabetLength The length of the source alphabet
     * @param dstAlphabetLeng The length of the destination alphabet
     * @return The equivalent length (same entropy) in destination alphabet for a string of length in source alphabet
     */
    public static int equivalentEntropySize(int length, int srcAlphabetLength, int dstAlphabetLeng) {
        return (int) Math.ceil(length * ((Math.log(srcAlphabetLength)) / (Math.log(dstAlphabetLeng))));
    }

    /**
     * 返回全位随机的伪 UUID，熵最大化。
     *
     * @return UUID with all bits random
     */
    public UUID generateSecureUUID() {
        byte[] data = randomBytes(16);
        return new UUID(toLong(data, 0), toLong(data, 8));
    }

    /** 从字节数组指定偏移读取 8 字节为 long（大端）。 */
    private static long toLong(byte[] data, int offset) {
        return  ((data[offset] & 0xFFL) << 56) |
                ((data[offset + 1] & 0xFFL) << 48) |
                ((data[offset + 2] & 0xFFL) << 40) |
                ((data[offset + 3] & 0xFFL) << 32) |
                ((data[offset + 4] & 0xFFL) << 24) |
                ((data[offset + 5] & 0xFFL) << 16) |
                ((data[offset + 6] & 0xFFL) <<  8) |
                ((data[offset + 7] & 0xFFL)) ;
    }
}
