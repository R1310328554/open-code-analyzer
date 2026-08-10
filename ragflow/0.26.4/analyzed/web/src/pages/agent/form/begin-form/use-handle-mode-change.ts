// use-handle-mode-change.ts — Begin 对话模式切换：切至 Webhook 时写入默认 HTTP/安全配置。

import { useCallback } from 'react';
import { UseFormReturn } from 'react-hook-form';
import {
  AgentDialogueMode,
  WebhookContentType,
  WebhookExecutionMode,
  WebhookMaxBodySize,
  WebhookMethod,
  WebhookRateLimitPer,
  WebhookSecurityAuthType,
} from '../../constant';

/** Webhook 模式下的表单默认值：methods、security、execution_mode 等。 */
const initialFormValuesMap = {
  methods: [WebhookMethod.Get],
  schema: {},
  'security.auth_type': WebhookSecurityAuthType.Basic,
  'security.rate_limit.per': WebhookRateLimitPer.Second,
  'security.rate_limit.limit': 10,
  'security.max_body_size': WebhookMaxBodySize[0],
  'response.status': 200,
  execution_mode: WebhookExecutionMode.Immediately,
  content_types: WebhookContentType.ApplicationJson,
};

/** 监听 mode 变更，切到 Webhook 时批量 setValue 初始化相关字段。 */
export function useHandleModeChange(form: UseFormReturn<any>) {
  const handleModeChange = useCallback(
    (mode: AgentDialogueMode) => {
      if (mode === AgentDialogueMode.Webhook) {
        Object.entries(initialFormValuesMap).forEach(([key, value]) => {
          form.setValue(key, value, { shouldDirty: true });
        });
      }
    },
    [form],
  );
  return { handleModeChange };
}
