// use-create-empty-document.ts — 创建空白文档弹窗：调用 createDocument 并在成功后关闭。

import { useSetModalState } from '@/hooks/common-hooks';
import { useCreateDocument } from '@/hooks/use-document-request';
import { useCallback } from 'react';

/** 封装空白文档创建弹窗显隐与 onCreateOk 提交逻辑。 */
export const useCreateEmptyDocument = () => {
  const { createDocument, loading } = useCreateDocument();

  const {
    visible: createVisible,
    hideModal: hideCreateModal,
    showModal: showCreateModal,
  } = useSetModalState();

  /** 按名称创建空文档，返回码为 0 时关闭弹窗。 */
  const onCreateOk = useCallback(
    async (name: string) => {
      const ret = await createDocument(name);
      if (ret === 0) {
        hideCreateModal();
      }
    },
    [hideCreateModal, createDocument],
  );

  return {
    createLoading: loading,
    onCreateOk,
    createVisible,
    hideCreateModal,
    showCreateModal,
  };
};
