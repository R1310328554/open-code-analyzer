// use-delete-file.ts — 删除文件/文件夹：批量 fileIds 与当前 parentId 提交后端。

import { useDeleteFile } from '@/hooks/use-file-request';
import { useCallback } from 'react';
import { useGetFolderId } from './hooks';

/** 封装 deleteFile 请求，携带当前目录 parentId 以保持列表上下文。 */
export const useHandleDeleteFile = () => {
  const { deleteFile: removeDocument } = useDeleteFile();
  const parentId = useGetFolderId();

  /** 删除一个或多个文件，返回后端状态码供上层提示。 */
  const handleRemoveFile = useCallback(
    async (fileIds: string[]) => {
      const code = await removeDocument({ fileIds, parentId });

      return code;
    },
    [parentId, removeDocument],
  );

  return { handleRemoveFile };
};
