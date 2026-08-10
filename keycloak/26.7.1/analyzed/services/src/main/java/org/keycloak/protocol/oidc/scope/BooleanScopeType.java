package org.keycloak.protocol.oidc.scope;

import jakarta.annotation.Nonnull;

import org.keycloak.models.ClientScopeModel;

/**
 * 布尔型参数化 scope 类型：参数值仅允许 {@code true} 或 {@code false}（不区分大小写）。
 * <p>不可重复（{@link #isRepeatable()} 返回 false）。</p>
 */
public class BooleanScopeType implements ParameterizedScopeTypeProvider {

    /** 参数化 scope 类型名 */
    public static final String TYPE = "boolean";

    /** {@inheritDoc} 返回 {@link #TYPE} */
    @Override
    public String getTypeName() {
        return TYPE;
    }

    /** {@inheritDoc} 布尔 scope 参数不可重复 */
    @Override
    public boolean isRepeatable() {
        return false;
    }

    /**
     * 校验参数为 true/false；否则抛出 {@link InvalidScopeParameterException}。
     * @param scope 客户端范围模型
     * @param parameter 待校验的参数值
     */
        if (!"true".equalsIgnoreCase(parameter) && !"false".equalsIgnoreCase(parameter)) {
            throw new InvalidScopeParameterException(String.format("'%s' is not a valid boolean, expected 'true' or 'false'", parameter));
        }
    }
}
