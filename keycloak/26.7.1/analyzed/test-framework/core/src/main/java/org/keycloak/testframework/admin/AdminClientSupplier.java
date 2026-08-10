package org.keycloak.testframework.admin;

import java.util.List;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.testframework.FatalTestClassException;
import org.keycloak.testframework.annotations.InjectAdminClient;
import org.keycloak.testframework.config.Config;
import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.realm.ManagedRealm;
import org.keycloak.testframework.realm.ManagedUser;

/**
 * 注入已配置的 {@link Keycloak} Admin 客户端，支持 BOOTSTRAP（master 管理员）与 MANAGED_REALM 模式。
 */
public class AdminClientSupplier implements Supplier<Keycloak, InjectAdminClient> {

    /** MANAGED_REALM 模式下额外依赖 ManagedRealm。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<Keycloak, InjectAdminClient> instanceContext) {
        DependenciesBuilder builder = DependenciesBuilder.create(AdminClientFactory.class);
        if (instanceContext.getAnnotation().mode().equals(InjectAdminClient.Mode.MANAGED_REALM)) {
            builder.add(ManagedRealm.class, instanceContext.getAnnotation().realmRef());
        }
        return builder.build();
    }

    /** 按 {@link InjectAdminClient.Mode} 组装 grant、realm、client 与可选用户凭据。 */
    @Override
    public Keycloak getValue(InstanceContext<Keycloak, InjectAdminClient> instanceContext) {
        InjectAdminClient annotation = instanceContext.getAnnotation();

        InjectAdminClient.Mode mode = annotation.mode();

        AdminClientBuilder adminBuilder = instanceContext.getDependency(AdminClientFactory.class).create()
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS);

        if (mode.equals(InjectAdminClient.Mode.BOOTSTRAP)) {
            adminBuilder.realm("master").clientId(Config.getAdminClientId()).clientSecret(Config.getAdminClientSecret());
        } else if (mode.equals(InjectAdminClient.Mode.MANAGED_REALM)) {
            ManagedRealm managedRealm = instanceContext.getDependency(ManagedRealm.class, instanceContext.getAnnotation().realmRef());
            adminBuilder.realm(managedRealm.getName());

            String clientId = !annotation.client().isEmpty() ? annotation.client() : null;
            String userId = !annotation.user().isEmpty() ? annotation.user() : null;

            if (clientId == null) {
                throw new FatalTestClassException("Client is required when using admin client in managed realm mode");
            }

            RealmRepresentation realmRep = managedRealm.getCreatedRepresentation();
            ClientRepresentation clientRep = realmRep.getClients().stream()
                    .filter(c -> c.getClientId().equals(annotation.client()))
                    .findFirst().orElseThrow(() -> new FatalTestClassException("Client with clientId=\"" + annotation.client() + "\" not found in realm with ref=\"" + annotation.realmRef() + "\""));

            adminBuilder.clientId(clientId).clientSecret(clientRep.getSecret());

            if (userId != null) {
                UserRepresentation userRep = realmRep.getUsers().stream()
                        .filter(u -> u.getUsername().equals(annotation.user()))
                        .findFirst().orElseThrow(() -> new FatalTestClassException("User with username=\"" + annotation.user() + "\" not found in realm with ref=\"" + annotation.realmRef() + "\""));
                String password = ManagedUser.getPassword(userRep);
                adminBuilder.username(userRep.getUsername()).password(password);
                adminBuilder.grantType(OAuth2Constants.PASSWORD);
            }
        }

        return adminBuilder.build();
    }

    /** Admin 客户端默认 GLOBAL 生命周期。 */
    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    /** 客户端实例始终兼容复用。 */
    @Override
    public boolean compatible(InstanceContext<Keycloak, InjectAdminClient> a, RequestedInstance<Keycloak, InjectAdminClient> b) {
        return true;
    }

    /** 关闭注入的 Keycloak 客户端。 */
    @Override
    public void close(InstanceContext<Keycloak, InjectAdminClient> instanceContext) {
        instanceContext.getValue().close();
    }

}
