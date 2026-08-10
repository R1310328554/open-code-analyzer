package org.keycloak.testframework.events;

import java.util.Set;

import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.authenticators.client.ClientIdAndSecretAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.EventType;
import org.keycloak.protocol.oidc.grants.AuthorizationCodeGrantTypeFactory;
import org.keycloak.protocol.oidc.grants.RefreshTokenGrantTypeFactory;
import org.keycloak.representations.idm.EventRepresentation;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;

import static org.hamcrest.Matchers.is;

/**
 * 登录与用户事件的流式断言工具。
 * <p>
 * 基于 {@link EventRepresentation} 提供常见 OAuth/OIDC 流程事件的预置断言，支持链式校验。
 */
public class EventAssertion {

    /** 被断言的用户事件。 */
    private final EventRepresentation event;

    /** 本地 IPv4 回环地址，用于 {@link #hasIpAddress()} 校验。 */
    private static final String DEFAULT_IP_ADDRESS = "127.0.0.1";
    private static final String DEFAULT_IP_ADDRESS_V6 = "0:0:0:0:0:0:0:1";
    private static final String DEFAULT_IP_ADDRESS_V6_SHORT = "::1";

    /** @param event 非空且含 ID 的事件表示 */
    protected EventAssertion(EventRepresentation event) {
        Assertions.assertNotNull(event, "Event was null");
        Assertions.assertNotNull(event.getId(), "Event id was null");
        this.event = event;
    }

    /**
     * 断言事件为成功类型（类型名不以 {@code _ERROR} 结尾）。
     *
     * @param event 待断言事件
     * @return 断言器实例，可继续链式校验
     */
    public static EventAssertion assertSuccess(EventRepresentation event) {
        Assertions.assertFalse(event.getType().endsWith("_ERROR"), "Expected successful event");
        return new EventAssertion(event);
    }

    /**
     * 断言事件为错误类型（类型名以 {@code _ERROR} 结尾）。
     *
     * @param event 待断言事件
     * @return 断言器实例，可继续链式校验
     */
    public static EventAssertion assertError(EventRepresentation event) {
        Assertions.assertTrue(event.getType().endsWith("_ERROR"), "Expected error event");
        return new EventAssertion(event);
    }

    /**
     * 断言成功的 {@link EventType#LOGIN} 事件及常见详情字段。
     *
     * @param event 待断言事件
     * @return 断言器实例
     */
    public static EventAssertion expectLoginSuccess(EventRepresentation event) {
        return assertSuccess(event)
                .type(EventType.LOGIN)
                .hasCodeId()
                .hasSessionId()
                .hasIpAddress()
                .loginSuccessEventHasAllRequiredDetails();
    }

    /**
     * 断言 {@link EventType#LOGIN_ERROR} 错误事件。
     *
     * @param event 待断言事件
     * @return 断言器实例
     */
    public static EventAssertion expectLoginError(EventRepresentation event) {
        return assertError(event)
                .type(EventType.LOGIN_ERROR)
                .hasCodeId()
                .hasIpAddress();
    }

    /**
     * 断言成功的 {@link EventType#LOGOUT} 事件。
     *
     * @param event 待断言事件
     * @return 断言器实例
     */
    public static EventAssertion expectLogoutSuccess(EventRepresentation event) {
        return assertSuccess(event)
                .type(EventType.LOGOUT)
                .hasIpAddress()
                .hasSessionId()
                .hasRedirectUri();
    }

    /**
     * 断言 {@link EventType#LOGOUT_ERROR} 错误事件（无 session/client/user）。
     *
     * @param event 待断言事件
     * @return 断言器实例
     */
    public static EventAssertion expectLogoutError(EventRepresentation event) {
        return assertError(event)
                .type(EventType.LOGOUT_ERROR)
                .hasIpAddress()
                .sessionId(null)
                .clientId(null)
                .userId(null)
                .withoutDetails(Details.CODE_ID);
    }

    /**
     * 断言成功的 {@link EventType#REGISTER} 事件。
     *
     * @param event 待断言事件
     * @return 断言器实例
     */
    public static EventAssertion expectRegisterSuccess(EventRepresentation event) {
        return assertSuccess(event)
                .type(EventType.REGISTER)
                .sessionId(null)
                .hasCodeId()
                .hasUserId()
                .hasRedirectUri()
                .details(Details.REGISTER_METHOD, "form");
    }

