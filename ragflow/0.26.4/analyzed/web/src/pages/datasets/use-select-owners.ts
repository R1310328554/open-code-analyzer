// use-select-owners.ts — 知识库列表所有者筛选：从列表数据构建 owners FilterCollection。

import { FilterCollection } from '@/components/list-filter-bar/interface';
import { useFetchKnowledgeList } from '@/hooks/use-knowledge-request';
import { buildOwnersFilter } from '@/utils/list-filter-util';
import { useTranslation } from 'react-i18next';

/** 拉取知识库列表并生成「所有者」筛选项供 list-filter-bar 使用。 */
export function useSelectOwners() {
  const { list } = useFetchKnowledgeList();
  const { t } = useTranslation();

  // 复用 buildOwnersFilter 按 tenant/昵称聚合计数
  const filters: FilterCollection[] = [
    buildOwnersFilter(list, undefined, t('common.owner')),
  ];

  return filters;
}
