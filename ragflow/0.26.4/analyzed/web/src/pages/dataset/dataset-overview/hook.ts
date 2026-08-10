// hook.ts — 数据集概览页数据请求：统计汇总与文件/数据集流水线日志列表。

import { useHandleFilterSubmit } from '@/components/list-filter-bar/use-handle-filter-submit';
import {
  useGetPaginationWithRouter,
  useHandleSearchChange,
} from '@/hooks/logic-hooks';
import {
  getKnowledgeBasicInfo,
  listDataPipelineLogDocument,
} from '@/services/knowledge-service';
import { useQuery } from '@tanstack/react-query';
import { useCallback, useState } from 'react';
import { useParams, useSearchParams } from 'react-router';
import { LogTabs } from './dataset-common';
import { IFileLogList, IOverviewTotal } from './interface';

/** 拉取知识库文档解析状态计数（完成/失败/处理中等）。 */
const useFetchOverviewTotal = () => {
  const [searchParams] = useSearchParams();
  const { id } = useParams();
  const knowledgeBaseId = searchParams.get('id') || id;
  const { data } = useQuery<IOverviewTotal>({
    queryKey: ['overviewTotal'],
    queryFn: async () => {
      const { data: res = {} } = await getKnowledgeBasicInfo(
        knowledgeBaseId || '',
      );
      return res.data || [];
    },
  });
  return { data };
};

/** 分页检索数据流水线日志，支持关键词、Tab 切换与筛选器。 */
const useFetchFileLogList = () => {
  const [searchParams] = useSearchParams();
  const { searchString, handleInputChange } = useHandleSearchChange();
  const { pagination, setPagination } = useGetPaginationWithRouter();
  const { filterValue, setFilterValue, handleFilterSubmit } =
    useHandleFilterSubmit();
  const { id } = useParams();
  const [active, setActive] = useState<(typeof LogTabs)[keyof typeof LogTabs]>(
    LogTabs.FILE_LOGS,
  );
  const knowledgeBaseId = searchParams.get('id') || id;
  // 当前 Tab 决定 log_type 查询参数
  const logType = active === LogTabs.DATASET_LOGS ? 'dataset' : 'file';
  const { data } = useQuery<IFileLogList>({
    queryKey: [
      'fileLogList',
      knowledgeBaseId,
      pagination,
      searchString,
      active,
      filterValue,
    ],
    // 翻页时保留上一页数据避免表格闪烁
    placeholderData: (previousData) => {
      if (previousData === undefined) {
        return { logs: [], total: 0 };
      }
      return previousData;
    },
    enabled: true,
    queryFn: async () => {
      const { data: res = {} } = await listDataPipelineLogDocument(
        knowledgeBaseId || '',
        {
          page: pagination.current,
          page_size: pagination.pageSize,
          keywords: searchString,
          log_type: logType,
          ...filterValue,
        },
      );
      return res.data || [];
    },
  });
  const onInputChange: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (e) => {
      setPagination({ page: 1 });
      handleInputChange(e);
    },
    [handleInputChange, setPagination],
  );
  return {
    data,
    searchString,
    handleInputChange: onInputChange,
    pagination: { ...pagination, total: data?.total },
    setPagination,
    active,
    setActive,
    filterValue,
    setFilterValue,
    handleFilterSubmit,
  };
};

export { useFetchFileLogList, useFetchOverviewTotal };
