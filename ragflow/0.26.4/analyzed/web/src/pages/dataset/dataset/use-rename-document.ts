// use-rename-document.ts — 文档重命名弹窗：saveName 提交与初始名称回填。

import { useSetModalState } from '@/hooks/common-hooks';
import { useSaveDocumentName } from '@/hooks/use-document-request';
import { IDocumentInfo } from '@/interfaces/database/document';
import { useCallback, useState } from 'react';

/** 维护待重命名文档 record 与弹窗状态，成功后刷新列表由上层 query 负责。 */
export const useRenameDocument = () => {
  const { saveName, loading } = useSaveDocumentName();
  const [record, setRecord] = useState<IDocumentInfo>();

  const {
    visible: renameVisible,
    hideModal: hideRenameModal,
    showModal: showRenameModal,
  } = useSetModalState();

  /** 调用 saveName 更新 document 名称，需 record.id 与 dataset_id。 */
  const onRenameOk = useCallback(
    async (name: string) => {
      if (record?.id && record?.dataset_id) {
        const ret = await saveName({
          documentId: record.id,
          name,
          kbId: record.dataset_id,
        });
        if (ret === 0) {
          hideRenameModal();
        }
      }
    },
    [record?.id, record?.dataset_id, saveName, hideRenameModal],
  );

  const handleShow = useCallback(
    (row: IDocumentInfo) => {
      setRecord(row);
      showRenameModal();
    },
    [showRenameModal],
  );

  return {
    renameLoading: loading,
    onRenameOk,
    renameVisible,
    hideRenameModal,
    showRenameModal: handleShow,
    initialName: record?.name,
  };
};

/** 表格操作列仅需 showRenameModal 时可引用此类型。 */
export type UseRenameDocumentShowType = Pick<
  ReturnType<typeof useRenameDocument>,
  'showRenameModal'
>;
