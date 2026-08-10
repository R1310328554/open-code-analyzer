// use-build-logical-options.ts — Loop 终止条件比较运算符选项（按变量类型映射）。

import { SwitchOperatorOptions } from '@/constants/agent';
import { camelCase, toLower } from 'lodash';
import { useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { LoopTerminationStringComparisonOperatorMap } from '../../constant';

/** 按变量 type 从 LoopTerminationStringComparisonOperatorMap 构建 i18n 选项。 */
export function useBuildLogicalOptions() {
  const { t } = useTranslation();

  /** 将运算符 value 映射为 flow.switchOperatorOptions 标签。 */
  const buildLogicalOptions = useCallback(
    (type: string) => {
      return LoopTerminationStringComparisonOperatorMap[
        toLower(type) as keyof typeof LoopTerminationStringComparisonOperatorMap
      ]?.map((x) => ({
        label: t(
          `flow.switchOperatorOptions.${camelCase(SwitchOperatorOptions.find((y) => y.value === x)?.label || x)}`,
        ),
        value: x,
      }));
    },
    [t],
  );

  return {
    buildLogicalOptions,
  };
}
