/**
 * document-util.ts — 文档与分块工具：PDF 高亮区域、扩展名判断、预览类型与上传状态。
 */

import { Images, SupportedPreviewDocumentTypes } from '@/constants/common';
import { UploadFile } from '@/interfaces/antd-compat';
import { IReferenceChunk } from '@/interfaces/database/chat';
import { IChunk } from '@/interfaces/database/dataset';
import { get } from 'lodash';
import { v4 as uuid } from 'uuid';

/** 将分块 positions 转为 PDF 高亮注释结构（boundingRect、页码与内容）。 */
export const buildChunkHighlights = (
  selectedChunk: IChunk | IReferenceChunk,
  size: { width: number; height: number },
) => {
  return Array.isArray(selectedChunk?.positions) &&
    selectedChunk.positions.every((x) => Array.isArray(x))
    ? selectedChunk?.positions?.map((x) => {
        const boundingRect = {
          width: size.width,
          height: size.height,
          x1: x[1],
          x2: x[2],
          y1: x[3],
          y2: x[4],
        };
        return {
          id: uuid(),
          comment: {
            text: '',
            emoji: '',
          },
          content: {
            text:
              get(selectedChunk, 'content_with_weight') ||
              get(selectedChunk, 'content', ''),
          },
          position: {
            boundingRect: boundingRect,
            rects: [boundingRect],
            pageNumber: x[0],
          },
        };
      })
    : [];
};

/** 判断 Ant Design UploadFile 是否已完成上传。 */
export const isFileUploadDone = (file: UploadFile) => file.status === 'done';

/** 从文件名提取小写扩展名（不含点）。 */
export const getExtension = (name: string) =>
  name?.slice(name.lastIndexOf('.') + 1).toLowerCase() ?? '';

/** 根据文件名判断是否为 PDF。 */
export const isPdf = (name: string) => {
  return getExtension(name) === 'pdf';
};

/** 从不支持文件类型的多行错误消息统计行数（即文件个数）。 */
export const getUnSupportedFilesCount = (message: string) => {
  return message.split('\n').length;
};

/** 扩展名是否在 SupportedPreviewDocumentTypes 可预览列表中。 */
export const isSupportedPreviewDocumentType = (fileExtension: string) => {
  return SupportedPreviewDocumentTypes.includes(fileExtension);
};

/** 判断扩展名是否为内置 Images 列表或 svg。 */
export const isImage = (image: string) => {
  return [...Images, 'svg'].some((x) => x === image);
};
