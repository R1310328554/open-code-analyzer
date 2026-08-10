package org.keycloak.services.resources.account;

import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.keycloak.services.cors.Cors;

/**
 * 账户 API CORS 预检（OPTIONS）处理。
 * <p>Created by st on 21/03/17.</p>
 */
public class CorsPreflightService {

    /**
     * 处理 CORS 预检请求。
     *
     * @return 带 CORS 头的 200 响应
     */
    @Path("{any:.*}")
    @OPTIONS
    public Response preflight() {
        return Cors.builder().auth().allowedMethods("GET", "POST", "DELETE", "PUT", "HEAD", "OPTIONS").preflight()
                .add(Response.ok());
    }

}
