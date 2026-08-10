package org.keycloak.broker.provider.util;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.broker.provider.ClientAssertionIdentityProvider;
import org.keycloak.broker.provider.ExchangeExternalToken;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.JWTAuthorizationGrantProvider;
import org.keycloak.broker.provider.TrustMaterialIdentityProvider;
import org.keycloak.broker.provider.UserAuthenticationIdentityProvider;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.models.IdentityProviderCapability;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * 身份提供方类型工具：根据提供者类或工厂 ID 推断 {@link IdentityProviderType}，并按能力筛选工厂。
 */
public class IdentityProviderTypeUtil {

    private IdentityProviderTypeUtil() {
    }

    /** 根据运行时提供者实例推断其 {@link IdentityProviderType} 列表。 */
    public static List<IdentityProviderType> listTypesFromProvider(KeycloakSession session, IdentityProvider provider) {
        return listTypesFromClass(provider.getClass());
    }

    /** 根据工厂 ID 解析提供者类型并推断 {@link IdentityProviderType}。 */
    public static List<IdentityProviderType> listTypesFromFactory(KeycloakSession session, String factoryId) {
        KeycloakSessionFactory sf = session.getKeycloakSessionFactory();
        ProviderFactory<?> factory = sf.getProviderFactory(IdentityProvider.class, factoryId);
        if (factory == null) {
            return List.of();
        }
        Class<? extends IdentityProvider> providerType = getType(factory);
        return listTypesFromClass(providerType);
    }

    /** 列出声明给定 {@link IdentityProviderCapability} 的全部 IdP 工厂 ID。 */
    public static List<String> listFactoriesByCapability(KeycloakSession session, IdentityProviderCapability capability) {
        Set<IdentityProviderType> types = Arrays.stream(IdentityProviderType.values()).filter(t -> t.getCapabilities().contains(capability)).collect(Collectors.toSet());
        return listFactoriesByTypes(session, types);
    }

    /** 列出实现指定 {@link IdentityProviderType} 的全部工厂 ID。 */
    public static List<String> listFactoriesByType(KeycloakSession session, IdentityProviderType type) {
        return listFactoriesByTypes(session, Set.of(type));
    }

    /** 按类继承关系匹配 {@link IdentityProviderType} 枚举。 */
    private static List<IdentityProviderType> listTypesFromClass(Class<? extends IdentityProvider> providerType) {
        return Arrays.stream(IdentityProviderType.values())
                .filter(t -> !t.equals(IdentityProviderType.ANY) && toTypeClass(t).isAssignableFrom(providerType))
                .collect(Collectors.toList());
    }

    /** 合并 IdentityProvider 与 SocialIdentityProvider 工厂并按类型过滤。 */
    private static List<String> listFactoriesByTypes(KeycloakSession session, Set<IdentityProviderType> types) {
        KeycloakSessionFactory sf = session.getKeycloakSessionFactory();

        Stream<ProviderFactory> factories = sf.getProviderFactoriesStream(IdentityProvider.class);
        if (types.contains(IdentityProviderType.ANY) || types.contains(IdentityProviderType.USER_AUTHENTICATION) || types.contains(IdentityProviderType.JWT_AUTHORIZATION_GRANT)) {
            factories = Stream.concat(factories, sf.getProviderFactoriesStream(SocialIdentityProvider.class));
        }

        Set<Class<?>> typeClasses = types.stream().map(IdentityProviderTypeUtil::toTypeClass).collect(Collectors.toSet());

        return factories.filter(f -> typeClasses.stream().anyMatch(t -> t.isAssignableFrom(getType(f))))
                .map(ProviderFactory::getId)
                .toList();
    }

    /** 通过工厂 {@code create} 方法返回类型推断提供者实现类。 */
    private static Class<? extends IdentityProvider> getType(ProviderFactory<?> f) {
        try {
            return (Class<? extends IdentityProvider>) f.getClass().getMethod("create", KeycloakSession.class, IdentityProviderModel.class).getReturnType();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /** 将 {@link IdentityProviderType} 映射到对应标记接口类。 */
    private static Class<?> toTypeClass(IdentityProviderType type) {
        return switch (type) {
            case USER_AUTHENTICATION -> UserAuthenticationIdentityProvider.class;
            case CLIENT_ASSERTION -> ClientAssertionIdentityProvider.class;
            case TRUST_MATERIAL -> TrustMaterialIdentityProvider.class;
            case EXCHANGE_EXTERNAL_TOKEN -> ExchangeExternalToken.class;
            case JWT_AUTHORIZATION_GRANT -> JWTAuthorizationGrantProvider.class;
            case ANY -> IdentityProvider.class;
        };
    }

}
