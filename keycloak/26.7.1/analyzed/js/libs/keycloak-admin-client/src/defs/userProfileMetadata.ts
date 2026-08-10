/** 用户档案（User Profile）完整配置：属性定义、分组及未托管属性策略 */
export interface UserProfileConfig {
  /** 用户属性定义列表 */
  attributes?: UserProfileAttribute[];
  /** 属性分组定义列表 */
  groups?: UserProfileGroup[];
  /** 未在配置中声明的属性（legacy attributes）的处理策略 */
  unmanagedAttributePolicy?: UnmanagedAttributePolicy;
}

/** 单个用户档案属性的声明式配置（Admin 配置侧） */
export interface UserProfileAttribute {
  /** 属性内部名称（键） */
  name?: string;
  /** 校验规则（键为校验器 ID，值为参数） */
  validations?: Record<string, unknown>;
  /** 校验器配置（与 validations 同义，部分版本使用） */
  validators?: Record<string, unknown>;
  /** 扩展注解（UI 提示、占位符等） */
  annotations?: Record<string, unknown>;
  /** 必填条件（按角色或客户端作用域） */
  required?: UserProfileAttributeRequired;
  /** 是否只读（用户不可编辑） */
  readOnly?: boolean;
  /** 查看/编辑权限（按角色或客户端） */
  permissions?: UserProfileAttributePermissions;
  /** 作用域选择器（仅在特定 OIDC scope 下展示） */
  selector?: UserProfileAttributeSelector;
  /** 显示名称（i18n 键或明文） */
  displayName?: string;
  /** 所属分组名称 */
  group?: string;
  /** 是否允许多值 */
  multivalued?: boolean;
  /** 默认值 */
  defaultValue?: string;
}

/** 属性必填条件：满足任一角色或作用域即视为必填 */
export interface UserProfileAttributeRequired {
  /** 要求用户具备的角色列表 */
  roles?: string[];
  /** 要求客户端令牌具备的作用域列表 */
  scopes?: string[];
}

/** 属性级查看/编辑权限（角色或客户端标识列表） */
export interface UserProfileAttributePermissions {
  /** 可查看该属性的主体列表 */
  view?: string[];
  /** 可编辑该属性的主体列表 */
  edit?: string[];
}

/** 属性作用域选择器：限定在特定 OIDC scope 下生效 */
export interface UserProfileAttributeSelector {
  /** 关联的 OIDC 作用域名称列表 */
  scopes?: string[];
}

/** 用户档案 UI 分组定义 */
export interface UserProfileGroup {
  /** 分组内部名称 */
  name?: string;
  /** 分组标题（显示用） */
  displayHeader?: string;
  /** 分组描述 */
  displayDescription?: string;
  /** 扩展注解 */
  annotations?: Record<string, unknown>;
}

/** 用户档案属性元数据（运行时/API 响应侧，供 Account/Registration UI 渲染） */
export interface UserProfileAttributeMetadata {
  /** 属性名称 */
  name?: string;
  /** 显示名称 */
  displayName?: string;
  /** 是否必填 */
  required?: boolean;
  /** 是否只读 */
  readOnly?: boolean;
  /** 所属分组 */
  group?: string;
  /** 扩展注解 */
  annotations?: Record<string, unknown>;
  /** 已解析的校验器及其参数 */
  validators?: Record<string, Record<string, unknown>>;
  /** 是否多值 */
  multivalued?: boolean;
  /** 默认值 */
  defaultValue?: string;
}

/** 用户档案分组元数据（运行时/API 响应侧） */
export interface UserProfileAttributeGroupMetadata {
  /** 分组名称 */
  name?: string;
  /** 分组标题 */
  displayHeader?: string;
  /** 分组描述 */
  displayDescription?: string;
  /** 扩展注解 */
  annotations?: Record<string, unknown>;
}

/** 用户档案元数据：Account/Admin 获取用户时可附带的属性与分组描述 */
export interface UserProfileMetadata {
  /** 属性元数据列表 */
  attributes?: UserProfileAttributeMetadata[];
  /** 分组元数据列表 */
  groups?: UserProfileAttributeGroupMetadata[];
}

/** 未托管用户属性的处理策略 */
export enum UnmanagedAttributePolicy {
  /** 完全禁用未声明属性 */
  Disabled = "DISABLED",
  /** 允许读写未声明属性 */
  Enabled = "ENABLED",
  /** 仅 Admin 可查看未声明属性 */
  AdminView = "ADMIN_VIEW",
  /** Admin 可查看并编辑未声明属性 */
  AdminEdit = "ADMIN_EDIT",
}
