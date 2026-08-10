package org.keycloak.protocol.oidc.resourceindicators;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * 资源指标格式校验工具：支持绝对 URI 与符合规范的 URN。
 * <p>遵循 OAuth 2.0 Resource Indicators 对 resource 参数的形态要求。</p>
 */
public class ResourceIndicatorValidation {

    /** URN 资源指标正则（scheme 为 urn 时使用） */
    private static final Pattern URN_REGEX = Pattern.compile("^urn:[a-z0-9][a-z0-9-]{0,31}:([a-z0-9()+,-.:=@;$_!*']|%[0-9a-f]{2})++$", Pattern.CASE_INSENSITIVE);

    private ResourceIndicatorValidation() {
    }

    /**
     * 校验 resource 指标字符串是否合法；null 视为有效（未指定 resource）。
     * @param resourceIndicator 待校验的 resource 参数值
     * @return 格式合法返回 true
     */
        if (resourceIndicator == null) {
            return true;
        }

        try {
            URI uri = new URI(resourceIndicator);
            if ("urn".equalsIgnoreCase(uri.getScheme())) {
                return URN_REGEX.matcher(resourceIndicator).matches();
            } else {
                if (!uri.isAbsolute()) {
                    return false;
                } else if (uri.getFragment() != null) {
                    return false;
                } else if (uri.getQuery() != null) {
                    return false;
                } else if (uri.getPath() == null) {
                    return false;
                }
            }
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

}
