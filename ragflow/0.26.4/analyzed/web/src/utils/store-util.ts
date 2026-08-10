/**
 * store-util.ts — Dva/Redux 副作用辅助：命名空间 loading 检测与 Promise 延迟。
 */

/** 判断 namespace 下任一 effect 是否处于 loading（effects 键为 namespace/effectName）。 */
export const getOneNamespaceEffectsLoading = (
  namespace: string,
  effects: Record<string, boolean>,
  effectNames: Array<string>,
) => {
  return effectNames.some(
    (effectName) => effects[`${namespace}/${effectName}`],
  );
};

/** 返回 resolve 于 ms 毫秒后的 Promise，用于异步流程等待。 */
export const delay = (ms: number) =>
  new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
