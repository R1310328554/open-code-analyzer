// use-select-filters.ts — 知识库文档列表筛选：按后缀、运行状态与元数据字段组装 FilterCollection。

import {
  FilterCollection,
  FilterType,
} from '@/components/list-filter-bar/interface';
import { useTranslate } from '@/hooks/common-hooks';
import { useGetDocumentFilter } from '@/hooks/use-document-request';
import { useMemo } from 'react';

/** 后端 metadata 聚合中标识「无元数据」文档的特殊字段名。 */
export const EMPTY_METADATA_FIELD = 'empty_metadata';

/** 拉取文档 filter 统计并转换为列表筛选栏所需的三组筛选项。 */
export function useSelectDatasetFilters() {
  const { t } = useTranslate('knowledgeDetails');
  const { filter, onOpenChange } = useGetDocumentFilter();

  // 文件后缀 -> 类型筛选项（label 大写展示）
  const fileTypes = useMemo(() => {
    if (filter.suffix) {
      return Object.keys(filter.suffix).map((x) => ({
        id: x,
        label: x.toUpperCase(),
        count: filter.suffix[x],
      }));
    }
  }, [filter.suffix]);
  // 解析状态 + 空元数据计数合并为「状态」分组
  const fileStatus = useMemo(() => {
    let list = [] as FilterType[];
    if (filter.run_status) {
      list = Object.keys(filter.run_status).map((x) => ({
        id: x,
        label: t(`runningStatus${x}`),
        count: filter.run_status[x as unknown as number],
      }));
    }
    if (filter.metadata) {
      const emptyMetadata = filter.metadata?.empty_metadata;
      if (emptyMetadata) {
        list.push({
          id: EMPTY_METADATA_FIELD,
          label: t('emptyMetadata'),
          count: emptyMetadata.true,
        });
      }
    }
    return list;
  }, [filter.run_status, filter.metadata, t]);
  // 各元数据字段及其取值分布，供可搜索的多级筛选
  const metaDataList = useMemo(() => {
    if (filter.metadata) {
      const list = Object.keys(filter.metadata)
        ?.filter((m) => m !== EMPTY_METADATA_FIELD)
        ?.map((x) => {
          return {
            id: x.toString(),
            field: x.toString(),
            label: x.toString(),
            list: Object.keys(filter.metadata[x]).map((y) => ({
              id: y.toString(),
              field: y.toString(),
              label: y.toString(),
              value: [y],
              count: filter.metadata[x][y],
            })),
            count: Object.keys(filter.metadata[x]).reduce(
              (acc, cur) => acc + filter.metadata[x][cur],
              0,
            ),
          };
        });
      return list;
    }
  }, [filter.metadata]);

  const filters: FilterCollection[] = useMemo(() => {
    return [
      { field: 'type', label: t('fileType'), list: fileTypes },
      { field: 'run', label: t('status'), list: fileStatus },
      {
        field: 'metadata',
        label: t('metadataField'),
        canSearch: true,
        list: metaDataList,
      },
    ] as FilterCollection[];
  }, [fileStatus, fileTypes, metaDataList, t]);

  // 筛选分组：系统属性（类型/状态）与元数据字段
  const filterGroup = {
    [t('systemAttribute')]: ['type', 'run'],
    // [t('metadataField')]: ['metadata'],
  };
  return { filters, onOpenChange, filterGroup };
}
