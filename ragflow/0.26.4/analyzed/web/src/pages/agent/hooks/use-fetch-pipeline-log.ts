// use-fetch-pipeline-log.ts — 流水线日志 Sheet：轮询 trace 直至 END 完成或用户取消。

import { useFetchMessageTrace } from '@/hooks/use-agent-request';
import { isEmpty } from 'lodash';
import { useCallback, useEffect, useMemo } from 'react';

/** 管理 message trace 拉取、完成判定与 isParsing 加载态。 */
export function useFetchPipelineLog(logSheetVisible: boolean) {
  const {
    setMessageId,
    data,
    loading,
    messageId,
    setISStopFetchTrace,
    isStopFetchTrace,
  } = useFetchMessageTrace();

  /** 末条 trace 为 END 且 message 非空时视为流水线执行完成。 */
  const isCompleted = useMemo(() => {
    if (Array.isArray(data)) {
      const latest = data?.at(-1);
      return (
        latest?.component_id === 'END' && !isEmpty(latest?.trace[0].message)
      );
    }
    return false;
  }, [data]);

  /** 尚无 trace 数据时为 true。 */
  const isLogEmpty = !data || !data.length;

  /** 停止 trace 轮询（用户取消或执行完成）。 */
  const stopFetchTrace = useCallback(() => {
    setISStopFetchTrace(true);
  }, [setISStopFetchTrace]);

  // 执行完成后自动停止轮询
  // cancel request
  useEffect(() => {
    if (isCompleted) {
      stopFetchTrace();
    }
  }, [isCompleted, stopFetchTrace]);

  /** 打开日志 Sheet 时恢复 trace 拉取。 */
  useEffect(() => {
    if (logSheetVisible) {
      setISStopFetchTrace(false);
    }
  }, [logSheetVisible, setISStopFetchTrace]);

  return {
    logs: data,
    isLogEmpty,
    isCompleted,
    loading,
    isParsing: !isLogEmpty && !isCompleted && !isStopFetchTrace,
    messageId,
    setMessageId,
    stopFetchTrace,
  };
}

export type UseFetchLogReturnType = ReturnType<typeof useFetchPipelineLog>;
