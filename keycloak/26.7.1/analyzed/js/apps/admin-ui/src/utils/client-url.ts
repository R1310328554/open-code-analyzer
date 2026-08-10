/** 根据客户端 rootUrl/baseUrl 与当前环境解析可访问的完整 URL。 */
import ClientRepresentation from "@keycloak/keycloak-admin-client/lib/defs/clientRepresentation";
import type { Environment } from "../environment-types";
import { joinPath } from "./joinPath";

/** 将 Client 的 rootUrl、baseUrl 与 admin/server 基址组合为最终 URL。 */
export const convertClientToUrl = (
  { rootUrl, baseUrl }: ClientRepresentation,
  environment: Environment,
) => {
  // 已配置绝对 baseUrl 时优先使用
  if (baseUrl?.startsWith("http")) {
    return baseUrl;
  }

  if (rootUrl === "${authAdminUrl}") {
    return joinPath(
      rootUrl.replace(/\$\{(authAdminUrl)\}/, environment.adminBaseUrl),
      baseUrl || "",
    );
  }

  if (rootUrl === "${authBaseUrl}") {
    return joinPath(
      rootUrl.replace(/\$\{(authBaseUrl)\}/, environment.serverBaseUrl),
      baseUrl || "",
    );
  }

  if (rootUrl?.startsWith("http")) {
    if (baseUrl) {
      return joinPath(rootUrl, baseUrl);
    }
    return rootUrl;
  }

  return baseUrl;
};
