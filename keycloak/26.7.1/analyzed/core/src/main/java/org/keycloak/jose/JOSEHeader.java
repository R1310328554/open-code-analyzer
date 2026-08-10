package org.keycloak.jose;

import java.io.Serializable;


/**
 * JOSE 头部接口，描述 JWT 签名或加密所用的算法与密钥标识。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface JOSEHeader extends Serializable {

    /**
     * 返回 JOSE 头部中用于签名或加密的原始算法标识。
     *
     * @return JOSE 头部中的算法值
     */
    String getRawAlgorithm();

    /** @return 密钥 ID（kid） */
    String getKeyId();
}
