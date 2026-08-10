/**
 * knowledge-service.ts — 知识库/数据集 CRUD、分块管理、文档/metadata/索引 Pipeline 等 REST 封装。
 * 含 legacy 字段映射层，兼容 chunk_num/doc_num 等旧前端字段名。
 */

import { IRenameTag } from '@/interfaces/database/dataset';
import {
  IFetchDocumentListRequestBody,
  IFetchKnowledgeListRequestParams,
} from '@/interfaces/request/knowledge';
import { ProcessingType } from '@/pages/dataset/dataset-overview/dataset-common';
import api from '@/utils/api';
import registerServer from '@/utils/register-server';
import request from '@/utils/request';

const {
  createKb,
  rmKb,
  kbList,
  documentThumbnails,
  documentIngest,
  listTagByKnowledgeIds,
  setMeta,
  getMeta,
  getMetaKeys,
  retrievalTestShare,
} = api;

/** 知识库基础 REST 方法表（createKb/rmKb/documentIngest 等）。 */
const methods = {
  createKb: {
    url: createKb,
    method: 'post',
  },
  rmKb: {
    url: rmKb,
    method: 'delete',
  },
  getList: {
    url: kbList,
    method: 'get',
  },
  documentIngest: {
    url: documentIngest,
    method: 'post',
  },
  documentThumbnails: {
    url: documentThumbnails,
    method: 'get',
  },
  setMeta: {
    url: setMeta,
    method: 'post',
  },
  listTagByKnowledgeIds: {
    url: listTagByKnowledgeIds,
    method: 'get',
  },
  getMeta: {
    url: getMeta,
    method: 'get',
  },
  getMetaKeys: {
    url: getMetaKeys,
    method: 'get',
  },
  retrievalTestShare: {
    url: retrievalTestShare,
    method: 'post',
  },
  pipelineRerun: {
    url: api.pipelineRerun,
    method: 'post',
  },
};

/** registerServer 生成的基础知识库客户端。 */
const baseKbService = registerServer<keyof typeof methods>(methods, request);

/** 从 params 解析 dataset_id（兼容 kb_id / knowledge_id）。 */
const getDatasetId = (params: Record<string, any>) =>
  params.dataset_id || params.kb_id || params.knowledge_id;

/** 从 params 解析 document_id（兼容 doc_id）。 */
const getDocumentId = (params: Record<string, any>) =>
  params.document_id || params.doc_id;

/** 将 REST 分块响应字段映射为 legacy 前端字段名。 */
const mapChunkToLegacy = (chunk: Record<string, any>) => ({
  ...chunk,
  chunk_id: chunk.chunk_id || chunk.id,
  content_with_weight: chunk.content_with_weight || chunk.content,
  doc_id: chunk.doc_id || chunk.document_id,
  doc_name: chunk.doc_name || chunk.docnm_kwd,
  image_id: chunk.image_id || chunk.img_id,
  important_kwd: chunk.important_kwd || chunk.important_keywords || [],
  question_kwd: chunk.question_kwd || chunk.questions || [],
  available_int: chunk.available_int ?? (chunk.available === false ? 0 : 1),
  positions: chunk.positions || chunk.position_int || [],
});

/** 将 REST 文档摘要映射为 legacy chunk_num/parser_id 等字段。 */
const mapDocumentToLegacy = (doc: Record<string, any>) => ({
  ...doc,
  chunk_num: doc.chunk_num ?? doc.chunk_count,
  kb_id: doc.kb_id || doc.dataset_id,
  parser_id: doc.parser_id || doc.chunk_method,
});

/** 将前端分块编辑 payload 转为 REST API 字段名。 */
const mapChunkPayloadToRest = (payload: Record<string, any>) => ({
  content: payload.content ?? payload.content_with_weight,
  important_keywords: payload.important_keywords ?? payload.important_kwd,
  questions: payload.questions ?? payload.question_kwd,
  tag_kwd: payload.tag_kwd,
  tag_feas: payload.tag_feas,
  positions: payload.positions,
  available:
    payload.available ??
    (payload.available_int === undefined
      ? undefined
      : payload.available_int === 1),
  image_base64: payload.image_base64,
});

