// use-save-graph.ts — Agent 画布持久化：buildDslData 组装参数并调用 setAgent，含调试与自动保存。

import {
  useFetchAgent,
  useResetAgent,
  useSetAgent,
} from '@/hooks/use-agent-request';
import {
  GlobalVariableType,
  RAGFlowNodeType,
} from '@/interfaces/database/agent';
import { formatDate } from '@/utils/date';
import { useDebounceEffect } from 'ahooks';
import { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router';
import useGraphStore from '../store';
import { useBuildDslData } from './use-build-dsl';

/** 将当前 nodes/edges 转为 DSL 并提交 setAgent；release 为 true 时一并发布。 */
export const useSaveGraph = (showMessage: boolean = true) => {
  const { data } = useFetchAgent();
  const { setAgent, loading } = useSetAgent(showMessage);
  const { id } = useParams();
  const { buildDslData } = useBuildDslData();

  const saveGraph = useCallback(
    async (
      currentNodes?: RAGFlowNodeType[],
      otherParam?: {
        globalVariables: Record<string, GlobalVariableType>;
      },
      release?: boolean,
    ) => {
      const params: Record<string, any> = {
        id,
        title: data.title,
        dsl: buildDslData(currentNodes, otherParam),
      };

      if (release) {
        params.release = 'true';
      }

      return setAgent(params);
    },
    [setAgent, data, id, buildDslData],
  );

  return { saveGraph, loading };
};

/** 打开调试抽屉前先 saveGraph，成功后 resetAgent 清空历史消息再 show。 */
export const useSaveGraphBeforeOpeningDebugDrawer = (show: () => void) => {
  const { saveGraph, loading } = useSaveGraph();
  const { resetAgent } = useResetAgent();

  const handleRun = useCallback(
    async (nextNodes?: RAGFlowNodeType[]) => {
      const saveRet = await saveGraph(nextNodes);
      if (saveRet?.code === 0) {
        // 每次打开运行抽屉前重置 Agent 会话，避免残留上下文
        // Call the reset api before opening the run drawer each time
        const resetRet = await resetAgent();
        // reset 成功后历史消息清空，再展示运行面板
        // After resetting, all previous messages will be cleared.
        if (resetRet?.code === 0) {
          show();
        }
      }
    },
    [saveGraph, resetAgent, show],
  );

  return { handleRun, loading };
};

/** nodes/edges 变更后防抖 20s 自动保存；聊天抽屉打开时暂停保存。 */
export const useWatchAgentChange = (chatDrawerVisible: boolean) => {
  const [time, setTime] = useState<string>();
  const nodes = useGraphStore((state) => state.nodes);
  const edges = useGraphStore((state) => state.edges);
  const { saveGraph } = useSaveGraph(false);
  const { data: flowDetail } = useFetchAgent();

  const setSaveTime = useCallback((updateTime: number) => {
    setTime(formatDate(updateTime));
  }, []);

  useEffect(() => {
    setSaveTime(flowDetail?.update_time);
  }, [flowDetail, setSaveTime]);

  const saveAgent = useCallback(async () => {
    if (!chatDrawerVisible) {
      const ret = await saveGraph();
      setSaveTime(ret.data.update_time);
    }
  }, [chatDrawerVisible, saveGraph, setSaveTime]);

  useDebounceEffect(
    () => {
      saveAgent();
    },
    [nodes, edges],
    {
      wait: 1000 * 20,
    },
  );

  return time;
};
