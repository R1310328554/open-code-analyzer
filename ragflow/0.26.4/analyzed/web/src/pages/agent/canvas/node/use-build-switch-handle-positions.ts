// use-build-switch-handle-positions.ts — Switch 节点条件分支与 Else Handle 垂直位置计算。

import { ISwitchCondition, RAGFlowNodeType } from '@/interfaces/database/agent';
import { useUpdateNodeInternals } from '@xyflow/react';
import get from 'lodash/get';
import { useEffect, useMemo } from 'react';
import { SwitchElseTo } from '../../constant';
import { generateSwitchHandleText } from '../../utils';

/** 按 conditions 与 Else 分支累加 top，结合条件块高度生成 Handle 标签与位置。 */
export const useBuildSwitchHandlePositions = ({
  data,
  id,
}: {
  id: string;
  data: RAGFlowNodeType['data'];
}) => {
  const updateNodeInternals = useUpdateNodeInternals();

  const conditions: ISwitchCondition[] = useMemo(() => {
    return get(data, 'form.conditions', []);
  }, [data]);

  const positions = useMemo(() => {
    const list: Array<{
      text: string;
      top: number;
      idx: number;
      condition?: ISwitchCondition;
    }> = [];

    [...conditions, ''].forEach((x, idx) => {
      // 首条 Case 固定 top=53，后续叠加 Case 标题与条件块高度
      let top = idx === 0 ? 53 : list[idx - 1].top + 10 + 14 + 16 + 16; // case number (Case 1) height + flex gap
      if (idx >= 1) {
        const previousItems = conditions[idx - 1]?.items ?? [];
        if (previousItems.length > 0) {
          // top += 12; // ConditionBlock padding
          top += previousItems.length * 26; // 每条条件变量行高约 26px
          // top += (previousItems.length - 1) * 25; // operator height
        }
      }

      list.push({
        text:
          idx < conditions.length
            ? generateSwitchHandleText(idx)
            : SwitchElseTo,
        idx,
        top,
        condition: typeof x === 'string' ? undefined : x,
      });
    });

    return list;
  }, [conditions]);

  useEffect(() => {
    updateNodeInternals(id);
  }, [id, updateNodeInternals, conditions]);

  return { positions };
};
