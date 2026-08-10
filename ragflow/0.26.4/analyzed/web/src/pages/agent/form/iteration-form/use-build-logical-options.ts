// use-build-logical-options.ts — Iteration 变量赋值逻辑运算符选项（按 JSON 类型分支）。

import { buildOptions } from '@/utils/form';
import { camelCase } from 'lodash';
import { useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import {
  JsonSchemaDataType,
  VariableAssignerLogicalArrayOperator,
  VariableAssignerLogicalNumberOperator,
  VariableAssignerLogicalNumberOperatorLabelMap,
  VariableAssignerLogicalOperator,
} from '../../constant';

/** 按变量类型返回 VariableAssigner 逻辑运算符下拉（数组/数字/默认）。 */
export function useBuildLogicalOptions() {
  const { t } = useTranslation();

  /** 从 constant 映射构建 i18n 逻辑运算符选项。 */
  const buildVariableAssignerLogicalOptions = useCallback(
    (record: Record<string, any>) => {
      return buildOptions(
        record,
        t,
        'flow.variableAssignerLogicalOperatorOptions',
        true,
      );
    },
    [t],
  );

  /** 数组类型用 Array 运算符，Number 用数值比较，其余用通用 Logical。 */
  const buildLogicalOptions = useCallback(
    (type: string) => {
      if (
        type?.toLowerCase().startsWith(JsonSchemaDataType.Array.toLowerCase())
      ) {
        return buildVariableAssignerLogicalOptions(
          VariableAssignerLogicalArrayOperator,
        );
      }

      if (type === JsonSchemaDataType.Number) {
        return Object.values(VariableAssignerLogicalNumberOperator).map(
          (val) => ({
            label: t(
              `flow.variableAssignerLogicalOperatorOptions.${camelCase(VariableAssignerLogicalNumberOperatorLabelMap[val as keyof typeof VariableAssignerLogicalNumberOperatorLabelMap] || val)}`,
            ),
            value: val,
          }),
        );
      }

      return buildVariableAssignerLogicalOptions(
        VariableAssignerLogicalOperator,
      );
    },
    [buildVariableAssignerLogicalOptions, t],
  );

  return {
    buildLogicalOptions,
  };
}
