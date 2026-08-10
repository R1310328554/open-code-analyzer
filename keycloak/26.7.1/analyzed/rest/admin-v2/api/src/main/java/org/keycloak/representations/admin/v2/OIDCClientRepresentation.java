package org.keycloak.representations.admin.v2;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.keycloak.representations.admin.v2.validation.ClientSecretNotBlank;
import org.keycloak.representations.admin.v2.validation.ConfidentialFlowsRequireAuth;
import org.keycloak.representations.admin.v2.validation.PutClient;
import org.keycloak.representations.admin.v2.validation.RedirectFlowsRequireUris;
import org.keycloak.representations.admin.v2.validation.ServiceAccountRolesRequireFlow;
import org.keycloak.representations.admin.v2.validation.ValidAuthMethod;
import org.keycloak.representations.admin.v2.validation.ValidWebOrigin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * OpenID Connect 客户端的 Admin v2 表示。
 * <p>
 * 协议固定为 {@link #PROTOCOL}（{@code openid-connect}），包含登录流、认证配置、
 * Web 来源及服务账户角色等 OIDC 特有属性；类级校验约束确保机密流、重定向 URI
 * 与服务账户角色配置的一致性。
 */
@Schema
@ConfidentialFlowsRequireAuth
@RedirectFlowsRequireUris
@ServiceAccountRolesRequireFlow
public class OIDCClientRepresentation extends BaseClientRepresentation {
    /** OIDC 协议鉴别值。 */
    public static final String PROTOCOL = "openid-connect";

    /** 客户端可启用的 OIDC 登录/授权流类型。 */
    public enum Flow {
        /** 标准授权码流。 */
        STANDARD,
        /** 隐式流。 */
        IMPLICIT,
        /** 直接访问授权（Resource Owner Password）流。 */
        DIRECT_GRANT,
        /** 服务账户（客户端凭证）流。 */
        SERVICE_ACCOUNT,
        /** 令牌交换流。 */
        TOKEN_EXCHANGE,
        /** 设备授权流。 */
        DEVICE,
        /** 客户端发起的后端认证（CIBA）流。 */
        CIBA
    }

    /** 为该客户端启用的登录流集合。 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonPropertyDescription("Login flows that are enabled for this client")
    private Set<Flow> loginFlows = new LinkedHashSet<>();

    /** 客户端认证配置（方法、密钥/证书等）。 */
    @JsonMerge
    @Valid
    @JsonPropertyDescription("Authentication configuration for this client")
    private Auth auth;

    /** 允许向该客户端发起跨域请求的 Web 来源。 */
    @Size(max = 100)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonPropertyDescription("Web origins that are allowed to make requests to this client")
    private Set<@NotBlank @Size(max = 255) @ValidWebOrigin String> webOrigins = new LinkedHashSet<>();

    /** 分配给服务账户的角色名称集合。 */
    @Size(max = 300)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonPropertyDescription("Roles assigned to the service account")
    private Set<@NotBlank @Size(max = 255) String> serviceAccountRoles = new LinkedHashSet<>();

    /** 默认将协议设为 {@link #PROTOCOL}。 */
    public OIDCClientRepresentation() {
        this.protocol = PROTOCOL;
    }

    /** 指定 clientId 并初始化 OIDC 协议。 */
    public OIDCClientRepresentation(String clientId) {
        this.protocol = PROTOCOL;
        this.clientId = clientId;
    }

    public Set<Flow> getLoginFlows() {
        return loginFlows;
    }

    public void setLoginFlows(Set<Flow> loginFlows) {
        this.loginFlows = loginFlows;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Set<String> getWebOrigins() {
        return webOrigins;
    }

    public void setWebOrigins(Set<String> webOrigins) {
        this.webOrigins = webOrigins;
    }

    public Set<String> getServiceAccountRoles() {
        return serviceAccountRoles;
    }

    public void setServiceAccountRoles(Set<String> serviceAccountRoles) {
        this.serviceAccountRoles = serviceAccountRoles;
    }

    /**
     * OIDC 客户端认证子表示。
     * <p>
     * PUT 时若认证方法为客户端密钥类型，{@link ClientSecretNotBlank} 要求 {@link #secret} 非空。
     */
    @ClientSecretNotBlank(groups = PutClient.class, affectedFieldNames = {"secret"})
    public static class Auth extends BaseRepresentation {

        /** 客户端认证方法（如 {@code client-secret}、{@code client-secret-jwt}）。 */
        @NotBlank
        @ValidAuthMethod
        @JsonPropertyDescription("Client authentication method (e.g. `client-secret`, `client-secret-jwt`)")
        private String method;

        /** 使用密钥认证时的客户端密钥。 */
        @Size(min = 6, max = 255)
        @JsonPropertyDescription("Secret used to authenticate this client with Secret authentication")
        private String secret;

        /** 使用 Signed JWT 认证时的公钥/证书。 */
        @Size(max = 65536)
        @JsonPropertyDescription("Public key used to authenticate this client with Signed JWT authentication")
        private String certificate;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getCertificate() {
            return certificate;
        }

        public void setCertificate(String certificate) {
            this.certificate = certificate;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Auth)) {
                return false;
            }
            Auth auth = (Auth)o;
            return Objects.equals(method, auth.method) && Objects.equals(secret, auth.secret) && Objects.equals(certificate, auth.certificate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(method, secret, certificate);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OIDCClientRepresentation)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        OIDCClientRepresentation that = (OIDCClientRepresentation)o;
        return Objects.equals(loginFlows, that.loginFlows) && Objects.equals(auth, that.auth) && Objects.equals(webOrigins, that.webOrigins) && Objects.equals(serviceAccountRoles, that.serviceAccountRoles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), loginFlows, auth, webOrigins, serviceAccountRoles);
    }
}
