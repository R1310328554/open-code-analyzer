// use-callback-ref.ts — Radix 风格回调 ref：避免 callback 作为 prop/依赖触发多余重渲染。

import * as React from 'react';

/**
 * @see https://github.com/radix-ui/primitives/blob/main/packages/react/use-callback-ref/src/useCallbackRef.tsx
 */

/**
 * 将 callback 存入 ref 并返回稳定引用函数，
 * 避免作为 prop 或 effect 依赖时引发多余重渲染或重复执行。
 */
function useCallbackRef<T extends (...args: never[]) => unknown>(
  callback: T | undefined,
): T {
  const callbackRef = React.useRef(callback);

  React.useEffect(() => {
    callbackRef.current = callback;
  });

  // https://github.com/facebook/react/issues/19240
  return React.useMemo(
    () => ((...args) => callbackRef.current?.(...args)) as T,
    [],
  );
}

export { useCallbackRef };
