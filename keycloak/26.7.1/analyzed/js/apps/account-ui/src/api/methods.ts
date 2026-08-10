/**
 * Account Console REST API 封装层。
 * 将 Keycloak Account API 端点映射为类型安全的异步函数，供各页面组件调用。
 */
import {
  BaseEnvironment,
  type KeycloakContext,
} from "@keycloak/keycloak-ui-shared";

import OrganizationRepresentation from "@keycloak/keycloak-admin-client/lib/defs/organizationRepresentation";
import { parseResponse } from "./parse-response";
import {
  ClientRepresentation,
  CredentialContainer,
  DeviceRepresentation,
  Group,
  IssuedUserVerifiableCredentialRepresentation,
  LinkedAccountRepresentation,
  Permission,
  UserRepresentation,
  UserVerifiableCredentialRepresentation,
} from "./representations";
import { request } from "./request";

/** 通用 API 调用选项：Keycloak 上下文与可选的 AbortSignal。 */
export type CallOptions = {
  context: KeycloakContext<BaseEnvironment>;
  signal?: AbortSignal;
};

/** 分页查询参数：起始偏移与最大返回条数。 */
export type PaginationParams = {
  first: number;
  max: number;
};

/** 获取当前登录用户的个人信息（含用户档案元数据）。 */
export async function getPersonalInfo({
  signal,
  context,
}: CallOptions): Promise<UserRepresentation> {
  const response = await request("/?userProfileMetadata=true", context, {
    signal,
  });
  return parseResponse<UserRepresentation>(response);
}

/** 获取账户控制台支持的语言区域列表。 */
export async function getSupportedLocales({
  signal,
  context,
}: CallOptions): Promise<string[]> {
  const response = await request("/supportedLocales", context, { signal });
  return parseResponse<string[]>(response);
}

/**
 * 保存用户个人信息。
 * 失败时从响应体解析 errors 字段并抛出。
 */
export async function savePersonalInfo(
  context: KeycloakContext<BaseEnvironment>,
  info: UserRepresentation,
): Promise<void> {
  const response = await request("/", context, { body: info, method: "POST" });
  if (!response.ok) {
    const { errors } = await response.json();
    throw errors;
  }
  return undefined;
}

/** 获取指定资源上的权限申请（待审批）列表。 */
export async function getPermissionRequests(
  resourceId: string,
  { signal, context }: CallOptions,
): Promise<Permission[]> {
  const response = await request(
    `/resources/${resourceId}/permissions/requests`,
    context,
    { signal },
  );

  return parseResponse<Permission[]>(response);
}

/** 获取当前用户在各设备上的活跃会话列表。 */
export async function getDevices({
  signal,
  context,
}: CallOptions): Promise<DeviceRepresentation[]> {
  const response = await request("/sessions/devices", context, { signal });
  return parseResponse<DeviceRepresentation[]>(response);
}

/** 获取用户已授权访问的客户端（应用）列表。 */
export async function getApplications({
  signal,
  context,
}: CallOptions): Promise<ClientRepresentation[]> {
  const response = await request("/applications", context, { signal });
  return parseResponse<ClientRepresentation[]>(response);
}

/** 撤销对指定客户端的授权同意（consent）。 */
export async function deleteConsent(
  context: KeycloakContext<BaseEnvironment>,
  id: string,
) {
  return request(`/applications/${encodeURIComponent(id)}/consent`, context, {
    method: "DELETE",
  });
}

/**
 * 注销会话。
 * 未传 id 时注销除当前会话外的全部会话；传入 id 时仅注销指定会话。
 */
export async function deleteSession(
  context: KeycloakContext<BaseEnvironment>,
  id?: string,
) {
  return request(`/sessions${id ? `/${id}` : ""}`, context, {
    method: "DELETE",
  });
}

/** 获取用户已注册的凭据（密码、OTP 等）列表。 */
export async function getCredentials({ signal, context }: CallOptions) {
  const response = await request("/credentials", context, {
    signal,
  });
  return parseResponse<CredentialContainer[]>(response);
}

/** 关联账户列表的查询参数：分页、搜索关键词及是否已关联。 */
export type LinkedAccountQueryParams = PaginationParams & {
  search?: string;
  linked?: boolean;
};

/** 分页查询用户的关联账户（社交/身份联邦）列表。 */
export async function getLinkedAccounts(
  { signal, context }: CallOptions,
  query: LinkedAccountQueryParams,
) {
  const response = await request("/linked-accounts", context, {
    searchParams: Object.entries(query).reduce(
      (acc, [key, value]) => ({ ...acc, [key]: value.toString() }),
      {},
    ),
    signal,
  });
  return parseResponse<LinkedAccountRepresentation[]>(response);
}

/**
 * 解除与指定身份提供者的账户关联。
 * 成功时静默返回；失败时解析并抛出 API 错误。
 */
export async function unLinkAccount(
  context: KeycloakContext<BaseEnvironment>,
  account: LinkedAccountRepresentation,
) {
  const response = await request(
    "/linked-accounts/" + account.providerName,
    context,
    {
      method: "DELETE",
    },
  );
  if (response.ok) return;
  return parseResponse(response);
}

/** 获取当前用户所属的用户组列表。 */
export async function getGroups({ signal, context }: CallOptions) {
  const response = await request("/groups", context, {
    signal,
  });
  return parseResponse<Group[]>(response);
}

/** 获取当前用户所属的组织列表。 */
export async function getUserOrganizations({ signal, context }: CallOptions) {
  const response = await request("/organizations", context, { signal });
  return parseResponse<OrganizationRepresentation[]>(response);
}

/** 获取用户可申请的 Verifiable Credential 类型列表。 */
export async function getVerifiableCredentials({
  signal,
  context,
}: CallOptions): Promise<UserVerifiableCredentialRepresentation[]> {
  const response = await request("/verifiable-credentials", context, {
    signal,
  });
  return parseResponse<UserVerifiableCredentialRepresentation[]>(response);
}

/** 删除指定作用域名称的可验证凭据配置。 */
export async function deleteVerifiableCredential(
  context: KeycloakContext<BaseEnvironment>,
  credentialScopeName: string,
): Promise<void> {
  const response = await request(
    `/verifiable-credentials/${credentialScopeName}`,
    context,
    { method: "DELETE" },
  );
  if (!response.ok) {
    const error = await parseResponse(response);
    throw error;
  }
}

/** 获取已签发给当前用户的 Verifiable Credential 列表。 */
export async function getIssuedVerifiableCredentials({
  signal,
  context,
}: CallOptions): Promise<IssuedUserVerifiableCredentialRepresentation[]> {
  const response = await request("/issued-verifiable-credentials", context, {
    signal,
  });
  return parseResponse<IssuedUserVerifiableCredentialRepresentation[]>(
    response,
  );
}

/** 撤销已签发的可验证凭据。 */
export async function revokeIssuedVerifiableCredential(
  context: KeycloakContext<BaseEnvironment>,
  issuedVerifiableCredentialId: string,
): Promise<void> {
  const response = await request(
    `/issued-verifiable-credentials/${issuedVerifiableCredentialId}`,
    context,
    { method: "DELETE" },
  );
  if (!response.ok) {
    throw await parseResponse(response);
  }
}
