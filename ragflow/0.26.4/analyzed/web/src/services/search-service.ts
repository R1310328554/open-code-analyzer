/**
 * search-service.ts — 搜索应用 CRUD 及共享链接问答/思维导图/相关问题 API。
 */

import api from '@/utils/api';
import { registerNextServer } from '@/utils/register-server';

const {
  createSearch,
  getSearchList,
  deleteSearch,
  getSearchDetail,
  updateSearchSetting,
  askShare,
  mindmapShare,
  getRelatedQuestionsShare,
  getSearchDetailShare,
} = api;

/** 搜索 REST 方法表：create/list/delete/detail 及 share 端点。 */
const methods = {
  createSearch: {
    url: createSearch,
    method: 'post',
  },
  getSearchList: {
    url: getSearchList,
    method: 'get',
  },
  deleteSearch: { url: deleteSearch, method: 'delete' },
  getSearchDetail: {
    url: getSearchDetail,
    method: 'get',
  },
  updateSearchSetting: {
    url: updateSearchSetting,
    method: 'put',
  },
  askShare: {
    url: askShare,
    method: 'post',
  },
  mindmapShare: {
    url: mindmapShare,
    method: 'post',
  },
  getRelatedQuestionsShare: {
    url: getRelatedQuestionsShare,
    method: 'post',
  },
  getSearchDetailShare: {
    url: getSearchDetailShare,
    method: 'get',
  },
} as const;

/** 默认导出：搜索 API 客户端。 */
const searchService = registerNextServer<keyof typeof methods>(methods);
/** 与 searchService 同引用，供 next-search 模块显式导入。 */
export const searchServiceNext = searchService;

export default searchService;
