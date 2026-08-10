import camelize from "camelize-ts";
import { parseTemplate } from "url-template";
import { defaultBaseUrl, defaultRealm } from "./constants.js";
import { fetchWithError } from "./fetchWithError.js";
import { joinPath } from "./joinPath.js";
import { stringifyQueryParams } from "./stringifyQueryParams.js";

/** OAuth2 / OpenID Connect 支持的授权类型 */
export type GrantTypes = "client_credentials" | "password" | "refresh_token";

/** 向 Token 端点发起请求时使用的客户端凭据与授权参数 */
export interface Credentials {
  username?: string;
  password?: string;
  grantType: GrantTypes;
  clientId: string;
  clientSecret?: string;
  totp?: string;
  offlineToken?: boolean;
  refreshToken?: string;
  scopes?: string[];
}

/** Admin Client 认证配置：Realm、Base URL 及凭据 */
export interface Settings {
  realmName?: string;
  baseUrl?: string;
  scope?: string;
  credentials: Credentials;
  requestOptions?: RequestInit;
}

/** Keycloak Token 端点原始 JSON 响应（蛇形命名） */
export interface TokenResponseRaw {
  access_token: string;
  expires_in: number;
  refresh_expires_in: number;
  refresh_token: string;
  token_type: string;
  not_before_policy: number;
  session_state: string;
  scope: string;
  id_token?: string;
}

/** 经 camelize 转换后的 Token 响应（驼峰命名） */
export interface TokenResponse {
  accessToken: string;
  expiresIn: number;
  refreshExpiresIn: number;
  refreshToken: string;
  tokenType: string;
  notBeforePolicy: number;
  sessionState: string;
  scope: string;
  idToken?: string;
}

// See: https://developer.mozilla.org/en-US/docs/Glossary/Base64
/** 将字节数组编码为 Base64 字符串 */
const bytesToBase64 = (bytes: Uint8Array) =>
  btoa(Array.from(bytes, (byte) => String.fromCodePoint(byte)).join(""));
/** 将 UTF-8 字符串编码为 Base64 */
const toBase64 = (input: string) =>
  bytesToBase64(new TextEncoder().encode(input));

// See: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/encodeURIComponent#encoding_for_rfc3986
/** 按 RFC 3986 规则编码 URI 组件（保留 RFC 6749 要求的字符集） */
const encodeRFC3986URIComponent = (input: string) =>
  encodeURIComponent(input).replace(
    /[!'()*]/g,
    (c) => `%${c.charCodeAt(0).toString(16).toUpperCase()}`,
  );

// See https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/encodeURIComponent
// Specifically, the section on encoding `application/x-www-form-urlencoded`.
/** 表单-urlencoded 编码：空格转为 `+` 而非 `%20` */
const encodeFormURIComponent = (data: string) =>
  encodeRFC3986URIComponent(data).replaceAll("%20", "+");

/**
 * 向 OpenID Connect Token 端点请求访问令牌。
 * 支持 client_credentials、password、refresh_token 等 grant type。
 */
export const getToken = async (settings: Settings): Promise<TokenResponse> => {
  const url = new URL(settings.baseUrl ?? defaultBaseUrl);
  const pathTemplate = parseTemplate(
    "/realms/{realmName}/protocol/openid-connect/token",
  );

  url.pathname = joinPath(
    url.pathname,
    pathTemplate.expand({
      realmName: settings.realmName ?? defaultRealm,
    }),
  );

  // Prepare credentials for openid-connect token request
  // ref: http://openid.net/specs/openid-connect-core-1_0.html#TokenEndpoint
  // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition -- credentials may be undefined at runtime despite the type
  const credentials = settings.credentials ?? ({} as Credentials);
  // 组装 x-www-form-urlencoded 请求体
  const payload = stringifyQueryParams({
    username: credentials.username,
    password: credentials.password,
    grant_type: credentials.grantType,
    client_id: credentials.clientId,
    totp: credentials.totp,
    ...(credentials.offlineToken ? { scope: "offline_access" } : {}),
    ...(credentials.scopes ? { scope: credentials.scopes.join(" ") } : {}),
    ...(credentials.refreshToken
      ? {
          refresh_token: credentials.refreshToken,
          client_secret: credentials.clientSecret,
        }
      : {}),
  });

  const options = settings.requestOptions ?? {};
  const headers = new Headers(options.headers);

  if (credentials.clientSecret) {
    // See: https://datatracker.ietf.org/doc/html/rfc6749#section-2.3.1
    // 机密客户端：使用 HTTP Basic 认证传递 client_id:client_secret
    const username = encodeFormURIComponent(credentials.clientId);
    const password = encodeFormURIComponent(credentials.clientSecret);

    // See: https://datatracker.ietf.org/doc/html/rfc2617#section-2
    headers.set(
      "authorization",
      `Basic ${toBase64(`${username}:${password}`)}`,
    );
  }

  headers.set("content-type", "application/x-www-form-urlencoded");

  const response = await fetchWithError(url, {
    ...options,
    method: "POST",
    headers,
    body: payload,
  });

  const data = (await response.json()) as TokenResponseRaw;
  // 将蛇形字段名转为驼峰，便于 TypeScript 消费
  return camelize(data);
};
