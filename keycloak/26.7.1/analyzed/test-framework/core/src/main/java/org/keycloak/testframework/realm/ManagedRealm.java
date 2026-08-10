package org.keycloak.testframework.realm;

import java.util.List;

import org.keycloak.admin.client.resource.ComponentResource;
import org.keycloak.admin.client.resource.IdentityProviderResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientPoliciesRepresentation;
import org.keycloak.representations.idm.ClientPolicyRepresentation;
import org.keycloak.representations.idm.ClientProfileRepresentation;
import org.keycloak.representations.idm.ClientProfilesRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.testframework.injection.ManagedTestResource;
import org.keycloak.testframework.util.ApiUtil;

import org.junit.jupiter.api.Assertions;

/**
 * 测试框架创建的托管 Keycloak Realm 封装。
 * <p>
 * 提供 Realm 基础 URL、Admin API、带自动回滚的配置/用户/客户端变更，以及测试结束清理。
 */
public class ManagedRealm extends ManagedTestResource {

    /** Realm 对外访问基础 URL。 */
    private final String baseUrl;
    /** 创建 Realm 时使用的表示对象。 */
    private final RealmRepresentation createdRepresentation;
    /** 对应 Realm 的 Admin REST 资源。 */
    private final RealmResource realmResource;
    /** 懒加载缓存的 Realm UUID。 */
    private String realmId;

    /** 懒初始化的 Realm 级清理任务收集器。 */
    private ManagedRealmCleanup cleanup;

    /**
     * @param baseUrl Realm 基础 URL
     * @param createdRepresentation 已创建的 Realm 表示
     * @param realmResource Admin Realm 资源
     */
    public ManagedRealm(String baseUrl, RealmRepresentation createdRepresentation, RealmResource realmResource) {
        this.baseUrl = baseUrl;
        this.createdRepresentation = createdRepresentation;
        this.realmResource = realmResource;
    }

    /**
     * 返回 Realm 基础 URL（例如 <code>http://localhost:8080/realms/myrealm</code>）。
     *
     * @return Realm 基础 URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * 返回 Realm 内部 UUID。
     *
     * @return Realm UUID
     */
    public String getId() {
        if (realmId == null && createdRepresentation.getId() != null) {
            realmId = createdRepresentation.getId();
        } else {
            realmId = admin().toRepresentation().getId();
        }
        return realmId;
    }

    /**
     * 返回 Realm 名称。
     *
     * @return Realm 名称
     */
    public String getName() {
        return createdRepresentation.getRealm();
    }

    /**
     * 返回用于查看或修改 Realm 配置的 Admin 资源。
     * <p>
     * 一般应优先使用 {@link #updateWithCleanup} 等带清理的更新方法。
     *
     * @return Realm Admin 资源
     */
    public RealmResource admin() {
        return realmResource;
    }

    /**
     * 返回创建 Realm 时使用的表示对象。
     *
     * @return Realm 表示
     */
    public RealmRepresentation getCreatedRepresentation() {
        return createdRepresentation;
    }

    /**
     * 在测试内更新 Realm 配置，测试结束后自动还原。
     *
     * @param updates 要应用的 Realm 变更
     */
    public void updateWithCleanup(RealmUpdate... updates) {
        RealmRepresentation rep = admin().toRepresentation();
        cleanup().resetToOriginalRepresentation(rep);

        RealmBuilder configBuilder = RealmBuilder.update(rep);
        for (RealmUpdate update : updates) {
            configBuilder = update.update(configBuilder);
        }

        admin().update(configBuilder.build());
    }

    /**
     * 向 Realm 添加用户，测试结束后自动删除。
     *
     * @param user 用户构建器
     */
    public void addUser(UserBuilder user) {
        UserRepresentation rep = user.build();
        String id = ApiUtil.getCreatedId(realmResource.users().create(rep));
        cleanup().add(r -> r.users().get(id).remove());
    }

    /**
     * 更新 Realm 内指定用户，测试结束后自动还原。
     *
     * @param username 要更新的用户名
     * @param update 用户变更
     */
    public void updateUserWithCleanup(String username, ManagedUser.UserUpdate update) {
        List<UserRepresentation> result = realmResource.users().search(username);
        Assertions.assertEquals(1, result.size());

        UserRepresentation original = result.get(0);
        UserBuilder updatedUser = UserBuilder.update(RepresentationUtils.clone(original));
        UserRepresentation updated = update.update(updatedUser).build();

        realmResource.users().get(original.getId()).update(updated);

        cleanup().add(r -> r.users().get(original.getId()).update(original));
    }

    /**
     * 更新 Realm 内指定客户端，测试结束后自动还原。
     *
     * @param clientId 目标 clientId
     * @param update 客户端变更
     */
    public void updateClientWithCleanup(String clientId, ManagedClient.ClientUpdate update) {
        List<ClientRepresentation> result = realmResource.clients().findByClientId(clientId);
        Assertions.assertEquals(1, result.size());

        ClientRepresentation original = result.get(0);
        ClientBuilder updatedClient = ClientBuilder.update(RepresentationUtils.clone(original));
        ClientRepresentation updated = update.update(updatedClient).build();

        realmResource.clients().get(original.getId()).update(updated);

        cleanup().add(r -> r.clients().get(original.getId()).update(original));
    }

