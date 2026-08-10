// use-create-folder.ts — 新建文件夹弹窗：在当前 folderId 下调用 createFolder。

import { useSetModalState } from '@/hooks/common-hooks';
import { useCreateFolder } from '@/hooks/use-file-request';
import { useCallback } from 'react';
import { useGetFolderId } from './hooks';

/** 封装新建文件夹弹窗显隐与 onFolderCreateOk 提交。 */
export const useHandleCreateFolder = () => {
  const {
    visible: folderCreateModalVisible,
    hideModal: hideFolderCreateModal,
    showModal: showFolderCreateModal,
  } = useSetModalState();
  const { createFolder, loading } = useCreateFolder();
  const id = useGetFolderId();

  /** 在 parentId（URL folderId）下创建子文件夹，成功则关闭弹窗。 */
  const onFolderCreateOk = useCallback(
    async (name: string) => {
      const ret = await createFolder({ parentId: id, name });

      if (ret === 0) {
        hideFolderCreateModal();
      }
    },
    [createFolder, hideFolderCreateModal, id],
  );

  return {
    folderCreateLoading: loading,
    onFolderCreateOk,
    folderCreateModalVisible,
    hideFolderCreateModal,
    showFolderCreateModal,
  };
};
