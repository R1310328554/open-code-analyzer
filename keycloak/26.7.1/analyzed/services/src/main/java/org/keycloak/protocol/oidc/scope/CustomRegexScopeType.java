package org.keycloak.protocol.oidc.scope;

import jakarta.annotation.Nonnull;

import org.keycloak.models.ClientScopeModel;
import org.keycloak.saml.common.util.StringUtil;

/**
 * 自定义正则参数化 scope 类型：参数须匹配客户端范围上配置的 {@code parameterizedScopeRegexp}。
 */
public class CustomRegexScopeType implements ParameterizedScopeTypeProvider {

    /** 参数化 scope 类型名 */
    public static final String TYPE = "custom";

    /** {@inheritDoc} 返回 {@link #TYPE} */
    @Override
    public String getTypeName() {
        return TYPE;
    }

    /**
     * 校验参数匹配 scope 上的正则；未配置正则或匹配失败时抛出异常。
     * @param scope 含正则配置的客户端范围
     * @param parameter 待校验的参数值
     */
        String regexp = scope.getParameterizedScopeRegexp();
        if (StringUtil.isNullOrEmpty(regexp)) {
            throw new InvalidScopeParameterException("custom scope type requires a regex pattern");
        }
        if (!parameter.matches(regexp)) {
            throw new InvalidScopeParameterException("does not match pattern '" + regexp + "'");
        }
    }
}
