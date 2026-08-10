package org.keycloak.ssf.transmitter.support;

import java.time.Duration;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.logging.Logger;

/** SSF 发送方通用工具：issuer 解析、时长解析、接收方判定、管理事件路径解析等。 */
public class SsfUtil {

    /** 标记客户端为 SSF 接收方的属性键。 */
    public static final String SSF_ENABLED_KEY = "ssf.enabled";

    private static final Logger log = Logger.getLogger(SsfUtil.class);

    /**
     * 解析当前领域的 SSF issuer URL。优先 realm {@code frontendUrl} 属性，
     * 其次 {@code KC_HOSTNAME_URL} 环境变量，再次 Keycloak {@code --hostname} 全 URL，
     * 最后当前 HTTP 请求 base URI。均无可用上下文时抛出 {@link IllegalStateException}。
     */
    public static String getIssuerUrl(KeycloakSession session) {
        KeycloakContext context = session.getContext();
        RealmModel realm = context.getRealm();

        String frontendUrl = realm.getAttribute("frontendUrl");
        if (frontendUrl != null && !frontendUrl.isBlank())  {
            return frontendUrl;
        }

        String hostnameUrl = System.getenv().get("KC_HOSTNAME_URL");
        if (hostnameUrl != null && !hostnameUrl.isBlank()) {
            return appendRealmPath(hostnameUrl, realm.getName());
        }

        String configuredHostname = Config.scope("hostname", "v2").get("hostname");
        if (configuredHostname != null && !configuredHostname.isBlank()
            && (configuredHostname.startsWith("http://") || configuredHostname.startsWith("https://"))) {
            return appendRealmPath(configuredHostname, realm.getName());
        }

        try {
            return appendRealmPath(context.getUri().getBaseUri().toString(), realm.getName());
        } catch (RuntimeException ignored) {
            // 无活跃 HTTP 请求上下文（例如定时 outbox 排水器）。
        }

        throw new IllegalStateException(
                "Cannot resolve SSF issuer URL for realm '" + realm.getName() + "' outside an HTTP request. "
                        + "Configure one of: the realm 'frontendUrl' attribute, the KC_HOSTNAME_URL environment variable, "
                        + "or the Keycloak '--hostname' option with a full URL.");
    }

    /** 将 base URL 与领域名拼接为 {@code .../realms/<realm>} 形式。 */
    private static String appendRealmPath(String baseUrl, String realmName) {
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        return baseUrl + "realms/" + realmName;
    }

    /**
     * 最小时长解析器：支持 {@code ms}、{@code s}、{@code m}、{@code h}、{@code d} 后缀，
     * 无单位时按秒解析。
     *
     * @param value 待解析字符串
     * @param defaultMillis 解析失败时的默认毫秒值
     */
    public static long parseDurationMillis(String value, long defaultMillis) {
        try {
            String trimmed = value.trim().toLowerCase();
            if (trimmed.endsWith("ms")) {
                return Long.parseLong(trimmed.substring(0, trimmed.length() - 2).trim());
            }
            if (trimmed.endsWith("s")) {
                return Duration.ofSeconds(Long.parseLong(trimmed.substring(0, trimmed.length() - 1).trim())).toMillis();
            }
            if (trimmed.endsWith("m")) {
                return Duration.ofMinutes(Long.parseLong(trimmed.substring(0, trimmed.length() - 1).trim())).toMillis();
            }
            if (trimmed.endsWith("h")) {
                return Duration.ofHours(Long.parseLong(trimmed.substring(0, trimmed.length() - 1).trim())).toMillis();
            }
            if (trimmed.endsWith("d")) {
                return Duration.ofDays(Long.parseLong(trimmed.substring(0, trimmed.length() - 1).trim())).toMillis();
            }
            return Duration.ofSeconds(Long.parseLong(trimmed)).toMillis();
        } catch (NumberFormatException e) {
            log.warnf("Invalid interval '%s' — falling back to default %dms", value, defaultMillis);
            return defaultMillis;
        }
    }

    /** 将 Jackson {@link JsonNode} 转为 {@code Map<String, Object>}。 */
    public static Map<String, Object> treeToMap(JsonNode node) {
        return JsonSerialization.mapper.convertValue(node, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 纯配置层面的接收方检查：客户端携带 {@code ssf.enabled=true} 时为 {@code true}。
     * 对 {@code null}、普通 OIDC 应用、服务账户或未设置/显式 {@code false} 的客户端返回 {@code false}。
     *
     * <p>仅反映<em>配置</em>——客户端是否设为接收方；<em>不</em>考虑启用/禁用状态。
     * 已禁用但已配置的接收方此处仍为 {@code true}。关心实时投递的调用方用 {@link #isReceiverEnabled}。
     */
    public static boolean isReceiverClient(ClientModel client) {
        if (client == null) {
            return false;
        }
        return Boolean.parseBoolean(client.getAttribute(SSF_ENABLED_KEY));
    }

    /**
     * 实时投递接收方检查：客户端既是已配置 SSF 接收方（{@link #isReceiverClient}）<em>且</em>
     * Keycloak 客户端已启用。禁用客户端（标准开关）使其流退出驱动<em>新</em>投递决策的查找——
     * 流枚举、合成 emit、接收方认证门控——关闭期间不排队或推送新 SSF 事件。对应领域级发送方禁用
     * （{@link org.keycloak.ssf.Ssf#isTransmitterEnabled}）。
     *
     * <p><em>不</em>取消禁用前已入 outbox 的事件：排水器仍可能通过 {@code getStreamForClient} 解析并推送。
     * 流配置保留，重新启用客户端即可恢复投递。见 keycloak/keycloak#50050。
     */
    public static boolean isReceiverEnabled(ClientModel client) {
        return isReceiverClient(client) && client.isEnabled();
    }

    /** 管理事件资源路径中 {@code users/} 前缀。 */
    private static final String ADMIN_EVENT_USERS_PREFIX = "users/";

    /** 从用户类型管理事件中提取用户 id；非用户资源或路径无效时返回 {@code null}。 */
    public static String userIdFromAdminEventPath(AdminEvent adminEvent) {
        if (adminEvent == null) {
            return null;
        }
        if (!ResourceType.USER.equals(adminEvent.getResourceType())) {
            return null;
        }
        return userIdFromAdminEventPath(adminEvent.getResourcePath());
    }

    /** 从 {@code users/<id>/...} 形式资源路径解析用户 id。 */
    public static String userIdFromAdminEventPath(String resourcePath) {
        if (resourcePath == null || !resourcePath.startsWith(ADMIN_EVENT_USERS_PREFIX)) {
            return null;
        }
        int start = ADMIN_EVENT_USERS_PREFIX.length();
        int end = resourcePath.indexOf('/', start);
        String id = end < 0 ? resourcePath.substring(start) : resourcePath.substring(start, end);
        return id.isEmpty() ? null : id;
    }

}
