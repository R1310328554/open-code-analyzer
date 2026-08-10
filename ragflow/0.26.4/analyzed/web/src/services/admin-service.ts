/**
 * admin-service.ts — 管理后台 HTTP 客户端：独立 axios 实例，封装用户/角色/白名单/沙箱等管理 API。
 */

import { history } from '@/utils/simple-history-util';
import axios from 'axios';

import message from '@/components/ui/message';
import { Authorization } from '@/constants/authorization';
import i18n from '@/locales/config';
import { Routes } from '@/routes';
import api from '@/utils/api';
import authorizationUtil, {
  getAuthorization,
} from '@/utils/authorization-util';
import { convertTheKeysOfTheObjectToSnake } from '@/utils/common-util';
import { ResultCode, RetcodeMessage } from '@/utils/request';

/** 管理端专用 axios 实例（超时 300s），与主站 request 分离。 */
const request = axios.create({
  timeout: 300000,
});

/** 请求拦截：body/params 转 snake_case，并注入 Authorization（除非 skipToken）。 */
request.interceptors.request.use((config) => {
  const data = convertTheKeysOfTheObjectToSnake(config.data);
  const params = convertTheKeysOfTheObjectToSnake(config.params) as any;

  const newConfig = { ...config, data, params };

  // @ts-ignore
  if (!newConfig.skipToken) {
    newConfig.headers.set(Authorization, getAuthorization());
  }

  return newConfig;
});

/** 响应拦截：统一处理 code 100/401 及网络异常，401 时清凭证并跳转 Admin 登录页。 */
request.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response;
    }

    const { data } = response ?? {};

    if (data?.code === 100) {
      message.error(data?.message);
    } else if (data?.code === 401) {
      message.error(data?.message, {
        description: data?.message,
      });

      authorizationUtil.removeAll();
      history.push(Routes.Admin);
      window.location.reload();
    } else if (data?.code && data.code !== 0) {
      message.error(`${i18n.t('message.hint')}: ${data?.code}`, {
        description: data?.message,
      });
    }

    return response;
  },
  (error) => {
    const { response } = error;
    const { data } = response ?? {};

    if (error.message === 'Failed to fetch') {
      message.error({
        description: i18n.t('message.networkAnomalyDescription'),
        message: i18n.t('message.networkAnomaly'),
      });
    } else if (data?.code === 100) {
      message.error(data?.message);
    } else if (response.status === 401 || data?.code === 401) {
      message.error({
        message: data?.message || response.statusText,
        description:
          data?.message || RetcodeMessage[response?.status as ResultCode],
        duration: 3,
      });

      authorizationUtil.removeAll();
      history.push(Routes.Admin);
      window.location.reload();
    } else if (data?.code && data.code !== 0) {
      message.error({
        message: `${i18n.t('message.hint')}: ${data?.code}`,
        description: data?.message,
        duration: 3,
      });
    } else if (response.status) {
      message.error({
        message: `${i18n.t('message.requestError')} ${response.status}: ${response.config.url}`,
        description:
          RetcodeMessage[response.status as ResultCode] || response.statusText,
      });
    } else if (response.status === 413 || response?.status === 504) {
      message.error(RetcodeMessage[response?.status as ResultCode]);
    }

    throw error;
  },
);

const {
  adminLogin,
  adminLogout,
  adminListUsers,
  adminCreateUser,
  adminGetUserDetails,
  adminUpdateUserStatus,
  adminUpdateUserPassword,
  adminDeleteUser,
  adminListUserDatasets,
  adminListUserAgents,

  adminListServices,
  adminShowServiceDetails,

  adminListRoles,
  adminListRolesWithPermission,
  adminCreateRole,
  adminDeleteRole,
  adminUpdateRoleDescription,
  adminGetRolePermissions,
  adminAssignRolePermissions,
  adminRevokeRolePermissions,

  adminGetUserPermissions,
  adminUpdateUserRole,

  adminListResources,

  adminListWhitelist,
  adminCreateWhitelistEntry,
  adminUpdateWhitelistEntry,
  adminDeleteWhitelistEntry,
  adminImportWhitelist,

  adminGetSystemVersion,

  adminListSandboxProviders,
  adminGetSandboxProviderSchema,
  adminGetSandboxConfig,
  adminSetSandboxConfig,
  adminTestSandboxConnection,
} = api;

