package org.keycloak.ssf.transmitter.event;

import java.util.Set;

import org.keycloak.ssf.subject.EmailSubjectId;
import org.keycloak.ssf.subject.IssuerSubjectId;
import org.keycloak.ssf.transmitter.SsfTransmitterConfig;
import org.keycloak.ssf.transmitter.stream.StreamConfig;

/**
 * 解析并校验发送方为出站 SSF SET 的<em>用户</em>部分使用的主体标识符格式。
 *
 * <p>默认为 {@link IssuerSubjectId#TYPE iss_sub}（发送方 realm 的发行方 URL 加用户 Keycloak ID）。
 * 接收方客户端可通过 {@code ssf.userSubjectFormat} 属性按流覆盖为 {@link #ALLOWED} 中的值之一；
 * 其他值在流创建/更新时被拒绝，使错误配置在首次事件发出前暴露。</p>
 *
 * <p>另支持两种组合——{@link #COMPLEX_ISS_SUB_PLUS_TENANT complex.iss_sub+tenant} 与
 * {@link #COMPLEX_EMAIL_PLUS_TENANT complex.email+tenant}——将用户主体包装为复合主体并添加携带用户 Keycloak 组织的
 * {@code tenant} 兄弟成员。{@code complex.} 前缀表示 SET 将携带 {@link org.keycloak.ssf.subject.ComplexSubjectId ComplexSubjectId}
 * 而非单一主体；{@code +tenant} 后缀命名附加成员。二者均为 Keycloak 专有组合，非 RFC 9493 主体标识符格式；
 * 不理解 {@code critical_subject_members=["user","tenant"]} 的接收方会在创建流时拒绝。</p>
 *
 * <p>允许列表刻意保持精简——扩展至其他 SSF 主体格式（如 {@code phone_number}、{@code aliases}）
 * 只需扩展 mapper 的 {@code buildUserSubjectId} 分发并在此添加对应值。</p>
 */
public final class SsfUserSubjectFormats {

    /**
     * 组合前缀，表示 SET 将携带 {@link org.keycloak.ssf.subject.ComplexSubjectId ComplexSubjectId}
     * 而非单一用户主体。前缀后的部分命名用户主体组件（如 {@code iss_sub}/{@code email}）；
     * 附加兄弟成员通过 {@link #TENANT_SUFFIX} 等组合后缀追加。
     */
    public static final String COMPLEX_PREFIX = "complex.";

    /** 组合后缀，要求 mapper 添加 {@code tenant} 成员。 */
    public static final String TENANT_SUFFIX = "+tenant";

    /** 组合：complex(user={@code iss_sub}, tenant=用户所属组织)。 */
    public static final String COMPLEX_ISS_SUB_PLUS_TENANT = COMPLEX_PREFIX + IssuerSubjectId.TYPE + TENANT_SUFFIX;

    /** 组合：complex(user={@code email}, tenant=用户所属组织)。 */
    public static final String COMPLEX_EMAIL_PLUS_TENANT = COMPLEX_PREFIX + EmailSubjectId.TYPE + TENANT_SUFFIX;

    /**
     * 发送方已知如何为 SSF SET 的用户部分生成的主体标识符格式。
     * 在流创建/更新时通过 {@link #isAllowed(String)} 校验。
     */
    public static final Set<String> ALLOWED = Set.of(
            IssuerSubjectId.TYPE,
            EmailSubjectId.TYPE,
            COMPLEX_ISS_SUB_PLUS_TENANT,
            COMPLEX_EMAIL_PLUS_TENANT);

    /**
     * 默认用户主体标识符格式——realm 发行方加用户 Keycloak ID。
     * 与添加此旋钮前发送方的行为一致，现有部署无变化。
     */
    public static final String DEFAULT = IssuerSubjectId.TYPE;

    private SsfUserSubjectFormats() {
    }

    /**
     * {@code format} 携带 {@code +tenant} 组合后缀时返回 {@code true}——
     * 即 mapper 应向复合 SET 主体添加 tenant 主体兄弟成员。
     */
    public static boolean includesTenant(String format) {
        return format != null && format.endsWith(TENANT_SUFFIX);
    }

    /**
     * 剥离 {@link #COMPLEX_PREFIX complex.} 前缀与 {@link #TENANT_SUFFIX +tenant} 后缀，
     * 返回裸用户主体格式（{@code iss_sub}/{@code email}）。两者均不存在时原样返回。
     */
    public static String userPartOf(String format) {
        if (format == null) {
            return null;
        }
        String result = format;
        if (result.startsWith(COMPLEX_PREFIX)) {
            result = result.substring(COMPLEX_PREFIX.length());
        }
        if (result.endsWith(TENANT_SUFFIX)) {
            result = result.substring(0, result.length() - TENANT_SUFFIX.length());
        }
        return result;
    }

    /**
     * 解析给定流的用户主体格式，优先级：按流覆盖 → 发送方 SPI 默认 → 硬编码 {@link #DEFAULT}。
     */
    public static String resolveForStream(StreamConfig streamConfig, SsfTransmitterConfig transmitterConfig) {

        if (streamConfig != null) {
            String streamFormat = streamConfig.getUserSubjectFormat();
            if (streamFormat != null && !streamFormat.isBlank()) {
                return streamFormat;
            }
        }

        if (transmitterConfig != null) {
            String configFormat = transmitterConfig.getUserSubjectFormat();
            if (configFormat != null && !configFormat.isBlank()) {
                return configFormat;
            }
        }

        return DEFAULT;
    }

    /**
     * 给定格式在允许列表内时返回 {@code true}。
     * 空白或 {@code null} 视为不允许——若希望缺失配置回退，调用方应先默认至 {@link #DEFAULT} 再调用本方法。
     */
    public static boolean isAllowed(String format) {
        return format != null && !format.isBlank() && ALLOWED.contains(format);
    }
}
