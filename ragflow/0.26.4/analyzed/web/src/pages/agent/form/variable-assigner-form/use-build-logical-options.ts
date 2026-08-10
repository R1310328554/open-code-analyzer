// use-build-logical-options.ts — VariableAssigner 逻辑运算符选项：按变量类型返回数组/数值/通用运算符。

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

/** 根据 JSON Schema 类型构建 VariableAssigner 可用的逻辑比较运算符下拉选项。 */
export function useBuildLogicalOptions() {
  const { t } = useTranslation();

  /** 通用枚举转 i18n 选项，prefix 为 flow.variableAssignerLogicalOperatorOptions。 */
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

  /** array* 用数组运算符，Number 用数值专用标签，其余用 VariableAssignerLogicalOperator。 */
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
