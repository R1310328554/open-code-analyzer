package org.keycloak.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * 替代查找缓存键计算器：对 realm、类型与查找参数做 MD5 摘要，
 * 生成稳定的本地缓存键字符串。
 */
class ComputedKey {

    /** 工具类，禁止实例化。 */
    private ComputedKey() {
    }

    /** 基于 realm、查找类型与单一替代键字符串计算缓存键。 */
    public static String computeKey(String realm, String type, String alternativeKey) {
        MessageDigest md = getMessageDigest();
        md.update(realm.getBytes(StandardCharsets.UTF_8));
        md.update(type.getBytes(StandardCharsets.UTF_8));
        md.update(alternativeKey.getBytes(StandardCharsets.UTF_8));
        return new String(md.digest(), StandardCharsets.UTF_8);
    }

    /** 基于 realm、类型与按 key 排序的属性映射计算缓存键。 */
    public static String computeKey(String realm, String type, Map<String, String> attributes) {
        MessageDigest md = getMessageDigest();
        md.update(realm.getBytes(StandardCharsets.UTF_8));
        md.update(type.getBytes(StandardCharsets.UTF_8));
        attributes.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
            md.update(e.getKey().getBytes(StandardCharsets.UTF_8));
            md.update(e.getValue().getBytes(StandardCharsets.UTF_8));
        });
        return new String(md.digest(), StandardCharsets.UTF_8);
    }

    /** @return MD5 摘要实例 */
    private static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
