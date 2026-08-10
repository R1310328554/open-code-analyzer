// use-build-form-refs.ts — 多聊天框场景下按 chatBoxId 管理 LLM 表单引用。

import { removeUselessFieldsFromValues } from '@/utils/form';
import { isEmpty } from 'lodash';
import { useCallback, useEffect, useRef } from 'react';

/**
 * 维护各聊天框 LLM 配置表单的 ref 映射，并在 chatBoxIds 变化时清理失效引用。
 */
export function useBuildFormRefs(chatBoxIds: string[]) {
  // chatBoxId -> 表单实例（含 getFormData）
  const formRefs = useRef<Record<string, { getFormData: () => any }>>({});

  /** 为指定 chatBoxId 注册或更新表单 ref。 */
  const setFormRef = (id: string) => (ref: { getFormData: () => any }) => {
    formRefs.current[id] = ref;
  };

  /** 移除已不在 chatBoxIds 列表中的表单引用，避免内存泄漏。 */
  const cleanupFormRefs = useCallback(() => {
    const currentIds = new Set(chatBoxIds);
    Object.keys(formRefs.current).forEach((id) => {
      if (!currentIds.has(id)) {
        delete formRefs.current[id];
      }
    });
  }, [chatBoxIds]);

  /** 读取指定聊天框的 LLM 配置并剔除空字段。 */
  const getLLMConfigById = useCallback(
    (chatBoxId?: string) => {
      const llmConfig = chatBoxId
        ? formRefs.current[chatBoxId].getFormData()
        : {};

      return removeUselessFieldsFromValues(llmConfig, '');
    },
    [formRefs],
  );

  /** 判断指定聊天框是否尚未选择 llm_id。 */
  const isLLMConfigEmpty = useCallback(
    (chatBoxId?: string) => {
      return isEmpty(getLLMConfigById(chatBoxId)?.llm_id);
    },
    [getLLMConfigById],
  );

  useEffect(() => {
    cleanupFormRefs();
  }, [cleanupFormRefs]);

  return {
    formRefs,
    setFormRef,
    getLLMConfigById,
    isLLMConfigEmpty,
  };
}