/** 将 available_int (0/1) 转为 REST 查询参数字符串 'true'/'false'。 */
const getAvailableParam = (available?: number) => {
  if (available === undefined) {
    return undefined;
  }
  return available === 1 ? 'true' : 'false';
};

/** 分块 CRUD 与检索测试子服务（含 legacy 响应归一化）。 */
const chunkService = {
  /** 分块检索测试：聚合 dataset_ids 后 POST retrievalTest。 */
  retrievalTest: async (params: Record<string, any>) => {
    const datasetId = params.dataset_id || params.kb_id || params.knowledge_id;
    if (!datasetId) {
      throw new Error(
        'dataset_id (or kb_id/knowledge_id) is required for retrievalTest',
      );
    }
    const datasetIds = Array.isArray(datasetId) ? datasetId : [datasetId];
    const rest = { ...params };
    delete rest.dataset_id;
    delete rest.kb_id;
    delete rest.knowledge_id;
    return request.post(api.retrievalTest, {
      data: { ...rest, dataset_ids: datasetIds },
    });
  },
  /** 分页列出文档分块，响应 chunks 经 mapChunkToLegacy 转换。 */
  chunkList: async (params: Record<string, any>) => {
    const datasetId = getDatasetId(params);
    const documentId = getDocumentId(params);
    const response = await request.get(api.chunkList(datasetId, documentId), {
      params: {
        page: params.page,
        page_size: params.page_size || params.size,
        keywords: params.keywords,
        available: getAvailableParam(params.available_int),
      },
    });

    if (response.data?.code === 0) {
      response.data.data = {
        ...response.data.data,
        chunks: (response.data.data?.chunks || []).map(mapChunkToLegacy),
        doc: mapDocumentToLegacy(response.data.data?.doc || {}),
      };
    }

    return response;
  },
  /** 创建分块，成功后归一化返回 chunk 字段。 */
  createChunk: async (payload: Record<string, any>) => {
    const datasetId = getDatasetId(payload);
    const documentId = getDocumentId(payload);
    const response = await request.post(api.chunkList(datasetId, documentId), {
      data: mapChunkPayloadToRest(payload),
    });

    if (response.data?.code === 0 && response.data.data?.chunk) {
      response.data.data.chunk = mapChunkToLegacy(response.data.data.chunk);
    }

    return response;
  },
  /** PATCH 更新单个分块内容/关键词/可用状态等。 */
  setChunk: (payload: Record<string, any>) => {
    const datasetId = getDatasetId(payload);
    const documentId = getDocumentId(payload);
    const chunkId = payload.chunk_id || payload.id;
    return request.patch(api.chunkDetail(datasetId, documentId, chunkId), {
      data: mapChunkPayloadToRest(payload),
    });
  },
  /** 获取单个分块详情并映射 legacy 字段。 */
  getChunk: async (params: Record<string, any>) => {
    const datasetId = getDatasetId(params);
    const documentId = getDocumentId(params);
    const chunkId = params.chunk_id || params.id;
    const response = await request.get(
      api.chunkDetail(datasetId, documentId, chunkId),
    );

    if (response.data?.code === 0) {
      response.data.data = mapChunkToLegacy(response.data.data || {});
    }

    return response;
  },
  /** 批量切换分块可用状态（available_int）。 */
  switchChunk: (params: Record<string, any>) => {
    const datasetId = getDatasetId(params);
    const documentId = getDocumentId(params);
    return request.patch(api.chunkList(datasetId, documentId), {
      data: {
        chunk_ids: params.chunk_ids || params.chunkIds,
        available_int: params.available_int,
      },
    });
  },
  /** 批量删除分块（支持 delete_all）。 */
  rmChunk: (params: Record<string, any>) => {
    const datasetId = getDatasetId(params);
    const documentId = getDocumentId(params);
    return request.delete(api.chunkList(datasetId, documentId), {
      data: {
        chunk_ids: params.chunk_ids || params.chunkIds,
        delete_all: params.delete_all,
      },
    });
  },
};

