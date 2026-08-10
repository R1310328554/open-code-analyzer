package org.keycloak.crypto.hash;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Argon2 密码哈希参数常量与类型/版本映射工具。
 */
public class Argon2Parameters {

    /** 默认 Argon2 变体（id）。 */
    public static String DEFAULT_TYPE = "id";
    /** 默认 Argon2 版本（1.3）。 */
    public static String DEFAULT_VERSION = "1.3";
    /** 默认哈希输出长度（字节）。 */
    public static int DEFAULT_HASH_LENGTH = 32;
    /** 默认内存占用（KB）。 */
    public static int DEFAULT_MEMORY = 7168;
    /** 默认迭代次数。 */
    public static int DEFAULT_ITERATIONS = 5;
    /** 默认并行度。 */
    public static int DEFAULT_PARALLELISM = 1;

    /** Argon2 变体名称到 BouncyCastle 常量值的映射。 */
    private static Map<String, Integer> types = new LinkedHashMap<>();

    static {
        types.put("id", org.bouncycastle.crypto.params.Argon2Parameters.ARGON2_id);
        types.put("d", org.bouncycastle.crypto.params.Argon2Parameters.ARGON2_d);
        types.put("i", org.bouncycastle.crypto.params.Argon2Parameters.ARGON2_i);
    }

    /** Argon2 版本字符串到 BouncyCastle 常量值的映射。 */
    private static Map<String, Integer> versions = new LinkedHashMap<>();

    static {
        versions.put("1.3", org.bouncycastle.crypto.params.Argon2Parameters.ARGON2_VERSION_13);
        versions.put("1.0", org.bouncycastle.crypto.params.Argon2Parameters.ARGON2_VERSION_10);
    }

    /** @return 支持的 Argon2 变体名称集合 */
    public static Set<String> listTypes() {
        return types.keySet();
    }

    /** @return 支持的 Argon2 版本字符串集合 */
    public static Set<String> listVersions() {
        return versions.keySet();
    }

    /**
     * 将变体名称解析为 BouncyCastle 类型常量。
     *
     * @param type 变体名称（如 {@code id}、{@code d}、{@code i}）
     * @return BouncyCastle Argon2 类型值
     */
    public static int getTypeValue(String type) {
        return types.get(type);
    }

    /**
     * 将版本字符串解析为 BouncyCastle 版本常量。
     *
     * @param version 版本字符串（如 {@code 1.3}）
     * @return BouncyCastle Argon2 版本值
     */
    public static int getVersionValue(String version) {
        return versions.get(version);
    }


}
