// use-build-options.ts — 表单下拉选项构建：Switch 逻辑运算符与模型类型 i18n 标签。

import { SwitchLogicOperator } from '@/constants/agent';
import { buildOptions } from '@/utils/form';
import { useCallback } from 'react';
import { useTranslation } from 'react-i18next';

/** 基于 SwitchLogicOperator 枚举与 i18n 生成 Switch 节点逻辑运算符选项。 */
export function useBuildSwitchLogicOperatorOptions() {
  const { t } = useTranslation();
  return buildOptions(
    SwitchLogicOperator,
    t,
    'flow.switchLogicOperatorOptions',
  );
}

/** 返回 buildModelTypeOptions，将模型类型字符串列表映射为 value/label 选项。 */
export function useBuildModelTypeOptions() {
  const { t } = useTranslation();

  const buildModelTypeOptions = useCallback(
    (list: string[]) => {
      return list.map((x) => ({
        value: x,
        label: t(`setting.modelTypes.${x}`),
      }));
    },
    [t],
  );

  return {
    buildModelTypeOptions,
  };
}
