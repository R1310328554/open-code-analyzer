package org.keycloak.device;

import jakarta.ws.rs.core.HttpHeaders;

import org.keycloak.cache.LocalCache;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.account.DeviceRepresentation;

import org.jboss.logging.Logger;
import ua_parser.Client;

/**
 * 设备表示提供者实现：从当前 HTTP 请求的 User-Agent 头解析浏览器、操作系统与 IP。
 * <p>解析结果封装为 {@link DeviceRepresentation}，供账户活动与设备管理使用。</p>
 */
public class DeviceRepresentationProviderImpl implements DeviceRepresentationProvider {
    private static final Logger logger = Logger.getLogger(DeviceActivityManager.class);
    /** User-Agent 头允许的最大长度（字节）。 */
    private static final int USER_AGENT_MAX_LENGTH = 512;

    private final LocalCache<String, Client> cache;
    private final KeycloakSession session;

    /** @param session 当前会话 @param cache User-Agent → 解析结果缓存 */
    DeviceRepresentationProviderImpl(KeycloakSession session, LocalCache<String, Client> cache) {
        this.session = session;
        this.cache = cache;
    }

    @Override
    /** 解析当前请求 User-Agent 并构建设备描述；无法解析时返回 {@code null}。 */
    public DeviceRepresentation deviceRepresentation() {
        KeycloakContext context = session.getContext();

        if (context.getRequestHeaders() == null) {
            return null;
        }

        String userAgent = context.getRequestHeaders().getHeaderString(HttpHeaders.USER_AGENT);

        if (userAgent == null) {
            return null;
        }

        if (userAgent.length() > USER_AGENT_MAX_LENGTH) {
            logger.warn("Ignoring User-Agent header. Length is above the permitted: " + USER_AGENT_MAX_LENGTH);
            return null;
        }

        DeviceRepresentation current;
        try {
            Client client = cache.get(userAgent);
            // 避免 IDEA 空指针警告；解析器理论上不会返回 null Client。
            assert client != null;
            current = new DeviceRepresentation();

            current.setDevice(client.device.family);

            String browserVersion = client.userAgent.major;

            if (client.userAgent.minor != null) {
                browserVersion += "." + client.userAgent.minor;
            }

            if (client.userAgent.patch != null) {
                browserVersion += "." + client.userAgent.patch;
            }

            if (browserVersion == null) {
                browserVersion = DeviceRepresentation.UNKNOWN;
            }

            current.setBrowser(client.userAgent.family, browserVersion);
            current.setOs(client.os.family);

            String osVersion = client.os.major;

            if (client.os.minor != null) {
                osVersion += "." + client.os.minor;
            }

            if (client.os.patch != null) {
                osVersion += "." + client.os.patch;
            }

            if (client.os.patchMinor != null) {
                osVersion += "." + client.os.patchMinor;
            }

            current.setOsVersion(resolveOsVersion(client, osVersion, browserVersion));
            current.setIpAddress(context.getConnection().getRemoteHost());
            current.setMobile(userAgent.toLowerCase().contains("mobile"));
            return current;
        } catch (Exception cause) {
            logger.error("Failed to create device info from user agent header", cause);
            return null;
        }
    }

    /** 针对 iOS Safari 特殊处理：当浏览器主版本高于 OS 主版本时用浏览器版本作为 OS 版本。 */
    static String resolveOsVersion(Client client, String osVersion, String browserVersion) {
        if (!"iOS".equalsIgnoreCase(client.os.family)) {
            return osVersion;
        }

        String browserFamily = client.userAgent.family;

        if (browserFamily == null || !browserFamily.toLowerCase().contains("safari")) {
            return osVersion;
        }

        if (toInt(client.userAgent.major) > toInt(client.os.major)) {
            return browserVersion;
        }

        return osVersion;
    }

    /** 将版本号主段解析为整数，失败时返回 -1。 */
    private static int toInt(String major) {
        try {
            return major == null ? -1 : Integer.parseInt(major.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
