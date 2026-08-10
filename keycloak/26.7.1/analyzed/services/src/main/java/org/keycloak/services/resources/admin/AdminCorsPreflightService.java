package org.keycloak.services.resources.admin;

import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.keycloak.services.cors.Cors;

/**
 * 管理 API CORS 预检（OPTIONS）服务。
 * <p>为管理控制台跨域请求返回允许的 HTTP 方法与认证头配置。</p>
 */
public class AdminCorsPreflightService {

    /**
     * 处理 CORS 预检请求。
     *
     * @return 含 CORS 头的 200 响应
     */
    @Path("{any:.*}")
    @OPTIONS
    public Response preflight() {
        return Cors.builder().preflight().allowedMethods("GET", "PUT", "POST", "DELETE").auth().add(Response.ok());
    }

}
