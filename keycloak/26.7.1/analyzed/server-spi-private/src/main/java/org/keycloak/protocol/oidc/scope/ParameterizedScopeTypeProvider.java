package org.keycloak.protocol.oidc.scope;

import jakarta.annotation.Nonnull;

import org.keycloak.Config;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;

/**
 * 参数化 Client Scope 的类型定义与校验规则。
 * <p>内置类型（string、number、boolean、username）通过 {@link #validateParameter} 校验参数值；仅 {@code custom} 类型使用管理员配置的正则匹配。</p>
 */
public interface ParameterizedScopeTypeProvider extends Provider, ProviderFactory<ParameterizedScopeTypeProvider> {

    /** @return 唯一类型名，亦作为 provider ID */

    String getTypeName();

    /**
     * 是否允许同一参数化 scope 在单次请求中以不同参数值多次出现（如 {@code scope:val1 scope:val2}）。
     *
     * @return {@code true} if multiple parameter values are allowed, {@code false} otherwise
     */
    default boolean isRepeatable() {
        return true;
    }

    /**
     * 请求时校验捕获的参数值（尚无已认证用户）；实现应在校验前规范化参数。
     *
     * @param scope the client scope model, never {@code null}
     * @param parameter the captured parameter value, never {@code null} or empty
     * @throws InvalidScopeParameterException if the parameter is invalid
     */
    void validateParameter(@Nonnull ClientScopeModel scope, @Nonnull String parameter) throws InvalidScopeParameterException;

    /**
     * 已知认证用户时校验参数（code-to-token、刷新、令牌交换等）；默认委托 {@link #validateParameter}。
     *
     * @param currentUser the authenticated user, never {@code null}
     * @param scope the client scope model, never {@code null}
     * @param parameter the captured parameter value, never {@code null} or empty
     * @throws InvalidScopeParameterException if the parameter is invalid for the given user
     */
    default void validateParameterWithUser(@Nonnull UserModel currentUser, @Nonnull ClientScopeModel scope, @Nonnull String parameter) throws InvalidScopeParameterException {
        validateParameter(scope, parameter);
    }

    /** 工厂与提供者合一，返回自身实例。 */
    @Override
    default ParameterizedScopeTypeProvider create(KeycloakSession session) {
        return this;
    }

    /** @return 与 {@link #getTypeName()} 相同的 provider ID */
    @Override
    default String getId() {
        return getTypeName();
    }

    @Override
    default void init(Config.Scope config) {
    }

    @Override
    default void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    default void close() {
    }
}
