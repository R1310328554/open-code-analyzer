import {
  KeycloakContext,
  type BaseEnvironment,
} from "@keycloak/keycloak-ui-shared";

import { CallOptions } from "./api/methods";
import { Links, parseLinks } from "./api/parse-links";
import { parseResponse } from "./api/parse-response";
import { Permission, Resource, Scope } from "./api/representations";
import { request } from "./api/request";

/**
 * 获取当前用户可访问的资源列表（自有或共享）。
 *
 * @param options 含 AbortSignal 与 Keycloak 上下文的调用选项
 * @param requestParams 查询参数字典（非 shared 模式时未使用）
 * @param shared 为 true 时请求 `/resources/shared-with-me` 端点
 */
export const fetchResources = async (
  { signal, context }: CallOptions,
  requestParams: Record<string, string>,
  shared: boolean | undefined = false,
): Promise<{ data: Resource[]; links: Links }> => {
  const response = await request(
    `/resources${shared ? "/shared-with-me?" : "?"}`,
    context,
    { searchParams: shared ? requestParams : undefined, signal },
  );

  const links = parseLinks(response);

  return {
    data: checkResponse(await response.json()),
    links,
  };
};

/**
 * 获取指定资源上的权限授予列表。
 *
 * @param options 含 AbortSignal 与 Keycloak 上下文的调用选项
 * @param resourceId 资源标识
 */
export const fetchPermission = async (
  { signal, context }: CallOptions,
  resourceId: string,
): Promise<Permission[]> => {
  const response = await request(
    `/resources/${resourceId}/permissions`,
    context,
    { signal },
  );
  return parseResponse<Permission[]>(response);
};

/**
 * 向指定资源提交权限更新请求（单用户 + 作用域列表）。
 *
 * @param context Keycloak 运行时上下文
 * @param resourceId 资源标识
 * @param username 被授权用户名
 * @param scopes 作用域对象或名称数组
 */
export const updateRequest = (
  context: KeycloakContext<BaseEnvironment>,
  resourceId: string,
  username: string,
  scopes: Scope[] | string[],
) =>
  request(`/resources/${resourceId}/permissions`, context, {
    method: "PUT",
    body: [{ username, scopes }],
  });

/**
 * 批量更新指定资源的完整权限列表。
 *
 * @param context Keycloak 运行时上下文
 * @param resourceId 资源标识
 * @param permissions 权限对象数组
 */
export const updatePermissions = (
  context: KeycloakContext<BaseEnvironment>,
  resourceId: string,
  permissions: Permission[],
) =>
  request(`/resources/${resourceId}/permissions`, context, {
    method: "PUT",
    body: permissions,
  });

/** 校验 API 响应非空，否则抛出获取失败错误。 */
function checkResponse<T>(response: T) {
  if (!response) throw new Error("Could not fetch");
  return response;
}
