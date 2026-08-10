/**
 * llm-service.ts — LLM 提供商/实例/模型管理 API（默认模型、连接验证、启停状态等）。
 */

import api from '@/utils/api';
import { registerNextServer } from '@/utils/register-server';

const {
  listAllAddedModels,
  defaultModel,
  listProviders,
  addProvider,
  addProviderInstance,
  verifyProviderConnection,
  listProviderModels,
  listProviderInstances,
  listInstanceModels,
  showProviderInstance,
  addInstanceModel,
  editInstanceModel,
  deleteProviderInstance,
  updateModelStatus,
} = api;

/** LLM 提供商 REST 方法表。 */
const methods = {
  listAllAddedModels: {
    url: listAllAddedModels,
    method: 'get',
  },
  listDefaultModels: {
    url: defaultModel,
    method: 'get',
  },
  setDefaultModel: {
    url: defaultModel,
    method: 'patch',
  },
  listProviders: {
    url: listProviders,
    method: 'get',
  },
  addProvider: {
    url: addProvider,
    method: 'put',
  },
  addProviderInstance: {
    url: addProviderInstance,
    method: 'post',
  },
  verifyProviderConnection: {
    url: verifyProviderConnection,
    method: 'post',
  },
  listProviderModels: {
    url: listProviderModels,
    method: 'get',
  },
  listProviderInstances: {
    url: listProviderInstances,
    method: 'get',
  },
  listInstanceModels: {
    url: listInstanceModels,
    method: 'get',
  },
  showProviderInstance: {
    url: showProviderInstance,
    method: 'get',
  },
  addInstanceModel: {
    url: addInstanceModel,
    method: 'post',
  },
  editInstanceModel: {
    url: editInstanceModel,
    method: 'put',
  },
  deleteProviderInstance: {
    url: deleteProviderInstance,
    method: 'delete',
  },
  updateModelStatus: {
    url: updateModelStatus,
    method: 'patch',
  },
} as const;

/** 默认导出：LLM 管理 API 客户端。 */
const llmService = registerNextServer<keyof typeof methods>(methods);

export default llmService;
