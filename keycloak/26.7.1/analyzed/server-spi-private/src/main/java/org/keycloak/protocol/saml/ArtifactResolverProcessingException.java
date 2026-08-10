package org.keycloak.protocol.saml;

/**
 * {@link ArtifactResolver} 处理 Artifact 时发生错误所抛出的受检异常。
 * Exception to indicate a processing error in {@link ArtifactResolver}.
 *
 */
public class ArtifactResolverProcessingException extends Exception{

    /** @param e 底层处理异常 */
    public ArtifactResolverProcessingException(Exception e){
        super(e);
    }

    /** @param message 错误描述 */
    public ArtifactResolverProcessingException(String message) {
        super(message);
    }

    /** @param message 错误描述
     * @param e 底层异常 */
    public ArtifactResolverProcessingException(String message, Exception e){
        super(message, e);
    }
}
