import Resource from "./resource.js";
import type RequiredActionProviderRepresentation from "../defs/requiredActionProviderRepresentation.js";
import type { KeycloakAdminClient } from "../client.js";
import type AuthenticationExecutionInfoRepresentation from "../defs/authenticationExecutionInfoRepresentation.js";
import type AuthenticationFlowRepresentation from "../defs/authenticationFlowRepresentation.js";
import type AuthenticatorConfigRepresentation from "../defs/authenticatorConfigRepresentation.js";
import type { AuthenticationProviderRepresentation } from "../defs/authenticatorConfigRepresentation.js";
import type AuthenticatorConfigInfoRepresentation from "../defs/authenticatorConfigInfoRepresentation.js";
import type RequiredActionProviderSimpleRepresentation from "../defs/requiredActionProviderSimpleRepresentation.js";
import type RequiredActionConfigInfoRepresentation from "../defs/requiredActionConfigInfoRepresentation.js";
import type RequiredActionConfigRepresentation from "../defs/requiredActionConfigRepresentation.js";

/**
 * 认证管理 Admin 资源：Required Action、认证流（Flow）、执行器（Execution）及配置 CRUD。
 * https://www.keycloak.org/docs-api/8.0/rest-api/index.html#_authentication_management_resource
 */
export class AuthenticationManagement extends Resource<{ realm?: string }> {
  /** 注册新的 Required Action 提供者实例 */
  public registerRequiredAction = this.makeRequest<Record<string, any>>({
    method: "POST",
    path: "/register-required-action",
  });

  /** 获取 Realm 已注册的 Required Action 列表 */
  public getRequiredActions = this.makeRequest<
    void,
    RequiredActionProviderRepresentation[]
  >({
    method: "GET",
    path: "/required-actions",
  });

  /** 按别名获取单个 Required Action 配置 */
  public getRequiredActionForAlias = this.makeRequest<{
    alias: string;
  }>({
    method: "GET",
    path: "/required-actions/{alias}",
    urlParamKeys: ["alias"],
    catchNotFound: true,
  });

  /** 列出可用的客户端认证器（Client Authenticator）提供者 */
  public getClientAuthenticatorProviders = this.makeRequest<
    void,
    AuthenticationProviderRepresentation[]
  >({
    method: "GET",
    path: "/client-authenticator-providers",
  });

  /** 列出可用的认证器（Authenticator）提供者 */
  public getAuthenticatorProviders = this.makeRequest<
    void,
    AuthenticationProviderRepresentation[]
  >({
    method: "GET",
    path: "/authenticator-providers",
  });

  /** 列出可用的表单动作（Form Action）提供者 */
  public getFormActionProviders = this.makeRequest<
    void,
    AuthenticationProviderRepresentation[]
  >({
    method: "GET",
    path: "/form-action-providers",
  });

  /** 更新指定别名的 Required Action 配置 */
  public updateRequiredAction = this.makeUpdateRequest<
    { alias: string },
    RequiredActionProviderRepresentation,
    void
  >({
    method: "PUT",
    path: "/required-actions/{alias}",
    urlParamKeys: ["alias"],
  });

  /** 删除指定别名的 Required Action */
  public deleteRequiredAction = this.makeRequest<{ alias: string }, void>({
    method: "DELETE",
    path: "/required-actions/{alias}",
    urlParamKeys: ["alias"],
  });

  /** 降低 Required Action 在 UI/流程中的显示优先级 */
  public lowerRequiredActionPriority = this.makeRequest<{
    alias: string;
  }>({
    method: "POST",
    path: "/required-actions/{alias}/lower-priority",
    urlParamKeys: ["alias"],
  });

  /** 提高 Required Action 的显示优先级 */
  public raiseRequiredActionPriority = this.makeRequest<{
    alias: string;
  }>({
    method: "POST",
    path: "/required-actions/{alias}/raise-priority",
    urlParamKeys: ["alias"],
  });

  /** 获取尚未注册到 Realm 的 Required Action 提供者列表 */
  public getUnregisteredRequiredActions = this.makeRequest<
    void,
    RequiredActionProviderSimpleRepresentation[]
  >({
    method: "GET",
    path: "/unregistered-required-actions",
  });

  /** 列出 Realm 内所有认证流 */
  public getFlows = this.makeRequest<{}, AuthenticationFlowRepresentation[]>({
    method: "GET",
    path: "/flows",
  });

  /** 按 ID 获取单个认证流 */
  public getFlow = this.makeRequest<
    { flowId: string },
    AuthenticationFlowRepresentation
  >({
    method: "GET",
    path: "/flows/{flowId}",
    urlParamKeys: ["flowId"],
  });

  /** 列出可用的表单（Form）提供者 */
  public getFormProviders = this.makeRequest<
    void,
    AuthenticationProviderRepresentation[]
  >({
    method: "GET",
    path: "/form-providers",
  });

  /** 创建新认证流（Location 头返回新流 ID） */
  public createFlow = this.makeRequest<
    AuthenticationFlowRepresentation,
    AuthenticationFlowRepresentation
  >({
    method: "POST",
    path: "/flows",
    returnResourceIdInLocationHeader: { field: "id" },
  });

  /** 复制已有认证流并重命名 */
  public copyFlow = this.makeRequest<{ flow: string; newName: string }>({
    method: "POST",
    path: "/flows/{flow}/copy",
    urlParamKeys: ["flow"],
  });

