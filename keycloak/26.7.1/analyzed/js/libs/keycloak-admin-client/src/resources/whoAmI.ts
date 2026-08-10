import type WhoAmIRepresentation from "../defs/whoAmIRepresentation.js";
import type KeycloakAdminClient from "../index.js";
import Resource from "./resource.js";

/** 当前登录身份 Admin 资源：Admin Console 下查询 whoami（Realm/用户上下文）。 */
export class WhoAmI extends Resource<{ realm?: string }> {
  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/admin/{realm}/console",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }

  /** 查询列表 */
  /** 查询当前 Admin Console 登录身份（whoami） */
  public find = this.makeRequest<
    { currentRealm: string },
    WhoAmIRepresentation
  >({
    method: "GET",
    path: "/whoami",
    queryParamKeys: ["currentRealm"],
  });
}
