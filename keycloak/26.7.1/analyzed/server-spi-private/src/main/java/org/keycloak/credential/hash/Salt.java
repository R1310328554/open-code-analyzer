package org.keycloak.credential.hash;

import java.security.SecureRandom;

/**
 * 密码哈希盐值工具类，使用 {@link SecureRandom} 生成 16 字节随机盐。
 */
public class Salt {

    /** @return 新生成的 16 字节随机盐 */
    public static byte[] generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return salt;
    }

}
