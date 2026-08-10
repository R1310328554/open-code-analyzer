package org.keycloak.encoding;

import org.keycloak.models.KeycloakSession;

/**
 * 资源编码辅助类：根据请求 {@code Accept-Encoding} 头解析并选择匹配的编码提供者。
 */
public class ResourceEncodingHelper {

    /**
     * 按 Accept-Encoding 优先级查找支持指定 Content-Type 的编码提供者。
     * @param session 当前会话
     * @param contentType 响应 Content-Type
     * @return 匹配的 {@link ResourceEncodingProvider}，无匹配时返回 {@code null}
     */
    public static ResourceEncodingProvider getResourceEncodingProvider(KeycloakSession session, String contentType) {
        String acceptEncoding = session.getContext().getRequestHeaders().getHeaderString("Accept-Encoding");
        if (acceptEncoding != null) {
            for (String e : acceptEncoding.split(",")) {
                e = e.trim();
                ResourceEncodingProviderFactory f = (ResourceEncodingProviderFactory) session.getKeycloakSessionFactory().getProviderFactory(ResourceEncodingProvider.class, e);
                if (f != null && f.encodeContentType(contentType)) {
                    ResourceEncodingProvider provider = session.getProvider(ResourceEncodingProvider.class, e.trim());
                    if (provider != null) {
                        return provider;
                    }
                } else {
                    return null;
                }
            }
        }
        return null;
    }

}
