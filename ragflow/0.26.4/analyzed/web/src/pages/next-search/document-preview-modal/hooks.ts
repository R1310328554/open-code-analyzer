// hooks.ts — 文档预览抽屉：点击引用 chunk 时打开 Modal 并记录 documentId。

import { useSetModalState } from '@/hooks/common-hooks';
import { IReferenceChunk } from '@/interfaces/database/chat';
import { useCallback, useState } from 'react';

/** 管理文档预览抽屉显隐及当前选中的 chunk 与 documentId。 */
export const useClickDrawer = () => {
  const { visible, showModal, hideModal } = useSetModalState();
  const [selectedChunk, setSelectedChunk] = useState<IReferenceChunk>(
    {} as IReferenceChunk,
  );
  const [documentId, setDocumentId] = useState<string>('');

  /** 点击引用文档按钮：展示抽屉并写入 chunk 与 documentId。 */
  const clickDocumentButton = useCallback(
    (documentId: string, chunk: IReferenceChunk) => {
      showModal();
      setSelectedChunk(chunk);
      setDocumentId(documentId);
    },
    [showModal],
  );

  return {
    clickDocumentButton,
    visible,
    showModal,
    hideModal,
    selectedChunk,
    documentId,
  };
};
