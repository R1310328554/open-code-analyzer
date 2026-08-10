import { DependencyList, useEffect } from "react";
import { useErrorBoundary } from "./ErrorBoundary";

/**
 * 在组件仍挂载时才更新状态的异步数据拉取 Hook。
 *
 * 接受两个函数：一个执行 adminClient 请求，另一个在数据返回后写入本地状态。
 *
 * @example
 * useFetch(
 *  () => adminClient.components.findOne({ id }),
 *  (component) => setupForm(component),
 *  []
 * );
 *
 * @param adminClientCall 执行 adminClient 异步调用的函数
 * @param callback 数据获取成功后用于更新组件状态的回调
 */
export function useFetch<T>(
  adminClientCall: () => Promise<T>,
  callback: (param: T) => void,
  deps?: DependencyList,
) {
  const { showBoundary } = useErrorBoundary();

  useEffect(() => {
    // 用 AbortController 在卸载或 deps 变更时取消过期的异步结果
    const controller = new AbortController();
    const { signal } = controller;
    adminClientCall()
      .then((result) => {
        if (!signal.aborted) {
          callback(result);
        }
      })
      .catch((error) => {
        console.error(error);
        if (!signal.aborted) {
          showBoundary(error);
        }
      });

    return () => controller.abort();
  }, deps);
}
