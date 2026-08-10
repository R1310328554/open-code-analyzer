/**
 * 将高频 React Query setQueryData 合并为定时批量更新，降低流式聊天时的渲染压力。
 */
import { useCallback, useRef } from "react";
import { useQueryClient, QueryClient } from "@tanstack/react-query";

/** 批处理间隔（毫秒）与是否立即应用首次更新。 */
interface BatcherConfig {
  batchInterval?: number; // milliseconds, default 8ms (~120fps)
  immediateFirst?: boolean; // if true, first update is immediate
}

/** Hook 版查询批处理器，绑定组件生命周期内的 queryClient。 */
export const useQueryBatcher = <T>(
  queryKey: readonly unknown[],
  config: BatcherConfig = {},
) => {
  const queryClient = useQueryClient();
  const { batchInterval = 8, immediateFirst = false } = config;

  const batchRef = useRef<{
    updateBatch: T | undefined;
    batchTimeout: number | null;
    isFirstUpdate: boolean;
  }>({
    updateBatch: undefined,
    batchTimeout: null,
    isFirstUpdate: true,
  });

  const flushBatch = useCallback(() => {
    const { updateBatch, batchTimeout } = batchRef.current;
    if (updateBatch) {
      queryClient.setQueryData(queryKey, updateBatch);
      batchRef.current.updateBatch = undefined;
    }
    if (batchTimeout) {
      clearTimeout(batchTimeout);
      batchRef.current.batchTimeout = null;
    }
  }, [queryClient, queryKey]);

  const scheduleBatch = useCallback(
    (updater: (old: T | undefined) => T | undefined) => {
      const currentData = queryClient.getQueryData<T>(queryKey);
      const newBatch = updater(batchRef.current.updateBatch || currentData);
      batchRef.current.updateBatch = newBatch;

      // If this is the first update and immediateFirst is enabled, apply immediately
      if (immediateFirst && batchRef.current.isFirstUpdate) {
        batchRef.current.isFirstUpdate = false;
        queryClient.setQueryData(queryKey, newBatch);
        batchRef.current.updateBatch = undefined;
        return;
      }

      if (batchRef.current.batchTimeout) {
        clearTimeout(batchRef.current.batchTimeout);
      }
      batchRef.current.batchTimeout = setTimeout(flushBatch, batchInterval);
    },
    [queryClient, queryKey, flushBatch, batchInterval, immediateFirst],
  );

  const cleanup = useCallback(() => {
    if (batchRef.current.batchTimeout) {
      clearTimeout(batchRef.current.batchTimeout);
      batchRef.current.batchTimeout = null;
    }
    batchRef.current.updateBatch = undefined;
    batchRef.current.isFirstUpdate = true;
  }, []);

  return {
    scheduleBatch,
    flushBatch,
    cleanup,
  };
};

/** 非 Hook 工厂：供 mutation 流等场景创建独立批处理器。 */
export const createQueryBatcher = <T>(
  queryClient: QueryClient,
  queryKey: readonly unknown[],
  config: BatcherConfig = {},
) => {
  const { batchInterval = 8, immediateFirst = false } = config;

  let updateBatch: T | undefined = undefined;
  let batchTimeout: number | null = null;
  let isFirstUpdate = true;

  const flushBatch = () => {
    if (updateBatch) {
      queryClient.setQueryData(queryKey, updateBatch);
      updateBatch = undefined;
    }
    if (batchTimeout) {
      clearTimeout(batchTimeout);
      batchTimeout = null;
    }
  };

  const scheduleBatch = (updater: (old: T | undefined) => T | undefined) => {
    const currentData = queryClient.getQueryData<T>(queryKey);
    updateBatch = updater(updateBatch || currentData);

    // If this is the first update and immediateFirst is enabled, apply immediately
    if (immediateFirst && isFirstUpdate) {
      isFirstUpdate = false;
      queryClient.setQueryData(queryKey, updateBatch);
      updateBatch = undefined;
      return;
    }

    if (batchTimeout) {
      clearTimeout(batchTimeout);
    }
    batchTimeout = setTimeout(flushBatch, batchInterval);
  };

  const cleanup = () => {
    if (batchTimeout) {
      clearTimeout(batchTimeout);
      batchTimeout = null;
    }
    updateBatch = undefined;
    isFirstUpdate = true;
  };

  return {
    scheduleBatch,
    flushBatch,
    cleanup,
  };
};
