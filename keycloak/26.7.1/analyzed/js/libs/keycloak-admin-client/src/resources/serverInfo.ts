import Resource from "./resource.js";
import type { ServerInfoRepresentation } from "../defs/serverInfoRepesentation.js";
import type KeycloakAdminClient from "../index.js";
import type EffectiveMessageBundleRepresentation from "../defs/effectiveMessageBundleRepresentation.js";

/** 消息包查询参数 */
export interface MessageBundleQuery {
  realm: string;
  theme?: string;
  themeType?: string;
  locale?: string;
  source?: boolean;
}

/** 服务器信息 Admin 资源：Keycloak 版本/特性及主题消息包查询。 */
export class ServerInfo extends Resource {
  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/",
      getBaseUrl: () => client.baseUrl,
    });
  }

  /** 查询列表 */
  public find = this.makeRequest<{}, ServerInfoRepresentation>({
    method: "GET",
    path: "/admin/serverinfo",
  });

  /** 获取有效消息包 */
  public findEffectiveMessageBundles = this.makeRequest<
    MessageBundleQuery,
    EffectiveMessageBundleRepresentation[]
  >({
    method: "GET",
    path: "/resources/{realm}/{themeType}/{locale}",
    urlParamKeys: ["realm", "themeType", "locale"],
    queryParamKeys: ["theme", "source"],
  });
}
