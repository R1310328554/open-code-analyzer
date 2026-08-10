/**
 * 布尔开关状态 Hook：适用于折叠面板、模态框显隐、开关按钮等场景。
 */
import { useCallback, useState } from "react";

/**
 * @param initialValue 初始布尔值，默认 false
 * @returns [value, toggleValue, setValue] 元组
 */
export default function useToggle(initialValue = false) {
  const [value, setValue] = useState(initialValue);
  const toggleValue = useCallback(() => setValue((val) => !val), []);

  return [value, toggleValue, setValue] as const;
}
