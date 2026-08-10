// use-save-meta.ts — 文档元数据编辑弹窗：绑定 record 并调用 setDocumentMeta 保存。

import { useSetModalState } from '@/hooks/common-hooks';
import { useSetDocumentMeta } from '@/hooks/use-document-request';
import { IDocumentInfo } from '@/interfaces/database/document';
import { useCallback, useState } from 'react';

/** 维护待编辑文档 record 与元数据弹窗状态，成功后关闭弹窗。 */
export const useSaveMeta = () => {
  const { setDocumentMeta, loading } = useSetDocumentMeta();
  const [record, setRecord] = useState<IDocumentInfo>({} as IDocumentInfo);

  const {
    visible: setMetaVisible,
    hideModal: hideSetMetaModal,
    showModal: showSetMetaModal,
  } = useSetModalState();

  /** 将 meta 字符串提交至后端，返回码为 0 时关闭弹窗。 */
  const onSetMetaModalOk = useCallback(
    async (meta: string) => {
      const ret = await setDocumentMeta({
        documentId: record?.id,
        meta,
      });
      if (ret === 0) {
        hideSetMetaModal();
      }
    },
    [setDocumentMeta, record?.id, hideSetMetaModal],
  );

  /** 打开弹窗前缓存当前行文档信息供提交时使用。 */
  const handleShowSetMetaModal = useCallback(
    (row: IDocumentInfo) => {
      setRecord(row);
      showSetMetaModal();
    },
    [showSetMetaModal],
  );

  return {
    setMetaLoading: loading,
    onSetMetaModalOk,
    setMetaVisible,
    hideSetMetaModal,
    showSetMetaModal: handleShowSetMetaModal,
    metaRecord: record,
  };
};

/** 仅需 showSetMetaModal 的组件可引用此类型。 */
export type UseSaveMetaShowType = Pick<
  ReturnType<typeof useSaveMeta>,
  'showSetMetaModal'
>;
