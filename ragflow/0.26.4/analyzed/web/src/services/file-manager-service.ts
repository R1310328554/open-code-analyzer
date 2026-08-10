/**
 * file-manager-service.ts — 文件管理器：目录树、上传/移动/删除及 Blob 下载 API。
 */

import api from '@/utils/api';
import registerServer from '@/utils/register-server';
import request from '@/utils/request';

const {
  listFile,
  removeFile,
  uploadFile,
  getAllParentFolder,
  createFolder,
  connectFileToKnowledge,
  getDocumentFile,
  getFile,
  moveFile,
  getDatasetDocumentFileDownload,
  getAttachmentFileDownload,
} = api;

/** 文件管理 REST 方法表（含 blob 响应类型）。 */
const methods = {
  listFile: {
    url: listFile,
    method: 'get',
  },
  removeFile: {
    url: removeFile,
    method: 'delete',
  },
  uploadFile: {
    url: uploadFile,
    method: 'post',
  },
  getAllParentFolder: {
    url: getAllParentFolder,
    method: 'get',
  },
  createFolder: {
    url: createFolder,
    method: 'post',
  },
  connectFileToKnowledge: {
    url: connectFileToKnowledge,
    method: 'post',
  },
  getFile: {
    url: getFile,
    method: 'get',
    responseType: 'blob',
  },
  getDocumentFile: {
    url: getDocumentFile,
    method: 'get',
    responseType: 'blob',
  },
  moveFile: {
    url: moveFile,
    method: 'post',
  },
} as const;

/** 默认导出：文件管理 registerServer 客户端。 */
const fileManagerService = registerServer<keyof typeof methods>(
  methods,
  request,
);

/** 下载 Agent 附件（按 docId + ext，responseType blob）。 */
export const downloadAgentFile = (data: { docId: string; ext: string }) => {
  return request.get(getAttachmentFileDownload(data.docId), {
    params: { ext: data.ext },
    responseType: 'blob',
  });
};

/** 下载知识库文档原文件（datasetId + docId + ext）。 */
export const downloadDatasetDocument = (data: {
  datasetId: string;
  docId: string;
  ext: string;
}) => {
  return request.get(
    getDatasetDocumentFileDownload(data.datasetId, data.docId),
    {
      params: { ext: data.ext },
      responseType: 'blob',
    },
  );
};
export default fileManagerService;
