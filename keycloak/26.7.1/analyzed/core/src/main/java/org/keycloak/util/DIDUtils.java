package org.keycloak.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.keycloak.common.util.StreamUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * DID 编解码工具类，主要支持 P-256（ES256）的 did:key 格式。
 * <p>
 * 提供：
 * <ul>
 * <li>EC 公钥 → did:key:z... 编码（multibase + multicodec + base58btc）</li>
 * <li>did:key → EC 公钥解码</li>
 * <li>multicodec 变长整数编解码</li>
 * <li>EC 点坐标归一化</li>
 * </ul>
 *
 * @author <a href="mailto:tdiesler@ibm.com">Thomas Diesler</a>
 */
public final class DIDUtils {

    private DIDUtils() {
    }

    /**
     * P-256 公钥的 multicodec 标识符。
     * 参见：https://github.com/multiformats/multicodec/
     * <p>
     * 编解码器名称："p256-pub"
     * 代码：0x1200（varint 编码为字节：0x80 0x24）
     */
    public static final int MULTICODEC_P256_PUB = 0x1200;
    /** P-384 公钥 multicodec 标识符。 */
    public static final int MULTICODEC_P384_PUB = 0x1201;
    /** P-521 公钥 multicodec 标识符。 */
    public static final int MULTICODEC_P521_PUB = 0x1202;
    /** JWK JCS 公钥 multicodec 标识符。 */
    public static final int MULTICODEC_JWK_JCS_PUB = 0xEB51;

    // ---------------------------------------------------------------------
    // 公开 API — did:key 编解码
    // ---------------------------------------------------------------------

