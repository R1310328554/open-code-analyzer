// use-set-default-model.ts — 聊天表单：首次加载时为 llm_id 填入第一个已添加的对话模型。

import { ModelTypeMap } from '@/components/model-tree-select';
import { useFetchAllAddedModels } from '@/hooks/use-llm-request';
import { getRealModelName } from '@/utils/llm-util';
import { useEffect, useRef } from 'react';
import { UseFormReturn } from 'react-hook-form';

/** 当 allAddedModels 就绪且尚未设置时，自动 setValue llm_id（仅执行一次）。 */
export function useSetDefaultModel(form: UseFormReturn<any>) {
  const { data: allAddedModels } = useFetchAllAddedModels();
  // 防止用户手动改选后被 effect 覆盖
  const hasSet = useRef(false);

  useEffect(() => {
    if (hasSet.current || !allAddedModels.length) return;
    // 仅保留 model_type 含 llm_id 的模型作为默认对话模型候选
    const chatModels = allAddedModels.filter((m) =>
      m.model_type?.some((t) => ModelTypeMap.llm_id.includes(t)),
    );
    const first = chatModels[0];
    if (first) {
      const modelName = getRealModelName(first.name);
      // 格式：modelName@instance_name@provider_name
      form.setValue(
        'llm_id',
        `${modelName}@${first.instance_name}@${first.provider_name}`,
      );
      hasSet.current = true;
    }
  }, [allAddedModels, form]);
}
