import { parseTemplate } from "url-template";
import type { KeycloakAdminClient } from "../client.js";
import {
  fetchWithError,
  NetworkError,
  parseResponse,
} from "../utils/fetchWithError.js";
import { joinPath } from "../utils/joinPath.js";
import { stringifyQueryParams } from "../utils/stringifyQueryParams.js";

// constants
const SLASH = "/";

/** HTTP 方法字面量 */
type Method = "GET" | "POST" | "PUT" | "DELETE";

/**
 * Agent 单次 REST 调用的声明式参数：路径模板、参数分流与响应处理选项。
 * 由 Resource.makeRequest / makeUpdateRequest 传入，生成实际请求函数。
 */
export interface RequestArgs {
  /** HTTP 方法 */
  method: Method;
  /** 相对路径模板（可含 {param} 占位符） */
  path?: string;
  // Keys of url params to be applied — 从 payload 提取并填入路径的键
  urlParamKeys?: string[];
  // Keys of query parameters to be applied — 从 payload 提取为查询字符串的键
  queryParamKeys?: string[];
  // Mapping of key transformations to be performed on the payload — 请求体/查询键名映射
  keyTransform?: Record<string, string>;
  // If responding with 404, catch it and return null instead — 404 时返回 null 而非抛错
  catchNotFound?: boolean;
  // The key of the value to use from the payload of request. Only works for POST & PUT.
  /** POST/PUT 时仅序列化 payload 中指定键的值作为请求体 */
  payloadKey?: string;
  // Whether the response header have a location field with newly created resource id
  // if this value is set, we return the field with format: {[field]: resourceId}
  // to represent the newly created resource
  // detail: keycloak/keycloak-nodejs-admin-client issue #11
  /** 从 Location 响应头解析新建资源 ID 并以 `{ [field]: id }` 返回 */
  returnResourceIdInLocationHeader?: { field: string };
  /**
   * Keys to be ignored, meaning that they will not be filtered out of the request payload even if they are a part of `urlParamKeys` or `queryParamKeys`,
   */
  /** 即使属于 url/query 参数键也保留在请求体中的字段名 */
  ignoredKeys?: string[];
  /** 额外请求头 */
  headers?: [string, string][] | Record<string, string> | Headers;
}

/** 从对象中仅保留指定键 */
const pick = (value: Record<string, unknown>, keys: string[]) =>
  Object.fromEntries(
    Object.entries(value).filter(([key]) => keys.includes(key)),
  );

/** 从对象中排除指定键 */
const omit = (value: Record<string, unknown>, keys: string[]) =>
  Object.fromEntries(
    Object.entries(value).filter(([key]) => !keys.includes(key)),
  );

/**
 * Admin REST 请求代理：封装 Bearer 认证、URL 模板展开、参数分流与响应解析。
 * 每个 Resource 子模块持有一个 Agent 实例，通过 `request()` 工厂生成 API 方法。
 */
export class Agent {
  #client: KeycloakAdminClient;
  #basePath: string;
  #getBaseParams?: () => Record<string, any>;
  #getBaseUrl: () => string;

  constructor({
    client,
    path = "/",
    getUrlParams = () => ({}),
    getBaseUrl = () => client.baseUrl,
  }: {
    client: KeycloakAdminClient;
    path?: string;
    getUrlParams?: () => Record<string, any>;
    getBaseUrl?: () => string;
  }) {
    this.#client = client;
    this.#getBaseParams = getUrlParams;
    this.#getBaseUrl = getBaseUrl;
    this.#basePath = path;
  }

