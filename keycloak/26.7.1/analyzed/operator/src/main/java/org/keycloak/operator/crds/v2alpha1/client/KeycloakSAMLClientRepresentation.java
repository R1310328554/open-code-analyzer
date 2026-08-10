package org.keycloak.operator.crds.v2alpha1.client;

import org.keycloak.representations.admin.v2.SAMLClientRepresentation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import io.sundr.builder.annotations.Buildable;

/**
 * SAML 客户端 CR 中的客户端表示，扩展 {@link SAMLClientRepresentation}。
 *
 * <p>protocol、uuid、clientId 由控制器或 CR 元数据推导，不在 spec JSON 中暴露。
 */
@JsonTypeInfo(use = Id.NONE)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", lazyCollectionInitEnabled = false)
public class KeycloakSAMLClientRepresentation extends SAMLClientRepresentation {

    /** 协议固定为 saml，由资源类型隐含。 */
    @JsonIgnore
    @Override
    public String getProtocol() {
        return super.getProtocol();
    }

    /** UUID 由 Keycloak 分配。 */
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