  /** 删除认证流 */
  public deleteFlow = this.makeRequest<{ flowId: string }>({
    method: "DELETE",
    path: "/flows/{flowId}",
    urlParamKeys: ["flowId"],
  });

  /** 更新认证流定义 */
  public updateFlow = this.makeUpdateRequest<
    { flowId: string },
    AuthenticationFlowRepresentation
  >({
    method: "PUT",
    path: "/flows/{flowId}",
    urlParamKeys: ["flowId"],
  });

  /** 获取认证流内的执行器（Execution）列表及状态 */
  public getExecutions = this.makeRequest<
    { flow: string },
    AuthenticationExecutionInfoRepresentation[]
  >({
    method: "GET",
    path: "/flows/{flow}/executions",
    urlParamKeys: ["flow"],
  });

  /** 向认证流添加执行器配置 */
  public addExecution = this.makeUpdateRequest<
    { flow: string },
    AuthenticationExecutionInfoRepresentation
  >({
    method: "POST",
    path: "/flows/{flow}/executions",
    urlParamKeys: ["flow"],
  });

  /** 向流中添加具体 Authenticator 执行步骤 */
  public addExecutionToFlow = this.makeRequest<
    { flow: string; provider: string },
    AuthenticationExecutionInfoRepresentation
  >({
    method: "POST",
    path: "/flows/{flow}/executions/execution",
    urlParamKeys: ["flow"],
    returnResourceIdInLocationHeader: { field: "id" },
  });

  /** 向流中嵌套子认证流（sub-flow） */
  public addFlowToFlow = this.makeRequest<
    {
      flow: string;
      alias: string;
      type: string;
      provider: string;
      description: string;
    },
    AuthenticationFlowRepresentation
  >({
    method: "POST",
    path: "/flows/{flow}/executions/flow",
    urlParamKeys: ["flow"],
    returnResourceIdInLocationHeader: { field: "id" },
  });

  /** 更新流内执行器配置（启用/必需/条件等） */
  public updateExecution = this.makeUpdateRequest<
    { flow: string },
    AuthenticationExecutionInfoRepresentation
  >({
    method: "PUT",
    path: "/flows/{flow}/executions",
    urlParamKeys: ["flow"],
  });

  /** 删除单个执行器 */
  public delExecution = this.makeRequest<{ id: string }>({
    method: "DELETE",
    path: "/executions/{id}",
    urlParamKeys: ["id"],
  });

  /** 降低执行器在流内的优先级 */
  public lowerPriorityExecution = this.makeRequest<{ id: string }>({
    method: "POST",
    path: "/executions/{id}/lower-priority",
    urlParamKeys: ["id"],
  });

  /** 提高执行器在流内的优先级 */
  public raisePriorityExecution = this.makeRequest<{ id: string }>({
    method: "POST",
    path: "/executions/{id}/raise-priority",
    urlParamKeys: ["id"],
  });

  /** 获取 Required Action 提供者的可配置项元数据描述 */
  public getRequiredActionConfigDescription = this.makeRequest<
    { alias: string },
    RequiredActionConfigInfoRepresentation
  >({
    method: "GET",
    path: "/required-actions/{alias}/config-description",
    urlParamKeys: ["alias"],
  });

  /** 获取当前 Realm 中某 Required Action 的运行时配置 */
  public getRequiredActionConfig = this.makeRequest<
    { alias: string },
    RequiredActionConfigRepresentation
  >({
    method: "GET",
    path: "/required-actions/{alias}/config",
    urlParamKeys: ["alias"],
  });

  /** 移除 Required Action 的 Realm 级配置 */
  public removeRequiredActionConfig = this.makeRequest<{ alias: string }>({
    method: "DELETE",
    path: "/required-actions/{alias}/config",
    urlParamKeys: ["alias"],
  });

  /** 更新 Required Action 的 Realm 级配置 */
  public updateRequiredActionConfig = this.makeUpdateRequest<
    { alias: string },
    RequiredActionConfigRepresentation,
    void
  >({
    method: "PUT",
    path: "/required-actions/{alias}/config",
    urlParamKeys: ["alias"],
  });

  /** 获取 Authenticator 提供者的配置项元数据（按 providerId） */
  public getConfigDescription = this.makeRequest<
    { providerId: string },
    AuthenticatorConfigInfoRepresentation
  >({
    method: "GET",
    path: "config-description/{providerId}",
    urlParamKeys: ["providerId"],
  });

  /** 为执行器创建 Authenticator 配置实例 */
  public createConfig = this.makeRequest<
    AuthenticatorConfigRepresentation,
    AuthenticatorConfigRepresentation
  >({
    method: "POST",
    path: "/executions/{id}/config",
    urlParamKeys: ["id"],
    returnResourceIdInLocationHeader: { field: "id" },
  });

  /** 更新 Authenticator 配置 */
  public updateConfig = this.makeRequest<
    AuthenticatorConfigRepresentation,
    void
  >({
    method: "PUT",
    path: "/config/{id}",
    urlParamKeys: ["id"],
  });

  /** 按 ID 获取 Authenticator 配置 */
  public getConfig = this.makeRequest<
    { id: string },
    AuthenticatorConfigRepresentation
  >({
    method: "GET",
    path: "/config/{id}",
    urlParamKeys: ["id"],
  });

  /** 删除 Authenticator 配置 */
  public delConfig = this.makeRequest<{ id: string }>({
    method: "DELETE",
    path: "/config/{id}",
    urlParamKeys: ["id"],
  });

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/admin/realms/{realm}/authentication",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }
}
