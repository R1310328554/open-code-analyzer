package org.keycloak.protocol.oidc.scope;

import jakarta.annotation.Nonnull;

import org.keycloak.models.ClientScopeModel;

/**
 * 字符串参数化 scope 类型。
 * <p>默认 scope 类型，不对参数做额外格式校验（接受任意字符串）。</p>
 */
public class StringScopeType implements ParameterizedScopeTypeProvider {

    /** 类型标识：string */
    public static final String TYPE = "string";

    /** @return 类型名称 {@link #TYPE} */
    @Override
    public String getTypeName() {
        return TYPE;
    }

    /** 字符串类型不做额外校验 @param scope 客户端范围 @param parameter scope 参数值 */
    @Override
    public void validateParameter(@Nonnull ClientScopeModel scope, @Nonnull String parameter) throws InvalidScopeParameterException {
    }
}
