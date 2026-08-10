/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.authorization.client.representation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * UMA/OIDC 发现端点返回的服务器元数据，映射各授权相关 REST 端点 URL。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerConfiguration {

    @JsonProperty("issuer")
    private String issuer;

    @JsonProperty("authorization_endpoint")
    private String authorizationEndpoint;

    @JsonProperty("token_endpoint")
    private String tokenEndpoint;

    @JsonProperty("introspection_endpoint")
    private String introspectionEndpoint;

    @JsonProperty("userinfo_endpoint")
    private String userinfoEndpoint;

    @JsonProperty("end_session_endpoint")
    private String logoutEndpoint;

    @JsonProperty("jwks_uri")
    private String jwksUri;

    @JsonProperty("check_session_iframe")
    private String checkSessionIframe;

    @JsonProperty("grant_types_supported")
    private List<String> grantTypesSupported;

    @JsonProperty("response_types_supported")
    private List<String> responseTypesSupported;

    @JsonProperty("subject_types_supported")
    private List<String> subjectTypesSupported;

    @JsonProperty("id_token_signing_alg_values_supported")
    private List<String> idTokenSigningAlgValuesSupported;

    @JsonProperty("userinfo_signing_alg_values_supported")
    private List<String> userInfoSigningAlgValuesSupported;

    @JsonProperty("request_object_signing_alg_values_supported")
    private List<String> requestObjectSigningAlgValuesSupported;

    @JsonProperty("response_modes_supported")
    private List<String> responseModesSupported;

    @JsonProperty("registration_endpoint")
    private String registrationEndpoint;

    @JsonProperty("token_endpoint_auth_methods_supported")
    private List<String> tokenEndpointAuthMethodsSupported;

    @JsonProperty("token_endpoint_auth_signing_alg_values_supported")
    private List<String> tokenEndpointAuthSigningAlgValuesSupported;

    @JsonProperty("claims_supported")
    private List<String> claimsSupported;

    @JsonProperty("claim_types_supported")
    private List<String> claimTypesSupported;

    @JsonProperty("claims_parameter_supported")
    private Boolean claimsParameterSupported;

    @JsonProperty("scopes_supported")
    private List<String> scopesSupported;

    @JsonProperty("request_parameter_supported")
    private Boolean requestParameterSupported;

    @JsonProperty("request_uri_parameter_supported")
    private Boolean requestUriParameterSupported;

    @JsonProperty("resource_registration_endpoint")
    private String resourceRegistrationEndpoint;

    @JsonProperty("permission_endpoint")
    private String permissionEndpoint;
    
    @JsonProperty("policy_endpoint")
    private String policyEndpoint;

    /** 发行者标识（issuer）。 */
    public String getIssuer() {
        return issuer;
    }

    /** OAuth2/OIDC 授权端点 URL。 */
    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    /** 令牌端点 URL。 */
    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    /** 令牌内省端点 URL。 */
    public String getIntrospectionEndpoint() {
        return introspectionEndpoint;
    }

    /** UserInfo 端点 URL。 */
    public String getUserinfoEndpoint() {
        return userinfoEndpoint;
    }

    /** 登出/会话结束端点 URL。 */
    public String getLogoutEndpoint() {
        return logoutEndpoint;
    }

    /** JWKS 公钥集 URI。 */
    public String getJwksUri() {
        return jwksUri;
    }

    /** 会话检查 iframe URL。 */
    public String getCheckSessionIframe() {
        return checkSessionIframe;
    }

    /** 服务器支持的 grant_type 列表。 */
    public List<String> getGrantTypesSupported() {
        return grantTypesSupported;
    }

    /** 服务器支持的 response_type 列表。 */
    public List<String> getResponseTypesSupported() {
        return responseTypesSupported;
    }

    /** 支持的 subject_type 列表。 */
    public List<String> getSubjectTypesSupported() {
        return subjectTypesSupported;
    }

    /** ID Token 支持的签名算法。 */
    public List<String> getIdTokenSigningAlgValuesSupported() {
        return idTokenSigningAlgValuesSupported;
    }

    /** UserInfo 响应支持的签名算法。 */
    public List<String> getUserInfoSigningAlgValuesSupported() {
        return userInfoSigningAlgValuesSupported;
    }

    /** Request Object 支持的签名算法。 */
    public List<String> getRequestObjectSigningAlgValuesSupported() {
        return requestObjectSigningAlgValuesSupported;
    }

    /** 支持的 response_mode 列表。 */
    public List<String> getResponseModesSupported() {
        return responseModesSupported;
    }

    /** 动态客户端注册端点 URL。 */
    public String getRegistrationEndpoint() {
        return registrationEndpoint;
    }

    /** 令牌端点支持的客户端认证方式。 */
    public List<String> getTokenEndpointAuthMethodsSupported() {
        return tokenEndpointAuthMethodsSupported;
    }

    /** 令牌端点客户端认证支持的签名算法。 */
    public List<String> getTokenEndpointAuthSigningAlgValuesSupported() {
        return tokenEndpointAuthSigningAlgValuesSupported;
    }

    /** 服务器声明支持的 claim 名称。 */
    public List<String> getClaimsSupported() {
        return claimsSupported;
    }

    /** 支持的 claim 类型。 */
    public List<String> getClaimTypesSupported() {
        return claimTypesSupported;
    }

    /** 是否支持 claims 请求参数。 */
    public Boolean getClaimsParameterSupported() {
        return claimsParameterSupported;
    }

    /** 服务器支持的 scope 列表。 */
    public List<String> getScopesSupported() {
        return scopesSupported;
    }

    /** 是否支持 request 参数。 */
    public Boolean getRequestParameterSupported() {
        return requestParameterSupported;
    }

    /** 是否支持 request_uri 参数。 */
    public Boolean getRequestUriParameterSupported() {
        return requestUriParameterSupported;
    }

    /** UMA 资源注册（Protection API）端点 URL。 */
    public String getResourceRegistrationEndpoint() {
        return resourceRegistrationEndpoint;
    }

    /** UMA 权限票据端点 URL。 */
    public String getPermissionEndpoint() {
        return permissionEndpoint;
    }
    
    /** UMA 用户托管策略（policy）端点 URL。 */
    public String getPolicyEndpoint() {
        return policyEndpoint;
    }
}
