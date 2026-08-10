package org.keycloak.ssf;

import org.keycloak.models.RealmModel;
import org.keycloak.ssf.event.SsfEventProvider;

import static org.keycloak.utils.KeycloakSessionUtil.getKeycloakSession;

/**
 * Shared Signals Framework（SSF）常量与入口方法集合。
 * <p>定义 SSF 版本、OAuth 范围、well-known 路径、传输方式 URI 及事件提供者访问点。</p>
 */
public class Ssf {

    /** SSF 规范版本 {@code 1_0}。 */
    public static final String SSF_VERSION_1_0 = "1_0";

    /** OAuth 2.0 授权方案 URN（RFC 6749）。 */
    public static final String SSF_OAUTH_AUTHORIZATION_SCHEME_URN = "urn:ietf:rfc:6749";

    /** SSF 只读 OAuth 范围。 */
    public static final String SCOPE_SSF_READ = "ssf.read";

    /** SSF 管理 OAuth 范围。 */
    public static final String SCOPE_SSF_MANAGE = "ssf.manage";

    /** SSF 配置 well-known 元数据路径。 */
    public static final String SSF_WELL_KNOWN_METADATA_PATH = ".well-known/ssf-configuration";

    /** Realm 级 SSF REST 资源路径段。 */
    public static final String SSF_REALM_RESOURCE_PATH = "ssf";

    /** SSF Transmitter 子资源路径段。 */
    public static final String SSF_TRANSMITTER_PATH = "transmitter";

    /** SET 的 HTTP Content-Type（application/secevent+jwt）。 */
    public static final String APPLICATION_SECEVENT_JWT_TYPE = "application/secevent+jwt";

    /**
     * SET JWT 类型声明（4.1.1 Explicit Typing of SETs）。
     *
     * @see https://openid.github.io/sharedsignals/openid-sharedsignals-framework-1_0.html#section-4.1.1
     */
    public static final String SECEVENT_JWT_TYPE = "secevent+jwt";

    /** Push 交付方式 URI（RFC 8935）。 */
    public static final String DELIVERY_METHOD_PUSH_URI = "urn:ietf:rfc:8935";

    /** RISC Push 交付方式 URI。 */
    public static final String DELIVERY_METHOD_RISC_PUSH_URI = "https://schemas.openid.net/secevent/risc/delivery-method/push";

    /** Poll 交付方式 URI（RFC 8936）。 */
    public static final String DELIVERY_METHOD_POLL_URI = "urn:ietf:rfc:8936";

    /** RISC Poll 交付方式 URI。 */
    public static final String DELIVERY_METHOD_RISC_POLL_URI = "https://schemas.openid.net/secevent/risc/delivery-method/poll";

    /** Realm 属性键：是否启用 SSF Transmitter。 */
    public static final String SSF_TRANSMITTER_ENABLED_KEY = "ssf.transmitterEnabled";

    /** 工具类，禁止实例化。 */
    private Ssf() {
    }

    /** @return 当前会话的 {@link SsfEventProvider}，无会话时返回 null */
    public static SsfEventProvider events() {
        var session = getKeycloakSession();
        if (session == null) {
            return null;
        }
        return session.getProvider(SsfEventProvider.class);
    }

    /** @param realm 目标领域 @return 是否启用 SSF Transmitter */
    public static boolean isTransmitterEnabled(RealmModel realm) {
        return Boolean.parseBoolean(realm.getAttribute(SSF_TRANSMITTER_ENABLED_KEY));
    }

}
