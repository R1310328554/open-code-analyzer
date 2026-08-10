/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.operator.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.api.AdminApi;
import org.keycloak.admin.api.client.ClientApi;
import org.keycloak.admin.client.ClientBuilderWrapper;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.operator.Config;
import org.keycloak.operator.Constants;
import org.keycloak.operator.Utils;
import org.keycloak.operator.crds.v2alpha1.client.KeycloakClientSpec;
import org.keycloak.operator.crds.v2alpha1.client.KeycloakClientStatus;
import org.keycloak.operator.crds.v2alpha1.client.KeycloakClientStatusBuilder;
import org.keycloak.operator.crds.v2alpha1.client.KeycloakClientStatusCondition;
import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;
import org.keycloak.operator.crds.v2beta1.deployment.KeycloakStatusAggregator;
import org.keycloak.operator.crds.v2beta1.deployment.KeycloakStatusCondition;
import org.keycloak.operator.crds.v2beta1.deployment.spec.FeatureSpec;
import org.keycloak.operator.crds.v2beta1.deployment.spec.HttpSpec;
import org.keycloak.representations.admin.v2.BaseClientRepresentation;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.CertUtils;
import io.javaoperatorsdk.operator.api.reconciler.Cleaner;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.ErrorStatusUpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.quarkus.logging.Log;
import org.apache.http.conn.ssl.NoopHostnameVerifier;

import static org.keycloak.operator.crds.v2beta1.CRDUtils.isTlsConfigured;

/**
 * 客户端控制器基类：通过 Admin API v2 创建/更新/删除 Keycloak 客户端。
 *
 * @param <R> 自定义资源类型
 * @param <T> 客户端服务端表示类型
 * @param <S> CR Spec 中的客户端表示类型
 */
