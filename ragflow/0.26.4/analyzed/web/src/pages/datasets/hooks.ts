// hooks.ts — 知识库列表页：搜索框状态与新建知识库弹窗/跳转逻辑。

import { ParseType } from '@/constants/knowledge';
import { useSetModalState } from '@/hooks/common-hooks';
import { useNavigatePage } from '@/hooks/logic-hooks/navigate-hooks';
import { useCreateKnowledge } from '@/hooks/use-knowledge-request';
import { useCallback, useState } from 'react';
/** 维护列表页搜索关键字与 input onChange 处理器。 */
export const useSearchKnowledge = () => {
  const [searchString, setSearchString] = useState<string>('');

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchString(e.target.value);
  };
  return {
    searchString,
    handleInputChange,
  };
};

/** 创建知识库表单提交的数据结构（名称、嵌入模型、解析方式等）。 */
export interface Iknowledge {
  name: string;
  embedding_model?: string;
  chunk_method?: string;
  parseType?: ParseType;
  pipeline_id?: string | null;
  ext?: {
    language?: string;
    [key: string]: any;
  };
}
/** 新建知识库弹窗：createKnowledge 成功后跳转至对应数据集详情。 */
export const useSaveKnowledge = () => {
  const { visible: visible, hideModal, showModal } = useSetModalState();
  const { loading, createKnowledge } = useCreateKnowledge();
  const { navigateToDataset } = useNavigatePage();

  /** 提交创建请求，code 为 0 时关闭弹窗并 navigateToDataset。 */
  const onCreateOk = useCallback(
    async (data: Iknowledge) => {
      const ret = await createKnowledge(data);

      if (ret?.code === 0) {
        hideModal();
        navigateToDataset(ret.data.id)();
      }
    },
    [createKnowledge, hideModal, navigateToDataset],
  );

  return {
    loading,
    onCreateOk,
    visible,
    hideModal,
    showModal,
  };
};
