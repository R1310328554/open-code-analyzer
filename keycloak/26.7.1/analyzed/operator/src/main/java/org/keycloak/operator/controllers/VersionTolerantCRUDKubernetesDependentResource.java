package org.keycloak.operator.controllers;

import java.util.Optional;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceContext;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import io.javaoperatorsdk.operator.processing.event.source.SecondaryToPrimaryMapper;

/**
 * 对 API 版本前缀兼容的 CRUD Kubernetes Dependent Resource 基类。
 *
 * <p>默认 SecondaryToPrimary 映射要求 ownerReference.apiVersion 完全相等；
 * 本类改为 {@code startsWith} 匹配，以便 Keycloak CR 从 v2alpha1 升级到 v2beta1 时
 * 次级资源仍能正确映射到主资源。
 *
 * @param <R> 次级 Kubernetes 资源类型
 * @param <P> 主 CustomResource 类型
 */
public class VersionTolerantCRUDKubernetesDependentResource<R extends HasMetadata, P extends HasMetadata>
        extends CRUDKubernetesDependentResource<R, P> {

    /** 无参构造，由子类或框架注入资源类型。 */
    public VersionTolerantCRUDKubernetesDependentResource() {
        super();
    }

    /** 指定次级资源 Class。 */
    public VersionTolerantCRUDKubernetesDependentResource(Class<R> clazz) {
        super(clazz);
    }

    /**
     * 覆盖默认映射：使用 apiVersion 前缀匹配而非完全相等。
     *
     * @param context 事件源上下文
     */
    @Override
    protected Optional<SecondaryToPrimaryMapper<R>> getSecondaryToPrimaryMapper(EventSourceContext<P> context) {
        return primaryMapper(context);
    }

    /**
     * 构建版本容忍的主资源映射器：按 kind 与 apiVersion 前缀过滤 ownerReference。
     *
     * @param context 事件源上下文
     * @param <R> 次级资源类型
     * @param <P> 主资源类型
     */
    static <R extends HasMetadata, P extends HasMetadata> Optional<SecondaryToPrimaryMapper<R>> primaryMapper(EventSourceContext<P> context) {
        Class<?> primaryClass = context.getPrimaryResourceClass();
        String apiVersion = HasMetadata.getApiVersion(primaryClass);
        String kind = HasMetadata.getKind(primaryClass);
        apiVersion = apiVersion.startsWith("/") ? apiVersion.substring(1) : apiVersion;
        String correctApi = apiVersion.substring(0, apiVersion.indexOf("/") + 1);
        boolean clusterScoped = !Namespaced.class.isAssignableFrom(primaryClass);
        return Optional.of(resource -> resource.getMetadata().getOwnerReferences().stream()
                .filter(owner -> owner.getKind().equals(kind) && owner.getApiVersion().startsWith(correctApi))
                .map(or -> ResourceID.fromOwnerReference(resource, or, clusterScoped)).collect(Collectors.toSet()));
    }

}
