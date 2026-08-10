/** Keycloak REST 错误响应中常见的错误字段名 */
const ERROR_FIELDS = ["error", "errorMessage"];

/** NetworkError 构造选项：保留原始 Response 与解析后的响应体 */
export type NetworkErrorOptions = { response: Response; responseData: unknown };

/**
 * 封装 fetch 失败时的结构化错误。
 * 便于上层从 responseData 中提取 Keycloak 返回的 error / errorMessage。
 */
export class NetworkError extends Error {
  response: Response;
  responseData: unknown;

  constructor(message: string, options: NetworkErrorOptions) {
    super(message);
    this.response = options.response;
    this.responseData = options.responseData;
  }
}

/**
 * 发起 fetch 并在 HTTP 非 2xx 时抛出 NetworkError。
 * 成功时返回原始 Response，由调用方自行解析 body。
 */
export async function fetchWithError(
  input: Request | string | URL,
  init?: RequestInit,
) {
  const response = await fetch(input, init);

  if (!response.ok) {
    const responseData = await parseResponse(response);
    const message = getErrorMessage(responseData);
    throw new NetworkError(message, {
      response,
      responseData,
    });
  }

  return response;
}

/** 将响应体解析为 JSON；非 JSON 时返回原始文本字符串 */
export async function parseResponse(response: Response): Promise<any> {
  if (!response.body) {
    return "";
  }

  const data = await response.text();

  try {
    return JSON.parse(data);
  } catch {
    return data;
  }
}

/** 从 Keycloak 错误 JSON 中提取可读错误消息 */
function getErrorMessage(data: unknown): string {
  if (typeof data !== "object" || data === null) {
    return "Unable to determine error message.";
  }

  for (const key of ERROR_FIELDS) {
    const value = (data as Record<string, unknown>)[key];

    if (typeof value === "string") {
      return value;
    }
  }

  return "Network response was not OK.";
}
