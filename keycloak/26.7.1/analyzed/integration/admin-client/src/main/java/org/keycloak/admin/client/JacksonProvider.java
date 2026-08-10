package org.keycloak.admin.client;

import jakarta.ws.rs.core.MediaType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;

/**
 * 管理客户端专用的 Jackson JSON 提供程序，兼容不同版本 Keycloak 服务端的表示模型差异。
 */
public class JacksonProvider extends ResteasyJackson2Provider {

    /**
     * 定位并配置 {@link ObjectMapper}，使其在序列化时忽略 null 字段，
     * 并在反序列化时容忍未知 JSON 属性。
     */
    @Override
    public ObjectMapper locateMapper(Class<?> type, MediaType mediaType) {
        ObjectMapper objectMapper = super.locateMapper(type, mediaType);

        // 与 JSONSerialization 类行为一致，使 admin-client 可对接属性可能不同的旧版 Keycloak 服务端
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        // 客户端须兼容新版服务端中客户端尚未识别的 JSON 字段，故忽略未知属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }
}
