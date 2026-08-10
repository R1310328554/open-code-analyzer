package org.keycloak.models;

/**
 * 客户端密钥（Client Secret）轮换与认证方式相关的客户端属性键名。
 *
 * @author <a href="mailto:masales@redhat.com">Marcelo Sales</a>
 */
public class ClientSecretConstants {

    // 客户端属性键名
    /** 是否启用客户端密钥轮换。 */
    public static final String CLIENT_SECRET_ROTATION_ENABLED = "client.secret.rotation.enabled";
    /** 当前密钥创建时间戳。 */
    public static final String CLIENT_SECRET_CREATION_TIME = "client.secret.creation.time";
    /** 当前密钥过期时间戳。 */
    public static final String CLIENT_SECRET_EXPIRATION = "client.secret.expiration.time";
    /** 轮换中的新密钥（尚未生效）。 */
    public static final String CLIENT_ROTATED_SECRET = "client.secret.rotated";
    /** 轮换密钥创建时间戳。 */
    public static final String CLIENT_ROTATED_SECRET_CREATION_TIME = "client.secret.rotated.creation.time";
    /** 轮换密钥过期时间戳。 */
    public static final String CLIENT_ROTATED_SECRET_EXPIRATION_TIME = "client.secret.rotated.expiration.time";
    /** 当前密钥剩余有效时间（秒）。 */
    public static final String CLIENT_SECRET_REMAINING_EXPIRATION_TIME = "client.secret.remaining.expiration.time";

    /**
     * 限制客户端认证方式：仅允许 {@code client_secret_basic}（HTTP Basic）或 {@code client_secret_post}（请求参数）。
     * <p>未设置时两种方法均允许。</p>
     */
    public static final String CLIENT_SECRET_AUTHENTICATION_ALLOWED_METHOD = "client.secret.authentication.allowed.method";

}