/** 管理端 API 通用响应包装：code / message / data。 */
type ResponseData<D = NonNullable<unknown>> = {
  code: number;
  message: string;
  data: D;
};

/** 管理员登录，返回 access_token 与用户资料。 */
export const login = (params: { email: string; password: string }) =>
  request.post<ResponseData<AdminService.LoginData>>(adminLogin, params);
/** 管理员登出。 */
export const logout = () => request.get<ResponseData<boolean>>(adminLogout);
/** 列出全部用户（邮箱、角色、激活状态等）。 */
export const listUsers = () =>
  request.get<ResponseData<AdminService.ListUsersItem[]>>(adminListUsers, {});

/** 创建新用户（username 即 email）。 */
export const createUser = (email: string, password: string) =>
  request.post<ResponseData<boolean>>(adminCreateUser, {
    username: email,
    password,
  });

/** 授予指定用户超级管理员权限。 */
export const grantSuperuser = (email: string) =>
  request.put<ResponseData<void>>(api.adminSetSuperuser(email));

/** 撤销指定用户的超级管理员权限。 */
export const revokeSuperuser = (email: string) =>
  request.delete<ResponseData<void>>(api.adminSetSuperuser(email));

/** 获取单个用户详情。 */
export const getUserDetails = (email: string) =>
  request.get<ResponseData<[AdminService.UserDetail]>>(
    adminGetUserDetails(email),
  );
/** 列出指定用户拥有的知识库/数据集。 */
export const listUserDatasets = (email: string) =>
  request.get<ResponseData<AdminService.ListUserDatasetItem[]>>(
    adminListUserDatasets(email),
  );
/** 列出指定用户创建的 Agent 画布。 */
export const listUserAgents = (email: string) =>
  request.get<ResponseData<AdminService.ListUserAgentItem[]>>(
    adminListUserAgents(email),
  );
/** 启用/禁用用户账号（activate_status）。 */
export const updateUserStatus = (email: string, status: 'on' | 'off') =>
  request.put(adminUpdateUserStatus(email), { activate_status: status });
/** 重置指定用户密码。 */
export const updateUserPassword = (email: string, password: string) =>
  request.put(adminUpdateUserPassword(email), { new_password: password });
/** 删除用户。 */
export const deleteUser = (email: string) =>
  request.delete(adminDeleteUser(email));

/** 列出系统微服务心跳状态（alive/timeout/fail）。 */
export const listServices = () =>
  request.get<ResponseData<AdminService.ListServicesItem[]>>(adminListServices);
/** 查看单个服务详情（含 task_executor 队列统计）。 */
export const showServiceDetails = (serviceId: number) =>
  request.get<ResponseData<AdminService.ServiceDetail>>(
    adminShowServiceDetails(String(serviceId)),
  );

/** 创建新角色。 */
export const createRole = (params: {
  roleName: string;
  description?: string;
}) =>
  request.post<ResponseData<AdminService.RoleDetail>>(adminCreateRole, params);
/** 更新角色描述。 */
export const updateRoleDescription = (role: string, description: string) =>
  request.put<ResponseData<AdminService.RoleDetail>>(
    adminUpdateRoleDescription(role),
    { description },
  );
/** 删除角色。 */
export const deleteRole = (role: string) =>
  request.delete<ResponseData<ResponseData<never>>>(adminDeleteRole(role));
/** 分页列出角色（不含权限明细）。 */
export const listRoles = () =>
  request.get<
    ResponseData<{ roles: AdminService.ListRoleItem[]; total: number }>
  >(adminListRoles);
/** 列出角色及其关联权限矩阵。 */
export const listRolesWithPermission = () =>
  request.get<
    ResponseData<{
      roles: AdminService.ListRoleItemWithPermission[];
      total: number;
    }>
  >(adminListRolesWithPermission);
