package org.keycloak.ssf.event.token;

import java.util.Map;

import org.keycloak.Token;

/**
 * RFC 8417 安全事件令牌（SET）契约，扩展 {@link Token}。
 * <p>定义 SET 标准声明的访问方法。</p>
 */
public interface SecurityEventToken extends Token {

    /** @return JWT ID */
    String getJti();

    /** @return 签发者标识 */
    String getIss();

    /** @return 签发时间（Unix 秒） */
    Integer getIat();

    /** @return 受众标识数组 */
    String[] getAud();

    /** @return 事件类型 URI 到事件载荷的映射 */
    Map<String, Object> getEvents();
}
