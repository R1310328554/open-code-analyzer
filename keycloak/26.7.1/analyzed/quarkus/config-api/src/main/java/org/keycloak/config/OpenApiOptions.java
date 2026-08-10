package org.keycloak.config;

/**
 * OpenAPI 文档与 UI 暴露相关配置选项。
 */
public class OpenApiOptions {

  /** 配置选项：openapi-enabled，是否暴露 OpenAPI 端点。 */
  public static final Option<Boolean> OPENAPI_ENABLED = new OptionBuilder<>("openapi-enabled", Boolean.class)
      .category(OptionCategory.OPENAPI)
      .description("If the server should expose OpenAPI Endpoint. If enabled, OpenAPI is available at '/openapi'.")
      .buildTime(true)
      .defaultValue(Boolean.FALSE)
      .build();
  /** 配置选项：openapi-ui-enabled，是否暴露 OpenAPI UI 端点。 */
  public static final Option<Boolean> OPENAPI_UI_ENABLED = new OptionBuilder<>("openapi-ui-enabled", Boolean.class)
      .category(OptionCategory.OPENAPI)
      .description("If the server should expose OpenApi-UI Endpoint. If enabled, OpenAPI UI is available at '/openapi/ui'.")
      .buildTime(true)
      .defaultValue(Boolean.FALSE)
      .build();
}
