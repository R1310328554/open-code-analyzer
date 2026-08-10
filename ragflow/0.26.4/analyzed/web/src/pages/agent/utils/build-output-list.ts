// build-output-list.ts — 将 outputs 对象转为 OutputType[] 供表单展示。

import { OutputType } from '../form/components/output';

/** 遍历 outputs 键值，生成 { title: key, type: val.type } 列表。 */
export function buildOutputList(outputs: Record<string, Record<string, any>>) {
  return Object.entries(outputs).reduce<OutputType[]>((pre, [key, val]) => {
    pre.push({ title: key, type: val.type });
    return pre;
  }, []);
}