    /**
     * 断言 {@link EventType#REGISTER_ERROR} 错误事件。
     *
     * @param event 待断言事件
     * @return 断言器实例
     */
    public static EventAssertion expectRegisterError(EventRepresentation event) {
        return assertError(event)
                .type(EventType.REGISTER_ERROR)
                .sessionId(null)
                .userId(null)
                .hasCodeId()
                .hasRedirectUri()
                .details(Details.REGISTER_METHOD, "form");
    }

    /**
     * 断言成功的 {@link EventType#CLIENT_LOGIN} 客户端凭证事件。
     *
     * @param event 待断言事件
     * @return 断言器实例
     */
    public static EventAssertion expectClientLoginSuccess(EventRepresentation event) {
        return assertSuccess(event)
                .type(EventType.CLIENT_LOGIN)
                .hasSessionId()
                .details(Details.CLIENT_AUTH_METHOD, ClientIdAndSecretAuthenticator.PROVIDER_ID)
                .details(Details.GRANT_TYPE, OAuth2Constants.CLIENT_CREDENTIALS);
    }

    /**
     * 断言成功的 {@link EventType#REFRESH_TOKEN} 刷新令牌事件。
     *
     * @param event 待断言事件
     * @return 断言器实例
     */
    public static EventAssertion expectRefreshTokenSuccess(EventRepresentation event) {
        return assertSuccess(event)
                .type(EventType.REFRESH_TOKEN)
                .hasSessionId()
                .hasTokenId(Details.UPDATED_REFRESH_TOKEN_ID)
                .hasAccessTokenId(RefreshTokenGrantTypeFactory.GRANT_SHORTCUT);
    }

    /**
     * 断言成功的 {@link EventType#CODE_TO_TOKEN} 授权码换令牌事件。
     *
     * @param event 待断言事件
     * @return 断言器实例
     */
    public static EventAssertion expectCodeToTokenSuccess(EventRepresentation event) {
        return assertSuccess(event)
                .type(EventType.CODE_TO_TOKEN)
                .hasSessionId()
                .hasCodeId()
                .hasTokenId(Details.REFRESH_TOKEN_ID)
                .hasAccessTokenId(AuthorizationCodeGrantTypeFactory.GRANT_SHORTCUT);
    }

    /**
     * 断言 {@link EventType#CODE_TO_TOKEN_ERROR} 错误事件。
     *
     * @param event 待断言事件
     * @return 断言器实例
     */
    public static EventAssertion expectCodeToTokenError(EventRepresentation event) {
        return assertError(event)
                .type(EventType.CODE_TO_TOKEN_ERROR)
                .hasSessionId()
                .hasCodeId()
                .details(Details.CLIENT_AUTH_METHOD, ClientIdAndSecretAuthenticator.PROVIDER_ID);
    }

    /**
     * 断言 Required Action 相关成功事件（无 session，含 code_id）。
     *
     * @param event 待断言事件
     * @return 断言器实例
     */
    public static EventAssertion expectRequiredAction(EventRepresentation event) {
        return assertSuccess(event)
                .sessionId(null)
                .hasIpAddress()
                .hasCodeId()
                .withoutDetails(Details.CONSENT);
    }

    /**
     * 断言事件的 error 字段与期望值一致。
     *
     * @param error 期望错误消息
     * @return 当前断言器
     */
    public EventAssertion error(String error) {
        Assertions.assertEquals(error, event.getError());
        return this;
    }

    /**
     * 断言事件类型。
     *
     * @param type 期望的 {@link EventType}
     * @return 当前断言器
     */
    public EventAssertion type(EventType type) {
        Assertions.assertEquals(type, EventType.valueOf(event.getType()));
        return this;
    }

    /**
     * 断言事件已设置合法的 sessionId。
     *
     * @return 当前断言器
     */
    public EventAssertion hasSessionId() {
        MatcherAssert.assertThat(event.getSessionId(), EventMatchers.isSessionId());
        return this;
    }

    /**
     * 断言事件已设置合法的 userId（UUID）。
     *
     * @return 当前断言器
     */
    public EventAssertion hasUserId() {
        MatcherAssert.assertThat(event.getUserId(), EventMatchers.isUUID());
        return this;
    }

    /**
     * 断言 details 中含合法的 {@link Details#CODE_ID}。
     *
     * @return 当前断言器
     */
    public EventAssertion hasCodeId() {
        MatcherAssert.assertThat(event.getDetails().get(Details.CODE_ID), EventMatchers.isCodeId());
        return this;
    }

