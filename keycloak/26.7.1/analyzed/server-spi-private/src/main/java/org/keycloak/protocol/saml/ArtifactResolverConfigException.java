package org.keycloak.protocol.saml;

/**
 * {@link ArtifactResolver} 配置错误时抛出的受检异常。
 * Exception to indicate a configuration error in {@link ArtifactResolver}.
 *
 */
public class ArtifactResolverConfigException extends Exception {

    /** @param e 底层配置异常 */
    public ArtifactResolverConfigException(Exception e){
        super(e);
    }
}
