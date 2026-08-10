// use-rename-dataset.ts — 知识库重命名弹窗：saveKnowledgeConfiguration 更新名称。

import { useSetModalState } from '@/hooks/common-hooks';
import { useUpdateKnowledge } from '@/hooks/use-knowledge-request';
import { IDataset } from '@/interfaces/database/dataset';
import { useCallback, useState } from 'react';

/** 维护待重命名 dataset record 与弹窗显隐，成功后关闭弹窗。 */
export const useRenameDataset = () => {
  const [dataset, setDataset] = useState<IDataset>({} as IDataset);
  const {
    visible: datasetRenameVisible,
    hideModal: hideDatasetRenameModal,
    showModal: showDatasetRenameModal,
  } = useSetModalState();
  const { saveKnowledgeConfiguration, loading } = useUpdateKnowledge(true);

  /** 按 kb_id 提交新名称，code 为 0 时关闭弹窗。 */
  const onDatasetRenameOk = useCallback(
    async (name: string) => {
      const ret = await saveKnowledgeConfiguration({
        kb_id: dataset.id,
        name,
      });

      if (ret.code === 0) {
        hideDatasetRenameModal();
      }
    },
    [saveKnowledgeConfiguration, dataset, hideDatasetRenameModal],
  );

  /** 打开弹窗前缓存当前行 dataset 以回填 initialDatasetName。 */
  const handleShowDatasetRenameModal = useCallback(
    async (record: IDataset) => {
      setDataset(record);
      showDatasetRenameModal();
    },
    [showDatasetRenameModal],
  );

  return {
    datasetRenameLoading: loading,
    initialDatasetName: dataset?.name,
    onDatasetRenameOk,
    datasetRenameVisible,
    hideDatasetRenameModal,
    showDatasetRenameModal: handleShowDatasetRenameModal,
  };
};
