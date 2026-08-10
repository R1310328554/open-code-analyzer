package org.keycloak.testframework.realm;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.testframework.annotations.InjectRealm;
import org.keycloak.testframework.injection.AbstractInterceptorHelper;
import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.Registry;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierHelpers;
import org.keycloak.testframework.injection.SupplierOrder;
import org.keycloak.testframework.server.KeycloakServer;
import org.keycloak.testframework.server.KeycloakUrls;
import org.keycloak.util.JsonSerialization;
import org.keycloak.util.Strings;

/**
 * 托管 Realm 的 {@link Supplier} 实现。
 * <p>
 * 可创建全新 Realm、或附着到已有 Realm；支持 JSON 导入、{@link RealmConfig} 配置及
 * {@link RealmConfigInterceptor} 链式拦截，并在测试结束时删除托管 Realm。
 */
public class RealmSupplier implements Supplier<ManagedRealm, InjectRealm> {

    /** {@inheritDoc} 依赖 {@link KeycloakServer}、{@link KeycloakUrls} 与 bootstrap Admin 客户端。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<ManagedRealm, InjectRealm> instanceContext) {
        return DependenciesBuilder.create(KeycloakServer.class)
                .add(KeycloakUrls.class)
                .add(Keycloak.class, "bootstrap-client").build();
    }

    /** {@inheritDoc} 创建或附着 Realm 并返回 {@link ManagedRealm} 封装。 */
    @Override
    public ManagedRealm getValue(InstanceContext<ManagedRealm, InjectRealm> instanceContext) {
        KeycloakServer server = instanceContext.getDependency(KeycloakServer.class);
        Keycloak adminClient = instanceContext.getDependency(Keycloak.class, "bootstrap-client");

        String attachTo = instanceContext.getAnnotation().attachTo();
        boolean managed = attachTo.isEmpty();

        RealmRepresentation realmRepresentation;

        if (managed) {
            RealmBuilder realmBuilder;
            if (!Strings.isEmpty(instanceContext.getAnnotation().fromJson())) {
                try {
                    InputStream jsonStream = instanceContext.getRegistry().getCurrentContext().getRequiredTestClass().getResourceAsStream(instanceContext.getAnnotation().fromJson());
                    if (jsonStream == null) {
                        throw new RuntimeException("Realm JSON representation not found in classpath");
                    }
                    realmBuilder = RealmBuilder.update(JsonSerialization.readValue(jsonStream, RealmRepresentation.class));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                realmBuilder = RealmBuilder.create();
            }

            RealmConfig config = SupplierHelpers.getInstanceWithInjectedFields(instanceContext.getAnnotation().config(), instanceContext);
            realmBuilder = config.configure(realmBuilder);

            RealmConfigInterceptorHelper interceptor = new RealmConfigInterceptorHelper(instanceContext.getRegistry());
            realmBuilder = interceptor.intercept(realmBuilder, instanceContext);

            realmRepresentation = realmBuilder.build();

            if (realmRepresentation.getRealm() == null) {
                realmRepresentation.setRealm(SupplierHelpers.createName(instanceContext));
            }

            if (realmRepresentation.getId() == null) {
                realmRepresentation.setId(realmRepresentation.getRealm());
            }

            adminClient.realms().create(realmRepresentation);

            // TODO 创建 Realm 后需使令牌失效，新 Realm 角色才会出现在令牌中；轻量访问令牌或可解决
            adminClient.tokenManager().invalidate(adminClient.tokenManager().getAccessTokenString());
        } else {
            realmRepresentation = adminClient.realm(attachTo).toRepresentation();
        }

        instanceContext.addNote("managed", managed);

        RealmResource realmResource = adminClient.realm(realmRepresentation.getRealm());
        return new ManagedRealm(server.getBaseUrl() + "/realms/" + realmRepresentation.getRealm(), realmRepresentation, realmResource);
    }

    /** {@inheritDoc} 当 {@code config} 与 {@code fromJson} 均相同时视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<ManagedRealm, InjectRealm> a, RequestedInstance<ManagedRealm, InjectRealm> b) {
        InjectRealm aa = a.getAnnotation();
        InjectRealm ba = b.getAnnotation();
        return aa.config().equals(ba.config()) && aa.fromJson().equals(ba.fromJson());
    }

    /** {@inheritDoc} 删除本 Supplier 创建的托管 Realm（附着模式不删除）。 */
    @Override
    public void close(InstanceContext<ManagedRealm, InjectRealm> instanceContext) {
        if (instanceContext.getNote("managed", Boolean.class)) {
            instanceContext.getValue().admin().remove();
        }
    }

    /** {@inheritDoc} 使用 {@link SupplierOrder#REALM} 顺序。 */
    @Override
    public int order() {
        return SupplierOrder.REALM;
    }

    /** 聚合并调用所有 {@link RealmConfigInterceptor} 实现。 */
    private static class RealmConfigInterceptorHelper extends AbstractInterceptorHelper<RealmConfigInterceptor, RealmBuilder> {

        /** @param registry 测试框架注册表 */
        private RealmConfigInterceptorHelper(Registry registry) {
            super(registry, RealmConfigInterceptor.class);
        }

        /** {@inheritDoc} 若供应器实现拦截器接口则调用其 {@code intercept}。 */
        @Override
        public RealmBuilder intercept(RealmBuilder value, Supplier<?, ?> supplier, InstanceContext<?, ?> existingInstance) {
            if (supplier instanceof RealmConfigInterceptor interceptor) {
                value = interceptor.intercept(value, existingInstance);
            }
            return value;
        }

    }

}
