import type { KeycloakAdminClient } from "../client.js";
import { Agent, RequestArgs } from "./agent.js";

/**
 * Admin Client 资源基类：通过 Agent 封装 REST 请求。
 * 子类使用 makeRequest / makeUpdateRequest 声明 Admin REST 端点。
 */

export default class Resource<ParamType = {}> {
  #agent: Agent;
  constructor(
    client: KeycloakAdminClient,
    settings: {
      path?: string;
      getUrlParams?: () => Record<string, any>;
      getBaseUrl?: () => string;
    } = {},
  ) {
    this.#agent = new Agent({
      client,
      ...settings,
    });
  }

  /** 创建 GET/POST/DELETE 类请求方法 */
  public makeRequest = <PayloadType = any, ResponseType = any>(
    args: RequestArgs,
  ): ((
    payload?: PayloadType & ParamType,
    options?: Pick<RequestArgs, "catchNotFound">,
  ) => Promise<ResponseType>) => {
    return this.#agent.request(args);
  };

  /** 创建 PUT/POST 类更新请求（query + payload） */
  public makeUpdateRequest = <
    QueryType = any,
    PayloadType = any,
    ResponseType = any,
  >(
    args: RequestArgs,
  ): ((
    query: QueryType & ParamType,
    payload: PayloadType,
  ) => Promise<ResponseType>) => {
    return this.#agent.updateRequest(args);
  };
}
