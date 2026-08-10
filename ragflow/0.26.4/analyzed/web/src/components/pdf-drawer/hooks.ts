// pdf-drawer/hooks.ts — PDF 预览抽屉 Hook：点击引用 chunk 时打开并记录选中片段。

import { useSetModalState } from '@/hooks/common-hooks';
import { IReferenceChunk } from '@/interfaces/database/chat';
import { useCallback, useState } from 'react';

/** 管理 PDF 抽屉显隐，并保存 documentId 与当前选中的 IReferenceChunk。 */
export const useClickDrawer = () => {
  const { visible, showModal, hideModal } = useSetModalState();
  const [selectedChunk, setSelectedChunk] = useState<IReferenceChunk>(
    {} as IReferenceChunk,
  );
  const [documentId, setDocumentId] = useState<string>('');

  /** 点击文档/引用按钮：打开抽屉并写入 documentId 与 chunk 详情。 */
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
