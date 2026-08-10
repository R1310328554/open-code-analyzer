/** 细粒度管理权限（Fine-Grained Admin Permissions）对某资源的引用与 scope 权限映射。 */
export interface ManagementPermissionReference {
  /** 是否已对该资源启用细粒度管理权限 */
  enabled?: boolean;
  /** 受保护资源的标识（通常为资源类型或内部 ID） */
  resource?: string;
  /** scope 名称到权限策略 ID 的映射（如 view → policy-uuid） */
  scopePermissions?: Record<string, string>;
}