    /**
     * 更新 Realm 内身份提供者，测试结束后自动还原。
     *
     * @param alias IdP 别名
     * @param update IdP 变更
     */
    public void updateIdentityProvider(String alias, IdentityProviderUpdate update) {
        IdentityProviderResource resource = realmResource.identityProviders().get(alias);

        IdentityProviderRepresentation original = resource.toRepresentation();
        IdentityProviderRepresentation updated = RepresentationUtils.clone(original);
        update.update(updated);
        resource.update(updated);

        cleanup().add(r -> r.identityProviders().get(alias).update(original));
    }

    /**
     * 更新客户端作用域，测试结束后自动还原。
     *
     * @param id 客户端作用域 id
     * @param update 作用域变更
     */
    public void updateClientScope(String id, ClientScopeUpdate update) {
        ClientScopeRepresentation original = realmResource.clientScopes().get(id).toRepresentation();

        ClientScopeBuilder updatedRep = ClientScopeBuilder.update(RepresentationUtils.clone(original));
        ClientScopeRepresentation updated = update.update(updatedRep).build();
        realmResource.clientScopes().get(id).update(updated);

        cleanup().add(r -> r.clientScopes().get(id).update(original));
    }

    /**
     * 更新客户端 Profile 策略集合，测试结束后还原。
     *
     * @param profiles 新的 Profile 列表
     */
    public void updateClientProfile(List<ClientProfileRepresentation> profiles) {
        ClientProfilesRepresentation oldProfiles = realmResource.clientPoliciesProfilesResource().getProfiles(true);
        ClientProfilesRepresentation profilesToUpdate = realmResource.clientPoliciesProfilesResource().getProfiles(true);
        profilesToUpdate.setProfiles(profiles);
        realmResource.clientPoliciesProfilesResource().updateProfiles(profilesToUpdate);
        cleanup().add(r -> r.clientPoliciesProfilesResource().updateProfiles(oldProfiles));
    }

    /**
     * 更新客户端 Policy 策略集合，测试结束后还原。
     *
     * @param policies 新的 Policy 列表
     */
    public void updateClientPolicy(List<ClientPolicyRepresentation> policies) {
        ClientPoliciesRepresentation oldPolicies = realmResource.clientPoliciesPoliciesResource().getPolicies();
        ClientPoliciesRepresentation policiesToUpdate = realmResource.clientPoliciesPoliciesResource().getPolicies();
        policiesToUpdate.setPolicies(policies);
        realmResource.clientPoliciesPoliciesResource().updatePolicies(policiesToUpdate);
        cleanup().add(r -> r.clientPoliciesPoliciesResource().updatePolicies(oldPolicies));
    }

    /**
     * 更新 Realm 内组件，测试结束后自动还原。
     *
     * @param id 组件 id
     * @param update 组件变更
     */
    public void updateComponent(String id, ComponentUpdate update) {
        ComponentResource componentResource = realmResource.components().component(id);

        ComponentRepresentation original = componentResource.toRepresentation();
        ComponentRepresentation updated = RepresentationUtils.clone(original);
        update.update(updated);
        componentResource.update(updated);

        cleanup().add(r -> r.components().component(id).update(original));
    }

    /** 返回（必要时创建）Realm 级清理任务收集器。 */
    public ManagedRealmCleanup cleanup() {
        if (cleanup == null) {
            cleanup = new ManagedRealmCleanup();
        }
        return cleanup;
    }

    /** {@inheritDoc} 执行已注册的 Realm 清理任务。 */
    @Override
    public void runCleanup() {
        if (cleanup != null) {
            cleanup.runCleanupTasks(realmResource);
            cleanup = null;
        }
    }

    /** 描述对 {@link RealmBuilder} 的单次变更。 */
    public interface RealmUpdate {

        /**
         * 应用变更并返回更新后的构建器。
         *
         * @param realm 当前 Realm 构建器
         * @return 变更后的构建器
         */
        RealmBuilder update(RealmBuilder realm);

    }

    /** 描述对 {@link ClientScopeBuilder} 的单次变更。 */
    public interface ClientScopeUpdate {

        /**
         * 应用变更并返回更新后的构建器。
         *
         * @param scope 当前客户端作用域构建器
         * @return 变更后的构建器
         */
        ClientScopeBuilder update(ClientScopeBuilder scope);

    }

    /** 就地修改 {@link IdentityProviderRepresentation} 的变更回调。 */
    public interface IdentityProviderUpdate {

        /**
         * 应用 IdP 表示变更。
         *
         * @param rep 可变的 IdP 表示
         */
        void update(IdentityProviderRepresentation rep);

    }

    /** 就地修改 {@link ComponentRepresentation} 的变更回调。 */
    public interface ComponentUpdate {

        /**
         * 应用组件表示变更。
         *
         * @param rep 可变的组件表示
         */
        void update(ComponentRepresentation rep);

    }



}
