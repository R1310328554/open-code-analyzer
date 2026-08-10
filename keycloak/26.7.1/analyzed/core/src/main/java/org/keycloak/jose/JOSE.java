package org.keycloak.jose;

/**
 * 表示已签名（JWS）或已加密（JWE）JWT 的统一接口。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface JOSE {

    /**
     * 返回 JWT 头部。
     *
     * @return JWT 头部
     */
    <H extends JOSEHeader> H getHeader();
}
