/** Keycloak Admin REST API 的 TypeScript 客户端入口：聚合各资源模块并管理认证与令牌生命周期。 */
import type { RequestArgs } from "./resources/agent.js";
import { AttackDetection } from "./resources/attackDetection.js";
import { AuthenticationManagement } from "./resources/authenticationManagement.js";
import { Cache } from "./resources/cache.js";
import { ClientPolicies } from "./resources/clientPolicies.js";
import { Clients } from "./resources/clients.js";
import { ClientScopes } from "./resources/clientScopes.js";
import { Components } from "./resources/components.js";
import { Groups } from "./resources/groups.js";
import { IdentityProviders } from "./resources/identityProviders.js";
import { Realms } from "./resources/realms.js";
import { Organizations } from "./resources/organizations.js";
import { Workflows } from "./resources/workflows.js";
import { Roles } from "./resources/roles.js";
import { ServerInfo } from "./resources/serverInfo.js";
import { Users } from "./resources/users.js";
import { UserStorageProvider } from "./resources/userStorageProvider.js";
import { WhoAmI } from "./resources/whoAmI.js";
import { Credentials, getToken, Settings } from "./utils/auth.js";
import { defaultBaseUrl, defaultRealm } from "./utils/constants.js";
import { DecodedToken, decodeToken } from "./utils/decode.js";

/** fetch 请求选项（不含 signal，由 timeout 统一注入） */
export type RequestOptions = Omit<RequestInit, "signal">;

/** 外部令牌提供者：由调用方自行维护 access token */
export interface TokenProvider {
  getAccessToken: () => Promise<string | undefined>;
}

/** 客户端连接与全局请求行为配置 */
export interface ConnectionConfig {
  /** Admin API 基址，默认 http://localhost:8080 */
  baseUrl?: string;
  /** 默认操作的 realm 名称 */
  realmName?: string;
  /** 全局 fetch 选项 */
  requestOptions?: RequestOptions;
  /** 全局请求参数（如 catchNotFound） */
  requestArgOptions?: Pick<RequestArgs, "catchNotFound">;
  /** 请求超时（毫秒），映射为 AbortSignal.timeout */
  timeout?: number;
  /**
   * Enable experimental APIs (e.g., v2 API).
   * These APIs are not yet stable and may change without notice.
   * @default false
   */
  /** 是否启用实验性 API（如 v2），默认 false */
  enableExperimentalApis?: boolean;
}

/** 令牌视为过期前的安全余量（秒） */
const MIN_VALIDITY = 5; // in seconds

/** Keycloak 管理 API 主客户端：挂载各 REST 资源子客户端。 */
export class KeycloakAdminClient {
  // Resources
  /** 用户管理 */
  public users: Users;
  /** 用户存储 Provider */
  public userStorageProvider: UserStorageProvider;
  /** 组管理 */
  public groups: Groups;
  /** 角色管理 */
  public roles: Roles;
  /** 组织（Organizations）管理 */
  public organizations: Organizations;
  /** 工作流管理 */
  public workflows: Workflows;
  /** 客户端管理 */
  public clients: Clients;
  /** Realm 管理 */
  public realms: Realms;
  /** Client Scope 管理 */
  public clientScopes: ClientScopes;
  /** Client Policy 管理 */
  public clientPolicies: ClientPolicies;
  /** 身份提供者管理 */
  public identityProviders: IdentityProviders;
  /** 组件（SPI 配置）管理 */
  public components: Components;
  /** 服务器信息 */
  public serverInfo: ServerInfo;
  /** 当前登录管理员身份 */
  public whoAmI: WhoAmI;
  /** 暴力破解检测 */
  public attackDetection: AttackDetection;
  /** 认证流程与执行器管理 */
  public authenticationManagement: AuthenticationManagement;
  /** 缓存管理 */
  public cache: Cache;

  // Members
  /** Admin API 基址 */
  public baseUrl: string;
  /** 当前默认 realm */
  public realmName: string;
  /** OAuth scope（可选） */
  public scope?: string;
  /** 当前 access token 字符串 */
  public accessToken?: string;
  /** 当前 refresh token 字符串 */
  public refreshToken?: string;
  /** 请求超时（毫秒） */
  public timeout?: number;
  /** 是否启用实验性 API */
  public enableExperimentalApis: boolean;

  #requestOptions?: RequestOptions;
  #globalRequestArgOptions?: Pick<RequestArgs, "catchNotFound">;
  #tokenProvider?: TokenProvider;
  #accessTokenDecoded?: DecodedToken;
  #refreshTokenDecoded?: DecodedToken;
  #credentials?: Credentials;

