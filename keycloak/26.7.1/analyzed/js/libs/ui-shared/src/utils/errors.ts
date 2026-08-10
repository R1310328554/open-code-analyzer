import { NetworkError } from "@keycloak/keycloak-admin-client";

/** Keycloak / OAuth 错误 JSON 中的常见错误码字段 */
const ERROR_FIELDS = ["error", "errorMessage"];
/** OAuth 标准错误描述字段 */
const ERROR_DESCRIPTION_FIELD = "error_description";

/**
 * 从任意 error 值提取用户可读的错误消息。
 * 支持字符串、NetworkError、普通 Error。
 */
export function getErrorMessage(error: unknown) {
  if (typeof error === "string") {
    return error;
  }

  if (error instanceof NetworkError) {
    return getNetworkErrorMessage(error.responseData);
  }

  if (error instanceof Error) {
    return error.message;
  }

  throw new Error("Unable to determine error message.");
}

/**
 * 提取 NetworkError 的详细描述（error_description）。
 * 非 NetworkError 时返回 undefined。
 */
export function getErrorDescription(error: unknown) {
  if (!(error instanceof NetworkError)) {
    return;
  }

  const data = error.responseData;

  return getNetworkErrorDescription(data);
}

/** 从响应 JSON 中读取 OAuth error_description 字段 */
export function getNetworkErrorDescription(data: unknown) {
  if (
    typeof data === "object" &&
    data !== null &&
    ERROR_DESCRIPTION_FIELD in data &&
    typeof data[ERROR_DESCRIPTION_FIELD] === "string"
  ) {
    return data[ERROR_DESCRIPTION_FIELD];
  }
}

/** 从 Keycloak 网络错误响应体中提取 error 或 errorMessage */
export function getNetworkErrorMessage(data: unknown) {
  if (typeof data !== "object" || data === null) {
    return;
  }

  for (const key of ERROR_FIELDS) {
    const value = (data as Record<string, unknown>)[key];

    if (typeof value === "string") {
      return value;
    }
  }
}