    /**
     * 将 P-256 {@link ECPublicKey} 编码为 did:key 字符串。
     *
     * @param pub EC 公钥
     * @return did:key 表示
     */
    public static String encodeDidKey(ECPublicKey pub) {
        try {
            return encodeDidKeyInternal(pub);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 did:key（P-256）解码为 {@link ECPublicKey}。
     *
     * @param did did:key 字符串
     * @return 解码后的 EC 公钥
     */
    public static ECPublicKey decodeDidKey(String did) {
        try {
            ECPublicKey pubKey = decodeDidKeyInternal(did);
            return pubKey;
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从 did:key 字符串读取 multicodec 类型码。
     *
     * @param did did:key 字符串
     * @return multicodec 代码；无法识别时返回 0
     */
    public static int getDidKeyCodec(String did) {
        if (did == null || !did.startsWith("did:key:z"))
            return 0;

        // 去掉 "did:key:z" 前缀（z 表示 multibase base58btc）
        String base58 = did.substring("did:key:z".length());
        byte[] decoded = Base58.decode(base58);

        // 读取 multicodec varint（LEB128）
        int codec = 0;
        int shift = 0;

        for (byte value : decoded) {
            int b = value & 0xff;
            codec |= (b & 0x7f) << shift;
            if ((b & 0x80) == 0) {
                break;
            }
            shift += 7;
        }

        switch (codec) {
            case MULTICODEC_P256_PUB:
            case MULTICODEC_P384_PUB:
            case MULTICODEC_P521_PUB:
            case MULTICODEC_JWK_JCS_PUB:
                return codec;
            default:
                return 0;
        }
    }

    // Private ---------------------------------------------------------------------------------------------------------

    private static String encodeDidKeyInternal(ECPublicKey pub) throws IOException {
        return encodeDidKeyInternal(pub, false);
    }

    private static String encodeDidKeyInternal(ECPublicKey pub, boolean useJwkJcsPub) throws IOException {

        ECParameterSpec params = pub.getParams();
        int fieldSize = params.getCurve().getField().getFieldSize();

        // 验证为 P-256（secp256r1）
        if (fieldSize != 256) {
            throw new IllegalArgumentException("Expected secp256r1, but key uses: " + params);
        }

        byte[] x = toUnsigned32(pub.getW().getAffineX().toByteArray());
        byte[] y = toUnsigned32(pub.getW().getAffineY().toByteArray());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (useJwkJcsPub) {
            writeVarint(MULTICODEC_JWK_JCS_PUB, baos);

            // 最小 EC JWK（公钥）；若不严格要求 JCS 规范化，字段顺序可灵活
            Map<String, Object> jwk = new LinkedHashMap<>();
            jwk.put("crv", "P-256");
            jwk.put("kty", "EC");
            jwk.put("x", Base64.getUrlEncoder().withoutPadding().encodeToString(x));
            jwk.put("y", Base64.getUrlEncoder().withoutPadding().encodeToString(y));
            String jwkJson = new ObjectMapper().writeValueAsString(jwk);

            baos.write(jwkJson.getBytes(UTF_8));

        } else {
            writeVarint(MULTICODEC_P256_PUB, baos);

            // EC 未压缩点格式：0x04 || X || Y
            baos.write(0x04);
            baos.write(x);
            baos.write(y);
        }

        return "did:key:z" + Base58.encode(baos.toByteArray());
    }

    private static ECPublicKey decodeDidKeyInternal(String did) throws GeneralSecurityException, IOException {

        if (!did.startsWith("did:key:z")) {
            throw new IllegalArgumentException("Unsupported DID format: " + did);
        }

        String b58 = did.substring("did:key:z".length());
        InputStream in = new ByteArrayInputStream(Base58.decode(b58));

        // 读取 multicodec varint
        int codec = readVarint(in);

        byte[] x, y;
        switch (codec) {
            case MULTICODEC_P256_PUB: {

                // 期望 0x04 表示未压缩 EC 点
                int tag = in.read();
                if (tag != 0x04) {
                    throw new IllegalArgumentException("Invalid EC point tag: " + tag);
                }

                x = readNBytes(in, 32);
                y = readNBytes(in, 32);
                break;
            }
            case MULTICODEC_JWK_JCS_PUB: {
                // 剩余字节为 UTF-8 编码的 JWK JSON（JCS 规范化）
                String jwkJson = StreamUtil.readString(in, UTF_8);

                JsonNode jwk = new ObjectMapper().readTree(jwkJson);
                String kty = jwk.path("kty").asText(null);
                String crv = jwk.path("crv").asText(null);
                String xB64 = jwk.path("x").asText(null);
                String yB64 = jwk.path("y").asText(null);

                if (!"EC".equals(kty) || xB64 == null || yB64 == null) {
                    throw new IllegalArgumentException("Invalid EC JWK in did:key");
                }
                if (!"P-256".equals(crv) && !"secp256r1".equalsIgnoreCase(crv)) {
                    throw new IllegalArgumentException("Unsupported JWK crv: " + crv);
                }

                x = Base64.getUrlDecoder().decode(xB64);
                y = Base64.getUrlDecoder().decode(yB64);
                break;
            }
            default:
                    throw new IllegalArgumentException("Unexpected multicodec: 0x" + Integer.toHexString(codec));
        }

        ECPoint point = new ECPoint(
                new BigInteger(1, x),
                new BigInteger(1, y)
        );

        AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
        params.init(new ECGenParameterSpec("secp256r1"));
        ECParameterSpec paramSpec = params.getParameterSpec(ECParameterSpec.class);
        ECPublicKeySpec keySpec = new ECPublicKeySpec(point, paramSpec);

        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return (ECPublicKey) keyFactory.generatePublic(keySpec);
    }

    // ---------------------------------------------------------------------
    // Multicodec varint（LEB128）编解码
    // ---------------------------------------------------------------------

    private static void writeVarint(int value, OutputStream out) throws IOException {
        while ((value & ~0x7F) != 0) {
            out.write((value & 0x7F) | 0x80); // 续位标志
            value >>>= 7;
        }
        out.write(value);
    }

    private static int readVarint(InputStream in) throws IOException {
        int value = 0;
        int shift = 0;

        while (true) {
            int b = in.read();
            if (b == -1) {
                throw new EOFException("EOF while reading varint");
            }

            value |= (b & 0x7F) << shift;

            if ((b & 0x80) == 0) {
                break; // 最后一字节
            }

            shift += 7;
            if (shift > 28) {
                throw new IllegalArgumentException("Varint too long");
            }
        }

        return value;
    }

    // ---------------------------------------------------------------------
    // EC 辅助方法
    // ---------------------------------------------------------------------

    // 从 InputStream 精确读取 n 字节
    //
    private static byte[] readNBytes(InputStream in, int n) throws IOException {
        int read = 0;
        byte[] bytes = new byte[n];
        while (read < n) {
            int r = in.read(bytes, read, bytes.length - read);
            if (r == -1)
                throw new IllegalStateException("Unexpected EOF");
            read += r;
        }
        return bytes;
    }

    // 将有符号 BigInteger 字节数组转为固定 32 字节无符号数组；按需去符号位或补零
    private static byte[] toUnsigned32(byte[] in) {
        if (in.length == 32) {
            return in;
        }
        if (in.length > 32) {
            // 去掉符号字节
            return Arrays.copyOfRange(in, in.length - 32, in.length);
        }
        // 左侧补零至 32 字节
        byte[] out = new byte[32];
        System.arraycopy(in, 0, out, 32 - in.length, in.length);
        return out;
    }
}
