package org.keycloak.protocol.oidc.resourceindicators;

/**
 * 资源指标（RFC 8707）相关常量：错误消息、URN 前缀与客户端属性键。
 */
public interface ResourceIndicatorConstants {
    /** 错误描述：请求的 resource 与原始授权不一致 */
    String ERROR_NOT_MATCHING = "The requested resource is not matching the original request.";
    /** 错误描述：resource 无效、缺失、未知或格式错误 */
    String ERROR_INVALID_RESOURCE = "The requested resource is invalid, missing, unknown, or malformed.";
    /** 客户端 URN 资源指标前缀 */
    String URN_CLIENT_PREFIX = "urn:client:";
    /** 客户端属性键：与 resource 指标匹配的 URL */
    String CLIENT_RESOURCE_URL_ATTRIBUTE = "resource_url";
}
