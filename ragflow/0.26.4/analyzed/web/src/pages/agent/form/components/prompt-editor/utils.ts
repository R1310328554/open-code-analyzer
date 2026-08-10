// utils.ts — Prompt 编辑器变量路径解析：拆分 node@var 与嵌套路径后缀。

import type { ReactNode } from 'react';

/** 变量下拉选项结构：label/value 及可选 parentLabel、icon、type。 */
type PromptVariableOptionLike = {
  label: string;
  value: string;
  parentLabel?: string | ReactNode;
  icon?: ReactNode;
  type?: string;
};

/** 变量 value 拆分结果：根 node@var 与 .path 后缀。 */
type PromptVariablePathParts = {
  rootValue: string;
  pathSuffix: string;
};

type PromptVariableLeadingPathMatch = {
  pathSuffix: string;
  remainingText: string;
};

/** 匹配文本开头的嵌套路径后缀（如 .field[0].name）。 */
const PromptVariableLeadingPathRegex =
  /^(?<pathSuffix>(?:\.(?:\d+|[A-Za-z_][A-Za-z0-9_]*))+)/;

/** 将 nodeId@variable.path 拆分为根 value 与 pathSuffix。 */
function splitPromptVariablePath(value: string): PromptVariablePathParts {
  const [nodeId, variable = ''] = value.split('@');

  if (!nodeId || !variable) {
    return { rootValue: value, pathSuffix: '' };
  }

  const dotIndex = variable.indexOf('.');
  if (dotIndex < 0) {
    return { rootValue: value, pathSuffix: '' };
  }

  return {
    rootValue: `${nodeId}@${variable.slice(0, dotIndex)}`,
    pathSuffix: variable.slice(dotIndex),
  };
}

/** 从文本开头提取变量嵌套路径后缀，返回剩余文本。 */
export function extractLeadingPromptVariablePath(
  text: string,
): PromptVariableLeadingPathMatch | undefined {
  const match = PromptVariableLeadingPathRegex.exec(text);
  const pathSuffix = match?.groups?.pathSuffix;

  if (!pathSuffix) {
    return undefined;
  }

  return {
    pathSuffix,
    remainingText: text.slice(pathSuffix.length),
  };
}

/** 将 pathSuffix 追加到选项的 value 与 label。 */
export function appendPromptVariablePath(
  option: PromptVariableOptionLike,
  pathSuffix: string,
): PromptVariableOptionLike {
  if (!pathSuffix) {
    return option;
  }

  return {
    ...option,
    value: `${option.value}${pathSuffix}`,
    label: `${option.label}${pathSuffix}`,
  };
}

/** 按完整 value 精确匹配，或基于根变量 + 路径后缀解析选项。 */
export function resolvePromptVariableOption(
  value: string,
  options: PromptVariableOptionLike[],
): PromptVariableOptionLike | undefined {
  const exactMatch = options.find((option) => option.value === value);
  if (exactMatch) {
    return exactMatch;
  }

  const { rootValue, pathSuffix } = splitPromptVariablePath(value);
  if (!pathSuffix) {
    return undefined;
  }

  const rootOption = options.find((option) => option.value === rootValue);
  if (!rootOption) {
    return undefined;
  }

  return appendPromptVariablePath(rootOption, pathSuffix);
}
