package org.keycloak.cache;

import java.util.List;
import java.util.Map;

import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.models.ClientModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderQuery;
import org.keycloak.models.IdentityProviderType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.services.resources.IdentityBrokerService;

import org.jboss.logging.Logger;

import static org.keycloak.models.utils.KeycloakModelUtils.CLIENT_ROLE_SEPARATOR;
import static org.keycloak.models.utils.KeycloakModelUtils.MAX_CLIENT_LOOKUPS_DURING_ROLE_RESOLVE;

/**
 * 默认替代查找提供者：通过 issuer、客户端属性或角色名字符串
 * 解析 IdP、Client 与 Role，并使用本地 Caffeine 缓存加速重复查询。
 */
public class DefaultAlternativeLookupProvider implements AlternativeLookupProvider {

    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(DefaultAlternativeLookupProvider.class);
    /** issuer/属性/角色查找结果缓存。 */
    private final LocalCache<String, CachedValue> lookupCache;

    /** @param lookupCache 共享本地查找缓存 */
    DefaultAlternativeLookupProvider(LocalCache<String, CachedValue> lookupCache) {
        this.lookupCache = lookupCache;
    }

    /** 按 issuer URL 与 IdP 类型查找唯一启用的身份代理，命中缓存时校验仍有效。 */
    @Override
    public IdentityProviderModel lookupIdentityProviderFromIssuer(KeycloakSession session, IdentityProviderType type, String issuerUrl) {
        String alternativeKey = ComputedKey.computeKey(session.getContext().getRealm().getId(), type.toString(), issuerUrl);

        CachedValue cachedIdpAlias = lookupCache.get(alternativeKey);
        if (cachedIdpAlias instanceof CachedValue.CachedString cachedString) {
            IdentityProviderModel idp = session.identityProviders().getByAlias(cachedString.value());
            if (idp != null && issuerUrl.equals(idp.getConfig().get(IdentityProviderModel.ISSUER)) && idp.isEnabled() && isType(session, idp, type)) {
                return idp;
            } else {
                lookupCache.invalidate(alternativeKey);
            }
        }

        List<IdentityProviderModel> idps = session.identityProviders().getAllStream(IdentityProviderQuery.any())
                .filter(i -> issuerUrl.equals(i.getConfig().get(IdentityProviderModel.ISSUER)) && i.isEnabled() && isType(session, i, type))
                .limit(2)
                .toList();
        IdentityProviderModel idp = null;
        if (idps.size() == 1) {
            idp = idps.get(0);
            if (idp.getAlias() != null) {
                lookupCache.put(alternativeKey, CachedValue.ofId(idp.getAlias()));
            }
        } else if (idps.size() > 1) {
            throw new RuntimeException("Multiple IDPs match the same issuer: " + idps.stream().map(IdentityProviderModel::getAlias).toList());
        }

        return idp;
    }

    /** 按客户端自定义属性查找唯一启用客户端。 */
    @Override
    public ClientModel lookupClientFromClientAttributes(KeycloakSession session, Map<String, String> attributes) {
        String alternativeKey = ComputedKey.computeKey(session.getContext().getRealm().getId(), "client", attributes);

        CachedValue cachedClientId = lookupCache.get(alternativeKey);
        if (cachedClientId instanceof CachedValue.CachedString cachedString) {
            ClientModel client = session.clients().getClientByClientId(session.getContext().getRealm(), cachedString.value());
            boolean match = client != null && client.isEnabled();
            if (match) {
                for (Map.Entry<String, String> e : attributes.entrySet()) {
                    if (!e.getValue().equals(client.getAttribute(e.getKey()))) {
                        match = false;
                        break;
                    }
                }
            }
            if (match) {
                return client;
            } else {
                lookupCache.invalidate(alternativeKey);
            }
        }

        ClientModel client = null;
        List<ClientModel> clients = session.clients().searchClientsByAttributes(session.getContext().getRealm(), attributes, null, null)
                .filter(ClientModel::isEnabled)
                .limit(2)
                .toList();
        if (clients.size() == 1) {
            client = clients.get(0);
            lookupCache.put(alternativeKey, CachedValue.ofId(client.getClientId()));
        } else if (clients.size() > 1) {
            throw new RuntimeException("Multiple clients matches attributes");
        }

        return client;
    }

