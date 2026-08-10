import Resource from "./resource.js";
import type { KeycloakAdminClient } from "../client.js";

/**
 * Realm 缓存管理 Admin 资源：清除用户、密钥、CRL 及 Realm 级缓存。
 * 对应 `/admin/realms/{realm}/clear-*-cache` 端点，常用于配置变更后强制刷新。
 */
export class Cache extends Resource<{ realm?: string }> {
  /** 清除用户缓存（用户实体与相关查询缓存） */
  public clearUserCache = this.makeRequest<{}, void>({
    method: "POST",
    path: "/clear-user-cache",
  });
  /** 清除密钥缓存（Realm/客户端签名与加密密钥） */
  public clearKeysCache = this.makeRequest<{}, void>({
    method: "POST",
    path: "/clear-keys-cache",
  });
  /** 清除 CRL（证书吊销列表）缓存 */
  public clearCrlCache = this.makeRequest<{}, void>({
    method: "POST",
    path: "/clear-crl-cache",
  });
  /** 清除整个 Realm 配置与元数据缓存 */
  public clearRealmCache = this.makeRequest<{}, void>({
    method: "POST",
    path: "/clear-realm-cache",
  });

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/admin/realms/{realm}",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }
}
