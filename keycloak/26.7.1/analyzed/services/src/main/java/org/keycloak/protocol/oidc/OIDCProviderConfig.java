package org.keycloak.protocol.oidc;

import java.util.Map;

import org.keycloak.Config;

/**
 * OIDC 协议 Provider 全局配置：请求参数长度限制、附加参数防 DoS 及自省/UserInfo 行为开关。
 *
 * @author <a href="mailto:patrick.weiner@prime-sign.com">Patrick Weiner</a>
 */
public class OIDCProviderConfig {

    private final Config.Scope config;

    /** OIDC 认证/令牌请求中标准参数默认最大长度。 */
    public static final int DEFAULT_REQ_PARAMS_DEFAULT_MAX_SIZE = 4000;

    private final int reqParamsDefaultMaxSize;

    /**
     * 特定标准 OIDC 参数的最大长度覆盖表。
     * <p>仍可在 {@link OIDCLoginProtocolFactory} 中按参数名覆盖；未覆盖时使用默认上限。</p>
     */
    private Map<String, Integer> DEFAULT_MAX_PARAMS_SIZES = Map.of(
            OIDCLoginProtocol.LOGIN_HINT_PARAM, 255 // 与用户名/邮箱 user-profile 长度对齐
    );

    /** 令牌类参数（如 subject_token、JWT）在 token 请求中的默认最大长度。 */
    public static final int DEFAULT_REQ_TOKEN_PARAMS_DEFAULT_MAX_SIZE = 20000;

    private final int reqTokenParamsDefaultMaxSize;

    /** {@link #additionalReqParamsMaxNumber} 未配置时的默认值。 */
    public static final int DEFAULT_ADDITIONAL_REQ_PARAMS_MAX_NUMBER = 5;

    /** 复制到客户端会话 note 的附加请求参数最大个数（防 DoS）。 */
    private final int additionalReqParamsMaxNumber;

    /** {@link #additionalReqParamsMaxSize} 未配置时的默认值。 */
    public static final int DEFAULT_ADDITIONAL_REQ_PARAMS_MAX_SIZE = 2000;

    /** 单个附加请求参数值的最大长度（防 DoS）。 */
    private final int additionalReqParamsMaxSize;

    /** {@link #additionalReqParamsFailFast} 未配置时的默认值。 */
    public static final boolean DEFAULT_ADDITIONAL_REQ_PARAMS_FAIL_FAST = false;

    /** 附加参数 fail-fast：false 静默忽略不合规参数，true 抛出异常。 */
    private final boolean additionalReqParamsFailFast;

    /** {@link #additionalReqTokenParamsFailFast} 未配置时的默认值。 */
    public static final boolean DEFAULT_ADDITIONAL_REQ_TOKEN_PARAMS_FAIL_FAST = true;

    /** 令牌类附加参数的 fail-fast 策略（语义同 {@link #additionalReqParamsFailFast}）。 */
    private final boolean additionalReqTokenParamsFailFast;

    /** {@link #additionalReqParamsMaxOverallSize} 未配置时的默认值。 */
    public static final int DEFAULT_ADDITIONAL_REQ_PARAMS_MAX_OVERALL_SIZE = Integer.MAX_VALUE;

    /** 所有附加请求参数值的总长度上限（防 DoS）。 */
    private final int additionalReqParamsMaxOverallSize;

    /** @deprecated Keycloak 27 将移除 */
    public static final boolean DEFAULT_ALLOW_MULTIPLE_AUDIENCES_FOR_JWT_CLIENT_AUTHENTICATION = false;

    /** JWT 客户端认证是否允许多个 audience。
     * @deprecated Keycloak 27 将移除
     */
    private final boolean allowMultipleAudiencesForJwtClientAuthentication;

    public static final boolean DEFAULT_ALLOW_TOKEN_INTROSPECTION_WITHOUT_AUDIENCE_CHECK = false;

    private final boolean allowTokenIntrospectionWithoutAudienceCheck;

    public static final boolean DEFAULT_ALLOW_USERINFO_WITH_LIGHTWEIGHT_ACCESS_TOKEN = false;

    private final boolean allowUserinfoWithLightweightAccessToken;

