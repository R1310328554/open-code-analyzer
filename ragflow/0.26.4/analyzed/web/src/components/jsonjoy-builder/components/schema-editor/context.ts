// schema-editor/context.ts — Schema 编辑器字段名输入的 React Context 与 pattern 读取 Hook。

import React, { useContext } from 'react';
import { KeyInputProps } from './interface';

/** 向子树注入字段名校验 pattern（RegExp 或字符串）。 */
export const KeyInputContext = React.createContext<KeyInputProps>({});

/** 从 KeyInputContext 读取当前字段名输入 pattern。 */
export function useInputPattern() {
  const x = useContext(KeyInputContext);
  return x.pattern;
}
