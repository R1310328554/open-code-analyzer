package org.keycloak.operator.controllers;

import java.util.Optional;
import java.util.UUID;

import org.keycloak.operator.Constants;
import org.keycloak.operator.Utils;
import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;
import org.keycloak.operator.crds.v2beta1.deployment.spec.BootstrapAdminSpec;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceContext;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.api.reconciler.dependent.GarbageCollected;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;
import io.javaoperatorsdk.operator.processing.event.source.SecondaryToPrimaryMapper;

@KubernetesDependent(
        informer = @Informer(labelSelector = Constants.DEFAULT_LABELS_AS_STRING)
)
/**
 * 初始管理员 Secret 依赖资源：在 Operator 未指定自定义管理员 Secret 时自动创建临时凭据。
 */
public class KeycloakAdminSecretDependentResource extends KubernetesDependentResource<Secret, Keycloak> implements Creator<Secret, Keycloak>, GarbageCollected<Keycloak> {

    /** 启用条件：仅当未配置自定义管理员 Secret 时创建。 */
    public static class EnabledCondition implements Condition<Secret, Keycloak> {
        @Override
        public boolean isMet(DependentResource<Secret, Keycloak> dependentResource, Keycloak primary,
                Context<Keycloak> context) {
            return !hasCustomAdminSecret(primary);
        }
    }

    public KeycloakAdminSecretDependentResource() {
        super(Secret.class);
    }

    @Override
    /** 构建包含临时管理员用户名与随机密码的 Secret。 */
    protected Secret desired(Keycloak primary, Context<Keycloak> context) {
        return new SecretBuilder()
                .withNewMetadata()
                .withName(getName(primary))
                .addToLabels(Utils.allInstanceLabels(primary))
                .withNamespace(primary.getMetadata().getNamespace())
                .endMetadata()
                .withType("kubernetes.io/basic-auth")
                .addToData("username", Utils.asBase64("temp-admin"))
                .addToData("password", Utils.asBase64(UUID.randomUUID().toString().replace("-", "")))
                .build();
    }

    /** 返回初始管理员 Secret 的资源名称。 */
    public static String getName(Keycloak keycloak) {
        return KubernetesResourceUtil.sanitizeName(keycloak.getMetadata().getName() + "-initial-admin");
    }

    /** 判断用户是否指定了与 Operator 默认名称不同的自定义管理员 Secret。 */
    public static boolean hasCustomAdminSecret(Keycloak keycloak) {
        return Optional.ofNullable(keycloak.getSpec().getBootstrapAdminSpec()).map(BootstrapAdminSpec::getUser)
                .map(BootstrapAdminSpec.User::getSecret).filter(s -> !s.equals(KeycloakAdminSecretDependentResource.getName(keycloak))).isPresent();
    }
    
    @Override
    protected Optional<SecondaryToPrimaryMapper<Secret>> getSecondaryToPrimaryMapper(
            EventSourceContext<Keycloak> context) {
        return VersionTolerantCRUDKubernetesDependentResource.primaryMapper(context);
    }

}
