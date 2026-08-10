// use-provider-modal-actions.ts — 提供商弹窗的「验证连接」与「提交保存」处理逻辑。

import { DynamicFormRef } from '@/components/dynamic-form';
import message from '@/components/ui/message';
import { useTranslate } from '@/hooks/common-hooks';
import { IModelInfo } from '@/interfaces/request/llm';
import { VerifyResult } from '@/pages/user-setting/setting-model/hooks';
import { RefObject, useCallback } from 'react';
import { FieldValues } from 'react-hook-form';
import type {
  IViewModeOkPayload,
  ProviderConfig,
  ProviderModalProps,
} from '../types';

type ActionParams = {
  config: ProviderConfig;
  viewMode?: boolean;
  hasModelNameField: boolean;
  llmFactory: string;
  initialValues?: Record<string, any>;
  modelInfoList: IModelInfo[];
  formRef: RefObject<DynamicFormRef>;
  /** inputSelect 字段的 URL → regionKey 映射，用于从当前 base URL 推导 region。 */
  baseUrlRegionMaps?: Record<string, Map<string, string>>;
  onOk: ProviderModalProps['onOk'];
  onVerify: ProviderModalProps['onVerify'];
  onViewModeOk: ProviderModalProps['onViewModeOk'];
};

/**
 * 根据当前选中的 base URL 在 baseUrlRegionMaps 中查找 region 键
 *（如 default / intl / cn）；无匹配则返回 undefined，提交时不写 region。
 */
const resolveRegionFromValues = (
  values: Record<string, any> | undefined,
  baseUrlRegionMaps?: Record<string, Map<string, string>>,
): string | undefined => {
  if (!values || !baseUrlRegionMaps) return undefined;
  for (const fieldName of Object.keys(baseUrlRegionMaps)) {
    const url = values[fieldName];
    if (typeof url !== 'string' || url === '') continue;
    const regionKey = baseUrlRegionMaps[fieldName].get(url);
    if (regionKey !== undefined) {
      return regionKey;
    }
  }
  return undefined;
};

/**
 * 构造提供商弹窗的两个出站处理器：
 *
 * - handleVerify：读表单 → verifyTransform → onVerify，返回 VerifyResult。
 * - handleSubmit：viewMode 仅通过 onViewModeOk 更新模型；普通模式经
 *   submitTransform 后调用 onOk，并补全 llm_factory。
 *
 * 两路径均会从 inputSelect 的 base URL 注入 region 字段。
 */
export const useProviderModalActions = ({
  config,
  viewMode,
  hasModelNameField,
  llmFactory,
  initialValues,
  modelInfoList,
  formRef,
  baseUrlRegionMaps,
  onOk,
  onVerify,
  onViewModeOk,
}: ActionParams) => {
  const { t } = useTranslate('setting');

  const handleVerify = useCallback(
    async (params: any) => {
      const values = formRef.current?.getValues() || params;
      if (!config.verifyTransform) {
        return { isValid: null, logs: '' } as VerifyResult;
      }
      if (hasModelNameField && modelInfoList.length === 0) {
        message.error(t('selectModelBeforeVerify'));
        return { isValid: null, logs: '' } as VerifyResult;
      }
      const verifyArgs = config.verifyTransform({
        ...values,
        model_info: modelInfoList,
      });
      const region = resolveRegionFromValues(values, baseUrlRegionMaps);
      if (region !== undefined) {
        verifyArgs.region = region;
      }
      const res = await onVerify({ ...params, ...verifyArgs });
      return (res || { isValid: null, logs: '' }) as VerifyResult;
    },
    [
      config,
      onVerify,
      modelInfoList,
      formRef,
      baseUrlRegionMaps,
      hasModelNameField,
      t,
    ],
  );

  const handleSubmit = useCallback(
    async (values?: FieldValues) => {
      if (!values) return;

      // viewMode：仅增改模型，实例级字段已禁用，由 onViewModeOk 回传选中模型或表单值
      if (viewMode) {
        if (!onViewModeOk) {
          // No viewMode handler provided — nothing to save, just close
          // (the modal's own hideModal flow handles closing).
          return;
        }
        const instanceName = String(
          (initialValues as any)?.instance_name ?? '',
        );
        const payload: IViewModeOkPayload = hasModelNameField
          ? {
              instanceName,
              llmFactory,
              modelInfos: modelInfoList,
            }
          : {
              instanceName,
              llmFactory,
              modelInfos: [],
              formValues: values as Record<string, any>,
            };
        await onViewModeOk(payload);
        return;
      }

      const transformed = (
        config.submitTransform
          ? config.submitTransform({
              ...(values as Record<string, any>),
              model_info: modelInfoList,
            })
          : values
      ) as Record<string, any>;
      const region = resolveRegionFromValues(
        values as Record<string, any>,
        baseUrlRegionMaps,
      );
      if (region !== undefined) {
        transformed.region = region;
      }
      // 确保 payload 含 llm_factory，否则父组件请求 URL 会变成 /providers/undefined/instances
      if (!transformed.llm_factory) {
        transformed.llm_factory = llmFactory;
      }
      await onOk?.(transformed, false);
    },
    [
      config,
      onOk,
      onViewModeOk,
      modelInfoList,
      viewMode,
      hasModelNameField,
      llmFactory,
      initialValues,
      baseUrlRegionMaps,
    ],
  );

  return { handleVerify, handleSubmit };
};
