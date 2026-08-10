// use-values.ts — Begin 节点表单初始值：开场白、mode 与 inputs 列表。

import { RAGFlowNodeType } from '@/interfaces/database/agent';
import { isEmpty } from 'lodash';
import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { AgentDialogueMode } from '../../constant';
import { buildBeginInputListFromObject } from './utils';

/** 从画布节点 form 推导 react-hook-form defaultValues，空节点用 i18n 开场白默认值。 */
export function useValues(node?: RAGFlowNodeType) {
  const { t } = useTranslation();

  // 无节点数据时的默认：enablePrologue、prologue、Conversational mode
  const defaultValues = useMemo(
    () => ({
      enablePrologue: true,
      prologue: t('chat.setAnOpenerInitial'),
      mode: AgentDialogueMode.Conversational,
      inputs: [],
    }),
    [t],
  );

  const values = useMemo(() => {
    const formData = node?.data?.form;

    if (isEmpty(formData)) {
      return defaultValues;
    }

    // 将 DSL 中 Record 形态 inputs 转为带 key 的数组
    const inputs = buildBeginInputListFromObject(formData?.inputs);

    return { ...(formData || {}), inputs };
  }, [defaultValues, node?.data?.form]);

  return values;
}
