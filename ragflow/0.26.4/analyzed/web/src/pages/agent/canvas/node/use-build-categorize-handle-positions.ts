// use-build-categorize-handle-positions.ts — Categorize 节点输出 Handle 垂直位置计算与 internals 刷新。

import { RAGFlowNodeType } from '@/interfaces/database/agent';
import { useUpdateNodeInternals } from '@xyflow/react';
import { get } from 'lodash';
import { useEffect, useMemo } from 'react';
import { z } from 'zod';
import { useCreateCategorizeFormSchema } from '../../form/categorize-form/use-form-schema';

/** 按 categorize form.items 顺序累加 top 偏移，供多出口 Handle 对齐渲染。 */
export const useBuildCategorizeHandlePositions = ({
  data,
  id,
}: {
  id: string;
  data: RAGFlowNodeType['data'];
}) => {
  const updateNodeInternals = useUpdateNodeInternals();

  const FormSchema = useCreateCategorizeFormSchema();

  type FormSchemaType = z.infer<typeof FormSchema>;

  // 从节点 data.form.items 读取分类项列表
  const items: Required<FormSchemaType['items']> = useMemo(() => {
    return get(data, `form.items`, []);
  }, [data]);

  const positions = useMemo(() => {
    const list: Array<{
      top: number;
      name: string;
      uuid: string;
    }> &
      Required<FormSchemaType['items']> = [];

    items.forEach((x, idx) => {
      list.push({
        ...x,
        top: idx === 0 ? 86 : list[idx - 1].top + 8 + 24,
      });
    });

    return list;
  }, [items]);

  // items 变化时通知 React Flow 重算 Handle 布局
  useEffect(() => {
    updateNodeInternals(id);
  }, [id, updateNodeInternals, items]);

  return { positions };
};
