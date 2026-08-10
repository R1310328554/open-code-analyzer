/**
 * data-source-service.ts — 外部数据源连接器 CRUD、重建、OAuth 授权（Google Drive/Gmail/Box）等 API。
 */

import api from '@/utils/api';
import registerServer from '@/utils/register-server';
import request from '@/utils/request';

const { dataSourceSet, dataSourceList } = api;
/** 基础方法表：dataSourceSet / dataSourceList。 */
const methods = {
  dataSourceSet: {
    url: dataSourceSet,
    method: 'post',
  },
  dataSourceList: {
    url: dataSourceList,
    method: 'get',
  },
} as const;
/** 默认导出：数据源 registerServer 客户端。 */
const dataSourceService = registerServer<keyof typeof methods>(
  methods,
  request,
);

/** 删除数据源连接器。 */
export const deleteDataSource = (id: string) =>
  request.delete(api.dataSourceDel(id));

/** 触发数据源向指定知识库重建同步。 */
export const dataSourceRebuild = (id: string, data: { kb_id: string }) => {
  return request.post(api.dataSourceRebuild(id), { data });
};

/** 部分更新数据源配置。 */
export const dataSourceUpdate = (id: string, data: Record<string, any>) => {
  return request.patch(api.dataSourceUpdate(id), { data });
};

/** 分页查询数据源同步日志。 */
export const getDataSourceLogs = (id: string, params?: any) =>
  request.get(api.dataSourceLogs(id), { params });
/** 获取数据源详情（注意：函数名保留原拼写 featch）。 */
export const featchDataSourceDetail = (id: string) =>
  request.get(api.dataSourceDetail(id));

/** 测试数据源连接是否可用。 */
export const testDataSource = (id: string) =>
  request.post(api.dataSourceTest(id));

/** 启动 Google Drive OAuth 授权流程，返回 flow_id。 */
export const startGoogleDriveWebAuth = (payload: {
  credentials: string;
  redirect_uri?: string;
}) => request.post(api.googleWebAuthStart('google-drive'), { data: payload });

/** 轮询 Google Drive OAuth 授权结果。 */
export const pollGoogleDriveWebAuthResult = (payload: { flow_id: string }) =>
  request.post(api.googleWebAuthResult('google-drive'), { data: payload });

// Gmail OAuth 流程与 Google Drive 相同，但走 gmail 专用端点（GmailTokenField 使用）
// Gmail-specific endpoints and is consumed by the GmailTokenField UI.
/** 启动 Gmail OAuth 授权流程。 */
export const startGmailWebAuth = (payload: {
  credentials: string;
  redirect_uri?: string;
}) => request.post(api.googleWebAuthStart('gmail'), { data: payload });

/** 轮询 Gmail OAuth 授权结果。 */
export const pollGmailWebAuthResult = (payload: { flow_id: string }) =>
  request.post(api.googleWebAuthResult('gmail'), { data: payload });

/** 启动 Box OAuth 授权流程。 */
export const startBoxWebAuth = (payload: {
  client_id: string;
  client_secret: string;
  redirect_uri?: string;
}) => request.post(api.boxWebAuthStart(), { data: payload });

/** 轮询 Box OAuth 授权结果。 */
export const pollBoxWebAuthResult = (payload: { flow_id: string }) =>
  request.post(api.boxWebAuthResult(), { data: payload });

export default dataSourceService;