    /**
     * 断言 details 中指定键对应的 token id 合法。
     *
     * @param tokenType details 中的 token 键名
     * @return 当前断言器
     */
    public EventAssertion hasTokenId(String tokenType) {
        MatcherAssert.assertThat(event.getDetails().get(tokenType), EventMatchers.isTokenId());
        return this;
    }

    /**
     * 断言 details 中 {@link Details#TOKEN_ID} 为合法 access token id。
     *
     * @param expectedGrantShortcut 期望的 grant type 两位缩写
     * @return 当前断言器
     */
    public EventAssertion hasAccessTokenId(String expectedGrantShortcut) {
        MatcherAssert.assertThat(event.getDetails().get(Details.TOKEN_ID), EventMatchers.isAccessTokenId(expectedGrantShortcut));
        return this;
    }

    /**
     * 断言 details 中 scope 与期望值一致（顺序无关）。
     *
     * @param scope 期望 scope 字符串（空格分隔）
     * @return 当前断言器
     */
    public EventAssertion hasScope(String scope) {
        MatcherAssert.assertThat(event.getDetails().get(Details.SCOPE), EventMatchers.isScope(scope));
        return this;
    }

    /**
     * 断言事件 ipAddress 为本地回环地址（IPv4 或 IPv6）。
     *
     * @return 当前断言器
     */
    public EventAssertion hasIpAddress() {
        Assertions.assertNotNull(event.getIpAddress());
        Assertions.assertFalse(event.getIpAddress().isEmpty());
        MatcherAssert.assertThat(event.getIpAddress(), Matchers.anyOf(is(DEFAULT_IP_ADDRESS), is(DEFAULT_IP_ADDRESS_V6), is(DEFAULT_IP_ADDRESS_V6_SHORT)));
        return this;
    }

    /**
     * 断言事件的 clientId。
     *
     * @param clientId 期望客户端 ID
     * @return 当前断言器
     */
    public EventAssertion clientId(String clientId) {
        Assertions.assertEquals(clientId, event.getClientId());
        return this;
    }

    /**
     * 断言事件的 sessionId 等于给定值。
     *
     * @param sessionId 期望 session ID
     * @return 当前断言器
     */
    public EventAssertion sessionId(String sessionId) {
        Assertions.assertEquals(sessionId, event.getSessionId());
        return this;
    }

    /**
     * 断言事件的 userId（sub）等于给定值。
     *
     * @param userId 期望用户 ID
     * @return 当前断言器
     */
    public EventAssertion userId(String userId) {
        Assertions.assertEquals(userId, event.getUserId());
        return this;
    }

    /**
     * 断言 details 映射包含指定键值对；{@code value} 为 {@code null} 时改为断言键不存在。
     *
     * @param key 期望 details 键
     * @param value 期望值
     * @return 当前断言器
     */
    public EventAssertion details(String key, String value) {
        if (value != null) {
            MatcherAssert.assertThat(event.getDetails(), Matchers.hasEntry(key, value));
        } else {
            withoutDetails(key);
        }
        return this;
    }

    /**
     * 断言 details 映射不包含给定键。
     *
     * @param keys 不应出现的 details 键
     * @return 当前断言器
     */
    public EventAssertion withoutDetails(String... keys) {
        for (String key : keys) {
            MatcherAssert.assertThat(event.getDetails(), Matchers.not(Matchers.hasKey(key)));
        }
        return this;
    }

    /** 校验 LOGIN 成功事件 details 中的全部必填键。 */

    private EventAssertion loginSuccessEventHasAllRequiredDetails() {
        Set<String> keyDetails = Set.of("auth_method", "response_type", "redirect_uri", "consent", "code_id", "response_mode");
        for (String key : keyDetails) {
            MatcherAssert.assertThat(event.getDetails(), Matchers.hasKey(key));
        }
        return this;
    }

    /** 断言 details 中含 {@code redirect_uri} 键。 */

    private EventAssertion hasRedirectUri() {
        MatcherAssert.assertThat(event.getDetails(), Matchers.hasKey("redirect_uri"));
        return this;
    }

    /**
     * 返回当前断言关联的事件对象。
     *
     * @return 已断言的 {@link EventRepresentation}
     */
    public EventRepresentation getEvent() {
        return event;
    }
}
