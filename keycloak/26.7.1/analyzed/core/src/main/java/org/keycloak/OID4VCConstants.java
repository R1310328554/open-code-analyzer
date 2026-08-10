package org.keycloak;

/**
 * OID4VC（OpenID for Verifiable Credentials）及相关规范（如 SD-JWT）中使用的常量定义。
 */
public class OID4VCConstants {

    /** 启用 OID4VCI 功能的 realm 属性键。 */
    public static final String OID4VCI_ENABLED_ATTRIBUTE_KEY = "oid4vci.enabled";

    // SD-JWT 相关常量
    /** SD-JWT 披露分隔符。 */
    public static final String SDJWT_DELIMITER = "~";
    /** SD 哈希声明名。 */
    public static final String SD_HASH = "sd_hash";
    /**
     * SD-JWT 凭证声明名，参见 {@linkplain https://drafts.oauth.net/oauth-sd-jwt-vc/draft-ietf-oauth-sd-jwt-vc.html SD-JWT-VC 草案}。
     */
    public static final String CLAIM_NAME_SD = "_sd";
    /** SD 哈希算法声明名。 */
    public static final String CLAIM_NAME_SD_HASH_ALGORITHM = "_sd_alg";
    /** 未披露数组占位符。 */
    public static final String CLAIM_NAME_SD_UNDISCLOSED_ARRAY = "...";

    /** 签发时间（iat）声明。 */
    public static final String CLAIM_NAME_IAT = "iat";
    /** 过期时间（exp）声明。 */
    public static final String CLAIM_NAME_EXP = "exp";
    /** 生效时间（nbf）声明。 */
    public static final String CLAIM_NAME_NBF = "nbf";
    /** 签发者（iss）声明。 */
    public static final String CLAIM_NAME_ISSUER = "iss";
    /** 确认密钥（cnf）声明。 */
    public static final String CLAIM_NAME_CNF = "cnf";
    /** JWK 嵌入声明。 */
    public static final String CLAIM_NAME_JWK = "jwk";
    /** 主体（sub）声明。 */
    public static final String CLAIM_NAME_SUB = "sub";
    /** 可验证凭证（vc）声明。 */
    public static final String CLAIM_NAME_VC = "vc";
    /** 凭证类型（vct）声明。 */
    public static final String CLAIM_NAME_VCT = "vct";

    // JWT 标识符（jti）在 SD-JWT 凭证中唯一标识一份凭证，可用于：
    //   * SD-JWT 重放防护
    //   * 自省缓存键
    //   * 去重
    //   * 凭证撤销跟踪（可选）
    /** JWT ID（jti）声明。 */
    public static final String CLAIM_NAME_JTI = "jti";

    // 凭证主体标识符：
    //   * VC 主体的稳定标识
    //   * 通常为主体 DID
    //   * 可映射到用户属性
    /** 凭证主体 ID 声明。 */
    public static final String CLAIM_NAME_SUBJECT_ID = "id";

    /** 密钥绑定 JWT 的 typ 值。 */
    public static final String KEYBINDING_JWT_TYP = "kb+jwt";

    /** SD 哈希默认算法。 */
    public static final String SD_HASH_DEFAULT_ALGORITHM = "sha-256";
    /** SD-JWT 密钥绑定默认最大允许年龄（秒）。 */
    public static final int SD_JWT_KEY_BINDING_DEFAULT_ALLOWED_MAX_AGE = 5 * 60; // 5 minutes
    /** SD-JWT 默认时钟偏移容忍（秒）。 */
    public static final int SD_JWT_DEFAULT_CLOCK_SKEW_SECONDS = 10;
    /**
     * JWT VC 签发者元数据端点路径，参见 {@linkplain https://datatracker.ietf.org/doc/html/draft-ietf-oauth-sd-jwt-vc-13#section-5 草案第 5 节}。
     */
    public static final String JWT_VC_ISSUER_END_POINT = "/.well-known/jwt-vc-issuer";

    /**
     * W3C 可验证凭证数据模型中的 credentialSubject 字段。
     * @see <a href="https://www.w3.org/TR/2022/REC-vc-data-model-20220303/#credential-subject">VC Data Model</a>
     */
    public static final String CREDENTIAL_SUBJECT = "credentialSubject";

    /**
     * 签名元数据 JWT 类型，参见 OID4VCI 规范附录 G.6.3。
     * @see <a href="https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#appendix-G.6.3">OID4VCI</a>
     */
    public static final String SIGNED_METADATA_JWT_TYPE = "openidvci-issuer-metadata+jwt";

    // --- 端点 / Well-Known ---
    /** OpenID Credential Issuer well-known 标识。 */
    public static final String WELL_KNOWN_OPENID_CREDENTIAL_ISSUER = "openid-credential-issuer";
    /** PNG 图片响应类型。 */
    public static final String RESPONSE_TYPE_IMG_PNG = "image/png";
    /** 凭证提供 URI 的 scope 值。 */
    public static final String CREDENTIAL_OFFER_URI_CODE_SCOPE = "credential-offer";

    // OID4VCI — https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html
    /** OpenID Credential 授权类型标识。 */
    public static final String OPENID_CREDENTIAL = "openid_credential";
    /** 凭证标识符列表参数名。 */
    public static final String CREDENTIAL_IDENTIFIERS = "credential_identifiers";
    /** 凭证配置 ID 参数名。 */
    public static final String CREDENTIAL_CONFIGURATION_ID = "credential_configuration_id";

    private OID4VCConstants() {
    }

    /**
     * OID4VCI 规范附录 D.2 定义的密钥存储与用户认证攻击潜力等级。
     * <p>规范指出该枚举可扩展，实现中应以字符串形式处理。</p>
     *
     * <pre>
     *  Appendix D.2. Attack Potential Resistance
     *
     *  This specification defines the following values for key_storage and user_authentication:
     *  iso_18045_high: It MUST be used when key storage or user authentication is resistant to attack with attack
     *  potential "High", equivalent to VAN.5 according to [ISO.18045].
     *  iso_18045_moderate: It MUST be used when key storage or user authentication is resistant to attack with attack
     *  potential "Moderate", equivalent to VAN.4 according to [ISO.18045]. iso_18045_enhanced-basic: It MUST be used
     *  when key storage or user authentication is resistant to attack with attack potential "Enhanced-Basic",
     *  equivalent to VAN.3 according to [ISO.18045]. iso_18045_basic: It MUST be used when key storage or user
     *  authentication is resistant to attack with attack potential "Basic", equivalent to VAN.2 according to
     *  [ISO.18045]. Specifications that extend this list MUST choose collision-resistant values.
     * </pre>
     */
    public static class KeyAttestationResistanceLevels {

        /** 高攻击潜力抗性（ISO 18045 VAN.5）。 */
        public static final String HIGH = "iso_18045_high"; // VAN.5

        /** 中等攻击潜力抗性（ISO 18045 VAN.4）。 */
        public static final String MODERATE = "iso_18045_moderate"; // VAN.4

        /** 增强基础攻击潜力抗性（ISO 18045 VAN.3）。 */
        public static final String ENHANCED_BASIC = "iso_18045_enhanced-basic"; // VAN.3

        /** 基础攻击潜力抗性（ISO 18045 VAN.2）。 */
        public static final String BASIC = "iso_18045_basic"; // VAN.2
    }
}
