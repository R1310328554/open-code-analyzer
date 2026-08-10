// use-run-document.ts — 单文档解析启停：带行级 loading 与确认弹窗的 runDocumentByIds 封装。

import { useSetModalState } from '@/hooks/common-hooks';
import { useRunDocument } from '@/hooks/use-document-request';
import { useState } from 'react';

/** 按文档 ID 触发解析/取消解析，仅当前行显示 loading 并管理确认弹窗显隐。 */
export const useHandleRunDocumentByIds = (id: string) => {
  const { runDocumentByIds, loading } = useRunDocument();
  const [currentId, setCurrentId] = useState<string>('');
  // 同一 hook 多行复用时，仅匹配 currentId 的行展示 loading
  const isLoading = loading && currentId !== '' && currentId === id;
  const { visible, showModal, hideModal } = useSetModalState();
  /** run=1 启动解析，run=2 取消；option 可携带 delete/apply_kb 等后端选项。 */
  const handleRunDocumentByIds = async (
    documentId: string,
    isRunning: boolean,
    option?: { delete: boolean; apply_kb: boolean },
  ) => {
    // 防重复点击：当前行已在请求中则直接返回
    if (isLoading) {
      return;
    }
    setCurrentId(documentId);
    try {
      await runDocumentByIds({
        documentIds: [documentId],
        run: isRunning ? 2 : 1,
        option,
      });
      setCurrentId('');
    } catch (error) {
      console.warn(error);
      setCurrentId('');
    }
    hideModal();
  };

  return {
    handleRunDocumentByIds,
    loading: isLoading,
    visible,
    showModal,
    hideModal,
  };
};
