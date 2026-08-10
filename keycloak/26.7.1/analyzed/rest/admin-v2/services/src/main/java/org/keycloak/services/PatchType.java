package org.keycloak.services;

import java.util.Optional;

import jakarta.ws.rs.core.MediaType;

import org.keycloak.admin.api.PatchTypeNames;

/**
 * Admin API v2 PATCH 请求支持的补丁媒体类型枚举。
 */
public enum PatchType {
    /** JSON Merge Patch（{@link PatchTypeNames#JSON_MERGE}）。 */
    JSON_MERGE(PatchTypeNames.JSON_MERGE);

    private final MediaType mediaType;

    PatchType(String mediaType) {
        this.mediaType = MediaType.valueOf(mediaType);
    }

    /** 返回该补丁类型对应的 JAX-RS {@link MediaType}。 */
    public MediaType getMediaType() {
        return mediaType;
    }

    /** 按 Content-Type 字符串解析补丁类型；非法格式返回 empty。 */
    public static Optional<PatchType> getByMediaType(String mediaType) {
        try {
            return getByMediaType(MediaType.valueOf(mediaType));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** 按 {@link MediaType} 解析兼容的补丁类型。 */
    public static Optional<PatchType> getByMediaType(MediaType mediaType) {
        for (var type : PatchType.values()) {
            if (type.getMediaType().isCompatible(mediaType)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
