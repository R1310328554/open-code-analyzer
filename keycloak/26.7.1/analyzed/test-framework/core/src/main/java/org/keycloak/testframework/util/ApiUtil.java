package org.keycloak.testframework.util;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;

/**
 * Keycloak Java Admin Client 的测试辅助工具。
 */
public class ApiUtil {

    /**
     * 从 Admin API POST 响应的 {@code Location} 头解析新建资源的 ID。
     * <p>
     * 许多创建端点不在响应体中返回实体，而是仅提供位置头；本方法同时断言 HTTP 201 并关闭 {@link Response}。
     *
     * @param response POST 请求的 JAX-RS 响应，例如创建 realm 用户
     * @return 新建资源的 ID，例如新用户的 UUID
     */
    public static String getCreatedId(Response response) {
        try (response) {
            Assertions.assertEquals(201, response.getStatus());
            String path = response.getLocation().getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        }
    }

}
