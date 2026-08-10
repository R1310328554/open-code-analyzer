package org.keycloak.protocol.oidc.scope;

import jakarta.annotation.Nonnull;

import org.keycloak.models.ClientScopeModel;

/**
 * 整数参数化 scope 类型。
 * <p>校验 scope 参数为合法整数字符串（使用 {@code Long.parseLong} 以支持超出 int 范围的整数）。</p>
 */
public class IntegerScopeType implements ParameterizedScopeTypeProvider {

    /** 类型标识：integer */
    public static final String TYPE = "integer";

    /** @return 类型名称 {@link #TYPE} */
    @Override
    public String getTypeName() {
        return TYPE;
    }

    /**
     * 校验参数为合法整数。
     * @param scope 客户端范围
     * @param parameter scope 参数值
     * @throws InvalidScopeParameterException 非整数时
     */
    @Override
    public void validateParameter(@Nonnull ClientScopeModel scope, @Nonnull String parameter) throws InvalidScopeParameterException {
        try {
            // 使用 Long 解析以接受任意整数（无小数部分），不限于 int 范围
            Long.parseLong(parameter);
        } catch (NumberFormatException e) {
            throw new InvalidScopeParameterException(String.format("'%s' is not a valid integer", parameter));
        }
    }
}
