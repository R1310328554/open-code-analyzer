package org.keycloak.testframework.realm;

import java.util.List;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.testframework.annotations.InjectClient;
import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierHelpers;
import org.keycloak.testframework.util.ApiUtil;

/**
 * 托管 OAuth/OIDC 客户端的 {@link Supplier} 实现。
 * <p>
 * 在指定 {@link ManagedRealm} 中创建客户端，并在测试结束时通过 Admin API 删除。
 */
public class ClientSupplier implements Supplier<ManagedClient, InjectClient> {

    /** {@inheritDoc} 依赖注解 {@code realmRef} 所引用的 {@link ManagedRealm}。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<ManagedClient, InjectClient> instanceContext) {
        return DependenciesBuilder.create(ManagedRealm.class, instanceContext.getAnnotation().realmRef()).build();
    }

    /** {@inheritDoc} 按配置创建客户端并包装为 {@link ManagedClient}。 */
    @Override
    public ManagedClient getValue(InstanceContext<ManagedClient, InjectClient> instanceContext) {
        ManagedRealm realm = instanceContext.getDependency(ManagedRealm.class, instanceContext.getAnnotation().realmRef());

        ClientConfig config = SupplierHelpers.getInstanceWithInjectedFields(instanceContext.getAnnotation().config(), instanceContext);
        ClientRepresentation clientRepresentation = config.configure(ClientBuilder.create()).build();

        if (clientRepresentation.getClientId() == null) {
            clientRepresentation.setClientId(SupplierHelpers.createName(instanceContext));
        }

        Response response = realm.admin().clients().create(clientRepresentation);
        if (Status.CONFLICT.equals(Status.fromStatusCode(response.getStatus()))) {
            throw new IllegalStateException("Client already exist with client id: " + clientRepresentation.getClientId());
        }
        clientRepresentation.setId(ApiUtil.getCreatedId(response));

        ClientResource clientResource = realm.admin().clients().get(clientRepresentation.getId());
        return new ManagedClient(clientRepresentation, clientResource);
    }

    /** {@inheritDoc} 仅当 {@code config} 注解值相同时视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<ManagedClient, InjectClient> a, RequestedInstance<ManagedClient, InjectClient> b) {
        return a.getAnnotation().config().equals(b.getAnnotation().config());
    }

    /** {@inheritDoc} 测试结束后删除已创建的客户端（忽略已不存在的情况）。 */
    @Override
    public void close(InstanceContext<ManagedClient, InjectClient> instanceContext) {
        try {
            instanceContext.getValue().admin().remove();
        } catch (NotFoundException ex) {}
    }

}
