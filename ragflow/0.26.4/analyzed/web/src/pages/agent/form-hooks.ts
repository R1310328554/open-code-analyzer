// form-hooks.ts — Agent 表单通用 Hook：上游节点选择与排序字段选项。

import { useTranslate } from '@/hooks/common-hooks';
import { useCallback, useMemo } from 'react';
import { Operator, RestrictedUpstreamMap } from './constant';
import useGraphStore from './store';

/** 按算子类型构建「连到」下拉选项，排除 Note/受限上游与已选节点。 */
export const useBuildFormSelectOptions = (
  operatorName: Operator,
  selfId?: string, // 排除当前节点自身
) => {
  const nodes = useGraphStore((state) => state.nodes);

  const buildCategorizeToOptions = useCallback(
    (toList: string[]) => {
      const excludedNodes: Operator[] = [
        Operator.Note,
        ...(RestrictedUpstreamMap[operatorName] ?? []),
      ];
      return nodes
        .filter(
          (x) =>
            excludedNodes.every((y) => y !== x.data.label) &&
            x.id !== selfId &&
            !toList.some((y) => y === x.id), // 过滤其他 to 字段已选值，避免重复
        )
        .map((x) => ({ label: x.data.name, value: x.id }));
    },
    [nodes, operatorName, selfId],
  );

  return buildCategorizeToOptions;
};

/** 返回排序字段选项：data / relevance（i18n 标签）。 */
export const useBuildSortOptions = () => {
  const { t } = useTranslate('flow');

  const options = useMemo(() => {
    return ['data', 'relevance'].map((x) => ({
      value: x,
      label: t(x),
    }));
  }, [t]);
  return options;
};
