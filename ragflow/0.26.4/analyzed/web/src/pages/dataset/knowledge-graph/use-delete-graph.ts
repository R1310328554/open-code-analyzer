// use-delete-graph.ts — 删除知识库知识图谱并在成功后跳回数据集详情页。

import { useNavigatePage } from '@/hooks/logic-hooks/navigate-hooks';
import { useRemoveKnowledgeGraph } from '@/hooks/use-knowledge-request';
import { useCallback } from 'react';
import { useParams } from 'react-router';

/** 调用 removeKnowledgeGraph，成功且路由带 id 时 navigateToDataset。 */
export function useDeleteKnowledgeGraph() {
  const { removeKnowledgeGraph, loading } = useRemoveKnowledgeGraph();
  const { navigateToDataset } = useNavigatePage();
  const { id } = useParams();

  /** 删除当前知识库的图谱索引数据。 */
  const handleDeleteKnowledgeGraph = useCallback(async () => {
    const ret = await removeKnowledgeGraph();
    if (ret === 0 && id) {
      navigateToDataset(id)();
    }
  }, [id, navigateToDataset, removeKnowledgeGraph]);

  return { handleDeleteKnowledgeGraph, loading };
}
