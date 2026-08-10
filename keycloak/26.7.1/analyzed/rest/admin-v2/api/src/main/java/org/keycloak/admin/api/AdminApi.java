package org.keycloak.admin.api;

import jakarta.ws.rs.Path;

import org.keycloak.admin.api.client.ClientsApi;

/**
 * 领域级 Admin API v2 入口，聚合各资源子 API。
 */
public interface AdminApi {

    /**
     * 获取客户端 Admin API v2 接口。
     */
    @Path("clients/v2")
    ClientsApi clientsV2();
}
