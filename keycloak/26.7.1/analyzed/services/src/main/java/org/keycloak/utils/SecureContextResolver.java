package org.keycloak.utils;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.function.Supplier;

import jakarta.ws.rs.core.HttpHeaders;

import org.keycloak.device.DeviceRepresentationProvider;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.account.DeviceRepresentation;

import io.netty.util.NetUtil;

/**
 * 安全上下文（Secure Context）判定工具。
 * <p>依据 W3C Secure Contexts 规范判断请求来源是否可被用户代理视为可信，影响 Cookie Secure 标志等行为。</p>
 */
public class SecureContextResolver {

    /**
     * 判断 Keycloak 会话是否处于安全上下文中。
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Security/Secure_Contexts">MDN Web Docs — Secure Contexts</a>
     * @see <a href="https://w3c.github.io/webappsec-secure-contexts/#algorithms">W3C Secure Contexts specification — Is origin potentially trustworthy?</a>
     * @param session 待检查的 Keycloak 会话
     * @return 可被用户代理视为可信时返回 true
     */
    public static boolean isSecureContext(KeycloakSession session) {
        KeycloakContext context = session.getContext();
        URI uri = context.getUri().getRequestUri();

        // 延迟解析 User-Agent，避免生产环境不必要的设备信息解析
        Supplier<DeviceRepresentation> deviceRepresentationSupplier = () -> {
            DeviceRepresentationProvider deviceRepresentationProvider = session.getProvider(DeviceRepresentationProvider.class);
            return deviceRepresentationProvider.deviceRepresentation();
        };

        HttpHeaders headers = context.getRequestHeaders();
        String referer = headers.getHeaderString("Referer");
        String secFetchDest = headers.getHeaderString("Sec-Fetch-Dest");

        return isSecureContext(uri, deviceRepresentationSupplier, referer, secFetchDest);
    }

    /** 包级可见：无 Referer/Sec-Fetch-Dest 的安全上下文判定。 */
    static boolean isSecureContext(URI uri, Supplier<DeviceRepresentation> deviceRepresentationSupplier) {
        return isSecureContext(uri, deviceRepresentationSupplier, null, null);
    }

    static boolean isSecureContext(URI uri, Supplier<DeviceRepresentation> deviceRepresentationSupplier, String referer, String secFetchDest) {
        if (uri.getScheme().equals("https")) {
            // W3C 规范：HTTPS iframe 嵌入 HTTP 父页面时不算安全上下文，通过 Sec-Fetch-Dest 检测
            // 参见 KEYCLOAK-37355 与 W3C Secure Contexts 规范
            if ("iframe".equals(secFetchDest) && isInsecureReferer(referer)) {
                return false;
            }
            return true;
        }

        DeviceRepresentation deviceRepresentation = deviceRepresentationSupplier.get();
        String browser = deviceRepresentation != null ? deviceRepresentation.getBrowser() : null;

        // Safari 存在 Secure Cookie 缺陷，保守降级为非安全上下文（KEYCLOAK-33557）
        if (browser != null && browser.toLowerCase().contains("safari")) {
            return false;
        }

        String host = uri.getHost();

        if (host == null) {
            return false;
        }

        return isLocal(host);
    }

    /** 判断主机名是否为本地（localhost 或回环地址）。 */
    public static boolean isLocal(String host) {
        return isLocalHost(host) || isLocalAddress(host);
    }

    /** 判断是否为 localhost 或其子域（*.localhost）。 */
    public static boolean isLocalHost(String host) {
        if (host.equals("localhost") || host.equals("localhost.")) {
            return true;
        }

        return host.endsWith(".localhost") || host.endsWith(".localhost.");
    }

    /**
     * 判断给定 IP 地址是否为回环地址。
     * @param address IP 地址字符串
     * @return 非回环或非合法地址时返回 false
     */
    public static boolean isLocalAddress(String address) {
        if (address == null) {
            return false;
        }

        if (NetUtil.isValidIpV4Address(address) || NetUtil.isValidIpV6Address(address)) {
            try {
                return InetAddress.getByName(address).isLoopbackAddress();
            } catch (UnknownHostException e) {
            }
        }

        return false;
    }

    /** 判断 Referer 是否为 HTTP（非 HTTPS）来源。 */
    private static boolean isInsecureReferer(String referer) {
        if (referer == null) {
            return false;
        }

        try {
            return "http".equals(new URI(referer).getScheme());
        } catch (URISyntaxException e) {
            return false;
        }
    }

}
