import type { BaseEnvironment } from "@keycloak/keycloak-ui-shared";

/**
 * Admin Console 运行时注入的环境配置，扩展共享 BaseEnvironment。
 * 包含管理控制台 URL、主领域名及资源版本等 SPA 启动所需信息。
 */
export type Environment = BaseEnvironment & {
  /**
   * 管理控制台根 URL（含路径前缀），已按 hostname 配置归一化且不含尾部斜杠。
   * 例如 Keycloak 在 auth.example.com，控制台可能在 admin.example.com/some/path。
   *
   * @see {@link https://www.keycloak.org/server/hostname#_administration_console}
   */
  adminBaseUrl: string;
  /** Admin Console SPA 静态资源基址。 */
  consoleBaseUrl: string;
  /** master 领域名称。 */
  masterRealm: string;
  /** 认证服务器构建/资源版本哈希，用于缓存 bust。 */
  resourceVersion: string;
};
