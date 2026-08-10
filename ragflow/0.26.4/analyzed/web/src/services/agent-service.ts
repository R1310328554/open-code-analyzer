/**
 * agent-service.ts — Agent 画布 CRUD、调试、日志、Webhook 追踪及共享页 trace 等 API 封装。
 */

import {
  IAgentLogsRequest,
  IPipeLineListRequest,
} from '@/interfaces/database/agent';
import { IAgentWebhookTraceRequest } from '@/interfaces/request/agent';
import api from '@/utils/api';
import { registerNextServer } from '@/utils/register-server';
import request from '@/utils/request';
import dayjs from 'dayjs';

const {
  createAgent,
  updateAgent: updateAgentApi,
  listAgents,
  deleteAgent,
  agentChatCompletion,
  resetAgent,
  listAgentTemplate,
  testDbConnect,
  getInputElements,
  trace,
  fetchVersionList,
  fetchVersion,
  getAgent,
  fetchAgentSessions,
  fetchExternalAgentInputs,
  prompt,
  cancelDataflow,
  cancelCanvas,
} = api;

/** registerNextServer 方法表：Agent 画布核心 REST 端点映射。 */
const methods = {
  getAgent: {
    url: getAgent,
    method: 'get',
  },
  createAgent: {
    url: createAgent,
    method: 'post',
  },
  fetchVersionList: {
    url: fetchVersionList,
    method: 'get',
  },
  fetchVersion: {
    url: (config: { agentId: string; versionId: string }) =>
      fetchVersion(config.agentId, config.versionId),
    method: 'get',
  },
  listAgents: {
    url: listAgents,
    method: 'get',
  },
  listAgentTags: {
    url: api.listAgentTags,
    method: 'get',
  },
  resetAgent: {
    url: resetAgent,
    method: 'post',
  },
  deleteAgent: {
    url: deleteAgent,
    method: 'delete',
  },
  agentChatCompletion: {
    url: agentChatCompletion,
    method: 'post',
  },
  listAgentTemplate: {
    url: listAgentTemplate,
    method: 'get',
  },
  testDbConnect: {
    url: testDbConnect,
    method: 'post',
  },
  getInputElements: {
    url: getInputElements,
    method: 'get',
  },
  debugSingle: {
    url: (config: { agentId: string; componentId: string }) =>
      api.debug(config.agentId, config.componentId),
    method: 'post',
  },
  uploadAgentFile: {
    url: (config: { agentId: string }) => api.uploadAgentFile(config.agentId),
    method: 'post',
  },
  trace: {
    url: (config: { agentId: string; messageId: string }) =>
      trace(config.agentId, config.messageId),
    method: 'get',
  },
  inputForm: {
    url: (config: { agentId: string; componentId: string }) =>
      api.inputForm(config.agentId, config.componentId),
    method: 'get',
  },
  fetchAgentLogs: {
    url: fetchAgentSessions,
    method: 'get',
  },
  fetchExternalAgentInputs: {
    url: fetchExternalAgentInputs,
    method: 'get',
  },
  fetchPrompt: {
    url: prompt,
    method: 'get',
  },
  cancelDataflow: {
    url: cancelDataflow,
    method: 'post',
  },
  cancelCanvas: {
    url: cancelCanvas,
    method: 'post',
  },
  createAgentSession: {
    url: api.createAgentSession,
    method: 'post',
  },
} as const;

/** 默认导出：通过方法名调用 registerNextServer 生成的 Agent API 客户端。 */
const agentService = registerNextServer<keyof typeof methods>(methods);

/** 更新 Agent 元数据（title/dsl/avatar/permission/release 等）。 */
export const updateAgent = (
  agentId: string,
  params: {
    title?: string;
    dsl?: Record<string, any>;
    avatar?: string;
    description?: string | null;
    permission?: string;
    release?: string;
  },
) => {
  return request(updateAgentApi(agentId), { method: 'put', data: params });
};

/** 更新 Agent 标签（逗号拼接提交）。 */
export const updateAgentTags = (agentId: string, tags: string[]) => {
  return request(api.updateAgentTags(agentId), {
    method: 'put',
    data: { tags: tags.join(',') },
  });
};

/** 拉取 Agent 消息执行 trace（需登录态）。 */
export const fetchTrace = (data: { canvas_id: string; message_id: string }) => {
  return request.get(
    methods.trace.url({
      agentId: data.canvas_id,
      messageId: data.message_id,
    }),
  );
};

// 共享/嵌入聊天页专用：仅凭 share APIToken 拉 trace（修复 #14985）
// is the share (beta) APIToken (fixes #14985).
/** 共享链接场景拉取 trace（使用 shared_id + message_id）。 */
export const fetchSharedTrace = (data: {
  shared_id: string;
  message_id: string;
}) => {
  return request.get(api.sharedTrace(data.shared_id, data.message_id));
};
/**
 * 按画布 ID 分页查询 Agent 会话日志。
 * 日期参数格式化为本地 wall-clock 字符串，避免 UTC 偏移导致筛选错位。
 */
export const fetchAgentLogsByCanvasId = (
  canvasId: string,
  params: IAgentLogsRequest,
) => {
  // Serialize Date values as local wall-clock strings ("YYYY-MM-DD HH:mm:ss").
  // Axios' default serializer turns a Date into a UTC ISO string, which the
  // backend then shifts by the server timezone — causing the picked local day
  // to mismatch the server-local dates shown in the table. Sending a plain
  // local datetime makes the backend compare it as-is against stored dates.
  // from_date snaps to the start of the day (00:00:00), to_date to the end
  // (23:59:59), so the full picked day range is covered.
  /** 将 Date 转为 YYYY-MM-DD HH:mm:ss；结束日期取当日 23:59:59。 */
  const normalizeDate = (value: string | Date | undefined, isEnd = false) => {
    if (!(value instanceof Date)) return value;
    const day = dayjs(value);
    return (isEnd ? day.endOf('day') : day.startOf('day')).format(
      'YYYY-MM-DD HH:mm:ss',
    );
  };

  const normalizedParams: IAgentLogsRequest = {
    ...params,
    from_date: normalizeDate(params.from_date),
    to_date: normalizeDate(params.to_date, true),
  };

  return request.get(methods.fetchAgentLogs.url(canvasId), {
    params: normalizedParams,
  });
};

/** 获取单个 Agent 会话详情。 */
export const fetchAgentLogsById = (canvasId: string, sessionId: string) => {
  return request.get(api.fetchAgentSessionById(canvasId, sessionId));
};

/** 列出 Pipeline 类型 Agent（复用 listAgents 接口）。 */
export const fetchPipeLineList = (params: IPipeLineListRequest) => {
  return request.get(api.listAgents, { params: params });
};

/** 查询 Webhook 触发 trace 记录。 */
export const fetchWebhookTrace = (
  id: string,
  params: IAgentWebhookTraceRequest,
) => {
  return request.get(api.fetchWebhookTrace(id), { params: params });
};

/** 为 Agent 画布创建新会话。 */
export function createAgentSession({ id, name }: { id: string; name: string }) {
  return request.post(api.createAgentSession(id), { data: { name } });
}

/** 删除指定 Agent 会话。 */
export const deleteAgentSession = (canvasId: string, sessionId: string) => {
  return request.delete(api.fetchAgentSessionById(canvasId, sessionId));
};

/** 向 Agent 上传附件文件。 */
export const uploadAgentFile = (agentId: string, data: FormData) => {
  return request(api.uploadAgentFile(agentId), {
    method: 'post',
    data,
  });
};

export default agentService;
