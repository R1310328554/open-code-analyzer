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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.operator.Config;
import org.keycloak.operator.Constants;
import org.keycloak.operator.ContextUtils;
import org.keycloak.operator.Utils;
import org.keycloak.operator.crds.v2beta1.CRDUtils;
import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;
import org.keycloak.operator.crds.v2beta1.deployment.KeycloakSpec;
import org.keycloak.operator.crds.v2beta1.deployment.ValueOrSecret;
import org.keycloak.operator.crds.v2beta1.deployment.spec.CacheSpec;
import org.keycloak.operator.crds.v2beta1.deployment.spec.HttpManagementSpec;
import org.keycloak.operator.crds.v2beta1.deployment.spec.HttpSpec;
import org.keycloak.operator.crds.v2beta1.deployment.spec.ProbeSpec;
import org.keycloak.operator.crds.v2beta1.deployment.spec.SchedulingSpec;
import org.keycloak.operator.crds.v2beta1.deployment.spec.Truststore;
import org.keycloak.operator.crds.v2beta1.deployment.spec.TruststoreSource;
import org.keycloak.operator.crds.v2beta1.deployment.spec.UnsupportedSpec;
import org.keycloak.operator.crds.v2beta1.deployment.spec.UpdateSpec;
import org.keycloak.operator.update.impl.RecreateOnImageChangeUpdateLogic;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.EnvVarSource;
import io.fabric8.kubernetes.api.model.EnvVarSourceBuilder;
import io.fabric8.kubernetes.api.model.PodSpecFluent;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretKeySelector;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;
import io.quarkus.logging.Log;

import static org.keycloak.operator.Utils.addResources;
import static org.keycloak.operator.controllers.KeycloakDistConfigurator.getKeycloakOptionEnvVarName;
import static org.keycloak.operator.crds.v2beta1.CRDUtils.LEGACY_MANAGEMENT_ENABLED;
import static org.keycloak.operator.crds.v2beta1.CRDUtils.isTlsConfigured;
import static org.keycloak.operator.crds.v2beta1.deployment.spec.TelemetrySpec.convertResourceAttributesToString;

@KubernetesDependent(
        informer = @Informer(labelSelector = Constants.DEFAULT_LABELS_AS_STRING)
)
/**
 * Keycloak StatefulSet 依赖资源：构建 Pod 模板、环境变量、TLS 与更新策略。
 */
public class KeycloakDeploymentDependentResource extends VersionTolerantCRUDKubernetesDependentResource<StatefulSet, Keycloak> {

    /** HTTP 管理端点协议配置键。 */
    public static final String HTTP_MANAGEMENT_SCHEME = "http-management-scheme";

    /** Pod IP 环境变量名，供 JGroups 绑定地址使用。 */
    public static final String POD_IP = "POD_IP";
    /** 嵌入式缓存机器名 SPI 选项环境变量。 */
    public static final String HOST_IP_SPI_OPTION = "KC_SPI_CACHE_EMBEDDED_DEFAULT_MACHINE_NAME";

    /** 从 Operator 进程复制的代理相关环境变量名。 */
    private static final List<String> COPY_ENV = Arrays.asList("HTTP_PROXY", "HTTPS_PROXY", "NO_PROXY");

    /** 缓存配置文件 ConfigMap 卷名。 */
    public static final String CACHE_CONFIG_FILE_MOUNT_NAME = "cache-config-file-configmap";

    /** 信任库路径环境变量。 */
    public static final String KC_TRUSTSTORE_PATHS = "KC_TRUSTSTORE_PATHS";
    /** Kubernetes CA 自动发现开关环境变量。 */
    public static final String KC_TRUSTSTORE_KUBERNETES_ENABLED = "KC_TRUSTSTORE_KUBERNETES_ENABLED";

    // 遥测相关环境变量
    public static final String KC_TELEMETRY_SERVICE_NAME = "KC_TELEMETRY_SERVICE_NAME";
    public static final String KC_TELEMETRY_RESOURCE_ATTRIBUTES = "KC_TELEMETRY_RESOURCE_ATTRIBUTES";
    public static final String KC_TRACING_RESOURCE_ATTRIBUTES = "KC_TRACING_RESOURCE_ATTRIBUTES";

    /** 优化启动参数。 */
    public static final String OPTIMIZED_ARG = "--optimized";

