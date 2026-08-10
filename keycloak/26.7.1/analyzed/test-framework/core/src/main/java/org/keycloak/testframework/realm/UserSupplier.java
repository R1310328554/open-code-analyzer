package org.keycloak.testframework.realm;

import java.util.List;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.testframework.annotations.InjectUser;
import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierHelpers;
import org.keycloak.testframework.util.ApiUtil;

/**
 * 托管用户的 {@link Supplier} 实现。
 * <p>
 * 在指定 {@link ManagedRealm} 中创建用户，并在测试结束时通过 Admin API 删除。
 */
public class UserSupplier implements Supplier<ManagedUser, InjectUser> {

    /** 实例上下文中存储用户 UUID 的 note 键。 */
    private static final String USER_UUID_KEY = "userUuid";

    /** {@inheritDoc} 依赖注解 {@code realmRef} 所引用的 {@link ManagedRealm}。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<ManagedUser, InjectUser> instanceContext) {
        return DependenciesBuilder.create(ManagedRealm.class, instanceContext.getAnnotation().realmRef()).build();
    }

    /** {@inheritDoc} 按 {@link UserConfig} 创建用户并包装为 {@link ManagedUser}。 */
    @Override
    public ManagedUser getValue(InstanceContext<ManagedUser, InjectUser> instanceContext) {
        ManagedRealm realm = instanceContext.getDependency(ManagedRealm.class, instanceContext.getAnnotation().realmRef());

        UserConfig config = SupplierHelpers.getInstanceWithInjectedFields(instanceContext.getAnnotation().config(), instanceContext);
        UserRepresentation userRepresentation = config.configure(UserBuilder.create()).build();

        if (userRepresentation.getUsername() == null) {
            String username = SupplierHelpers.createName(instanceContext);
            userRepresentation.setUsername(username);
        }

        if (userRepresentation.getRealmRoles() != null  || userRepresentation.getClientRoles() != null) {
            throw new UnsupportedOperationException("Creating user with roles or client roles is not supported!");
        }

        try (Response response = realm.admin().users().create(userRepresentation)) {
            if (Status.CONFLICT.equals(Status.fromStatusCode(response.getStatus()))) {
                throw new IllegalStateException("User already exist with username: " + userRepresentation.getUsername());
            }
            String uuid = ApiUtil.getCreatedId(response);

            instanceContext.addNote(USER_UUID_KEY, uuid);

            UserResource userResource = realm.admin().users().get(uuid);
            userRepresentation.setId(uuid);

            return new ManagedUser(userRepresentation, userResource);
        }
    }

    /** {@inheritDoc} 仅当 {@code config} 注解值相同时视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<ManagedUser, InjectUser> a, RequestedInstance<ManagedUser, InjectUser> b) {
        return a.getAnnotation().config().equals(b.getAnnotation().config());
    }

    /** {@inheritDoc} 测试结束后删除已创建的用户（忽略已不存在的情况）。 */
    @Override
    public void close(InstanceContext<ManagedUser, InjectUser> instanceContext) {
        try {
            instanceContext.getValue().admin().remove();
        } catch (NotFoundException ex) {}
    }

}
