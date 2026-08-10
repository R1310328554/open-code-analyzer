package org.keycloak.rest.admin.api;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.keycloak.admin.api.AdminApi;
import org.keycloak.admin.api.AdminRootV2;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.AdminCorsPreflightService;

/**
 * {@link org.keycloak.admin.api.AdminRootV2} JAX-RS 入口：在 Admin API v2 特性启用时暴露 realm 级 API 与 CORS 预检。
 */
@Provider
public class DefaultAdminRootV2 implements AdminRootV2 {

  @Context
  protected KeycloakSession session;

  /** {@inheritDoc} 返回指定 realm 的 Admin API v2 实例。 */
  @Override
  public AdminApi adminApi(String realmName) {
    checkApiEnabled();
    return new DefaultAdminApi(session, realmName);
  }

  /** {@inheritDoc} 处理 Admin API v2 的 CORS 预检请求。 */
  @Override
  public Response preFlight() {
    checkApiEnabled();
    return new AdminCorsPreflightService().preflight();
  }

  /** 特性未启用时抛出 404。 */
  private void checkApiEnabled() {
    if (!isAdminApiV2Enabled()) {
      throw new NotFoundException();
    }
  }

  /** 判断 {@link Profile.Feature#CLIENT_ADMIN_API_V2} 是否已启用。 */
  public static boolean isAdminApiV2Enabled() {
    return Profile.isFeatureEnabled(Profile.Feature.CLIENT_ADMIN_API_V2); // 当前 v2 仅包含 Client API
  }
}