  constructor(connectionConfig?: ConnectionConfig) {
    this.baseUrl = connectionConfig?.baseUrl || defaultBaseUrl;
    this.realmName = connectionConfig?.realmName || defaultRealm;
    this.timeout = connectionConfig?.timeout;
    this.enableExperimentalApis =
      connectionConfig?.enableExperimentalApis ?? false;
    this.#requestOptions = connectionConfig?.requestOptions;
    this.#globalRequestArgOptions = connectionConfig?.requestArgOptions;

    // Initialize resources
    this.users = new Users(this);
    this.userStorageProvider = new UserStorageProvider(this);
    this.groups = new Groups(this);
    this.roles = new Roles(this);
    this.organizations = new Organizations(this);
    this.workflows = new Workflows(this);
    this.clients = new Clients(this);
    this.realms = new Realms(this);
    this.clientScopes = new ClientScopes(this);
    this.clientPolicies = new ClientPolicies(this);
    this.identityProviders = new IdentityProviders(this);
    this.components = new Components(this);
    this.authenticationManagement = new AuthenticationManagement(this);
    this.serverInfo = new ServerInfo(this);
    this.whoAmI = new WhoAmI(this);
    this.attackDetection = new AttackDetection(this);
    this.cache = new Cache(this);
  }

  /** 使用用户名密码或 client 凭据换取 access/refresh token。 */
  public async auth(credentials: Credentials) {
    const { accessToken, refreshToken } = await getToken(
      this.#getTokenSettings(credentials),
    );
    this.#credentials = credentials;
    this.setAccessToken(accessToken);
    this.setRefreshToken(refreshToken);
  }

  #getTokenSettings(credentials: Credentials): Settings {
    return {
      baseUrl: this.baseUrl,
      realmName: this.realmName,
      scope: this.scope,
      credentials,
      requestOptions: {
        ...this.#requestOptions,
        ...(this.timeout ? { signal: AbortSignal.timeout(this.timeout) } : {}),
      },
    };
  }

  /** 注册外部 TokenProvider（仅允许注册一次）。 */
  public registerTokenProvider(provider: TokenProvider) {
    if (this.#tokenProvider) {
      throw new Error("An existing token provider was already registered.");
    }

    this.#tokenProvider = provider;
  }

  /** 设置 access token 并解码缓存 exp 等声明。 */
  public setAccessToken(token: string) {
    this.accessToken = token;
    this.#accessTokenDecoded = decodeToken(token);
  }

  /** 设置 refresh token 并解码缓存。 */
  public setRefreshToken(token: string) {
    this.refreshToken = token;
    this.#refreshTokenDecoded = decodeToken(token);
  }

  /** 获取有效 access token：优先 TokenProvider，否则在过期前自动 refresh。 */
  public async getAccessToken() {
    if (this.#tokenProvider) {
      return this.#tokenProvider.getAccessToken();
    }

    if (this.isTokenExpired()) {
      await this.#refreshAccessToken();
    }

    return this.accessToken;
  }

  /** 使用 refresh_token grant 刷新令牌对。 */
  async #refreshAccessToken() {
    if (!this.refreshToken || !this.#credentials) {
      throw new Error(
        "Cannot refresh token: missing refresh token or credentials",
      );
    }

    if (this.isRefreshTokenExpired()) {
      throw new Error("Cannot refresh token: refresh token has expired");
    }

    const { accessToken, refreshToken } = await getToken(
      this.#getTokenSettings({
        grantType: "refresh_token",
        clientId: this.#credentials.clientId,
        clientSecret: this.#credentials.clientSecret,
        refreshToken: this.refreshToken,
      }),
    );

    this.setAccessToken(accessToken);
    this.setRefreshToken(refreshToken);
  }

  /** access token 是否已过期（含 MIN_VALIDITY 缓冲）。 */
  public isTokenExpired(): boolean {
    return this.#isExpired(this.#accessTokenDecoded);
  }

  /** refresh token 是否已过期。 */
  public isRefreshTokenExpired(): boolean {
    return this.#isExpired(this.#refreshTokenDecoded);
  }

  #isExpired(token?: DecodedToken): boolean {
    if (typeof token?.exp !== "number") {
      return false;
    }
    const expiresIn =
      token.exp - Math.ceil(new Date().getTime() / 1000) - MIN_VALIDITY;
    return expiresIn < 0;
  }

  public getRequestOptions() {
    return this.#requestOptions;
  }

  public getGlobalRequestArgOptions():
    | Pick<RequestArgs, "catchNotFound">
    | undefined {
    return this.#globalRequestArgOptions;
  }

  /** 运行时更新 baseUrl、realmName 与 requestOptions。 */
  public setConfig(connectionConfig: ConnectionConfig) {
    if (
      typeof connectionConfig.baseUrl === "string" &&
      connectionConfig.baseUrl
    ) {
      this.baseUrl = connectionConfig.baseUrl;
    }

    if (
      typeof connectionConfig.realmName === "string" &&
      connectionConfig.realmName
    ) {
      this.realmName = connectionConfig.realmName;
    }
    this.#requestOptions = connectionConfig.requestOptions;
  }
}
