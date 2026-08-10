import type { KeycloakAdminClient } from "../client.js";
import type SynchronizationResultRepresentation from "../defs/synchronizationResultRepresentation.js";
import Resource from "./resource.js";

type ActionType = "triggerFullSync" | "triggerChangedUsersSync";
export type DirectionType = "fedToKeycloak" | "keycloakToFed";
type NameResponse = {
  id: string;
  name: string;
};

/** 用户存储提供者 Admin 资源：联邦用户同步、导入用户清理与映射器同步。 */
export class UserStorageProvider extends Resource<{ realm?: string }> {
  /** 获取用户存储提供者名称 */
  public name = this.makeRequest<{ id: string }, NameResponse>({
    method: "GET",
    path: "/{id}/name",
    urlParamKeys: ["id"],
  });

  /** 移除已导入的外部用户 */
  public removeImportedUsers = this.makeRequest<{ id: string }, void>({
    method: "POST",
    path: "/{id}/remove-imported-users",
    urlParamKeys: ["id"],
  });

  /** 触发用户存储同步 */
  public sync = this.makeRequest<
    { id: string; action?: ActionType },
    SynchronizationResultRepresentation
  >({
    method: "POST",
    path: "/{id}/sync",
    urlParamKeys: ["id"],
    queryParamKeys: ["action"],
  });

  /** 解除用户与外部存储关联 */
  public unlinkUsers = this.makeRequest<{ id: string }, void>({
    method: "POST",
    path: "/{id}/unlink-users",
    urlParamKeys: ["id"],
  });

  /** 触发属性映射器同步 */
  public mappersSync = this.makeRequest<
    { id: string; parentId: string; direction?: DirectionType },
    SynchronizationResultRepresentation
  >({
    method: "POST",
    path: "/{parentId}/mappers/{id}/sync",
    urlParamKeys: ["id", "parentId"],
    queryParamKeys: ["direction"],
  });

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/admin/realms/{realm}/user-storage",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }
}
