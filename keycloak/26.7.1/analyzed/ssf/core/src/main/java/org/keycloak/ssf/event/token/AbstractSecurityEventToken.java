package org.keycloak.ssf.event.token;

import java.util.LinkedHashMap;
import java.util.Map;

import org.keycloak.TokenCategory;
import org.keycloak.json.StringOrArrayDeserializer;
import org.keycloak.json.StringOrArraySerializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * RFC 8417 安全事件令牌（SET）的抽象基类实现。
 * <p>封装 {@code jti}、{@code iss}、{@code iat}、{@code aud}、{@code events} 等标准声明，
 * 供 {@link SsfSecurityEventToken} 与 {@link SseCaepSecurityEventToken} 继承。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbstractSecurityEventToken implements SecurityEventToken {

    /** JWT ID，唯一标识此 SET。 */
    @JsonProperty("jti")
    protected String jti;

    /** 签发者（Transmitter）标识。 */
    @JsonProperty("iss")
    protected  String iss;

    /** 签发时间（Unix 秒）。 */
    @JsonProperty("iat")
    protected  Integer iat;

    /** 受众（Receiver）标识，可为字符串或字符串数组。 */
    @JsonProperty("aud")
    @JsonSerialize(using = StringOrArraySerializer.class)
    @JsonDeserialize(using = StringOrArrayDeserializer.class)
    protected  String[] aud;

    /** 安全事件声明映射，键为事件类型 URI，值为事件载荷对象。 */
    @JsonProperty("events")
    @JsonDeserialize(using = SsfEventMapJsonDeserializer.class)
    private Map<String, Object> events;

    @Override
    public TokenCategory getCategory() {
        // SSF SET 由服务端生成，不会像 OIDC access/id token 那样发给客户端，
        // 因此没有合适的 OIDC 导向 TokenCategory。此处返回值不被 SSF 签名路径 consult：
        // SecurityEventTokenDispatcher 通过 SsfSignatureAlgorithms#resolveForStream
        //（流覆盖 → transmitter SPI 默认 → 硬编码 RS256）解析 JWS 算法并直接传给
        // SecurityEventTokenEncoder。返回 INTERNAL 是满足 org.keycloak.Token 契约的中性回退；
        // 若未来调用方误将 SET 路由到 session.tokens() 辅助方法，将使用内部令牌签名器，
        // 而非静默继承 Receiver 客户端的 OIDC access-token 算法。
        return TokenCategory.INTERNAL;
    }

    @Override
    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    @Override
    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    @Override
    public Integer getIat() {
        return iat;
    }

    public void setIat(Integer iat) {
        this.iat = iat;
    }

    @Override
    public String[] getAud() {
        return aud;
    }

    public void setAud(String[] aud) {
        this.aud = aud;
    }

    @Override
    public Map<String, Object> getEvents() {
        if (events == null) {
            events = new LinkedHashMap<>();
        }
        return events;
    }

    public void setEvents(Map<String, Object> events) {
        this.events = events;
    }
}
