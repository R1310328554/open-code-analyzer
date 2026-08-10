import type AuthenticationExecutionExportRepresentation from "./authenticationExecutionExportRepresentation.js";

/**
 * 认证流程（Authentication Flow）的完整表示：别名、Provider 与执行步骤列表。
 * @see https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_authenticationflowrepresentation
 */
export default interface AuthenticationFlowRepresentation {
  /** Flow 内部 ID */
  id?: string;
  /** 唯一别名（如 browser、direct grant） */
  alias?: string;
  /** 流程描述 */
  description?: string;
  /** Flow Provider ID（通常为 basic-flow） */
  providerId?: string;
  /** 是否为顶层 flow（非嵌套子 flow） */
  topLevel?: boolean;
  /** 是否为 Keycloak 内置 flow */
  builtIn?: boolean;
  /** 流程内的认证执行步骤列表 */
  authenticationExecutions?: AuthenticationExecutionExportRepresentation[];
}