    // 在初始管理员 Secret 创建后再创建 Deployment，避免 Pod 重启。
    // 不使用原生 dependsOn，因管理员 Secret 可能由用户提供而非 Operator 创建。
    /** 协调前置条件：自定义或 Operator 创建的初始管理员 Secret 已存在。 */
    public static class ReconcilePrecondition implements Condition<StatefulSet, Keycloak> {
        @Override
        public boolean isMet(DependentResource<StatefulSet, Keycloak> dependentResource, Keycloak primary, Context<Keycloak> context) {
            return KeycloakAdminSecretDependentResource.hasCustomAdminSecret(primary)
                    || context.getSecondaryResourcesAsStream(Secret.class)
                    .anyMatch(s -> s.getMetadata().getName().equals(KeycloakAdminSecretDependentResource.getName(primary)));
        }
    }

    public KeycloakDeploymentDependentResource() {
        super(StatefulSet.class);
    }

    /** 计算初始期望 StatefulSet（含 TLS、信任库与环境变量），供控制器缓存。 */
    public StatefulSet initialDesired(Keycloak primary, Context<Keycloak> context) {
        Config operatorConfig = ContextUtils.getOperatorConfig(context);
        WatchedResources watchedResources = ContextUtils.getWatchedResources(context);

        StatefulSet baseDeployment = createBaseDeployment(primary, context, operatorConfig);
        WatchedResources.Watched allSecrets = new WatchedResources.Watched();
        WatchedResources.Watched allConfigMaps = new WatchedResources.Watched();
        if (isTlsConfigured(primary)) {
            configureTLS(primary, baseDeployment, allSecrets);
        }
        Container kcContainer = baseDeployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        addTruststores(primary, baseDeployment, kcContainer, allSecrets, allConfigMaps);
        addEnvVars(baseDeployment, primary, allSecrets, context);
        addResources(primary.getSpec().getResourceRequirements(), operatorConfig, kcContainer);
        Optional.ofNullable(primary.getSpec().getCacheSpec())
                .ifPresent(c -> configureCache(baseDeployment, kcContainer, c, allConfigMaps));

        watchedResources.annotateDeployment(allSecrets, Secret.class, baseDeployment, context);
        watchedResources.annotateDeployment(allConfigMaps, ConfigMap.class, baseDeployment, context);

        // 默认使用新版本号——必要时会被旧版本覆盖
        UpdateSpec.getRevision(primary).ifPresent(rev -> addUpdateRevisionAnnotation(rev, baseDeployment));
        addUpdateHashAnnotation(KeycloakUpdateJobDependentResource.keycloakHash(primary), baseDeployment);

        var existingDeployment = ContextUtils.getCurrentStatefulSet(context).orElse(null);

        String serviceName = KeycloakDiscoveryServiceDependentResource.getName(primary);
        if (existingDeployment != null) {
            // 复制现有注解以保持状态一致
            CRDUtils.findUpdateReason(existingDeployment).ifPresent(r -> baseDeployment.getMetadata().getAnnotations()
                    .put(Constants.KEYCLOAK_UPDATE_REASON_ANNOTATION, r));
            CRDUtils.fetchIsRecreateUpdate(existingDeployment).ifPresent(b -> baseDeployment.getMetadata()
                    .getAnnotations().put(Constants.KEYCLOAK_RECREATE_UPDATE_ANNOTATION, b.toString()));
            serviceName = existingDeployment.getSpec().getServiceName();
        }

        baseDeployment.getSpec().setServiceName(serviceName);
        return baseDeployment;
    }

    @Override
    /** 根据更新类型（滚动/重建）返回最终期望 StatefulSet。 */
    public StatefulSet desired(Keycloak primary, Context<Keycloak> context) {
        StatefulSet baseDeployment = ContextUtils.getDesiredStatefulSet(context);
        var existingDeployment = ContextUtils.getCurrentStatefulSet(context).orElse(null);

        var updateType = ContextUtils.getUpdateType(context);

        if (existingDeployment == null || updateType.isEmpty()) {
            return baseDeployment;
        }

        // v22 变更了 matchLabels，需处理旧版 StatefulSet
        if (!existingDeployment.isMarkedForDeletion() && !hasExpectedMatchLabels(existingDeployment, primary)) {
            context.getClient().resource(existingDeployment).lockResourceVersion().delete();
            Log.info("Existing Deployment found with old label selector, it will be recreated");
        }

        baseDeployment.getMetadata().getAnnotations().put(Constants.KEYCLOAK_UPDATE_REASON_ANNOTATION, ContextUtils.getUpdateReason(context));

        return switch (updateType.get()) {
            case ROLLING -> handleRollingUpdate(baseDeployment);
            case RECREATE -> handleRecreateUpdate(existingDeployment, baseDeployment, CRDUtils.firstContainerOf(baseDeployment).orElseThrow());
        };
    }

