// utils.ts — Code 节点 output 契约：保留键校验、单/多 output 兼容与序列化。

import { ICodeForm } from '@/interfaces/database/agent';

/** 表单层单一业务 output：name 与 type。 */
export type CodeOutputContract = {
  name: string;
  type: string;
};

type DeserializeCodeOutputResult = {
  contract: CodeOutputContract | null;
  legacyOutputs: string[];
};

/** CodeExec 运行时保留 output 键，禁止用户 output 命名冲突。 */
const CodeExecReservedOutputKeys = [
  'content',
  'actual_type',
  'raw_result',
  'attachments',
  '_ERROR',
  '_ARTIFACTS',
  '_ATTACHMENT_CONTENT',
  '_created_time',
  '_elapsed_time',
] as const;

/** 调试面板展示的系统级 outputs 默认值。 */
export const CodeExecPanelSystemOutputs: ICodeForm['outputs'] = {
  content: {
    type: 'String',
    value: '',
  },
  actual_type: {
    type: 'String',
    value: '',
  },
  attachments: {
    type: 'Array<String>',
    value: [],
  },
};

const CodeExecReservedOutputKeySet = new Set<string>(
  CodeExecReservedOutputKeys,
);

/** 默认业务 output：result / String。 */
export function buildDefaultCodeOutput(): CodeOutputContract {
  return {
    name: 'result',
    type: 'String',
  };
}

/** 校验 output 名：非空、非保留键且不含路径点号。 */
export function isValidCodeOutputName(name: string): boolean {
  const value = name.trim();
  return (
    !!value && !CodeExecReservedOutputKeySet.has(value) && !value.includes('.')
  );
}

/** 从 outputs 中过滤掉 CodeExec 保留键，仅保留用户业务 output。 */
export function getBusinessOutputs(
  outputs: ICodeForm['outputs'] = {},
): ICodeForm['outputs'] {
  return Object.entries(outputs).reduce<ICodeForm['outputs']>((next, entry) => {
    const [name, value] = entry;

    if (!CodeExecReservedOutputKeySet.has(name)) {
      next[name] = value;
    }

    return next;
  }, {});
}

/** 从节点 outputs 反序列化单一契约；多 output 时返回 legacyOutputs 列表。 */
export function deserializeCodeOutputContract(
  form?: Pick<ICodeForm, 'outputs'> | null,
): DeserializeCodeOutputResult {
  const outputs = form?.outputs ?? {};
  const businessOutputs = Object.entries(getBusinessOutputs(outputs));

  if (businessOutputs.length === 0) {
    return { contract: buildDefaultCodeOutput(), legacyOutputs: [] };
  }

  if (businessOutputs.length > 1) {
    return {
      contract: null,
      legacyOutputs: businessOutputs.map(([name]) => name),
    };
  }

  const [name, output] = businessOutputs[0];

  return {
    contract: {
      name,
      type: output.type,
    },
    legacyOutputs: [],
  };
}

/** 判断是否存在多个业务 output（旧版多输出 DSL）。 */
export function hasLegacyMultiOutputs(
  outputs: ICodeForm['outputs'] = {},
): boolean {
  return Object.keys(getBusinessOutputs(outputs)).length > 1;
}

/** 将单一 output 契约序列化为 DSL outputs 对象。 */
export function serializeCodeOutputContract(
  contract: CodeOutputContract | null,
): ICodeForm['outputs'] {
  const name = contract?.name?.trim();
  const type = contract?.type?.trim();

  if (!name || !type || !isValidCodeOutputName(name)) {
    return {};
  }

  return {
    [name]: {
      type,
      value: null,
    },
  };
}
