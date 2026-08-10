package org.keycloak.utils;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

/**
 * 根据 HTTP {@code Accept} 头判断客户端期望的响应媒体类型。
 */
public class MediaTypeMatcher {

    private static final Logger logger = Logger.getLogger(MediaTypeMatcher.class);

    /** 判断 Accept 头是否包含 HTML 类型。 */
    public static boolean isHtmlRequest(HttpHeaders headers) {
        return isAcceptMediaType(headers, MediaType.TEXT_HTML_TYPE);
    }

    /** 判断 Accept 头是否包含 JSON 类型。 */
    public static boolean isJsonRequest(HttpHeaders headers) {
        return isAcceptMediaType(headers, MediaType.APPLICATION_JSON_TYPE);
    }

    /** 检查 Accept 列表中是否存在与目标类型兼容的非通配类型。 */
    private static boolean isAcceptMediaType(HttpHeaders headers, MediaType textHtmlType) {
        try {
            for (MediaType m : headers.getAcceptableMediaTypes()) {
                if (!m.isWildcardType() && m.isCompatible(textHtmlType)) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Accept 头格式非法时捕获异常并返回 false
            logger.debug("Could not determine if the media type is accepted", e);
        }
        return false;
    }
}
