// use-select-filters.ts — Agent 列表页筛选器：聚合所有者、画布类别与标签计数。

import { FilterCollection } from '@/components/list-filter-bar/interface';
import {
  useFetchAgentList,
  useFetchAgentTags,
} from '@/hooks/use-agent-request';
import { buildOwnersFilter, groupListByType } from '@/utils/list-filter-util';
import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';

/** 基于 Agent 列表与标签统计构建 FilterCollection 数组供列表栏渲染。 */
export function useSelectFilters() {
  const { t } = useTranslation();
  const { data } = useFetchAgentList({});
  const { data: tagCounts } = useFetchAgentTags();

  // 按 canvas_category 字段分组并统计各分类数量
  const canvasCategory = useMemo(() => {
    return groupListByType(
      data?.canvas ?? [],
      'canvas_category',
      'canvas_category',
    );
  }, [data?.canvas]);

  // 将后端 tag 计数映射为 { id, label, count } 供多选筛选
  const tagList = useMemo(
    () =>
      (tagCounts ?? []).map((t) => ({
        id: t.tag,
        label: t.tag,
        count: t.count,
      })),
    [tagCounts],
  );

  // 固定三项：所有者、画布类别、标签
  const filters: FilterCollection[] = [
    buildOwnersFilter(data?.canvas ?? [], undefined, t('common.owner')),
    {
      field: 'canvasCategory',
      list: canvasCategory,
      label: t('flow.canvasCategory'),
    },
    {
      field: 'tags',
      list: tagList,
      label: t('flow.tags'),
    },
  ];

  return filters;
}