  /**
   * 创建单 payload 请求函数：将 payload 按 urlParamKeys/queryParamKeys 分流后发起 HTTP 调用。
   */
  public request({
    method,
    path = "",
    urlParamKeys = [],
    queryParamKeys = [],
    catchNotFound = false,
    keyTransform,
    payloadKey,
    returnResourceIdInLocationHeader,
    ignoredKeys,
    headers,
  }: RequestArgs) {
    return async (
      payload: any = {},
      options?: Pick<RequestArgs, "catchNotFound">,
    ) => {
      const baseParams = this.#getBaseParams?.() ?? {};

      // Filter query parameters by queryParamKeys — 提取查询参数
      const queryParams =
        queryParamKeys.length > 0
          ? (pick(payload, queryParamKeys) as any)
          : undefined;

      // Add filtered payload parameters to base parameters — 合并路径参数
      const allUrlParamKeys = [...Object.keys(baseParams), ...urlParamKeys];
      const urlParams = { ...baseParams, ...pick(payload, allUrlParamKeys) };

      if (!(payload instanceof FormData)) {
        // Omit url parameters and query parameters from payload — 剩余部分作为请求体
        const omittedKeys = ignoredKeys
          ? [...allUrlParamKeys, ...queryParamKeys].filter(
              (key) => !ignoredKeys.includes(key),
            )
          : [...allUrlParamKeys, ...queryParamKeys];

        payload = omit(payload, omittedKeys);
      }

      // Transform keys of both payload and queryParams — 应用键名映射（如 camelCase → kebab）
      if (keyTransform) {
        this.#transformKey(payload, keyTransform);
        this.#transformKey(queryParams, keyTransform);
      }

      return this.#requestWithParams({
        method,
        path,
        payload,
        urlParams,
        queryParams,
        // catchNotFound precedence: global > local > default — 404 处理优先级
        catchNotFound,
        ...(this.#client.getGlobalRequestArgOptions() ?? options ?? {}),
        payloadKey,
        returnResourceIdInLocationHeader,
        headers,
      });
    };
  }

  /**
   * 创建双参数请求函数：query 供路径/查询参数，payload 为请求体（PUT/POST 更新场景）。
   */
  public updateRequest({
    method,
    path = "",
    urlParamKeys = [],
    queryParamKeys = [],
    catchNotFound = false,
    keyTransform,
    payloadKey,
    returnResourceIdInLocationHeader,
    headers,
  }: RequestArgs) {
    return async (query: any = {}, payload: any = {}) => {
      const baseParams = this.#getBaseParams?.() ?? {};

      // Filter query parameters by queryParamKeys
      const queryParams =
        queryParamKeys.length > 0
          ? (pick(query, queryParamKeys) as any)
          : undefined;

      // Add filtered query parameters to base parameters
      const allUrlParamKeys = [...Object.keys(baseParams), ...urlParamKeys];
      const urlParams = {
        ...baseParams,
        ...pick(query, allUrlParamKeys),
      };

      // Transform keys of queryParams
      if (keyTransform) {
        this.#transformKey(queryParams, keyTransform);
      }

      return this.#requestWithParams({
        method,
        path,
        payload,
        urlParams,
        queryParams,
        catchNotFound,
        payloadKey,
        returnResourceIdInLocationHeader,
        headers,
      });
    };
  }

  /** 组装 URL、请求头与 body，执行 fetch 并解析响应 */
  async #requestWithParams({
    method,
    path,
    payload,
    urlParams,
    queryParams,
    catchNotFound,
    payloadKey,
    returnResourceIdInLocationHeader,
    headers,
  }: {
    method: Method;
    path: string;
    payload: any;
    urlParams: any;
    queryParams?: Record<string, string>;
    catchNotFound: boolean;
    payloadKey?: string;
    returnResourceIdInLocationHeader?: { field: string };
    headers?: [string, string][] | Record<string, string> | Headers;
  }) {
    const requestOptions = { ...this.#client.getRequestOptions() };
    const requestHeaders = new Headers([
      ...new Headers(requestOptions.headers).entries(),
      ["authorization", `Bearer ${await this.#client.getAccessToken()}`],
      ["accept", "application/json, text/plain, */*"],
      ...new Headers(headers).entries(),
    ]);

    const searchParams: Record<string, string> = {};

    // Add payload parameters to search params if method is 'GET'. — GET 时将 payload 作为查询参数
    if (method === "GET") {
      Object.assign(searchParams, payload);
    } else if (requestHeaders.get("content-type") === "text/plain") {
      // Pass the payload as a plain string if the content type is 'text/plain'.
      requestOptions.body = payload as unknown as string;
    } else if (payload instanceof FormData) {
      requestOptions.body = payload;
    } else {
      // Otherwise assume it's JSON and stringify it. — 默认 JSON 序列化
      requestOptions.body =
        payloadKey && typeof payload[payloadKey] === "string"
          ? payload[payloadKey]
          : JSON.stringify(payloadKey ? payload[payloadKey] : payload);
    }

    if (
      requestOptions.body &&
      !requestHeaders.has("content-type") &&
      !(payload instanceof FormData)
    ) {
      requestHeaders.set("content-type", "application/json");
    }

    if (queryParams) {
      Object.assign(searchParams, queryParams);
    }

    const url = new URL(this.#getBaseUrl());
    const pathTemplate = parseTemplate(joinPath(this.#basePath, path));

    url.pathname = joinPath(url.pathname, pathTemplate.expand(urlParams));
    url.search = stringifyQueryParams(searchParams);

    try {
      const res = await fetchWithError(url, {
        ...requestOptions,
        headers: requestHeaders,
        method,
        ...(this.#client.timeout
          ? { signal: AbortSignal.timeout(this.#client.timeout) }
          : {}),
      });

      // now we get the response of the http request
      // if `resourceIdInLocationHeader` is true, we'll get the resourceId from the location header field
      // todo: find a better way to find the id in path, maybe some kind of pattern matching
      // for now, we simply split the last sub-path of the path returned in location header field
      /** 从 Location 头末段解析新建资源 ID */
      if (returnResourceIdInLocationHeader) {
        const locationHeader = res.headers.get("location");

        if (typeof locationHeader !== "string") {
          throw new Error(
            `location header is not found in request: ${res.url}`,
          );
        }

        const resourceId = locationHeader.split(SLASH).pop();
        if (!resourceId) {
          // throw an error to let users know the response is not expected
          throw new Error(
            `resourceId is not found in Location header from request: ${res.url}`,
          );
        }

        // return with format {[field]: string}
        const { field } = returnResourceIdInLocationHeader;
        return { [field]: resourceId };
      }

      /** Accept 为 octet-stream 时返回二进制 ArrayBuffer */
      if (
        Object.entries(headers || []).find(
          ([key, value]) =>
            key.toLowerCase() === "accept" &&
            value === "application/octet-stream",
        )
      ) {
        return await res.arrayBuffer();
      }

      return await parseResponse(res);
    } catch (err) {
      /** 启用 catchNotFound 且响应为 404 时返回 null */
      if (
        err instanceof NetworkError &&
        err.response.status === 404 &&
        catchNotFound
      ) {
        return null;
      }
      throw err;
    }
  }

  /** 按 keyMapping 重命名 payload 中的键（原地修改） */
  #transformKey(payload: any, keyMapping: Record<string, string>) {
    if (!payload) {
      return;
    }

    Object.keys(keyMapping).forEach((key) => {
      if (typeof payload[key] === "undefined") {
        return;
      }
      const newKey = keyMapping[key];
      payload[newKey] = payload[key];
      delete payload[key];
    });
  }
}
