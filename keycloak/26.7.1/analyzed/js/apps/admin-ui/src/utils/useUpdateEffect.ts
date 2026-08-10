/**
 * 仅在依赖更新时执行的 useEffect，跳过组件首次挂载。
 * 等价于 class 组件 componentDidUpdate 中按 deps 触发的副作用。
 */
import { DependencyList, EffectCallback, useEffect, useRef } from "react";

export function useUpdateEffect(effect: EffectCallback, deps?: DependencyList) {
  const didMount = useRef(false);

  useEffect(() => {
    if (didMount.current) {
      return effect();
    }

    // 首次渲染仅标记已挂载，不执行 effect
    didMount.current = true;
  }, deps);
}
