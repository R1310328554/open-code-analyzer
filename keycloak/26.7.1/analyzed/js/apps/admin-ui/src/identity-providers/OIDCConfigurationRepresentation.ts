/**
 * OpenID Connect Provider 元数据（/.well-known/openid-configuration）的 TypeScript 表示。
 * 字段命名与 OIDC Discovery 规范一致，供身份提供方发现端点解析与表单映射。
 */
export interface OIDCConfigurationRepresentation {
  /** 颁发者标识符（issuer）。 */
  issuer?: string;
  /** 授权端点 URL。 */
  authorization_endpoint?: string;
  /** 令牌端点 URL。 */
  token_endpoint?: string;
  /** 令牌 introspection 端点 URL。 */
  introspection_endpoint?: string;
  /** UserInfo 端点 URL。 */
  userinfo_endpoint?: string;
  /** 登出端点 URL。 */
  end_session_endpoint?: string;
  /** JSON Web Key Set 文档 URI。 */
  jwks_uri?: string;
  /** 会话检查 iframe URL（OIDC Session Management）。 */
  check_session_iframe?: string;
  /** 支持的 OAuth 授权类型列表。 */
  grant_types_supported?: string[];
  /** 支持的响应类型列表。 */
  response_types_supported?: string[];
  /** 支持的主体标识类型（如 pairwise、public）。 */
  subject_types_supported?: string[];
  /** ID Token 支持的签名算法。 */
  id_token_signing_alg_values_supported?: string[];
  /** ID Token 加密算法。 */
  id_token_encryption_alg_values_supported?: string[];
  /** ID Token 内容加密算法。 */
  id_token_encryption_enc_values_supported?: string[];
  /** UserInfo 响应签名算法。 */
  userinfo_signing_alg_values_supported?: string[];
  /** Request Object 签名算法。 */
  request_object_signing_alg_values_supported?: string[];
  /** 支持的响应模式（query、fragment、form_post 等）。 */
  response_modes_supported?: string[];
  /** 动态客户端注册端点。 */
  registration_endpoint?: string;
  /** 令牌端点支持的客户端认证方式。 */
  token_endpoint_auth_methods_supported?: string[];
  /** 令牌端点客户端认证签名算法。 */
  token_endpoint_auth_signing_alg_values_supported?: string[];
  /** Introspection 端点认证方式。 */
  introspection_endpoint_auth_methods_supported?: string[];
  /** Introspection 端点认证签名算法。 */
  introspection_endpoint_auth_signing_alg_values_supported?: string[];
  /** 支持的声明（claim）名称。 */
  claims_supported?: string[];
  /** 支持的声明类型。 */
  claim_types_supported?: string[];
  /** 是否支持 claims 请求参数。 */
  claims_parameter_supported?: boolean;
  /** 支持的 scope 列表。 */
  scopes_supported?: string[];
  /** 是否支持 request 参数。 */
  request_parameter_supported?: boolean;
  /** 是否支持 request_uri 参数。 */
  request_uri_parameter_supported?: boolean;
  /** 使用 request_uri 是否必须先注册。 */
  require_request_uri_registration?: boolean;
  /** PKCE 支持的 code_challenge 方法。 */
  code_challenge_methods_supported?: string[];
  /** 是否支持 TLS 客户端证书绑定的访问令牌。 */
  tls_client_certificate_bound_access_tokens?: boolean;
  /** 令牌撤销端点。 */
  revocation_endpoint?: string;
  /** 撤销端点认证方式。 */
  revocation_endpoint_auth_methods_supported?: string[];
  /** 撤销端点认证签名算法。 */
  revocation_endpoint_auth_signing_alg_values_supported?: string[];
  /** 是否支持 OIDC Back-Channel Logout。 */
  backchannel_logout_supported?: boolean;
  /** Back-Channel Logout 是否携带 session 信息。 */
  backchannel_logout_session_supported?: boolean;
  /** 设备授权端点（RFC 8628）。 */
  device_authorization_endpoint?: string;
}
