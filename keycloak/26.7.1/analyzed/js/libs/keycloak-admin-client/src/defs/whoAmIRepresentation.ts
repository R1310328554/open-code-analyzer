/** 权限检查器：基于当前 Admin 会话的 realm_access 判断权限组合 */
export type AccessChecker = {
  /** 是否同时具备所列全部权限类型 */
  hasAll: (...types: AccessType[]) => boolean;
  /** 是否具备所列权限类型中的任意一种 */
  hasAny: (...types: AccessType[]) => boolean;
};

/** 自定义权限谓词：接收 AccessChecker 并返回布尔结果 */
export type AccessTypeFunc = (accessChecker: AccessChecker) => boolean;

/** Admin Console / REST 细粒度权限类型或自定义检查函数 */
export type AccessType =
  | "view-realm"
  | "view-identity-providers"
  | "manage-identity-providers"
  | "impersonation"
  | "create-client"
  | "manage-users"
  | "query-realms"
  | "view-authorization"
  | "query-clients"
  | "query-users"
  | "manage-events"
  | "manage-realm"
  | "view-events"
  | "view-users"
  | "view-clients"
  | "manage-authorization"
  | "manage-clients"
  | "manage-organizations"
  | "view-organizations"
  | "query-groups"
  | "query-organizations"
  | "admin"
  | "realm-admin"
  | "anyone"
  | AccessTypeFunc;

/**
 * WhoAmI 响应：当前 Admin 令牌对应用户、Realm 及跨 Realm 管理权限摘要。
 * 用于 Admin Console 判断菜单可见性与操作授权。
 */
export default interface WhoAmIRepresentation {
  /** 当前 Admin 用户 UUID */
  userId: string;
  /** 当前操作的 Realm 名称 */
  realm: string;
  /** 用户显示名 */
  displayName: string;
  /** 用户区域设置（如 en、zh-CN） */
  locale: string;
  /** 是否允许创建新 Realm（master 级权限） */
  createRealm: boolean;
  /** 各 Realm 下Granted 的 AccessType 列表（键为 realm 名） */
  realm_access: { [key: string]: AccessType[] };
  /** 是否为临时/ impersonation 会话 */
  temporary: boolean;
}
