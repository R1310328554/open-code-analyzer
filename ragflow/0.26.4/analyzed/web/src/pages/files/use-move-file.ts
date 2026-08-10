// use-move-file.ts — 文件移动弹窗：批量/单选 src_file_ids 提交至目标文件夹。

import { useSetModalState } from '@/hooks/common-hooks';
import { UseRowSelectionType } from '@/hooks/logic-hooks/use-row-selection';
import { useMoveFile } from '@/hooks/use-file-request';
import { useCallback, useRef, useState } from 'react';

/** 管理移动文件弹窗与 moveFile 请求；批量移动成功后可清空行选择。 */
export const useHandleMoveFile = ({
  clearRowSelection,
}: Pick<UseRowSelectionType, 'clearRowSelection'>) => {
  const {
    visible: moveFileVisible,
    hideModal: hideMoveFileModal,
    showModal: showMoveFileModal,
  } = useSetModalState();
  const { moveFile, loading } = useMoveFile();
  const [sourceFileIds, setSourceFileIds] = useState<string[]>([]);
  // 标记本次是否为表格多选批量移动
  const isBulkRef = useRef(false);

  /** 确认移动：dest_file_id 为目标目录，返回码 0 时关闭弹窗。 */
  const onMoveFileOk = useCallback(
    async (targetFolderId: string) => {
      const ret = await moveFile({
        src_file_ids: sourceFileIds,
        dest_file_id: targetFolderId,
      });

      // 移动成功：批量模式需清除表格勾选
      if (ret === 0) {
        if (isBulkRef.current) {
          clearRowSelection();
        }
        hideMoveFileModal();
      }
      return ret;
    },
    [moveFile, sourceFileIds, hideMoveFileModal, clearRowSelection],
  );

  /** 打开弹窗前记录待移动文件 ID 列表及是否批量操作。 */
  const handleShowMoveFileModal = useCallback(
    (ids: string[], isBulk = false) => {
      isBulkRef.current = isBulk;
      setSourceFileIds(ids);
      showMoveFileModal();
    },
    [showMoveFileModal],
  );

  return {
    initialValue: '',
    moveFileLoading: loading,
    onMoveFileOk,
    moveFileVisible,
    hideMoveFileModal,
    showMoveFileModal: handleShowMoveFileModal,
  };
};

/** useHandleMoveFile 的完整返回值类型。 */
export type UseMoveDocumentReturnType = ReturnType<typeof useHandleMoveFile>;

/** 仅需触发 showMoveFileModal 的上层组件可引用此类型。 */
export type UseMoveDocumentShowType = Pick<
  ReturnType<typeof useHandleMoveFile>,
  'showMoveFileModal'
>;
