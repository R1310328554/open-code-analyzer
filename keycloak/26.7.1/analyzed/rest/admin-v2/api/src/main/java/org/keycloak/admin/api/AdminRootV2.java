package org.keycloak.admin.api;

import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * Admin API v2 的根 JAX-RS 资源，挂载于 {@code /admin/api}。
 * <p>
 * 按领域名称路由到 {@link AdminApi}，并处理 CORS 预检请求。
 */
@Path("admin/api")
public interface AdminRootV2 {

    /**
     * 返回指定领域的 Admin API v2 子资源。
     *
     * @param realmName 领域名称
     */
    @Path("{realmName}")
    AdminApi adminApi(@PathParam("realmName") String realmName);

    // TODO Fix preflights
    /**
     * CORS 预检（OPTIONS）处理端点，匹配领域下任意子路径。
     */
    @Path("{realmName}/{any:.*}")
    @OPTIONS
    @Operation(hidden = true)
    Response preFlight();
}
