package org.keycloak.quarkus.runtime.configuration.mappers;

import java.util.List;

import org.keycloak.common.Profile;
import org.keycloak.config.OpenApiOptions;

import static org.keycloak.quarkus.runtime.configuration.Configuration.isTrue;
import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

/**
 * OpenAPI / Swagger UI 相关 {@link PropertyMapper} 分组：
 * 依赖 {@link Profile.Feature#OPENAPI} 特性，映射到 Quarkus SmallRye OpenAPI 与 Swagger UI 开关。
 */
public final class OpenApiPropertyMappers implements PropertyMapperGrouping {

  @Override
  public List<? extends PropertyMapper<?>> getPropertyMappers() {
    return List.of(
        fromOption(OpenApiOptions.OPENAPI_ENABLED)
            .isEnabled(OpenApiPropertyMappers::isClientApiEnabled, "OpenAPI feature is enabled")
            .to("quarkus.smallrye-openapi.enable")
            .build(),
        fromOption(OpenApiOptions.OPENAPI_UI_ENABLED)
            .isEnabled(OpenApiPropertyMappers::isOpenApiEnabled, "OpenAPI Endpoint is enabled")
            .to("quarkus.swagger-ui.enable")
            .build()
    );
  }

  /** OpenAPI 端点（非 UI）是否已启用。 */
  private static boolean isOpenApiEnabled() {
    return isTrue(OpenApiOptions.OPENAPI_ENABLED);
  }

  /** Client API OpenAPI 预览特性是否已启用。 */
  private static boolean isClientApiEnabled() {
    return Profile.isFeatureEnabled(Profile.Feature.OPENAPI);
  }
}
