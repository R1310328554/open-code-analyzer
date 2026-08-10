/**
 * admin.service.d.ts — 管理后台 API 的 TypeScript 类型声明（AdminService 命名空间）。
 */

/** 管理端接口响应与请求体的类型集合。 */
declare namespace AdminService {
  /** 管理员登录成功后的用户会话数据。 */
  export type LoginData = {
    access_token: string;
    avatar: unknown;
    color_schema: 'Bright' | 'Dark';
    create_date: string;
    create_time: number;
    email: string;
    id: string;
    is_active: '0' | '1';
    is_anonymous: '0' | '1';
    is_authenticated: '0' | '1';
    is_superuser: boolean;
    language: string;
    last_login_time: string;
    login_channel: unknown;
    nickname: string;
    password: string;
    status: '0' | '1';
    timezone: string;
    update_date: [string];
    update_time: [number];
  };

  /** 用户列表单行摘要。 */
  export type ListUsersItem = {
    create_date: string;
    email: string;
    is_active: '0' | '1';
    is_superuser: boolean;
    role: string;
    nickname: string;
  };

  /** 单个用户完整资料。 */
  export type UserDetail = {
    avatar?: string;
    create_date: string;
    email: string;
    is_active: '0' | '1';
    is_anonymous: '0' | '1';
    is_superuser: boolean;
    language: string;
    last_login_time: string;
    login_channel: unknown;
    status: '0' | '1';
    update_date: string;
    role: string;
  };

  /** 用户名下知识库/数据集摘要。 */
  export type ListUserDatasetItem = {
    avatar?: string;
    chunk_num: number;
    create_date: string;
    doc_num: number;
    language: string;
    name: string;
    permission: string;
    status: '0' | '1';
    token_num: number;
    update_date: string;
  };

  /** 用户创建的 Agent 画布摘要。 */
  export type ListUserAgentItem = {
    avatar?: string;
    canvas_category: 'agent';
    permission: 'string';
    title: string;
  };

  /** task_executor 心跳单条记录（队列 done/failed/pending 等）。 */
  export type TaskExecutorHeartbeatItem = {
    name: string;
    boot_at: string;
    now: string;
    ip_address: string;
    current: Record<string, object>;
    done: number;
    failed: number;
    lag: number;
    pending: number;
    pid: number;
  };

  /** 按 executor 名称分组的任务执行器心跳映射。 */
  export type TaskExecutorInfo = Record<string, TaskExecutorHeartbeatItem[]>;

  /** 微服务列表项（host/port/status）。 */
  export type ListServicesItem = {
    extra: Record<string, unknown>;
    host: string;
    id: number;
    name: string;
    port: number;
    service_type: string;
    status: 'alive' | 'timeout' | 'fail';
  };

  /** 服务详情：普通服务返回 message 字符串/对象；task_executor 返回 TaskExecutorInfo。 */
  export type ServiceDetail =
    | {
        service_name: string;
        status: 'alive' | 'timeout';
        message: string | Record<string, any> | Record<string, any>[];
      }
    | {
        service_name: 'task_executor';
        status: 'alive' | 'timeout';
        message: AdminService.TaskExecutorInfo;
      };

  /** 资源权限四维：enable/read/write/share。 */
  export type PermissionData = {
    enable: boolean;
    read: boolean;
    write: boolean;
    share: boolean;
  };

  /** 角色列表项（不含权限）。 */
  export type ListRoleItem = {
    id: string;
    role_name: string;
    description: string;
    create_date: string;
    update_date: string;
  };

  /** 角色列表项，附带 permissions 矩阵。 */
  export type ListRoleItemWithPermission = ListRoleItem & {
    permissions: Record<string, PermissionData>;
  };

  /** 角色详情及其权限集合。 */
  export type RoleDetailWithPermission = {
    role: {
      id: string;
      name: string;
      description: string;
    };
    permissions: Record<string, PermissionData>;
  };

  /** 角色基本信息。 */
  export type RoleDetail = {
    id: string;
    name: string;
    description: string;
    create_date: string;
    update_date: string;
  };

  /** 授予角色权限时的输入（资源 → 部分 PermissionData）。 */
  export type AssignRolePermissionsInput = Record<
    string,
    Partial<PermissionData>
  >;
  /** 撤销角色权限时的输入，结构与 AssignRolePermissionsInput 相同。 */
  export type RevokeRolePermissionInput = AssignRolePermissionsInput;

  /** 用户及其角色权限快照。 */
  export type UserDetailWithPermission = {
    user: {
      id: string;
      username: string;
      role: string;
    };
    role_permissions: Record<string, PermissionData>;
  };

  /** 可授权资源类型列表。 */
  export type ResourceType = {
    resource_types: string[];
  };

  /** 注册白名单条目。 */
  export type ListWhitelistItem = {
    id: number;
    email: string;
    create_date: string;
    create_time: number;
    update_date: string;
    update_time: number;
  };

  // 沙箱配置相关类型
  /** 沙箱提供商元信息。 */
  export type SandboxProvider = {
    id: string;
    name: string;
    description: string;
    tags: string[];
  };

  /** 沙箱配置字段公共属性（label/placeholder/required 等）。 */
  export type SandboxConfigFieldBase = {
    required?: boolean;
    label?: string;
    placeholder?: string;
    description?: string;
    multiline?: boolean;
    readonly?: boolean;
    scope?: 'runtime' | 'deployment';
  };

  /** 字符串类型沙箱配置字段。 */
  export type SandboxConfigStringField = SandboxConfigFieldBase & {
    type: 'string';
    default?: string;
    secret?: boolean;
  };

  /** 整数类型沙箱配置字段（含 min/max）。 */
  export type SandboxConfigIntegerField = SandboxConfigFieldBase & {
    type: 'integer';
    default?: number;
    min?: number;
    max?: number;
  };

  /** 布尔类型沙箱配置字段。 */
  export type SandboxConfigBooleanField = SandboxConfigFieldBase & {
    type: 'boolean';
    default?: boolean;
  };

  /** JSON 类型沙箱配置字段。 */
  export type SandboxConfigJsonField = SandboxConfigFieldBase & {
    type: 'json';
    default?: unknown;
  };

  /** 沙箱配置字段联合类型。 */
  export type SandboxConfigField =
    | SandboxConfigStringField
    | SandboxConfigIntegerField
    | SandboxConfigBooleanField
    | SandboxConfigJsonField;

  /** 当前生效的沙箱配置（provider_type + config 键值）。 */
  export type SandboxConfig = {
    provider_type: string;
    config: Record<string, unknown>;
  };
}
