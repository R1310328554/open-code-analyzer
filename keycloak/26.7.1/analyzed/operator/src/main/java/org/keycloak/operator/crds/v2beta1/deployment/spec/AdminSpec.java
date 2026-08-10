package org.keycloak.operator.crds.v2beta1.deployment.spec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.sundr.builder.annotations.Buildable;

/**
 * Operator 连接 Keycloak 管理 API 时的客户端 TLS 配置。
 *
 * <p>这些设置仅影响 Operator 到服务器的管理连接，不会写入 Keycloak 服务器进程配置。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
public class AdminSpec {

    /** 管理客户端 mTLS 所需的 TLS Secret 名称。 */
    @JsonPropertyDescription("If mTLS is required, this references a secret containing the client TLS configuration for the admin client. Reference: https://kubernetes.io/docs/concepts/configuration/secret/#tls-secrets.")
    private String tlsSecret;

    public String getTlsSecret() {
        return tlsSecret;
    }

    public void setTlsSecret(String tlsSecret) {
        this.tlsSecret = tlsSecret;
    }

}
