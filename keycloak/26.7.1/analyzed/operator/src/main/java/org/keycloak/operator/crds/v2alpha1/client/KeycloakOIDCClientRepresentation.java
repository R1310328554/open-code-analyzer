package org.keycloak.operator.crds.v2alpha1.client;

import org.keycloak.representations.admin.v2.OIDCClientRepresentation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import io.fabric8.crd.generator.annotation.SchemaSwap;
import io.fabric8.kubernetes.api.model.SecretKeySelector;
import io.sundr.builder.annotations.Buildable;

/**
 * OIDC 客户端 CR 中的客户端表示，扩展 {@link OIDCClientRepresentation}。
 *
 * <p>通过 {@link SchemaSwap} 将 auth 字段替换为支持 Kubernetes Secret 引用的 {@link AuthWithSecretRef}；
 * protocol、uuid、clientId 由控制器填充，序列化时忽略。
 */
@JsonTypeInfo(use = Id.NONE)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
@SchemaSwap(fieldName = "auth", originalType = KeycloakOIDCClientRepresentation.class, targetType = KeycloakOIDCClientRepresentation.AuthWithSecretRef.class)
public class KeycloakOIDCClientRepresentation extends OIDCClientRepresentation {

    /**
     * 认证配置：客户端密钥通过 {@link SecretKeySelector} 引用 Secret，而非明文 secret 字段。
     */
    public static class AuthWithSecretRef extends OIDCClientRepresentation.Auth {

        /** 存放 client secret 的 Secret 键选择器。 */
        private SecretKeySelector secretRef;

        @JsonPropertyDescription("Secret containing the client secret")
        public SecretKeySelector getSecretRef() {
            return secretRef;
        }

        public void setSecretRef(SecretKeySelector secretRef) {
            this.secretRef = secretRef;
        }

        /** 明文 secret 在 CR 中不可用，由 secretRef 解析。 */
        @JsonIgnore
        @Override
        public String getSecret() {
            return super.getSecret();
        }

    }

    /** 协议固定为 openid-connect，由类型隐含。 */
    @JsonIgnore
    @Override
    public String getProtocol() {
        return super.getProtocol();
    }

    /** UUID 由 Keycloak 分配，不在 spec 中声明。 */
    @JsonIgnore
    @Override
    public String getUuid() {
        return super.getUuid();
    }

    /** clientId 默认使用 CR metadata.name。 */
    @JsonIgnore
    @Override
    public String getClientId() {
        return super.getClientId();
    }

}