/** 合并 baseKbService 与 chunkService 的默认导出对象。 */
const kbService = {
  ...baseKbService,
  ...chunkService,
};

/**
 * 获取知识库详情；将 chunk_count/document_count 归一化为 chunk_num/doc_num。
 */
export const getKbDetail = async (datasetId: string) => {
  const response = await request.get(api.getKbDetail(datasetId));
  // The /api/v1/datasets/<id> endpoint returns chunk_count/document_count,
  // but legacy consumers (e.g. the GraphRAG/Raptor "magic wand" enable check
  // in dataset/index.tsx) read chunk_num/doc_num. Normalize both shapes.
  if (response.data?.code === 0 && response.data.data) {
    const d = response.data.data;
    response.data.data = {
      ...d,
      chunk_num: d.chunk_num ?? d.chunk_count,
      doc_num: d.doc_num ?? d.document_count,
    };
  }
  return response;
};

/** 列出知识库标签。 */
export const listTag = (knowledgeId: string) =>
  request.get(api.listTag(knowledgeId));

/** 批量删除知识库标签。 */
export const removeTag = (knowledgeId: string, tags: string[]) =>
  request.delete(api.removeTag(knowledgeId), { data: { tags } });

/** 重命名知识库标签（fromTag → toTag）。 */
export const renameTag = (
  knowledgeId: string,
  { fromTag, toTag }: IRenameTag,
) => request.put(api.renameTag(knowledgeId), { data: { fromTag, toTag } });

/** 获取知识图谱数据。 */
export function getKnowledgeGraph(knowledgeId: string) {
  return request.get(api.getKnowledgeGraph(knowledgeId));
}

/** 删除知识库关联的知识图谱。 */
export function deleteKnowledgeGraph(knowledgeId: string) {
  return request.delete(api.knowledgeGraph(knowledgeId));
}

/** 分页列出知识库/数据集。 */
export const listDataset = (params?: IFetchKnowledgeListRequestParams) =>
  request.get(api.kbList, { params });

/** 更新知识库配置（名称、embedding 模型等）。 */
export const updateKb = (datasetId: string, data: Record<string, any>) =>
  request.put(api.updateKb(datasetId), { data });

/** 触发索引任务（GraphRAG/Raptor 等 indexType）。 */
export const runIndex = (datasetId: string, indexType: string) =>
  request.post(api.runIndex(datasetId, indexType));

/** 查询索引任务执行 trace/进度。 */
export const traceIndex = (datasetId: string, indexType: string) =>
  request.get(api.traceIndex(datasetId, indexType));

// RESTful 文档列表：GET /api/v1/datasets/{dataset_id}/documents
/** 分页列出数据集内文档（合并 page/page_size/keywords 与 body 筛选）。 */
export const listDocument = (
  params?: IFetchKnowledgeListRequestParams,
  body?: IFetchDocumentListRequestBody,
) => {
  if (!params || !params.id) {
    throw new Error('params and params.id are required');
  }
  // Extract page, page_size, and ext.keywords from params
  const { page, page_size, ext } = params;
  // Merge: page, page_size, keywords (from ext), body, and remaining params
  const mergedParams = {
    page,
    page_size,
    keywords: ext?.keywords,
    ...body,
  };
  return request.get(api.getDocumentList(params.id), { params: mergedParams });
};

/** 获取文档筛选器可选值。 */
export const documentFilter = (kb_id: string) =>
  request.get(api.getDatasetFilter(kb_id), { params: {} });

/** 上传文档到指定数据集。 */
export const uploadDocument = async (datasetId: string, formData: FormData) => {
  const url = api.documentUpload(datasetId);
  const response = await request.post(url, { data: formData });
  return response.data;
};

