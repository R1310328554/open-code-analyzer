/**
 * user-service.ts — 用户与租户 API：登录注册、资料、系统令牌及 Langfuse 配置。
 */

import api from '@/utils/api';
import registerServer from '@/utils/register-server';
import request, { post } from '@/utils/request';

const {
  login,
  logout,
  register,
  setting,
  userInfo,
  tenantInfo,
  getSystemVersion,
  getSystemTokenList,
  removeSystemToken,
  createSystemToken,
  getSystemConfig,
  setLangfuseConfig,
} = api;

/** registerServer 方法表：URL + HTTP 动词映射。 */
const methods = {
  login: {
    url: login,
    method: 'post',
  },
  logout: {
    url: logout,
    method: 'post',
  },
  register: {
    url: register,
    method: 'post',
  },
  setting: {
    url: setting,
    method: 'patch',
  },
  userInfo: {
    url: userInfo,
    method: 'get',
  },
  getTenantInfo: {
    url: tenantInfo,
    method: 'get',
  },
  getSystemVersion: {
    url: getSystemVersion,
    method: 'get',
  },
  listToken: {
    url: getSystemTokenList,
    method: 'get',
  },
  createToken: {
    url: createSystemToken,
    method: 'post',
  },
  removeToken: {
    url: removeSystemToken,
    method: 'delete',
  },
  getSystemConfig: {
    url: getSystemConfig,
    method: 'get',
  },
  setLangfuseConfig: {
    url: setLangfuseConfig,
    method: 'put',
  },
  getLangfuseConfig: {
    url: setLangfuseConfig,
    method: 'get',
  },
  deleteLangfuseConfig: {
    url: setLangfuseConfig,
    method: 'delete',
  },
} as const;

/** 类型安全的用户服务客户端（login/logout/setting 等）。 */
const userService = registerServer<keyof typeof methods>(methods, request);

/** 获取可用第三方登录渠道列表。 */
export const getLoginChannels = () => request.get(api.loginChannels);
/** 跳转至指定 OAuth/SSO 登录入口。 */
export const loginWithChannel = (channel: string) =>
  (window.location.href = api.loginChannel(channel));

/** 列出租户成员。 */
export const listTenantUser = (tenantId: string) =>
  request.get(api.listTenantUser(tenantId));

/** 邀请用户加入租户（按邮箱）。 */
export const addTenantUser = (tenantId: string, email: string) =>
  post(api.addTenantUser(tenantId), { email });

/** 从租户移除成员。 */
export const deleteTenantUser = ({
  tenantId,
  userId,
}: {
  tenantId: string;
  userId: string;
}) =>
  request.delete(api.deleteTenantUser(tenantId), {
    data: { userId },
  });

/** 列出当前用户关联的全部租户。 */
export const listTenant = () => request.get(api.listTenant);

/** 接受租户邀请。 */
export const agreeTenant = (tenantId: string) =>
  request.patch(api.agreeTenant(tenantId));

export default userService;
