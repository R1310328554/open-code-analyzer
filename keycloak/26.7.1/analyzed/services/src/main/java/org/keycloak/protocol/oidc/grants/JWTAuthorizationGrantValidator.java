package org.keycloak.protocol.oidc.grants;

import java.util.List;

import org.keycloak.protocol.oidc.JWTAuthorizationGrantValidationContext ;


/**
 * JWT 授权断言校验器接口：定义客户端、issuer、subject 及令牌有效性等校验步骤。
 *
 * @author <a href="mailto:yutaka.obuchi.sd@hitachi.com">Yutaka Obuchi</a>
 */

public interface JWTAuthorizationGrantValidator extends JWTAuthorizationGrantValidationContext {

    /** 校验请求客户端是否允许使用 JWT 授权模式 */
    void validateClient();
    
    /** 校验 JWT iss 声明 */
    void validateIssuer();
    
    /** 校验 JWT sub 声明 */
    void validateSubject();
    
    /**
     * 校验断言是否仍在有效期内。
     * @param allowedClockSkew 时钟偏差（秒）
     * @param maxExp 最大 exp 限制
     * @param reusePermitted 是否允许断言重用
     * @return 校验是否通过
     */
    boolean validateTokenActive(int allowedClockSkew, int maxExp, boolean reusePermitted); 
    
    /** 校验 JWT 签名算法是否与 IdP 配置一致 @param expectedSignatureAlg 期望算法 */
    boolean validateSignatureAlgorithm(String expectedSignatureAlg);
    
    /**
     * 校验 JWT aud 声明。
     * @param expectedAudiences 允许的受众列表
     * @param multipleAudienceAllowed 是否允许多个 aud
     * @return 校验是否通过
     */
    boolean validateTokenAudience(List<String> expectedAudiences, boolean multipleAudienceAllowed);
    
}