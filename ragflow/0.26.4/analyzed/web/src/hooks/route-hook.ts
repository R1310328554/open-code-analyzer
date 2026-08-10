// route-hook.ts — 路由辅助 Hooks：路径段解析、知识库 query 参数与分页 URL 同步。

import {
  KnowledgeRouteKey,
  KnowledgeSearchParams,
} from '@/constants/knowledge';
import { Routes } from '@/routes';
import { useCallback } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router';

/** pathname 分段索引：Second='2'、Third='3'（split('/') 下标）。 */
export enum SegmentIndex {
  Second = '2',
  Third = '3',
}

/** 按 SegmentIndex 取 pathname 对应路径段。 */
export const useSegmentedPathName = (index: SegmentIndex) => {
  const { pathname } = useLocation();

  const pathArray = pathname.split('/');
  return pathArray[index] || '';
};

export const useSecondPathName = () => {
  return useSegmentedPathName(SegmentIndex.Second);
};

export const useThirdPathName = () => {
  return useSegmentedPathName(SegmentIndex.Third);
};

/** 从 URL 读取知识库相关 query：type、documentId、knowledgeId（Dataflow 页特殊处理）。 */
export const useGetKnowledgeSearchParams = () => {
  const [currentQueryParameters] = useSearchParams();
  const { pathname } = useLocation();
  const isDataflowResultPage = pathname === Routes.DataflowResult;

  return {
    type: currentQueryParameters.get(KnowledgeSearchParams.Type) || '',
    documentId:
      currentQueryParameters.get(KnowledgeSearchParams.DocumentId) || '',
    knowledgeId: isDataflowResultPage
      ? currentQueryParameters.get('knowledgeId') || ''
      : currentQueryParameters.get(KnowledgeSearchParams.KnowledgeId) || '',
  };
};

/** 带 state.from 的 navigate，供返回上一页等场景使用。 */
export const useNavigateWithFromState = () => {
  const navigate = useNavigate();
  return useCallback(
    (path: string) => {
      navigate(path, { state: { from: path } });
    },
    [navigate],
  );
};

export const useNavigateToDataset = () => {
  const navigate = useNavigate();
  const { knowledgeId } = useGetKnowledgeSearchParams();

  return useCallback(() => {
    navigate(`/knowledge/${KnowledgeRouteKey.Dataset}?id=${knowledgeId}`);
  }, [knowledgeId, navigate]);
};

/** 只读读取 URL 中 page/size 分页参数（原始字符串/默认值）。 */
export const useGetPaginationParams = () => {
  const [currentQueryParameters] = useSearchParams();

  return {
    page: currentQueryParameters.get('page') || 1,
    size: currentQueryParameters.get('size') || 10,
  };
};

/** 写入 URL query 的 page/size，并返回数值型 page/size。 */
export const useSetPaginationParams = () => {
  const [queryParameters, setSearchParams] = useSearchParams();
  // const newQueryParameters: URLSearchParams = useMemo(
  //   () => new URLSearchParams(queryParameters.toString()),
  //   [queryParameters],
  // );

  const setPaginationParams = useCallback(
    (page: number = 1, pageSize?: number) => {
      queryParameters.set('page', page.toString());
      if (pageSize) {
        queryParameters.set('size', pageSize.toString());
      }
      setSearchParams(queryParameters);
    },
    [setSearchParams, queryParameters],
  );

  return {
    setPaginationParams,
    page: Number(queryParameters.get('page')) || 1,
    size: Number(queryParameters.get('size')) || 50,
  };
};
