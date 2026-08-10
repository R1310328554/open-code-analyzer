package org.keycloak.quarkus.deployment;

import java.util.Map;

import org.keycloak.authentication.authenticators.browser.WebAuthnAuthenticatorMetadata;


/**
 * WebAuthn 认证器元数据解析结果的构建项。
 */
import io.quarkus.builder.item.SimpleBuildItem;


/**
 * WebAuthn 认证器元数据解析结果的构建项。
 */
final class WebAuthnMetadataBuildItem extends SimpleBuildItem {

    /** 认证器 ID 到元数据的映射。 */
    private final Map<String, WebAuthnAuthenticatorMetadata> metadata;

    /** 构造 WebAuthn 元数据构建项。 */
    WebAuthnMetadataBuildItem(Map<String, WebAuthnAuthenticatorMetadata> metadata) {
        this.metadata = metadata;
    }

    /** @return WebAuthn 认证器元数据映射 */
    /** @return WebAuthn 认证器元数据映射 */
    Map<String, WebAuthnAuthenticatorMetadata> getMetadata() {
        return metadata;
    }
}
