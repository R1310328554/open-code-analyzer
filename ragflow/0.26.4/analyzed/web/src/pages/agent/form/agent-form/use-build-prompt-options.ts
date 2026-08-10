// use-build-prompt-options.ts — Agent Prompt 插入项：框架模板与引用指南过滤。

import { useFetchPrompt } from '@/hooks/use-agent-request';
import { Edge } from '@xyflow/react';
import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { hasSubAgentOrTool } from '../../utils';

/** Prompt 选择器分组标识，用于框架内置模板。 */
export const PromptIdentity = 'RAGFlow-Prompt';

/** 将 prompt 正文包裹为 <TAG>...</TAG> 便于插入编辑器。 */
function wrapPromptWithTag(text: string, tag: string) {
  const capitalTag = tag.toUpperCase();
  return `<${capitalTag}>
  ${text}
</${capitalTag}>`;
}

/** 构建 extra prompt 选项：无子 Agent/工具时仅保留 citation_guidelines。 */
export function useBuildPromptExtraPromptOptions(
  edges: Edge[],
  nodeId?: string,
) {
  const { data: prompts } = useFetchPrompt();
  const { t } = useTranslation();
  const has = hasSubAgentOrTool(edges, nodeId);

  const options = useMemo(() => {
    return Object.entries(prompts || {})
      .map(([key, value]) => ({
        label: key,
        value: wrapPromptWithTag(value, key),
        icon: null,
      }))
      .filter((x) => {
        if (!has) {
          return x.label === 'citation_guidelines';
        }
        return true;
      });
  }, [has, prompts]);

  const extraOptions = [
    { label: PromptIdentity, title: t('flow.frameworkPrompts'), options },
  ];

  return { extraOptions };
}
