// use-fetch-data.ts — 挂载时拉取 Agent 详情并将 DSL 转为画布图数据。

import { useFetchAgent } from '@/hooks/use-agent-request';
import { useEffect } from 'react';
import { dslToGraph } from '../utils/dsl-bridge';
import { useSetGraphInfo } from './use-set-graph';

/** data 变更时 dslToGraph 写入 store，并在 mount 时 refetch 一次。 */
export const useFetchDataOnMount = () => {
  const { loading, data, refetch } = useFetchAgent();
  const setGraphInfo = useSetGraphInfo();

  /** Agent DSL 到达后同步到 React Flow 图状态。 */
  useEffect(() => {
    setGraphInfo(dslToGraph(data?.dsl));
  }, [setGraphInfo, data]);

  /** 组件挂载时主动刷新 Agent 详情。 */
  useEffect(() => {
    refetch();
  }, [refetch]);

  return { loading, flowDetail: data };
};
