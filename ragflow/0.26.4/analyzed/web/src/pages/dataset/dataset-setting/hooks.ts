// hooks.ts — 知识库设置页 hooks：分块方法列表、表单回填、嵌入模型校验与标签重命名。

import { useSetModalState } from '@/hooks/common-hooks';

import { useFetchKnowledgeBaseConfiguration } from '@/hooks/use-knowledge-request';
import { useSelectParserList } from '@/hooks/use-user-setting-request';
import { checkEmbedding } from '@/services/knowledge-service';
import { useIsFetching } from '@tanstack/react-query';
import { pick } from 'lodash';
import { useCallback, useEffect, useState } from 'react';
import { UseFormReturn } from 'react-hook-form';
import { useParams, useSearchParams } from 'react-router';
import { z } from 'zod';
import { formSchema } from './form-schema';

// 解析方法下拉中隐藏的 chunk_method 值（email/picture/audio）
// The value that does not need to be displayed in the analysis method Select
const HiddenFields = ['email', 'picture', 'audio'];

/** 返回用户可见的分块/解析方法选项列表。 */
export function useSelectChunkMethodList() {
  const parserList = useSelectParserList();

  return parserList.filter((x) => !HiddenFields.some((y) => y === x.value));
}

/** 判断知识库是否已有分块（chunk_count > 0），用于限制部分字段修改。 */
export function useHasParsedDocument(isEdit?: boolean) {
  const { data: knowledgeDetails } = useFetchKnowledgeBaseConfiguration({
    isEdit,
  });
  return knowledgeDetails.chunk_count > 0;
}

/** 挂载后将 knowledgeDetails 合并进 react-hook-form 并重置表单。 */
export const useFetchKnowledgeConfigurationOnMount = (
  form: UseFormReturn<z.infer<typeof formSchema>>,
) => {
  const { data: knowledgeDetails, loading } =
    useFetchKnowledgeBaseConfiguration();

  useEffect(() => {
    const parser_config = {
      ...form.formState?.defaultValues?.parser_config,
      ...knowledgeDetails.parser_config,
      raptor: {
        ...form.formState?.defaultValues?.parser_config?.raptor,
        ...knowledgeDetails.parser_config?.raptor,
        clustering_method:
          knowledgeDetails.parser_config?.raptor?.ext?.clustering_method,
        use_raptor: true,
      },
      graphrag: {
        ...form.formState?.defaultValues?.parser_config?.graphrag,
        ...knowledgeDetails.parser_config?.graphrag,
        use_graphrag: true,
      },
    };
    const formValues = {
      ...pick({ ...knowledgeDetails, parser_config: parser_config }, [
        'description',
        'name',
        'permission',
        'language',
        'parser_config',
        'connectors',
        'pagerank',
        'avatar',
      ]),
      embedding_model: knowledgeDetails.embedding_model,
      chunk_method: knowledgeDetails.chunk_method,
    } as z.infer<typeof formSchema>;
    form.reset(formValues);
  }, [form, knowledgeDetails]);

  return { knowledgeDetails, loading };
};

/** 知识库详情 query 是否正在 fetch。 */
export const useSelectKnowledgeDetailsLoading = () =>
  useIsFetching({ queryKey: ['fetchKnowledgeDetail'] }) > 0;

/** 知识库标签重命名弹窗状态与初始 tag 名。 */
export const useRenameKnowledgeTag = () => {
  const [tag, setTag] = useState<string>('');
  const {
    visible: tagRenameVisible,
    hideModal: hideTagRenameModal,
    showModal: showFileRenameModal,
  } = useSetModalState();

  const handleShowTagRenameModal = useCallback(
    (record: string) => {
      setTag(record);
      showFileRenameModal();
    },
    [showFileRenameModal],
  );

  return {
    initialName: tag,
    tagRenameVisible,
    hideTagRenameModal,
    showTagRenameModal: handleShowTagRenameModal,
  };
};

/** 切换嵌入模型前调用 checkEmbedding 校验维度兼容性。 */
export const useHandleKbEmbedding = () => {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const knowledgeBaseId = searchParams.get('id') || id;
  const handleChange = useCallback(
    async ({ embed_id }: { embed_id: string }) => {
      const res = await checkEmbedding(knowledgeBaseId || '', {
        embd_id: embed_id,
      });
      return res.data;
    },
    [knowledgeBaseId],
  );
  return {
    handleChange,
  };
};
