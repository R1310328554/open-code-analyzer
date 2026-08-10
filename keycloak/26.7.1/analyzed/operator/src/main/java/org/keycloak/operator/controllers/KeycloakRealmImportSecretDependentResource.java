package org.keycloak.operator.controllers;

import org.keycloak.operator.Constants;
import org.keycloak.operator.Utils;
import org.keycloak.operator.crds.v2beta1.realmimport.KeycloakRealmImport;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

@KubernetesDependent(
        informer = @Informer(labelSelector = Constants.DEFAULT_LABELS_AS_STRING)
)
/**
 * Realm 导入 Secret 依赖资源：将 Realm JSON 序列化后存入 Secret 供导入 Job 挂载。
 */
public class KeycloakRealmImportSecretDependentResource extends VersionTolerantCRUDKubernetesDependentResource<Secret, KeycloakRealmImport> {

    /** 依赖资源名称，供 Workflow 引用。 */
    public static final String DEPENDENT_NAME = "realm-import-secret";

    public KeycloakRealmImportSecretDependentResource() {
        super(Secret.class);
    }

    @Override
    /** 将 Realm 定义序列化为 JSON 并写入 Secret 的 data 字段。 */
    protected Secret desired(KeycloakRealmImport primary, Context<KeycloakRealmImport> context) {
        var fileName = primary.getRealmName() + "-realm.json";
        var content = context.getClient().getKubernetesSerialization().asJson(primary.getSpec().getRealm());

        return new SecretBuilder()
                .withNewMetadata()
                .withName(getSecretName(primary))
                .withNamespace(primary.getMetadata().getNamespace())
                // 标签标识 Realm 导入实例，而非 Keycloak 部署本身
                .addToLabels(Utils.allInstanceLabels(primary))
                .endMetadata()
                .addToData(fileName, Utils.asBase64(content))
                .build();
    }

    /** 根据 Keycloak CR 名称与 Realm 名称生成 Secret 名称。 */
    public static String getSecretName(KeycloakRealmImport realmCR) {
        return KubernetesResourceUtil.sanitizeName(realmCR.getSpec().getKeycloakCRName() + "-" + realmCR.getRealmName() + "-realm");
    }

}
