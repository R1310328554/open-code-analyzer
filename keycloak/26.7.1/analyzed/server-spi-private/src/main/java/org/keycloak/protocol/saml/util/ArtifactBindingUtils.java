package org.keycloak.protocol.saml.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * SAML Artifact 绑定工具类：从 Artifact 提取解析器 ID、计算实体标识符等。
 * <p>Artifact 前 2 字节为 TypeCode，用于路由到对应的 {@link ArtifactResolver} 提供者。</p>
 */
public class ArtifactBindingUtils {
    /** 从 Base64 编码的 Artifact 字符串提取解析器提供者 ID（TypeCode 十六进制）。 */
    public static String artifactToResolverProviderId(String artifact) {
        return byteArrayToResolverProviderId(Base64.getDecoder().decode(artifact));
    }
    
    /** 从 Artifact 字节数组前 2 字节生成解析器提供者 ID。 */
    public static String byteArrayToResolverProviderId(byte[] ar) {
        if (ar.length < 4) {
            // Artifact 至少需 4 字节（TypeCode + EndpointIndex）
            throw new IllegalArgumentException("SAML artifact must be at least 4 bytes (TypeCode + EndpointIndex)");
        }
        return String.format("%02X%02X", ar[0], ar[1]);
    }

    /**
     * 从字符串（如 entityId）计算 Artifact 绑定标识符的 Base64 表示。
     * Computes identifier from the given String, for example, from entityId
     *
     * @param identifierFrom String that will be turned into an identifier
     * @return Base64 of SHA-1 hash of the identifierFrom
     */
    public static String computeArtifactBindingIdentifierString(String identifierFrom) {
        return Base64.getEncoder().encodeToString(computeArtifactBindingIdentifier(identifierFrom));
    }

    /**
     * 将标识符字节数组编码为 Base64 字符串。
     * Turns byte representation of the identifier into readable String
     *
     * @param identifier byte representation of the identifier
     * @return Base64 of the identifier
     */
    public static String getArtifactBindingIdentifierString(byte[] identifier) {
        return Base64.getEncoder().encodeToString(identifier);
    }

    /**
     * 计算给定字符串的 20 字节 SHA-1 标识符（如 entityId）。
     * Computes 20 bytes long byte identifier of the given string, for example, from entityId
     *
     * @param identifierFrom String that will be turned into an identifier
     * @return SHA-1 hash of the given identifierFrom
     */
    public static byte[] computeArtifactBindingIdentifier(String identifierFrom) {
        try {
            MessageDigest sha1Digester = MessageDigest.getInstance("SHA-1");
            return sha1Digester.digest(identifierFrom.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("JVM does not support required cryptography algorithms: SHA-1/SHA1PRNG.", e);
        }
    }
}
