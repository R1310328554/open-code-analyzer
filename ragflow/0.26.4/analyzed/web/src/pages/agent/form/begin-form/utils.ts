// utils.ts — Begin 节点 inputs 在数组与 Record 形态间的转换工具。

import { BeginQuery } from '../../interface';

/** 将 DSL inputs 对象还原为表单用的 BeginQuery[]（每项补 key）。 */
export function buildBeginInputListFromObject(
  inputs: Record<string, Omit<BeginQuery, 'key'>>,
) {
  return Object.entries(inputs || {}).reduce<BeginQuery[]>(
    (pre, [key, value]) => {
      pre.push({ ...(value || {}), key });

      return pre;
    },
    [],
  );
}