/** 获取单个角色的权限详情。 */
export const getRolePermissions = (role: string) =>
  request.get<ResponseData<AdminService.RoleDetailWithPermission>>(
    adminGetRolePermissions(role),
  );
/** 为角色批量授予权限（new_permissions）。 */
export const assignRolePermissions = (
  role: string,
  permissions: Partial<AdminService.AssignRolePermissionsInput>,
) =>
  request.post<ResponseData<never>>(adminAssignRolePermissions(role), {
    new_permissions: permissions,
  });
/** 从角色批量撤销权限（revoke_permissions）。 */
export const revokeRolePermissions = (
  role: string,
  permissions: Partial<AdminService.RevokeRolePermissionInput>,
) =>
  request.delete<ResponseData<never>>(adminRevokeRolePermissions(role), {
    data: { revoke_permissions: permissions },
  });

/** 修改用户所属角色。 */
export const updateUserRole = (username: string, role: string) =>
  request.put<ResponseData<never>>(adminUpdateUserRole(username), {
    role_name: role,
  });
/** 获取用户及其角色权限快照。 */
export const getUserPermissions = (username: string) =>
  request.get<ResponseData<AdminService.UserDetailWithPermission>>(
    adminGetUserPermissions(username),
  );
/** 列出可授权的资源类型枚举。 */
export const listResources = () =>
  request.get<ResponseData<AdminService.ResourceType>>(adminListResources);

/** 列出注册白名单条目。 */
export const listWhitelist = () =>
  request.get<
    ResponseData<{
      total: number;
      white_list: AdminService.ListWhitelistItem[];
    }>
  >(adminListWhitelist);

/** 新增白名单邮箱。 */
export const createWhitelistEntry = (email: string) =>
  request.post<ResponseData<never>>(adminCreateWhitelistEntry, { email });

/** 更新白名单条目邮箱。 */
export const updateWhitelistEntry = (id: number, email: string) =>
  request.put<ResponseData<never>>(adminUpdateWhitelistEntry(id), { email });

/** 按邮箱删除白名单条目。 */
export const deleteWhitelistEntry = (email: string) =>
  request.delete<ResponseData<never>>(adminDeleteWhitelistEntry(email));

/** 从 Excel 批量导入白名单。 */
export const importWhitelistFromExcel = (file: File) => {
  const fd = new FormData();

  fd.append('file', file);

  return request.post<ResponseData<never>>(adminImportWhitelist, fd);
};

/** 获取系统版本号。 */
export const getSystemVersion = () =>
  request.get<ResponseData<{ version: string }>>(adminGetSystemVersion);

// 沙箱（Sandbox）配置相关 API
/** 列出可用沙箱提供商（id/name/description/tags）。 */
export const listSandboxProviders = () =>
  request.get<ResponseData<AdminService.SandboxProvider[]>>(
    adminListSandboxProviders,
  );

/** 获取指定提供商的配置字段 schema。 */
export const getSandboxProviderSchema = (providerId: string) =>
  request.get<ResponseData<Record<string, AdminService.SandboxConfigField>>>(
    adminGetSandboxProviderSchema(providerId),
  );

/** 读取当前沙箱配置。 */
export const getSandboxConfig = () =>
  request.get<ResponseData<AdminService.SandboxConfig>>(adminGetSandboxConfig);

/** 保存沙箱配置（provider_type + config）。 */
export const setSandboxConfig = (params: {
  providerType: string;
  config: Record<string, unknown>;
}) =>
  request.post<ResponseData<AdminService.SandboxConfig>>(
    adminSetSandboxConfig,
    {
      provider_type: params.providerType,
      config: params.config,
    },
  );

/** 测试沙箱连接并返回执行结果（stdout/stderr/exit_code）。 */
export const testSandboxConnection = (params: {
  providerType: string;
  config: Record<string, unknown>;
}) =>
  request.post<
    ResponseData<{
      success: boolean;
      message: string;
      details?: {
        exit_code: number;
        execution_time: number;
        stdout: string;
        stderr: string;
      };
    }>
  >(adminTestSandboxConnection, {
    provider_type: params.providerType,
    config: params.config,
  });
