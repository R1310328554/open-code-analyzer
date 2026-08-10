// use-values.ts — Code 节点表单初始值：脚本模板、arguments 数组与 output 契约。

import { RAGFlowNodeType } from '@/interfaces/database/agent';
import { isEmpty } from 'lodash';
import { useMemo } from 'react';
import { initialCodeValues } from '../../constant';
import { buildDefaultCodeOutput, deserializeCodeOutputContract } from './utils';

/** 将 DSL arguments 对象转为表单用的 { name, type }[]。 */
function convertToArray(args: Record<string, string>) {
  return Object.entries(args).map(([key, value]) => ({
    name: key,
    type: value,
  }));
}

/** 从节点 form 推导 values 与 legacyOutputs（多 output 旧数据兼容）。 */
export function useValues(node?: RAGFlowNodeType) {
  const valueState = useMemo(() => {
    const formData = node?.data?.form;

    if (isEmpty(formData)) {
      return {
        values: {
          ...initialCodeValues,
          arguments: convertToArray(initialCodeValues.arguments),
          output: buildDefaultCodeOutput(),
        },
        legacyOutputs: [],
      };
    }

    const { contract, legacyOutputs } = deserializeCodeOutputContract(formData);

    return {
      values: {
        ...formData,
        arguments: convertToArray(formData.arguments),
        output: contract ?? buildDefaultCodeOutput(),
      },
      legacyOutputs,
    };
  }, [node?.data?.form]);

  return valueState;
}
