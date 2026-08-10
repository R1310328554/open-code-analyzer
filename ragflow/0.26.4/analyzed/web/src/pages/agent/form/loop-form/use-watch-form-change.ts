// use-watch-form-change.ts — Loop 表单监听：loop_variables 推导 outputs，终止条件字段初始化。

import { JsonSchemaDataType } from '@/constants/agent';
import { buildVariableValue } from '@/utils/canvas-util';
import { useCallback, useEffect } from 'react';
import { UseFormReturn, useFormContext, useWatch } from 'react-hook-form';
import { InputMode } from '../../constant';
import { IOutputs } from '../../interface';
import useGraphStore from '../../store';
import { LoopFormSchemaType } from './schema';
import { useBuildLogicalOptions } from './use-build-logical-options';

/** 监听表单变更，将 loop_variables 聚合为 outputs 并 replaceNodeForm 写回画布。 */
export function useWatchFormChange(
  id?: string,
  form?: UseFormReturn<LoopFormSchemaType>,
) {
  const values = useWatch({ control: form?.control });
  const { replaceNodeForm } = useGraphStore((state) => state);

  useEffect(() => {
    if (id) {
      const nextValues = {
        ...values,
        // 每个循环变量名映射为 output 键，type 来自变量定义
        outputs: values.loop_variables?.reduce((pre, cur) => {
          const variable = cur.variable;
          if (variable) {
            pre[variable] = {
              type: cur.type,
              value: '',
            };
          }
          return pre;
        }, {} as IOutputs),
      };

      replaceNodeForm(id, nextValues);
    }
  }, [form?.formState.isDirty, id, replaceNodeForm, values]);
}

/** 终止条件数组项字段路径前缀（含索引占位）。 */
type ConditionPrefixType = `loop_termination_condition.${number}.`;
/** 终止条件中 variable 字段的完整路径类型。 */
export type ConditionKeyType = `${ConditionPrefixType}variable`;
/** 终止条件 input_mode 字段路径类型。 */
export type ConditionModeType = `${ConditionPrefixType}input_mode`;
/** 终止条件 value 字段路径类型。 */
export type ConditionValueType = `${ConditionPrefixType}value`;
/** 终止条件 operator 字段路径类型。 */
export type ConditionOperatorType = `${ConditionPrefixType}operator`;
/** 循环变量类型变更时，重置关联终止条件的 mode/value/operator 默认值。 */
export function useInitializeConditions(id?: string) {
  const form = useFormContext<LoopFormSchemaType>();
  const { buildLogicalOptions } = useBuildLogicalOptions();

  /** 数值类型变量强制 input_mode 为 Constant。 */
  const initializeConditionMode = useCallback(
    (modeFieldAlias: ConditionModeType, keyType: string) => {
      if (keyType === JsonSchemaDataType.Number) {
        form.setValue(modeFieldAlias, InputMode.Constant, {
          shouldDirty: true,
          shouldValidate: true,
        });
      }
    },
    [form],
  );

  /** 按变量类型设置比较值初值：Number→0、Boolean→true、其余空串。 */
  const initializeConditionValue = useCallback(
    (valueFieldAlias: ConditionValueType, keyType: string) => {
      let initialValue: string | boolean | number = '';

      if (keyType === JsonSchemaDataType.Number) {
        initialValue = 0;
      } else if (keyType === JsonSchemaDataType.Boolean) {
        initialValue = true;
      }

      form.setValue(valueFieldAlias, initialValue, {
        shouldDirty: true,
        shouldValidate: true,
      });
    },
    [form],
  );

  /** 按变量类型取首个可用比较运算符写入 operator 字段。 */
  const initializeConditionOperator = useCallback(
    (operatorFieldAlias: ConditionOperatorType, keyType: string) => {
      const logicalOptions = buildLogicalOptions(keyType);

      form.setValue(operatorFieldAlias, logicalOptions?.at(0)?.value, {
        shouldDirty: true,
        shouldValidate: true,
      });
    },
    [buildLogicalOptions, form],
  );

  /** 遍历终止条件，匹配当前变量后批量初始化 mode/value/operator。 */
  const initializeVariableRelatedConditions = useCallback(
    (variable: string, variableType: string) => {
      form?.getValues('loop_termination_condition').forEach((x, idx) => {
        if (variable && x.variable === buildVariableValue(variable, id)) {
          const prefix: ConditionPrefixType = `loop_termination_condition.${idx}.`;
          initializeConditionMode(`${prefix}input_mode`, variableType);
          initializeConditionValue(`${prefix}value`, variableType);
          initializeConditionOperator(`${prefix}operator`, variableType);
        }
      });
    },
    [
      form,
      id,
      initializeConditionMode,
      initializeConditionOperator,
      initializeConditionValue,
    ],
  );

  return {
    initializeVariableRelatedConditions,
    initializeConditionMode,
    initializeConditionValue,
    initializeConditionOperator,
  };
}
