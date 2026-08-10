/**
 * plugin-service.ts — LLM 插件/工具列表 API（getLlmTools）。
 */

import api from '@/utils/api';
import registerServer from '@/utils/register-server';
import request from '@/utils/request';

const { llmTools } = api;

/** 插件方法表：getLlmTools。 */
const methods = {
  getLlmTools: {
    url: llmTools,
    method: 'get',
  },
} as const;

/** 默认导出：插件 registerServer 客户端。 */
const pluginService = registerServer<keyof typeof methods>(methods, request);

export default pluginService;
