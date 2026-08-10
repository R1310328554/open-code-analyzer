package org.keycloak.protocol.docker;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.keycloak.models.utils.Base32;

/**
 * Docker JWT 头 {@code kid} 字段的 libtrust 指纹格式生成器。
 * <p>步骤：DER 公钥 → SHA-256 哈希截断至 240 位 → Base32 编码并按 4 字符一组用 {@code :} 分隔（共 12 组）。</p>
 * <p>示例：{@code "kid": "PYYO:TEWU:V7JH:26JV:AQTZ:LJC3:SXVJ:XGHA:34F2:2LAQ:ZRMK:Z7Q6"}</p>
 *
 * @see https://docs.docker.com/registry/spec/auth/jwt/
 * @see https://github.com/docker/libtrust/blob/master/key.go#L24
 */
public class DockerKeyIdentifier {

    private final String identifier;

    /** 从签名密钥生成 libtrust 兼容的 kid 字符串。 */
    public DockerKeyIdentifier(final Key key) throws InstantiationException {
        try {
            final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            final byte[] hashed = sha256.digest(key.getEncoded());
            final byte[] hashedTruncated = truncateToBitLength(240, hashed);
            final String base32Id = Base32.encode(hashedTruncated);
            identifier = byteStream(base32Id.getBytes()).collect(new DelimitingCollector());
        } catch (final NoSuchAlgorithmException e) {
            throw new InstantiationException("Could not instantiate docker key identifier, no SHA-256 algorithm available.");
        }
    }

    // 将 byte[] 转为 Stream<Byte> 以便 Collector 处理
    private Stream<Byte> byteStream(final byte[] bytes) {
        final Collection<Byte> colectionedBytes = new ArrayList<>();
        for (final byte aByte : bytes) {
            colectionedBytes.add(aByte);
        }

        return colectionedBytes.stream();
    }

    /** 将字节数组截断至指定位长度（须为 8 的倍数）。 */
    private byte[] truncateToBitLength(final int bitLength, final byte[] arrayToTruncate) {
        if (bitLength % 8 != 0) {
            throw new IllegalArgumentException("Bit length for truncation of byte array given as a number not divisible by 8");
        }

        final int numberOfBytes = bitLength / 8;
        return Arrays.copyOfRange(arrayToTruncate, 0, numberOfBytes);
    }

    @Override
    public String toString() {
        return identifier;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DockerKeyIdentifier)) return false;

        final DockerKeyIdentifier that = (DockerKeyIdentifier) o;

        return identifier != null ? identifier.equals(that.identifier) : that.identifier == null;

    }

    @Override
    public int hashCode() {
        return identifier != null ? identifier.hashCode() : 0;
    }

    // 可按组长度与分隔符泛化；当前仅满足 Docker kid 格式
    /** 每 4 个 Base32 字符插入 {@code :} 分隔符的 Stream 收集器。 */
    public static class DelimitingCollector implements Collector<Byte, StringBuilder, String> {

        @Override
        public Supplier<StringBuilder> supplier() {
            return () -> new StringBuilder();
        }

        @Override
        public BiConsumer<StringBuilder, Byte> accumulator() {
            return ((stringBuilder, aByte) -> {
                if (needsDelimiter(4, ":", stringBuilder)) {
                    stringBuilder.append(":");
                }

                stringBuilder.append(new String(new byte[]{aByte}));
            });
        }

        private static boolean needsDelimiter(final int maxLength, final String delimiter, final StringBuilder builder) {
            final int lastDelimiter = builder.lastIndexOf(delimiter);
            final int charsSinceLastDelimiter = builder.length() - lastDelimiter;
            return charsSinceLastDelimiter > maxLength;
        }

        @Override
        public BinaryOperator<StringBuilder> combiner() {
            return ((left, right) -> new StringBuilder(left.toString()).append(right.toString()));
        }

        @Override
        public Function<StringBuilder, String> finisher() {
            return StringBuilder::toString;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }
}
