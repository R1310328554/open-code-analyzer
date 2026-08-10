// file-manager.ts — 文件列表分页与关联知识库请求体。

import { IPaginationRequestBody } from './base';

/** 文件管理器列表请求：继承分页并可选 parent_id 文件夹。 */
export interface IFileListRequestBody extends IPaginationRequestBody {
  parent_id?: string; // 父文件夹 ID
}

/** 将文件批量关联到知识库：fileIds 与 kbIds。 */
export interface IConnectRequestBody {
  fileIds: string[];
  kbIds: string[];
}
