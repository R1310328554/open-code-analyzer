import Resource from "./resource.js";
import type KeycloakAdminClient from "../index.js";

/**
 * 暴力破解检测（Attack Detection）Admin 资源：查询与清除用户 brute-force 锁定状态。
 * 对应 `/admin/realms/{realm}/attack-detection/brute-force` 端点。
 */
export class AttackDetection extends Resource<{ realm?: string }> {
  /** 查询指定用户的暴力破解检测状态（404 时返回 null） */
  public findOne = this.makeRequest<
    { id: string },
    Record<string, any> | undefined
  >({
    method: "GET",
    path: "/users/{id}",
    urlParamKeys: ["id"],
    catchNotFound: true,
  });

  /** 清除指定用户的暴力破解检测记录与临时锁定 */
  public del = this.makeRequest<{ id: string }, void>({
    method: "DELETE",
    path: "/users/{id}",
    urlParamKeys: ["id"],
  });

  /** 清除 Realm 内所有用户的暴力破解检测状态 */
  public delAll = this.makeRequest<{}, void>({
    method: "DELETE",
    path: "/users",
  });

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/admin/realms/{realm}/attack-detection/brute-force",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }
}