public abstract class KeycloakClientBaseController<R extends CustomResource<? extends KeycloakClientSpec<S>, KeycloakClientStatus>, T extends BaseClientRepresentation, S extends BaseClientRepresentation>
        implements Reconciler<R>, Cleaner<R> {

    /** client-admin-api:v2 功能开关名称。 */
    public static final String CLIENT_ADMIN_API_V2 = "client-admin-api:v2";
    private static final String HTTPS = "https";

    /** 客户端 CR 状态聚合器：维护条件与哈希。 */
    static class KeycloakClientStatusAggregator {
        Long generation;
        KeycloakClientStatus existingStatus;
        Map<String, KeycloakClientStatusCondition> existingConditions;
        Map<String, KeycloakClientStatusCondition> newConditions = new LinkedHashMap<String, KeycloakClientStatusCondition>();
        String uuid;

        KeycloakClientStatusAggregator(CustomResource<?, KeycloakClientStatus> resource) {
            this.generation = resource.getMetadata().getGeneration();
            this.existingStatus = Optional.ofNullable(resource.getStatus()).orElse(new KeycloakClientStatus());
            existingConditions = KeycloakStatusAggregator.getConditionMap(existingStatus.getConditions());
        }

        void setCondition(String type, Boolean status, String message) {
            KeycloakClientStatusCondition condition = new KeycloakClientStatusCondition();
            condition.setType(type);
            condition.setStatus(status);
            condition.setMessage(message);
            condition.setObservedGeneration(generation);
            newConditions.put(type, condition); // 当前尚未做条件合并
        }

        void setUuid(String uuid) {
            this.uuid = uuid;
        }

        KeycloakClientStatus build() {
            KeycloakClientStatusBuilder statusBuilder = new KeycloakClientStatusBuilder();
            String now = Utils.iso8601Now();
            statusBuilder.withObservedGeneration(generation);
            newConditions.values().forEach(c -> KeycloakStatusAggregator.updateConditionFromExisting(c, existingConditions, now));
            existingConditions.putAll(newConditions);
            existingConditions.computeIfAbsent(KeycloakStatusCondition.HAS_ERRORS,
                    k -> new KeycloakClientStatusCondition(KeycloakStatusCondition.HAS_ERRORS, false, null, now,
                            generation));
            statusBuilder.withConditions(new ArrayList<>(existingConditions.values().stream().sorted(Comparator.comparing(KeycloakClientStatusCondition::getType)).toList()));
            statusBuilder.withUuid(uuid);
            return statusBuilder.build();
        }

        public KeycloakClientStatus getExistingStatus() {
            return existingStatus;
        }

    }

    @Inject
    Config config;

    @Override
    /** 协调客户端 CR：校验功能开关、调用 Admin API 并更新状态。 */
    public UpdateControl<R> reconcile(R resource, Context<R> context) throws Exception {
        String kcName = resource.getSpec().getKeycloakCRName();

        // TODO: 应通过 Informer 获取，而非每次查询
        // 控制器间无法直接共享 Informer，需注入 KeycloakController 并访问其保存的 context
        Keycloak keycloak = context.getClient().resources(Keycloak.class)
                .inNamespace(resource.getMetadata().getNamespace()).withName(kcName).require();

        KeycloakClientStatusAggregator statusAggregator = new KeycloakClientStatusAggregator(resource);

        if (!hasFeatureEnabled(keycloak)) {
            statusAggregator.setCondition(KeycloakClientStatusCondition.HAS_ERRORS, Boolean.TRUE, "Cannot create/update because the server does not have %s enabled".formatted(CLIENT_ADMIN_API_V2));
            resource.setStatus(statusAggregator.build());
            return UpdateControl.patchStatus(resource);
        }

        S client = resource.getSpec().getClient();
        // 先转换为服务端目标表示——Spec 表示是特化类型
        var map = context.getClient().getKubernetesSerialization().convertValue(client, Map.class);
        map.put(BaseClientRepresentation.DISCRIMINATOR_FIELD, client.getProtocol());
        T rep = context.getClient().getKubernetesSerialization().convertValue(map, getTargetRepresentation());
        // 再由子类执行特定预处理
        boolean poll = prepareRepresentation(client, rep, context);
        rep.setClientId(resource.getMetadata().getName());

        String hash = Utils.hash(List.of(rep));

        if (!hash.equals(statusAggregator.getExistingStatus().getHash())) {
            var response = invoke(resource, context, keycloak, clientApi -> {
                return clientApi.createOrUpdateClient(rep);
            });

            if (response.getStatus() == HttpURLConnection.HTTP_OK || response.getStatus() == HttpURLConnection.HTTP_CREATED) {
                BaseClientRepresentation resultingRep = response.readEntity(BaseClientRepresentation.class);
                statusAggregator.setUuid(resultingRep.getUuid());
            } else {
                // 非成功响应则抛异常进入重试循环
                // TODO：并非所有错误都应每 10 秒重试，校验失败应写入状态
                String message = response.hasEntity() ? response.readEntity(String.class) : "";
                throw new RuntimeException("Client update operation not sucessful with status code " + response.getStatus() + " : " + message);
            }
        }

        statusAggregator.setCondition(KeycloakClientStatusCondition.HAS_ERRORS, false, null);
        KeycloakClientStatus status = statusAggregator.build();
        status.setHash(hash);
        UpdateControl<R> updateControl;

        if (status.equals(resource.getStatus())) {
            updateControl = UpdateControl.noUpdate();
        } else {
            resource.setStatus(status);
            updateControl = UpdateControl.patchStatus(resource);
        }

        if (poll) {
            updateControl.rescheduleAfter(config.keycloak().pollIntervalSeconds(), TimeUnit.SECONDS);
        }

        return updateControl;
    }

    /** 检查关联 Keycloak StatefulSet 是否有就绪副本。 */
    private boolean isServerReady(Context<R> context, R resource) {
        StatefulSet existingDeployment = context.getClient().resources(StatefulSet.class)
                .inNamespace(resource.getMetadata().getNamespace()).withName(resource.getSpec().getKeycloakCRName())
                .get();

        return existingDeployment != null && KeycloakRealmImportController.getReadyReplicas(existingDeployment) > 0;
    }

    // TODO: 与当前 feature 概念不够契合
    // 需要 v2 显式启用，不能仅检查 client-admin-api
    // 后续 Keycloak 版本可能默认启用 client-admin-api:v2，需检查是否被禁用或移除此校验
    private boolean hasFeatureEnabled(Keycloak keycloak) {
        return Optional.ofNullable(keycloak.getSpec().getFeatureSpec()).map(FeatureSpec::getEnabledFeatures)
                .filter(ef -> ef.contains(CLIENT_ADMIN_API_V2)).isPresent();
    }

    abstract boolean prepareRepresentation(S crRepresentation, T targetRepresentation, Context<?> context);

    abstract Class<T> getTargetRepresentation();

    /**
     * 使用 finalizer 确保客户端不会在 CR 删除时被孤立，
     * 除非用户主动绕过清理流程。
     */
    @Override
    public DeleteControl cleanup(R resource, Context<R> context) throws Exception {
        String kcName = resource.getSpec().getKeycloakCRName();

        Keycloak keycloak = context.getClient().resources(Keycloak.class)
                .inNamespace(resource.getMetadata().getNamespace()).withName(kcName).get();

        if (keycloak == null) {
            return DeleteControl.defaultDelete();
        }

        if (!hasFeatureEnabled(keycloak)) {
            // TODO: 此行为不够直观，目前仅记录错误后继续
            Log.error("Cannot delete Client $s/%s because the server does not have %s enabled.".formatted(
                    resource.getMetadata().getNamespace(), resource.getMetadata().getName(), CLIENT_ADMIN_API_V2));
            return DeleteControl.defaultDelete();
        }

        invoke(resource, context, keycloak, client -> {
            try {
                client.deleteClient();
            } catch (WebApplicationException e) {
                if (e.getResponse().getStatus() != 404) {
                    throw e;
                }
            }
            return null;
        });

        return DeleteControl.defaultDelete();
    }

    @Override
    public ErrorStatusUpdateControl<R> updateErrorStatus(R resource, Context<R> context, Exception e) {
        Log.error("--- Error reconciling", e);

        KeycloakClientStatusAggregator status = new KeycloakClientStatusAggregator(resource);
        status.setCondition(KeycloakClientStatusCondition.HAS_ERRORS, true, "Error performing operations:\n" + e.getMessage());
        resource.setStatus(status.build());

        return ErrorStatusUpdateControl.patchStatus(resource).rescheduleAfter(Constants.RETRY_DURATION);
    }

    /** Admin API v2 根路径代理接口。 */
    @Path("admin/api")
    public interface AdminRootV2 {

        @Path("{realmName}")
        AdminApi adminApi(@PathParam("realmName") String realmName);

    }

    //TODO: 仅供本地测试——考虑移除
    private String addressOverride;

    public void setAddressOverride(String addressOverride) {
        this.addressOverride = addressOverride;
    }

    /** 在服务器就绪后调用 Admin API 执行指定操作。 */
    private <V> V invoke(R resource, Context<R> context, Keycloak keycloak,
            Function<ClientApi, V> action) {
        if (!isServerReady(context, resource)) {
            throw new RuntimeException("A replica of the server is not yet ready. The operation will be retried");
        }
        try (var kcAdmin = getAdminClient(context.getClient(), keycloak, addressOverride)) {
            var target = getWebTarget(kcAdmin);
            AdminRootV2 root = org.keycloak.admin.client.Keycloak.getClientProvider().targetProxy(target,
                    AdminRootV2.class);
            return action.apply(root.adminApi(resource.getSpec().getRealm()).clientsV2()
                    .client(resource.getMetadata().getName()));
        }
    }

    private WebTarget getWebTarget(org.keycloak.admin.client.Keycloak kcAdmin) {
        // TODO: 应改进 API 设计，避免反射访问 target 字段
        try {
            Field field = kcAdmin.getClass().getDeclaredField("target");
            field.setAccessible(true);
            return (WebTarget)field.get(kcAdmin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 构建用于调用 Keycloak Admin API 的客户端，支持 HTTPS/mTLS。 */
    public static org.keycloak.admin.client.Keycloak getAdminClient(KubernetesClient client, Keycloak keycloak, String addressOverride) {
        Secret adminSecret = client.resources(Secret.class)
                .inNamespace(keycloak.getMetadata().getNamespace())
                .withName(keycloak.getMetadata().getName() + "-admin").require();

        String adminUrl = getAdminUrl(keycloak, client, addressOverride);

        Client restEasyClient = null;

        // 使用 HTTPS/mTLS 时需创建自定义 REST 客户端
        if (adminUrl.startsWith(HTTPS)) {
            restEasyClient = createRestEasyClient(client, keycloak, restEasyClient);
        }

        return KeycloakBuilder.builder()
                .serverUrl(adminUrl)
                .realm("master") // TODO: 可配置为其他 realm
                // TODO: 校验这些字段
                .clientId(new String(Base64.getDecoder().decode(adminSecret.getData().get(Constants.CLIENT_ID_KEY)),
                        StandardCharsets.UTF_8))
                .clientSecret(new String(Base64.getDecoder().decode(adminSecret.getData().get(Constants.CLIENT_SECRET_KEY)),
                                StandardCharsets.UTF_8))
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .resteasyClient(restEasyClient)
                .build();
    }

    private static Client createRestEasyClient(KubernetesClient client, Keycloak keycloak, Client restEasyClient) {
        // 添加服务端证书信任
        String tlsSecretName = keycloak.getSpec().getHttpSpec().getTlsSecret();
        Secret tlsSecret = client.resources(Secret.class)
                .inNamespace(keycloak.getMetadata().getNamespace()).withName(tlsSecretName).require();
        byte[] certBytes = Base64.getDecoder().decode(tlsSecret.getData().get("tls.crt"));

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null);
            ks.setCertificateEntry("cert", cert);
            tmf.init(ks);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyManager[] keyManagers = createKeyManagers(client, keycloak);

            sslContext.init(keyManagers, tmf.getTrustManagers(), null);

            ClientBuilder clientBuilder = ClientBuilderWrapper.create(sslContext, false);

            // 仅信任服务端证书，因此禁用主机名验证
            // 仅当 tlsSecret 泄露且 Service 主机名流量可被劫持时才有风险
            //
            // TODO: 若 CA 证书被用作服务端证书时可发出警告
            clientBuilder.hostnameVerifier(NoopHostnameVerifier.INSTANCE);

            restEasyClient = clientBuilder.build();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException
                | KeyManagementException | UnrecoverableKeyException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        return restEasyClient;
    }

    private static KeyManager[] createKeyManagers(KubernetesClient client, Keycloak keycloak)
            throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
            InvalidKeySpecException, IOException {
        if (keycloak.getSpec().getAdminSpec() == null) {
            return null;
        }
        String clientTlsSecretName = keycloak.getSpec().getAdminSpec().getTlsSecret();
        if (clientTlsSecretName == null) {
            return null;
        }
        Secret clientTlsSecret = client.resources(Secret.class)
                .inNamespace(keycloak.getMetadata().getNamespace()).withName(clientTlsSecretName).require();

        byte[] certBytes = Base64.getDecoder().decode(clientTlsSecret.getData().get("tls.crt"));
        byte[] keyBytes = Base64.getDecoder().decode(clientTlsSecret.getData().get("tls.key"));

        KeyStore store = null;
        // TODO: 密钥类型算法可在 CR 中指定、更好推断（Quarkus 逻辑位置不明），
        // 或从文件内容推断——如 BEGIN RSA PRIVATE KEY
        try {
            store = CertUtils.createKeyStore(new ByteArrayInputStream(certBytes), new ByteArrayInputStream(keyBytes), "RSA", null, null, null);
        } catch (Exception e) {
            store = CertUtils.createKeyStore(new ByteArrayInputStream(certBytes), new ByteArrayInputStream(keyBytes), "EC", null, null, null);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(store, null);
        return kmf.getKeyManagers();
    }

    private static String getAdminUrl(Keycloak keycloak, KubernetesClient client, String addressOverride) {
        boolean https = isTlsConfigured(keycloak);
        String protocol = https?HTTPS:"http";
        String address = addressOverride;

        int port = https?HttpSpec.serviceHttpsPort(keycloak):HttpSpec.serviceHttpPort(keycloak);

        if (address == null) {
            // 使用 Service 主机名——TODO: 假设 Operator 与 Keycloak 在同一集群
            // 若 kube client 可指向其他集群，此假设可能不成立
            address = String.format("%s.%s.svc:%s", KeycloakServiceDependentResource.getServiceName(keycloak),
                    keycloak.getMetadata().getNamespace(), port);
        }

        var relativePath = KeycloakDeploymentDependentResource.readConfigurationValue(Constants.KEYCLOAK_HTTP_RELATIVE_PATH_KEY, keycloak, client)
                .map(path -> !path.isEmpty() && !path.startsWith("/") ? "/" + path : path)
                .orElse("");

        return String.format("%s://%s%s", protocol, address, relativePath);
    }

}