    private void configureCache(StatefulSet deployment, Container kcContainer, CacheSpec spec, WatchedResources.Watched allConfigMaps) {
        Optional.ofNullable(spec.getConfigMapFile()).ifPresent(configFile -> {
            if (configFile.getName() == null || configFile.getKey() == null) {
                throw new IllegalStateException("Cache file ConfigMap requires both a name and a key");
            }

            var volume = new VolumeBuilder()
                    .withName(CACHE_CONFIG_FILE_MOUNT_NAME)
                    .withNewConfigMap()
                    .withName(configFile.getName())
                    .withOptional(configFile.getOptional())
                    .endConfigMap()
                    .build();

            var volumeMount = new VolumeMountBuilder()
                    .withName(volume.getName())
                    .withMountPath(Constants.CACHE_CONFIG_FOLDER)
                    .build();

            deployment.getSpec().getTemplate().getSpec().getVolumes().add(0, volume);
            kcContainer.getVolumeMounts().add(0, volumeMount);
            allConfigMaps.add(configFile.getName(), configFile.getOptional());
        });
    }

    private void addTruststores(Keycloak keycloakCR, StatefulSet deployment, Container kcContainer, WatchedResources.Watched allSecrets, WatchedResources.Watched allConfigMaps) {
        for (Truststore truststore : keycloakCR.getSpec().getTruststores().values()) {
            // 目前仅支持 Secret 作为信任库源，后续可支持 ConfigMap
            TruststoreSource source = truststore.getSecret();
            if (source != null) {
                String secretName = source.getName();
                var volume = new VolumeBuilder()
                        .withName("truststore-secret-" + secretName)
                        .withNewSecret()
                        .withSecretName(secretName)
                        .withOptional(source.getOptional())
                        .endSecret()
                        .build();

                var volumeMount = new VolumeMountBuilder()
                        .withName(volume.getName())
                        .withMountPath(Constants.TRUSTSTORES_FOLDER + "/secret-" + secretName)
                        .build();

                deployment.getSpec().getTemplate().getSpec().getVolumes().add(0, volume);
                kcContainer.getVolumeMounts().add(0, volumeMount);
                allSecrets.add(secretName, source.getOptional());
            } else {
                source = truststore.getConfigMap();
                if (source != null) {
                    String name = source.getName();
                    var volume = new VolumeBuilder()
                            .withName("truststore-configmap-" + name)
                            .withNewConfigMap()
                            .withName(name)
                            .withOptional(source.getOptional())
                            .endConfigMap()
                            .build();

                    var volumeMount = new VolumeMountBuilder()
                            .withName(volume.getName())
                            .withMountPath(Constants.TRUSTSTORES_FOLDER + "/configmap-" + name)
                            .build();

                    deployment.getSpec().getTemplate().getSpec().getVolumes().add(0, volume);
                    kcContainer.getVolumeMounts().add(0, volumeMount);
                    allConfigMaps.add(name, source.getOptional());
                }
            }
        }
    }

    void configureTLS(Keycloak keycloakCR, StatefulSet deployment, WatchedResources.Watched allSecrets) {
        var kcContainer = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);

        var volume = new VolumeBuilder()
                .withName("keycloak-tls-certificates")
                .withNewSecret()
                .withSecretName(keycloakCR.getSpec().getHttpSpec().getTlsSecret())
                .withOptional(false)
                .endSecret()
                .build();

        var volumeMount = new VolumeMountBuilder()
                .withName(volume.getName())
                .withMountPath(Constants.CERTIFICATES_FOLDER)
                .build();

