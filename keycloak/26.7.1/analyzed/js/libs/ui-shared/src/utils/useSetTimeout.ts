import { useEffect, useRef, useCallback } from "react";

/**
 * 安全调度 `setTimeout` 的 Hook：组件卸载后禁止再调度，并自动清理未触发的定时器。
 * 返回的函数签名与原生 `setTimeout` 类似，额外提供取消句柄。
 */
export function useSetTimeout() {
  // 标记组件是否已卸载，防止卸载后继续调度
  const didUnmountRef = useRef(false);
  // 跟踪当前组件内所有尚未触发的 timer id
  const scheduledTimersRef = useRef(new Set<number>());

  useEffect(() => {
    didUnmountRef.current = false;

    return () => {
      didUnmountRef.current = true;
      clearAll();
    };
  }, []);

  /** 清除本 Hook 登记的全部 pending 定时器 */
  function clearAll() {
    scheduledTimersRef.current.forEach((timer) => clearTimeout(timer));
    scheduledTimersRef.current.clear();
  }

  return useCallback((callback: () => void, delay: number) => {
    if (didUnmountRef.current) {
      throw new Error("Can't schedule a timeout on an unmounted component.");
    }

    const timer = Number(setTimeout(handleCallback, delay));

    scheduledTimersRef.current.add(timer);

    function handleCallback() {
      scheduledTimersRef.current.delete(timer);
      callback();
    }

    // 返回取消函数，供调用方在需要时提前清除单个定时器
    return function cancelTimeout() {
      clearTimeout(timer);
      scheduledTimersRef.current.delete(timer);
    };
  }, []);
}
