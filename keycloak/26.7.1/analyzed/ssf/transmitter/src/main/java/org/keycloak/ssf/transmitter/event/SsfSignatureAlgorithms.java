package org.keycloak.ssf.transmitter.event;

import java.util.Set;

import org.keycloak.crypto.Algorithm;
import org.keycloak.ssf.transmitter.SsfTransmitterConfig;
import org.keycloak.ssf.transmitter.stream.StreamConfig;

/**
 * 解析并校验出站 SSF 安全事件令牌（SET）使用的 JWS 签名算法。
 *
 * <p>CAEP 互操作配置文件 1.0 §2.6 将发送方限定为使用 2048 位 RSA 密钥的 RS256，
 * 故 {@link #ALLOWED} 当前仅含 {@code RS256}。周边机制（{@link StreamConfig} 上的按接收方覆盖、
 * {@link SsfTransmitterConfig} 上的 SPI 默认值）刻意保持灵活，以便工作组扩展配置文件后允许列表可增长——
 * 实践中今日无论覆盖如何各接收方最终均用 RS256 签名，但运维可见该旋钮存在，测试可演练解析优先级。</p>
 *
 * @see <a href="https://openid.github.io/sharedsignals/openid-caep-interoperability-profile-1_0.html#section-2.6">CAEP Interoperability Profile §2.6</a>
 */
public final class SsfSignatureAlgorithms {

    /**
     * 发送方愿意用于签发 SSF SET 的算法集合。此集合外的 alg 在流创建/更新时被拒绝，
     * 使错误配置在首次 push 前即暴露。
     */
    public static final Set<String> ALLOWED = Set.of(Algorithm.RS256);

    /**
     * 与 CAEP 互操作配置文件一致的安全网硬编码默认值。
     * 流与发送方 SPI 配置均未提供值时使用。
     */
    public static final String DEFAULT = Algorithm.RS256;

    private SsfSignatureAlgorithms() {
    }

    /**
     * 解析给定流使用的签名算法，优先级：按流覆盖 → 发送方 SPI 默认 → 硬编码 {@link #DEFAULT}。
     */
    public static String resolveForStream(StreamConfig streamConfig, SsfTransmitterConfig transmitterConfig) {

        if (streamConfig != null) {
            String streamAlg = streamConfig.getSignatureAlgorithm();
            if (streamAlg != null && !streamAlg.isBlank()) {
                return streamAlg;
            }
        }

        if (transmitterConfig != null) {
            String configAlg = transmitterConfig.getSignatureAlgorithm();
            if (configAlg != null && !configAlg.isBlank()) {
                return configAlg;
            }
        }

        return DEFAULT;
    }

    /**
     * 给定算法在允许列表内时返回 {@code true}。
     * 空白或 {@code null} 视为不允许——若希望缺失配置回退，调用方应先默认至 {@link #DEFAULT} 再调用本方法。
     */
    public static boolean isAllowed(String algorithm) {
        return algorithm != null && !algorithm.isBlank() && ALLOWED.contains(algorithm);
    }
}
