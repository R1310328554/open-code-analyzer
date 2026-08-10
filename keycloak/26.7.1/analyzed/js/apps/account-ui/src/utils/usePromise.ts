import type { DependencyList } from "react";
import { useEffect, useState } from "react";

/**
 * 创建 Promise 的工厂函数类型。
 * 接收 AbortSignal：组件卸载或依赖变更时信号会被 abort，便于取消 fetch 等异步操作。
 */
export type PromiseFactoryFn<T> = (signal: AbortSignal) => Promise<T>;

/** Promise 成功解析后的回调。 */
export type PromiseResolvedFn<T> = (value: T) => void;

/**
 * 在 React 组件中执行异步 Promise 并将结果写入 state 的钩子。
 *
 * 依赖变更或卸载时自动 abort 进行中的请求，并忽略 AbortError。
 *
 * ```ts
 * const [products, setProducts] = useState();
 *
 * function getProducts() {
 *  return fetch('/api/products').then((res) => res.json());
 * }
 *
 * usePromise(() => getProducts(), setProducts);
 * ```
 *
 * 可传入依赖数组，依赖变化时重新创建 Promise：
 *
 * ```ts
 * usePromise(() => getProduct(id), setProduct, [id]);
 * ```
 *
 * 工厂函数接收 AbortSignal，可用于取消 fetch：
 *
 * ```ts
 * usePromise((signal) => fetch(`/api/products/${id}`, { signal }).then((res) => res.json()), setProduct, [id]);
 * ```
 *
 * @param factory 创建 Promise 的函数
 * @param callback Promise 成功时调用，通常用于 setState
 * @param deps 依赖列表；变化时重新执行 factory
 */
export function usePromise<T>(
  factory: PromiseFactoryFn<T>,
  callback: PromiseResolvedFn<T>,
  deps: DependencyList = [],
) {
  const [error, setError] = useState<unknown>();
  useEffect(() => {
    const controller = new AbortController();
    const { signal } = controller;

    async function handlePromise() {
      // 解析 Promise；失败时若非 abort 则记录错误
      try {
        callback(await factory(signal));
      } catch (error) {
        // 组件卸载或依赖变更导致的 abort 不视为错误
        if (error instanceof Error && error.name === "AbortError") {
          return;
        }

        setError(error);
      }
    }

    void handlePromise();

    // 清理：abort 进行中的 Promise
    return () => controller.abort();
  }, deps);

  // 非 abort 错误向上抛出，由 Error Boundary 或上层处理
  if (error) {
    throw error;
  }
}
