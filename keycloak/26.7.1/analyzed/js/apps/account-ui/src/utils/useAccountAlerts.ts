import { useAlerts } from "@keycloak/keycloak-ui-shared";
import { AlertVariant } from "@patternfly/react-core";
import { useCallback, useMemo } from "react";
import { useTranslation } from "react-i18next";

import { ApiError } from "../api/parse-response";

/**
 * Account Console 专用告警钩子。
 * 对 ApiError 展示翻译后的消息与服务器返回的描述；其他错误走通用 addError。
 */
export function useAccountAlerts() {
  const { t } = useTranslation();
  const { addAlert, addError } = useAlerts();
  const addAccountError = useCallback(
    (messageKey: string, error: unknown) => {
      if (!(error instanceof ApiError)) {
        addError(messageKey, error);
        return;
      }

      const message = t(messageKey, { error: error.message });
      addAlert(message, AlertVariant.danger, error.description);
    },
    [addAlert, addError, t],
  );

  return useMemo(
    () => ({ addAlert, addError: addAccountError }),
    [addAccountError, addAlert],
  );
}
