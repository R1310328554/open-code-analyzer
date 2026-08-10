/**
 * dataflow-service.ts — Dataflow 画布列表/创建/运行/删除等 API（registerNextServer 封装）。
 */

import api from '@/utils/api';
import { registerNextServer } from '@/utils/register-server';

const {
  listDataflow,
  removeDataflow,
  fetchDataflow,
  runDataflow,
  setDataflow,
} = api;

/** Dataflow REST 方法表：list/remove/fetch/run/set。 */
const methods = {
  listDataflow: {
    url: listDataflow,
    method: 'get',
  },
  removeDataflow: {
    url: removeDataflow,
    method: 'post',
  },
  fetchDataflow: {
    url: fetchDataflow,
    method: 'get',
  },
  runDataflow: {
    url: runDataflow,
    method: 'post',
  },
  setDataflow: {
    url: setDataflow,
    method: 'post',
  },
} as const;

/** 默认导出：Dataflow API 客户端。 */
const dataflowService = registerNextServer<keyof typeof methods>(methods);

export default dataflowService;
