package org.keycloak.broker.spiffe;

/**
 * SPIFFE 联邦客户端认证常量。
 * <p>定义 OAuth SPIFFE JWT SVID 客户端断言类型 URN。</p>
 */
public interface SpiffeConstants {

    /** SPIFFE JWT SVID 客户端断言类型 URN。 */
    String CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-spiffe";

}