    /** 从 server 配置 scope 加载 OIDC Provider 参数。 */
    public OIDCProviderConfig(Config.Scope config) {
        this.config = config;

        this.reqParamsDefaultMaxSize = config.getInt(OIDCLoginProtocolFactory.CONFIG_OIDC_REQ_PARAMS_DEFAULT_MAX_SIZE, DEFAULT_REQ_PARAMS_DEFAULT_MAX_SIZE);
        this.reqTokenParamsDefaultMaxSize = config.getInt(OIDCLoginProtocolFactory.CONFIG_OIDC_REQ_TOKEN_PARAMS_DEFAULT_MAX_SIZE, DEFAULT_REQ_TOKEN_PARAMS_DEFAULT_MAX_SIZE);
        this.additionalReqParamsMaxNumber = config.getInt(OIDCLoginProtocolFactory.CONFIG_OIDC_ADD_REQ_PARAMS_MAX_NUMBER, DEFAULT_ADDITIONAL_REQ_PARAMS_MAX_NUMBER);
        this.additionalReqParamsMaxSize = config.getInt(OIDCLoginProtocolFactory.CONFIG_OIDC_ADD_REQ_PARAMS_MAX_SIZE, DEFAULT_ADDITIONAL_REQ_PARAMS_MAX_SIZE);
        this.additionalReqParamsMaxOverallSize = config.getInt(OIDCLoginProtocolFactory.CONFIG_OIDC_ADD_REQ_PARAMS_MAX_OVERALL_SIZE, DEFAULT_ADDITIONAL_REQ_PARAMS_MAX_OVERALL_SIZE);
        this.additionalReqParamsFailFast = config.getBoolean(OIDCLoginProtocolFactory.CONFIG_OIDC_ADD_REQ_PARAMS_FAIL_FAST, DEFAULT_ADDITIONAL_REQ_PARAMS_FAIL_FAST);
        this.additionalReqTokenParamsFailFast = config.getBoolean(OIDCLoginProtocolFactory.CONFIG_OIDC_ADD_REQ_TOKEN_PARAMS_FAIL_FAST, DEFAULT_ADDITIONAL_REQ_TOKEN_PARAMS_FAIL_FAST);

        this.allowMultipleAudiencesForJwtClientAuthentication = config.getBoolean(OIDCLoginProtocolFactory.CONFIG_OIDC_ALLOW_MULTIPLE_AUDIENCES_FOR_JWT_CLIENT_AUTHENTICATION, DEFAULT_ALLOW_MULTIPLE_AUDIENCES_FOR_JWT_CLIENT_AUTHENTICATION);
        this.allowTokenIntrospectionWithoutAudienceCheck = config.getBoolean(OIDCLoginProtocolFactory.CONFIG_ALLOW_TOKEN_INTROSPECTION_WITHOUT_AUDIENCE_CHECK, DEFAULT_ALLOW_TOKEN_INTROSPECTION_WITHOUT_AUDIENCE_CHECK);
        this.allowUserinfoWithLightweightAccessToken = config.getBoolean(OIDCLoginProtocolFactory.CONFIG_ALLOW_USERINFO_WITH_LIGHTWEIGHT_ACCESS_TOKEN, DEFAULT_ALLOW_USERINFO_WITH_LIGHTWEIGHT_ACCESS_TOKEN);
    }

    /** @return 附加参数最大个数 */
    public int getAdditionalReqParamsMaxNumber() {
        return additionalReqParamsMaxNumber;
    }

    /** @return 单个附加参数值最大长度 */
    public int getAdditionalReqParamsMaxSize() {
        return additionalReqParamsMaxSize;
    }

    /** @param isTokenParam 是否为令牌类参数
     * @return 是否 fail-fast */
    public boolean isAdditionalReqParamsFailFast(boolean isTokenParam) {
        return isTokenParam ? additionalReqTokenParamsFailFast : additionalReqParamsFailFast;
    }

    /** @return 附加参数总长度上限 */
    public int getAdditionalReqParamsMaxOverallSize() {
        return additionalReqParamsMaxOverallSize;
    }

    /** @return 是否允许多 audience JWT 客户端认证 */
    public boolean isAllowMultipleAudiencesForJwtClientAuthentication() {
        return allowMultipleAudiencesForJwtClientAuthentication;
    }

    /** @return 自省是否跳过 audience 校验 */
    public boolean isAllowTokenIntrospectionWithoutAudienceCheck() {
        return allowTokenIntrospectionWithoutAudienceCheck;
    }

    /** @return 是否允许轻量访问令牌访问 UserInfo */
    public boolean isAllowUserinfoWithLightweightAccessToken() {
        return allowUserinfoWithLightweightAccessToken;
    }

    /**
     * 返回指定 OIDC 参数允许的最大长度。
     * @param paramName 参数名（已知 OIDC 参数）
     * @param isTokenParam 是否为令牌类参数
     * @return 最大长度
     */
    public int getMaxLengthForTheParameter(String paramName, boolean isTokenParam) {
        // 该参数在配置中的显式上限
        Integer paramMaxSize = config.getInt(OIDCLoginProtocolFactory.CONFIG_OIDC_REQ_PARAMS_MAX_SIZE_PREFIX + "--" + paramName);

        // 回退到 DEFAULT_MAX_PARAMS_SIZES
        if (paramMaxSize == null) {
            paramMaxSize = DEFAULT_MAX_PARAMS_SIZES.get(paramName);
        }

        // 再回退到全局默认（区分 token/普通参数）
        if (paramMaxSize == null) {
            paramMaxSize = isTokenParam ? reqTokenParamsDefaultMaxSize : reqParamsDefaultMaxSize;
        }

        return paramMaxSize;
    }
}
