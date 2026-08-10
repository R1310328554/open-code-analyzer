import KeycloakAdminClient, {
  fetchWithError,
} from "@keycloak/keycloak-admin-client";
import { getAuthorizationHeaders } from "../../utils/getAuthorizationHeaders";
import { joinPath } from "../../utils/joinPath";
import { UiRealmInfo } from "./uiRealmInfo";

/**
 * 对当前领域下的 Admin UI 扩展端点发起 GET 请求。
 * 路径形如 {baseUrl}/admin/realms/{realm}/{endpoint}，自动附带访问令牌。
 */
export async function fetchAdminUI<T>(
  adminClient: KeycloakAdminClient,
  endpoint: string,
  query?: Record<string, string>,
): Promise<T> {
  const accessToken = await adminClient.getAccessToken();
  const baseUrl = adminClient.baseUrl;

  const response = await fetchWithError(
    joinPath(
      baseUrl,
      "admin/realms",
      encodeURIComponent(adminClient.realmName),
      endpoint,
    ) + (query ? "?" + new URLSearchParams(query) : ""),
    {
      method: "GET",
      headers: getAuthorizationHeaders(accessToken),
    },
  );

  return await response.json();
}

/**
 * 对 Admin UI 扩展端点发起 POST（JSON  body）。
 * 响应体为空时返回 undefined，否则解析 JSON。
 */
export async function postAdminUI<T>(
  adminClient: KeycloakAdminClient,
  endpoint: string,
  body: unknown,
): Promise<T | undefined> {
  const accessToken = await adminClient.getAccessToken();
  const baseUrl = adminClient.baseUrl;

  const response = await fetchWithError(
    joinPath(
      baseUrl,
      "admin/realms",
      encodeURIComponent(adminClient.realmName),
      endpoint,
    ),
    {
      method: "POST",
      headers: {
        ...getAuthorizationHeaders(accessToken),
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    },
  );

  const text = await response.text();
  return text ? JSON.parse(text) : undefined;
}

/** 拉取 ui-ext/info，返回领域级 UI 能力开关（如用户配置存储是否启用）。 */
export async function fetchRealmInfo(
  adminClient: KeycloakAdminClient,
): Promise<UiRealmInfo> {
  return fetchAdminUI(adminClient, `ui-ext/info`);
}