    /** 解析 clientId.roleName 或 realm 角色名，带点分段尝试 client 前缀匹配。 */
    @Override
    public RoleModel lookupRoleFromString(RealmModel realm, String roleName) {
        if (roleName == null) {
            return null;
        }

        var roleModel = findRoleInCache(realm, roleName);
        if (roleModel != null) {
            return roleModel;
        }

        // 按点号从右向左尝试 clientId 前缀，解析 client 角色
        int counter = 0;
        int scopeIndex = roleName.lastIndexOf(CLIENT_ROLE_SEPARATOR);
        while (scopeIndex >= 0 && counter < MAX_CLIENT_LOOKUPS_DURING_ROLE_RESOLVE) {
            counter++;
            String appName = roleName.substring(0, scopeIndex);
            ClientModel client = realm.getClientByClientId(appName);
            if (client != null) {
                return storeClientRoleInCache(client, cachedRoleKey(realm, roleName), roleName.substring(scopeIndex + 1), counter);
            }

            scopeIndex = roleName.lastIndexOf(CLIENT_ROLE_SEPARATOR, scopeIndex - 1);
        }
        if (counter >= MAX_CLIENT_LOOKUPS_DURING_ROLE_RESOLVE) {
            logger.warnf("Not able to retrieve role model from the role name '%s'. Please use shorter role names with the limited amount of dots, roleName", roleName.length() > 100 ? roleName.substring(0, 100) + "..." : roleName);
            return null;
        }

        return storeRealmRoleInCache(realm, roleName);
    }

    /** 无额外资源需释放。 */
    @Override
    public void close() {
    }

    /** 从缓存读取角色限定并校验底层角色仍存在。 */
    private RoleModel findRoleInCache(RealmModel realm, String roleName) {
        var cacheKey = cachedRoleKey(realm, roleName);
        var cachedRole = lookupCache.get(cacheKey);
        if (!(cachedRole instanceof CachedValue.CachedRoleQualifier cachedRoleQualifier)) {
            return null;
        }
        if (cachedRoleQualifier.isRealmRole()) {
            var role = realm.getRole(cachedRoleQualifier.roleName());
            if (role == null) {
                lookupCache.invalidate(cacheKey);
            }
            return role;
        }

        var client = realm.getClientByClientId(cachedRoleQualifier.clientId());
        if (client == null) {
            lookupCache.invalidate(cacheKey);
            return null;
        }

        var role = client.getRole(cachedRoleQualifier.roleName());
        if (role == null) {
            lookupCache.invalidate(cacheKey);
        }
        return role;
    }

    /** 查找 client 角色；仅当点分段数大于 1 时写入缓存（单段已由内部缓存覆盖）。 */
    private RoleModel storeClientRoleInCache(ClientModel client, String cacheKey, String roleName, int dotCount) {
        // dotCount 为 1（client-id.role）时跳过缓存，底层已有内部缓存
        // It means, we have the following format, client-id.role-name.
        // Both realm.getClientByClientId and client.getRole methods already use an internal cache.
        var roleModel = client.getRole(roleName);
        if (roleModel != null && dotCount > 1) {
            lookupCache.put(cacheKey, CachedValue.ofClientRole(client.getClientId(), roleName));
        }
        return roleModel;
    }

    /** 查找 realm 角色并在存在时缓存。 */
    private RoleModel storeRealmRoleInCache(RealmModel realm, String roleName) {
        // 判断 roleName 是否为 realm 级角色
        var roleModel = realm.getRole(roleName);
        if (roleModel != null) {
            // 仅当角色存在时写入缓存
            lookupCache.put(cachedRoleKey(realm, roleName), CachedValue.ofRealmRole(roleName));
        }
        return roleModel;
    }

    /** 生成 realmId 与角色名拼接的缓存键。 */
    private static String cachedRoleKey(RealmModel realm, String roleName) {
        return realm.getId() + roleName;
    }

    /** 校验 IdP 实例是否匹配请求的 {@link IdentityProviderType}。 */
    private static boolean isType(KeycloakSession session, IdentityProviderModel idp, IdentityProviderType type) {
        IdentityProvider provider = IdentityBrokerService.getIdentityProvider(session, idp, IdentityProvider.class);
        return provider != null ? provider.isType(session, type) : false;
    }
}
