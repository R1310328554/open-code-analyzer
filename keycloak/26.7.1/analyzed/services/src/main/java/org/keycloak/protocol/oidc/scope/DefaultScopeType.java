package org.keycloak.protocol.oidc.scope;

/**
 * 默认参数化 scope 类型别名。
 * <p>复用 {@link StringScopeType#TYPE}，表示未显式配置类型时的默认字符串 scope。</p>
 */
public class DefaultScopeType {
    /** 默认 scope 类型标识（与 {@link StringScopeType#TYPE} 相同） */
    public static final String TYPE = StringScopeType.TYPE;
}
