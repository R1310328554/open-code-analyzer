/**
 * Admin Console 与 Account Console 共享的基础环境变量类型。
 * 由 Keycloak 在 HTML 中注入，供 React 应用读取。
 */
export type BaseEnvironment = {
  /**
   * The URL to the root of the Keycloak server, including the path if present, this is **NOT** always equivalent to the URL of the Admin Console.
   * For example, the Keycloak server could be hosted on `auth.example.com` and Admin Console may be hosted on `admin.example.com/some/path`.
   *
   * Note that this URL is normalized not to include a trailing slash, so take this into account when constructing URLs.
   *
   * Keycloak 服务器根 URL（含路径前缀），**不一定**等于 Admin Console 的访问地址。
   *
   * @see {@link https://www.keycloak.org/server/hostname#_administration_console}
   */
  serverBaseUrl: string;
  /** The identifier of the realm used to authenticate the user. */
  /** 当前认证用户所属 Realm 标识 */
  realm: string;
  /** The identifier of the client used to authenticate the user. */
  /** 用于认证的前端客户端 ID */
  clientId: string;
  /** The base URL of the resources. */
  /** 静态资源（JS/CSS）的基础 URL */
  resourceUrl: string;
  /** The source URL for the logo image. */
  /** Logo 图片地址 */
  logo: string;
  /** The URL to be followed when the logo is clicked. */
  /** 点击 Logo 时跳转的 URL */
  logoUrl: string;
  /** The scopes to be requested when sending authorization requests*/
  /** 授权请求中请求的 OAuth scope（可选） */
  scope?: string;
};

/**
 *  Extracts the environment variables from the document, these variables are injected by Keycloak as a script tag, the contents of which can be parsed as JSON. For example:
 *
 *```html
 * <script id="environment" type="application/json">
 *   {
 *     "realm": "master",
 *     "clientId": "security-admin-console",
 *     "etc": "..."
 *   }
 * </script>
 * ```
 *
 * 从页面 `#environment` script 标签读取 Keycloak 注入的运行时配置并解析为 JSON。
 */
export function getInjectedEnvironment<T>(): T {
  const element = document.getElementById("environment");
  const contents = element?.textContent;

  if (typeof contents !== "string") {
    throw new Error("Environment variables not found in the document.");
  }

  try {
    return JSON.parse(contents);
  } catch {
    throw new Error("Unable to parse environment variables as JSON.");
  }
}
