/**
 * Account API 底层 HTTP 请求封装。
 * 负责构建 realm 账户端点 URL、注入 Bearer 令牌并发起 fetch。
 */
import {
  KeycloakContext,
  type BaseEnvironment,
} from "@keycloak/keycloak-ui-shared";
import Keycloak from "keycloak-js";

import { joinPath } from "../utils/joinPath";
import { CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON } from "./constants";

/** 单次 HTTP 请求的选项：方法、请求体、查询参数与取消信号。 */
export type RequestOptions = {
  signal?: AbortSignal;
  getAccessToken?: () => Promise<string | undefined>;
  method?: "POST" | "PUT" | "DELETE";
  searchParams?: Record<string, string>;
  body?: unknown;
};

/** 内部 fetch 实现：合并查询参数、序列化 JSON 请求体并设置 Authorization 头。 */
async function _request(
  url: URL,
  { signal, getAccessToken, method, searchParams, body }: RequestOptions = {},
): Promise<Response> {
  if (searchParams) {
    Object.entries(searchParams).forEach(([key, value]) =>
      url.searchParams.set(key, value),
    );
  }

  return fetch(url, {
    signal,
    method,
    body: body ? JSON.stringify(body) : undefined,
    headers: {
      [CONTENT_TYPE_HEADER]: CONTENT_TYPE_JSON,
      authorization: `Bearer ${await getAccessToken?.()}`,
    },
  });
}

/**
 * 向 Account API 发起请求。
 * 默认根据 environment 拼接 URL；也可传入完整 fullUrl 覆盖。
 */
export async function request(
  path: string,
  { environment, keycloak }: KeycloakContext<BaseEnvironment>,
  opts: RequestOptions = {},
  fullUrl?: URL,
) {
  if (typeof fullUrl === "undefined") {
    fullUrl = url(environment, path);
  }
  return _request(fullUrl, {
    ...opts,
    getAccessToken: token(keycloak),
  });
}

/** 根据服务器基址、realm 与相对路径构建 Account API 完整 URL。 */
export const url = (environment: BaseEnvironment, path: string) =>
  new URL(
    joinPath(
      environment.serverBaseUrl,
      "realms",
      environment.realm,
      "account",
      path,
    ),
  );

/**
 * 返回获取访问令牌的异步函数。
 * 请求前尝试 refresh（5 秒余量）；刷新失败则触发重新登录。
 */
export const token = (keycloak: Keycloak) =>
  async function getAccessToken() {
    try {
      await keycloak.updateToken(5);
    } catch {
      await keycloak.login();
    }

    return keycloak.token;
  };
