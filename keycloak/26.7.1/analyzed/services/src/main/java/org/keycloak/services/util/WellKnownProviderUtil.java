package org.keycloak.services.util;

import java.util.Comparator;
import java.util.Optional;

import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.wellknown.WellKnownProvider;
import org.keycloak.wellknown.WellKnownProviderFactory;

/**
 * Well Known Provider 工厂解析工具类。
 * <p>根据 alias 从 {@link KeycloakSessionFactory} 中查找 {@link WellKnownProviderFactory}，
 * 多个工厂共享 alias 时取 priority 最低者。</p>
 *
 * This is a utility class and is not intended to be instantiated.
 */
public class WellKnownProviderUtil {

    private WellKnownProviderUtil() {
        // 工具类禁止实例化
    }

    /**
     * 按 alias 解析 {@link WellKnownProviderFactory}。
     * <p>多个工厂共享同一 alias 时，选择 priority 数值最小（优先级最高）的实例。</p>
     *
     * @param sessionFactory 用于获取 Provider 工厂的会话工厂
     * @param alias 目标工厂 alias；为 null 时返回空 {@link Optional}
     * @return 解析到的工厂，未找到时为空
     */
    public static Optional<WellKnownProviderFactory> resolveFromAlias(KeycloakSessionFactory sessionFactory, String alias) {

        if (alias == null) {
            return Optional.empty();
        }

        return sessionFactory.getProviderFactoriesStream(WellKnownProvider.class)
                .map(providerFactory -> (WellKnownProviderFactory) providerFactory)
                .filter(wellKnownProviderFactory -> alias.equals(wellKnownProviderFactory.getAlias()))
                .min(Comparator.comparingInt(WellKnownProviderFactory::getPriority));
    }
}