/** 创建空白文档占位符。 */
export const createDocument = async (datasetId: string, name: string) => {
  const response = await request.post(api.documentCreate(datasetId), {
    data: { name },
  });
  return response.data;
};

/** 重命名文档。 */
export const renameDocument = (
  datasetId: string,
  documentId: string,
  data: { name?: string },
) => request.patch(api.documentRename(datasetId, documentId), { data });

/** 修改文档解析器/chunk 方法。 */
export const changeDocumentParser = (
  datasetId: string,
  documentId: string,
  data: { name?: string },
) => request.patch(api.documentChangeParser(datasetId, documentId), { data });

/** 批量删除文档。 */
export const deleteDocument = (datasetId: string, documentIds: string[]) =>
  request.delete(api.documentDelete(datasetId), { data: { ids: documentIds } });

/** 批量获取文档 metadata（可选 doc_ids 过滤）。 */
export const getMetaDataService = ({
  kb_id,
  doc_ids,
}: {
  kb_id: string;
  doc_ids?: string[];
}) =>
  request.get(api.getMetaData(kb_id), {
    params: doc_ids?.length ? { doc_ids: doc_ids.join(',') } : undefined,
  });
/** 按 selector 批量更新/删除文档 metadata 字段。 */
export const updateDocumentsMetadata = ({
  dataset_id,
  selector,
  updates,
  deletes,
}: {
  dataset_id: string;
  selector?: {
    document_ids?: string[];
    metadata_condition?: any;
  };
  updates?: any[];
  deletes?: any[];
}) =>
  request.patch(api.updateDocumentsMetadata(dataset_id), {
    data: { selector, updates, deletes },
  });

/** 更新单文档 metadata 配置 schema。 */
export const updateDocumentMetaDataConfig = ({
  kb_id,
  doc_id,
  data,
}: {
  kb_id: string;
  doc_id: string;
  data: any;
}) =>
  request.put(api.documentUpdateMetaDataConfig(kb_id, doc_id), {
    data: { ...data },
  });

/** 批量修改文档解析/索引状态。 */
export const changeDocumentsStatus = ({
  kb_id,
  doc_ids,
  status,
}: {
  kb_id: string;
  doc_ids?: string[];
  status: number;
}) =>
  request.post(api.documentChangeStatus(kb_id), { data: { doc_ids, status } });

/** 列出数据集 Pipeline 文档级日志。 */
export const listDataPipelineLogDocument = (
  datasetId: string,
  params?: Record<string, any>,
) => request.get(api.fetchDataPipelineLog(datasetId), { params });

/** 列出数据集 Pipeline 运行日志。 */
export const listPipelineDatasetLogs = (
  datasetId: string,
  params?: Record<string, any>,
) => request.get(api.fetchPipelineDatasetLogs(datasetId), { params });

/** 获取单条 Pipeline 日志详情。 */
export const getPipelineDetail = (datasetId: string, logId: string) =>
  request.get(api.getPipelineDetail(datasetId, logId));

/** 获取知识库基础信息摘要。 */
export const getKnowledgeBasicInfo = (datasetId: string) =>
  request.get(api.getKnowledgeBasicInfo(datasetId));

/** 校验 embedding 模型配置是否可用。 */
export const checkEmbedding = (datasetId: string, data: Record<string, any>) =>
  request.post(api.checkEmbedding(datasetId), { data });

/** 更新知识库级 metadata 配置。 */
export const kbUpdateMetaData = (
  datasetId: string,
  data: Record<string, any>,
) => request.put(api.kbUpdateMetaData(datasetId), { data });

/** 解绑/删除 Pipeline 任务（GraphRAG/Raptor 等，可选 wipe）。 */
export function deletePipelineTask({
  kb_id,
  type,
  wipe,
}: {
  kb_id: string;
  type: ProcessingType;
  wipe?: boolean;
}) {
  return request.delete(api.unbindPipelineTask(kb_id, type, wipe));
}

export default kbService;