        deployment.getSpec().getTemplate().getSpec().getVolumes().add(0, volume);
        kcContainer.getVolumeMounts().add(0, volumeMount);
        allSecrets.add(keycloakCR.getSpec().getHttpSpec().getTlsSecret(), null);
    }

    private boolean hasExpectedMatchLabels(StatefulSet statefulSet, Keycloak keycloak) {
        return Optional.ofNullable(statefulSet).map(s -> Utils.allInstanceLabels(keycloak).equals(s.getSpec().getSelector().getMatchLabels())).orElse(true);
    }

    static Optional<PodTemplateSpec> getPodTemplateSpec(Keycloak keycloakCR) {
        return Optional.ofNullable(keycloakCR.getSpec()).map(KeycloakSpec::getUnsupported).map(UnsupportedSpec::getPodTemplate);
    }

    private StatefulSet createBaseDeployment(Keycloak keycloakCR, Context<Keycloak> context, Config operatorConfig) {
        Map<String, String> labels = Utils.allInstanceLabels(keycloakCR);
        labels.put("app.kubernetes.io/component", "server");
        Map<String, String> schedulingLabels = new LinkedHashMap<>(labels);
        if (operatorConfig.keycloak().podLabels() != null) {
            labels.putAll(operatorConfig.keycloak().podLabels());
        }

        /* 创建 StatefulSet 构建器；以 podTemplate 为基础，
         * 部分字段强制覆盖，部分允许模板覆盖，部分合并。
         */

        StatefulSetBuilder baseDeploymentBuilder = new StatefulSetBuilder()
                .withNewMetadata()
                    .withName(getName(keycloakCR))
                    .withNamespace(keycloakCR.getMetadata().getNamespace())
                    .withLabels(Utils.allInstanceLabels(keycloakCR))
                    .addToAnnotations(Constants.KEYCLOAK_MIGRATING_ANNOTATION, Boolean.FALSE.toString())
                .endMetadata()
                .withNewSpec()
                    .withNewSelector()
                        .withMatchLabels(Utils.allInstanceLabels(keycloakCR))
                    .endSelector()
                    .withNewTemplateLike(getPodTemplateSpec(keycloakCR).orElseGet(PodTemplateSpec::new))
                        .editOrNewMetadata().addToLabels(labels).endMetadata()
                        .editOrNewSpec().withImagePullSecrets(keycloakCR.getSpec().getImagePullSecrets()).endSpec()
                    .endTemplate()
                    .withReplicas(keycloakCR.getSpec().getInstances())
                .endSpec();
        
        if (KeycloakController.isMultiNamespace(context)) {
            baseDeploymentBuilder = baseDeploymentBuilder.editSpec().editTemplate().editSpec().withServiceAccount(null)
                    .withServiceAccountName(null).endSpec().endTemplate().endSpec();
        }

        var specBuilder = baseDeploymentBuilder.editSpec().editTemplate().editOrNewSpec();

        if (!specBuilder.hasRestartPolicy()) {
            specBuilder.withRestartPolicy("Always");
        }
        if (!specBuilder.hasTerminationGracePeriodSeconds()) {
            specBuilder.withTerminationGracePeriodSeconds(30L);
        }
        if (!specBuilder.hasDnsPolicy()) {
            specBuilder.withDnsPolicy("ClusterFirst");
        }
        boolean automount = !Boolean.FALSE.equals(keycloakCR.getSpec().getAutomountServiceAccountToken());
        specBuilder.withAutomountServiceAccountToken(automount);
        handleScheduling(keycloakCR, schedulingLabels, specBuilder);

        // 尚无 editOrNewFirstContainer，需手动处理首个容器
        var containerBuilder = specBuilder.buildContainers().isEmpty() ? specBuilder.addNewContainer() : specBuilder.editFirstContainer();

        containerBuilder.withName("keycloak");

        var customImage = Optional.ofNullable(keycloakCR.getSpec().getImage());
        containerBuilder.withImage(customImage.orElse(operatorConfig.keycloak().image()));

        if (!containerBuilder.hasImagePullPolicy()) {
            containerBuilder.withImagePullPolicy(operatorConfig.keycloak().imagePullPolicy());
        }
        if (Optional.ofNullable(containerBuilder.getArgs()).orElse(List.of()).isEmpty()) {
            containerBuilder.withArgs("--verbose", "start");
        }
        if (Boolean.TRUE.equals(keycloakCR.getSpec().getStartOptimized())
                || keycloakCR.getSpec().getStartOptimized() == null
                        && (customImage.isPresent() || operatorConfig.keycloak().startOptimized())) {
            containerBuilder.addToArgs(OPTIMIZED_ARG);
        }
        // IPv6 环境下 JGroups 集群组建需要绑定地址
        containerBuilder.addToArgs(0, "-Djgroups.bind.address=$(%s)".formatted(POD_IP));

        ManagementEndpoint endpoint = managementEndpoint(keycloakCR, context, true);

        // 就绪/存活/启动探针
        var readinessOptionalSpec = Optional.ofNullable(keycloakCR.getSpec().getReadinessProbeSpec());
        var livenessOptionalSpec = Optional.ofNullable(keycloakCR.getSpec().getLivenessProbeSpec());
        var startupOptionalSpec = Optional.ofNullable(keycloakCR.getSpec().getStartupProbeSpec());

        if (!containerBuilder.hasReadinessProbe()) {
            containerBuilder.withNewReadinessProbe()
                .withPeriodSeconds(readinessOptionalSpec.map(ProbeSpec::getProbePeriodSeconds).orElse(10))
                .withFailureThreshold(readinessOptionalSpec.map(ProbeSpec::getProbeFailureThreshold).orElse(3))
                .withNewHttpGet()
                .withScheme(endpoint.protocol)
                .withNewPort(endpoint.port)
                .withPath(endpoint.relativePath + "health/ready")
                .endHttpGet()
                .endReadinessProbe();
        }
        if (!containerBuilder.hasLivenessProbe()) {
            containerBuilder.withNewLivenessProbe()
                .withPeriodSeconds(livenessOptionalSpec.map(ProbeSpec::getProbePeriodSeconds).orElse(10))
                .withFailureThreshold(livenessOptionalSpec.map(ProbeSpec::getProbeFailureThreshold).orElse(3))
                .withNewHttpGet()
                .withScheme(endpoint.protocol)
                .withNewPort(endpoint.port)
                .withPath(endpoint.relativePath + "health/live")
                .endHttpGet()
                .endLivenessProbe();
        }
        if (!containerBuilder.hasStartupProbe()) {
            containerBuilder.withNewStartupProbe()
                .withPeriodSeconds(startupOptionalSpec.map(ProbeSpec::getProbePeriodSeconds).orElse(1))
                .withFailureThreshold(startupOptionalSpec.map(ProbeSpec::getProbeFailureThreshold).orElse(600))
                .withNewHttpGet()
                .withScheme(endpoint.protocol)
                .withNewPort(endpoint.port)
                .withPath(endpoint.relativePath + "health/started")
                .endHttpGet()
                .endStartupProbe();
        }

        // 添加容器端口——此处不做合并
        return containerBuilder
            .addNewPort()
                .withName(Constants.KEYCLOAK_HTTPS_PORT_NAME)
                .withContainerPort(Constants.KEYCLOAK_HTTPS_PORT)
                .withProtocol(Constants.KEYCLOAK_SERVICE_PROTOCOL)
            .endPort()
            .addNewPort()
                .withName(Constants.KEYCLOAK_HTTP_PORT_NAME)
                .withContainerPort(Constants.KEYCLOAK_HTTP_PORT)
                .withProtocol(Constants.KEYCLOAK_SERVICE_PROTOCOL)
            .endPort()
            .addNewPort()
                .withName(Constants.KEYCLOAK_MANAGEMENT_PORT_NAME)
                .withContainerPort(Constants.KEYCLOAK_MANAGEMENT_PORT)
                .withProtocol(Constants.KEYCLOAK_SERVICE_PROTOCOL)
            .endPort()
            .endContainer().endSpec().endTemplate().endSpec().build();
    }

    private void handleScheduling(Keycloak keycloakCR, Map<String, String> labels, PodSpecFluent<?> specBuilder) {
        SchedulingSpec schedulingSpec = keycloakCR.getSpec().getSchedulingSpec();
        if (schedulingSpec != null) {
            if (!specBuilder.hasPriorityClassName()) {
                specBuilder.withPriorityClassName(schedulingSpec.getPriorityClassName());
            }
            if (!specBuilder.hasAffinity()) {
                specBuilder.withAffinity(schedulingSpec.getAffinity());
            }
            if (!specBuilder.hasTolerations()) {
                specBuilder.withTolerations(schedulingSpec.getTolerations());
            }
            if (!specBuilder.hasTopologySpreadConstraints()) {
                specBuilder.withTopologySpreadConstraints(schedulingSpec.getTopologySpreadConstraints());
            }
        }

        if (!specBuilder.hasTopologySpreadConstraints()) {
            specBuilder.addNewTopologySpreadConstraint()
                    .withMaxSkew(1)
                    .withTopologyKey("topology.kubernetes.io/zone")
                    .withWhenUnsatisfiable("ScheduleAnyway")
                    .withNewLabelSelector()
                    .withMatchLabels(labels)
                    .endLabelSelector()
                    .endTopologySpreadConstraint()
                    .addNewTopologySpreadConstraint()
                    .withMaxSkew(1)
                    .withTopologyKey("kubernetes.io/hostname")
                    .withWhenUnsatisfiable("ScheduleAnyway")
                    .withNewLabelSelector()
                    .withMatchLabels(labels)
                    .endLabelSelector()
                    .endTopologySpreadConstraint();
        }
    }

    private void addEnvVars(StatefulSet baseDeployment, Keycloak keycloakCR, WatchedResources.Watched allSecrets, Context<Keycloak> context) {
        var distConfigurator = ContextUtils.getDistConfigurator(context);
        var firstClasssEnvVars = distConfigurator.configureDistOptions(keycloakCR);

        var additionalEnvVars = getDefaultAndAdditionalEnvVars(keycloakCR);

        var unsupportedEnv = Optional.ofNullable(baseDeployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv()).orElse(List.of());

        var env = keycloakCR.getSpec().getEnv().stream().map(this::toEnvVar);

        // 按优先级累积环境变量：unsupported → 一等公民 → 附加项 → CR env
        LinkedHashMap<String, EnvVar> varMap = Stream.concat(Stream.concat(unsupportedEnv.stream(), firstClasssEnvVars.stream()), Stream.concat(additionalEnvVars.stream(), env))
                .collect(Collectors.toMap(EnvVar::getName, Function.identity(), (e1, e2) -> e1, LinkedHashMap::new));

        // 关闭 Kubernetes CA 自动发现
        if (Boolean.FALSE.equals(keycloakCR.getSpec().getAutomountServiceAccountToken())) {
            varMap.putIfAbsent(KC_TRUSTSTORE_KUBERNETES_ENABLED, new EnvVarBuilder().withName(KC_TRUSTSTORE_KUBERNETES_ENABLED).withValue("false").build());
        }

        setTelemetryEnvVars(keycloakCR, varMap);

        var envVars = new ArrayList<>(varMap.values());
        baseDeployment.getSpec().getTemplate().getSpec().getContainers().get(0).setEnv(envVars);

        // 监听 secretKeyRef 引用的 Secret——目前不监听 ConfigMap 或 initial-admin
        TreeSet<String> serverConfigSecretsNames = envVars.stream().map(EnvVar::getValueFrom).filter(Objects::nonNull)
                .map(EnvVarSource::getSecretKeyRef).filter(Objects::nonNull).peek(s -> allSecrets.add(s.getName(), s.getOptional())).map(SecretKeySelector::getName).collect(Collectors.toCollection(TreeSet::new));

        Log.debugf("Found config secrets names: %s", serverConfigSecretsNames);
    }

    private static void setTelemetryEnvVars(Keycloak keycloakCR, Map<String, EnvVar> varMap) {
        varMap.putIfAbsent(KC_TELEMETRY_SERVICE_NAME,
                new EnvVarBuilder().withName(KC_TELEMETRY_SERVICE_NAME)
                        .withValue(keycloakCR.getMetadata().getName())
                        .build()
        );

        // OTel K8s 属性约定参见 https://opentelemetry.io/docs/specs/semconv/attributes-registry/k8s/#kubernetes-attributes
        var telemetryAttributes = Map.of("k8s.namespace.name", keycloakCR.getMetadata().getNamespace());

        if (varMap.containsKey(KC_TELEMETRY_RESOURCE_ATTRIBUTES)) {
            appendExistingResourceAttributes(KC_TELEMETRY_RESOURCE_ATTRIBUTES, telemetryAttributes, varMap);
        } else if (varMap.containsKey(KC_TRACING_RESOURCE_ATTRIBUTES)) {
            appendExistingResourceAttributes(KC_TRACING_RESOURCE_ATTRIBUTES, telemetryAttributes, varMap);
        } else {
            varMap.put(KC_TELEMETRY_RESOURCE_ATTRIBUTES,
                    new EnvVarBuilder().withName(KC_TELEMETRY_RESOURCE_ATTRIBUTES)
                            .withValue(convertResourceAttributesToString(telemetryAttributes))
                            .build()
            );
        }
    }

    /**
     * 将默认资源属性追加到指定的 OTel 资源属性环境变量。
     */
    private static void appendExistingResourceAttributes(String resourceAttributesEnvVar, Map<String, String> existingResourceAttributes, Map<String, EnvVar> varMap) {
        var existingAttributes = convertResourceAttributesToMap(resourceAttributesEnvVar, varMap);
        existingResourceAttributes.forEach(existingAttributes::putIfAbsent);
        varMap.get(resourceAttributesEnvVar).setValue(convertResourceAttributesToString(existingAttributes));
    }

    private static Map<String, String> convertResourceAttributesToMap(String resourceAttributesEnvVar, Map<String, EnvVar> envVars) {
        return Arrays.stream(Optional.ofNullable(envVars.get(resourceAttributesEnvVar).getValue()).orElse("").split(","))
                .filter(entry -> entry.contains("="))
                .map(entry -> entry.split("=", 2))
                .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
    }

    private EnvVar toEnvVar(ValueOrSecret v) {
        var envBuilder = new EnvVarBuilder().withName(v.getName());
        var secret = v.getSecret();
        if (secret != null) {
            envBuilder.withValueFrom(
                    new EnvVarSourceBuilder().withSecretKeyRef(secret).build());
        } else {
            envBuilder.withValue(v.getValue());
        }
        return envBuilder.build();
    }

    private List<EnvVar> getDefaultAndAdditionalEnvVars(Keycloak keycloakCR) {
        // 默认配置值
        List<ValueOrSecret> serverConfigsList = new ArrayList<>(Constants.DEFAULT_DIST_CONFIG_LIST);
        Set<String> defaultKeys = serverConfigsList.stream().map(ValueOrSecret::getName).collect(Collectors.toSet());

        // 与 CR 合并，CR 中的值优先
        if (keycloakCR.getSpec().getAdditionalOptions() != null) {
            Set<String> inCr = keycloakCR.getSpec().getAdditionalOptions().stream().map(ValueOrSecret::getName).collect(Collectors.toSet());
            serverConfigsList.removeIf(v -> inCr.contains(v.getName()));
            serverConfigsList.addAll(keycloakCR.getSpec().getAdditionalOptions());
        }

        // set env vars
        List<EnvVar> envVars = serverConfigsList.stream()
                .flatMap(v -> {
                    var envBuilder = new EnvVarBuilder().withName(getKeycloakOptionEnvVarName(v.getName()));
                    var secret = v.getSecret();
                    if (secret != null) {
                        envBuilder.withValueFrom(
                                new EnvVarSourceBuilder().withSecretKeyRef(secret).build());
                    } else {
                        envBuilder.withValue(v.getValue());
                    }
                    EnvVar mainVar = envBuilder.build();
                    if (!defaultKeys.contains(v.getName())) {
                        EnvVar keyVar = new EnvVarBuilder()
                                .withName("KCKEY_" + mainVar.getName().substring(KeycloakDistConfigurator.KC_PREFIX.length()))
                                .withValue(v.getName()).build();
                        return Stream.of(mainVar, keyVar);
                    }
                    return Stream.of(mainVar);
                })
                .collect(Collectors.toCollection(ArrayList::new));

        for (String env : COPY_ENV) {
            String value = System.getenv(env);
            if (value != null) {
                envVars.add(new EnvVarBuilder().withName(env).withValue(value).build());
            }
        }

        envVars.add(new EnvVarBuilder().withName(POD_IP).withNewValueFrom().withNewFieldRef()
                .withFieldPath("status.podIP").withApiVersion("v1").endFieldRef().endValueFrom().build());

        // status.hostIP 与 spec.nodeName 均可；理论上 hostIP 更小，JGroups 消息标签开销更低。
        // 使用 spec.nodeName 以避免在日志中暴露 IP 地址。
        envVars.add(new EnvVarBuilder().withName(HOST_IP_SPI_OPTION).withNewValueFrom().withNewFieldRef()
                .withFieldPath("spec.nodeName").withApiVersion("v1").endFieldRef().endValueFrom().build());

        return envVars;
    }

    /** 返回 StatefulSet 资源名称（与 Keycloak CR 同名）。 */
    public static String getName(Keycloak keycloak) {
        return keycloak.getMetadata().getName();
    }

    /** 从 additionalOptions 或 Secret 读取指定配置项值。 */
    static Optional<String> readConfigurationValue(String key, Keycloak keycloakCR, KubernetesClient client) {
        return Optional.ofNullable(keycloakCR.getSpec()).map(KeycloakSpec::getAdditionalOptions)
                .flatMap(l -> l.stream().filter(sc -> sc.getName().equals(key)).findFirst().map(serverConfigValue -> {
            if (serverConfigValue.getValue() != null) {
                return serverConfigValue.getValue();
            }
            var secretSelector = serverConfigValue.getSecret();
            if (secretSelector == null) {
                throw new IllegalStateException("Secret " + serverConfigValue.getName() + " not defined");
            }
            var secret = client.secrets().inNamespace(keycloakCR.getMetadata().getNamespace()).withName(secretSelector.getName()).get();
            if (secret == null) {
                throw new IllegalStateException("Secret " + secretSelector.getName() + " not found in cluster");
            }
            if (secret.getData().containsKey(secretSelector.getKey())) {
                return new String(Base64.getDecoder().decode(secret.getData().get(secretSelector.getKey())), StandardCharsets.UTF_8);
            }
            throw new IllegalStateException("Secret " + secretSelector.getName() + " doesn't contain the expected key " + secretSelector.getKey());
        }));
    }

    /** 滚动更新：Kubernetes 默认就地滚动，直接返回期望 StatefulSet。 */
    private static StatefulSet handleRollingUpdate(StatefulSet desired) {
        // Kubernetes 默认执行就地滚动更新
        Log.debug("Performing a rolling update");
        desired.getMetadata().getAnnotations().put(Constants.KEYCLOAK_RECREATE_UPDATE_ANNOTATION, Boolean.FALSE.toString());
        return desired;
    }

    private static StatefulSet handleRecreateUpdate(StatefulSet actual, StatefulSet desired, Container kcContainer) {
        desired.getMetadata().getAnnotations().put(Constants.KEYCLOAK_RECREATE_UPDATE_ANNOTATION, Boolean.TRUE.toString());

        if (Optional.ofNullable(actual.getStatus().getReplicas()).orElse(0) == 0) {
            Log.debug("Performing a recreate update - scaling up the stateful set");

            // 期望状态已正确，无需修改
        } else {
            Log.debug("Performing a recreate update - scaling down the stateful set");

            // 保留旧版本号、镜像与哈希，标记迁移中并缩容至 0
            addOrRemoveAnnotation(CRDUtils.getRevision(actual).orElse(null), Constants.KEYCLOAK_UPDATE_REVISION_ANNOTATION, desired);
            addOrRemoveAnnotation(CRDUtils.getUpdateHash(actual).orElse(null), Constants.KEYCLOAK_UPDATE_HASH_ANNOTATION, desired);
            desired.getMetadata().getAnnotations().put(Constants.KEYCLOAK_MIGRATING_ANNOTATION, Boolean.TRUE.toString());
            desired.getSpec().setReplicas(0);
            var currentImage = RecreateOnImageChangeUpdateLogic.extractImage(actual);
            kcContainer.setImage(currentImage);
        }
        return desired;
    }

    private static void addUpdateRevisionAnnotation(String revision, StatefulSet toUpdate) {
        toUpdate.getMetadata().getAnnotations().put(Constants.KEYCLOAK_UPDATE_REVISION_ANNOTATION, revision);
    }

    private static void addUpdateHashAnnotation(String hash, StatefulSet toUpdate) {
        toUpdate.getMetadata().getAnnotations().put(Constants.KEYCLOAK_UPDATE_HASH_ANNOTATION, hash);
    }

    private static void addOrRemoveAnnotation(String value, String annotation, StatefulSet toUpdate) {
        toUpdate.getMetadata().getAnnotations().compute(annotation, (k, v) -> value);
    }

    /** 管理/健康检查端点描述（相对路径、协议、端口）。 */
    record ManagementEndpoint(String relativePath, String protocol, int port, String portName) {}

    /** 根据 TLS 与管理端配置解析健康/管理探针端点。 */
    static ManagementEndpoint managementEndpoint(Keycloak keycloakCR, Context<Keycloak> context, boolean health) {
        boolean tls = isTlsConfigured(keycloakCR);
        String protocol = tls ? "HTTPS" : "HTTP";
        int port;
        String portName;

        var legacy = readConfigurationValue(LEGACY_MANAGEMENT_ENABLED, keycloakCR, context.getClient()).map(Boolean::valueOf).orElse(false);

        var healthManagementEnabled = readConfigurationValue(CRDUtils.HTTP_MANAGEMENT_HEALTH_ENABLED, keycloakCR, context.getClient()).map(Boolean::valueOf).orElse(true);

        if (!legacy && (!health || healthManagementEnabled)) {
            port = HttpManagementSpec.managementPort(keycloakCR);
            portName = Constants.KEYCLOAK_MANAGEMENT_PORT_NAME;
            if (readConfigurationValue(HTTP_MANAGEMENT_SCHEME, keycloakCR, context.getClient()).filter("http"::equals).isPresent()) {
                protocol = "HTTP";
            }
        } else {
            port = tls ? HttpSpec.httpsPort(keycloakCR) : HttpSpec.httpPort(keycloakCR);
            portName = tls ? Constants.KEYCLOAK_HTTPS_PORT_NAME : Constants.KEYCLOAK_HTTP_PORT_NAME;
        }

        var relativePath = readConfigurationValue(Constants.KEYCLOAK_HTTP_MANAGEMENT_RELATIVE_PATH_KEY, keycloakCR, context.getClient())
              .or(() -> readConfigurationValue(Constants.KEYCLOAK_HTTP_RELATIVE_PATH_KEY, keycloakCR, context.getClient()))
              .map(path -> !path.endsWith("/") ? path + "/" : path)
              .orElse("/");

        return new ManagementEndpoint(relativePath, protocol, port, portName);
    }
}
