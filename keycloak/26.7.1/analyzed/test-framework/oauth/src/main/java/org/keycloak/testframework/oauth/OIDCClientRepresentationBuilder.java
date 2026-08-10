package org.keycloak.testframework.oauth;

import org.keycloak.representations.oidc.OIDCClientRepresentation;


/**
 * 构建 {@link org.keycloak.representations.oidc.OIDCClientRepresentation} 的函数式接口。
 * <p>
 * 供 {@link org.keycloak.testframework.oauth.annotations.InjectCimdProvider} 通过 {@code config()} 指定 CIMD 元数据内容。
 *
 * @author rmartinc
 */
public interface OIDCClientRepresentationBuilder {

    /** 生成 OIDC 客户端元数据表示。 */
    OIDCClientRepresentation build();
}
