/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.operator.Constants;
import org.keycloak.operator.ContextUtils;
import org.keycloak.operator.Utils;
import org.keycloak.operator.crds.v2beta1.CRDUtils;
import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;
import org.keycloak.operator.crds.v2beta1.deployment.KeycloakSpecBuilder;
import org.keycloak.operator.crds.v2beta1.deployment.spec.UpdateSpec;

import io.fabric8.kubernetes.api.model.ContainerFluent;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobSpecFluent;
import io.javaoperatorsdk.operator.api.config.informer.InformerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResourceConfigBuilder;

/**
 * Keycloak 滚动/兼容性更新决策 Job 的 Dependent Resource。
 *
 * <p>通过 init 容器写入当前配置元数据、主容器执行 {@code update-compatibility check}，
 * 比较当前与期望 StatefulSet 以决定是否允许更新。Job 完成后 TTL 自动清理。
 */
@ApplicationScoped
public class KeycloakUpdateJobDependentResource extends VersionTolerantCRUDKubernetesDependentResource<Job, Keycloak> {

    // 共享临时工作目录卷
    /** 更新 Job 临时工作目录卷名。 */
    private static final String WORK_DIR_VOLUME_NAME = "keycloak-update-job-temporary-workdir"; // unlikely to conflict
    /** 卷挂载路径。 */
    private static final String WORK_DIR_VOLUME_MOUNT_PATH = "/mnt/" + WORK_DIR_VOLUME_NAME; // unlikely to conflict
    /** 更新元数据 JSON 文件路径。 */
    private static final String UPDATES_FILE_PATH = WORK_DIR_VOLUME_MOUNT_PATH + "/updates.json";

    // 注解
    /** 记录关联 Keycloak CR 内容哈希的 Job 注解键。 */
    public static final String KEYCLOAK_CR_HASH_ANNOTATION = "operator.keycloak.org/keycloak-hash";

    // 标签
    /** 更新 Job 的 app 标签值。 */
    private static final String APP_LABEL_VALUE = "keycloak-update-job";
    /** Informer 使用的标签选择器。 */
    private static final String LABEL_SELECTOR = "app=keycloak-update-job,app.kubernetes.io/managed-by=keycloak-operator";

    // 容器配置
    /** init 容器名：写入“实际”配置元数据。 */
    private static final String INIT_CONTAINER_NAME = "actual";
    /** 主容器名：校验“期望”配置兼容性。 */
    private static final String CONTAINER_NAME = "desired";
    /** init 容器参数：生成 updates.json。 */
    private static final List<String> INIT_CONTAINER_ARGS = List.of("update-compatibility", "metadata", "--file", UPDATES_FILE_PATH);
    /** 主容器参数：执行兼容性检查。 */
    private static final List<String> CONTAINER_ARGS = List.of("update-compatibility", "check", "--file", UPDATES_FILE_PATH);

    // Job 与 Pod 默认值
    // Pod 在非零退出码失败时不应重启
    /** Job 不重试（由退出码表达更新决策）。 */
    private static final int JOB_RETRIES = 0;
    /** Job 完成后保留时长（秒）。 */
    private static final int JOB_TIME_TO_LIVE_SECONDS = (int) TimeUnit.MINUTES.toSeconds(30);

    /** 配置 Job 类型 informer 与标签选择器。 */
    public KeycloakUpdateJobDependentResource() {
        super(Job.class);
        this.configureWith(new KubernetesDependentResourceConfigBuilder<Job>()
                .withKubernetesDependentInformerConfig(InformerConfiguration.builder(resourceType())
                        .withLabelSelector(LABEL_SELECTOR)
                        .build())
                .build());
    }

    /** 构建带 Pod 模板、backoff 与 TTL 的更新兼容性 Job。 */
    @Override
    public Job desired(Keycloak primary, Context<Keycloak> context) {
        var builder = new JobBuilder();
        builder.withMetadata(createMetadata(jobName(primary), primary));
        var specBuilder = builder.withNewSpec();
        addPodSpecTemplate(specBuilder, primary, context);
        // 不使用重试；非 1 退出码表示更新决策结果
        specBuilder.withBackoffLimit(JOB_RETRIES);
        // 30 分钟后自动删除 Job
        specBuilder.withTtlSecondsAfterFinished(JOB_TIME_TO_LIVE_SECONDS);
        specBuilder.endSpec();
        return builder.build();
    }

    /**
     * 判断 Job 是否由当前 Keycloak CR 生成（owner + CR 哈希一致）。
     *
     * @param job 集群中的 Job
     * @param keycloak Keycloak CR
     */
    public static boolean isJobFromCurrentKeycloakCr(Job job, Keycloak keycloak) {
        var annotations = job.getMetadata().getAnnotations();
        var hash = annotations.get(KEYCLOAK_CR_HASH_ANNOTATION);
        return job.hasOwnerReferenceFor(keycloak) && Objects.equals(hash, keycloakHash(keycloak));
    }

    /** 更新 Job 资源名：{@code <cr-name>-update-job}。 */
    public static String jobName(Keycloak keycloak) {
        return keycloak.getMetadata().getName() + "-update-job";
    }

    /** Pod 模板名：{@code <cr-name>-update-pod}。 */
    private static String podName(Keycloak keycloak) {
        return keycloak.getMetadata().getName() + "-update-pod";
    }

    /** 创建 Job/Pod 元数据：标签、命名空间与 CR 哈希注解。 */
    private static ObjectMeta createMetadata(String name, Keycloak keycloak) {
        var labels = new HashMap<String ,String>();
        var optionalSpec = Optional.ofNullable(keycloak.getSpec().getUpdateSpec());
        optionalSpec.map(UpdateSpec::getLabels).ifPresent(labels::putAll);
        var builder = new ObjectMetaBuilder();
        builder.withName(name)
                .withNamespace(keycloak.getMetadata().getNamespace())
                .addToLabels(labels)
                .addToLabels(getLabels(keycloak))
                .withAnnotations(Map.of(KEYCLOAK_CR_HASH_ANNOTATION, keycloakHash(keycloak)));
        return builder.build();
    }

    /** 为 Job 添加 Pod 模板，并应用 updateSpec 调度配置。 */
    private void addPodSpecTemplate(JobSpecFluent<?> builder, Keycloak keycloak, Context<Keycloak> context) {
        var podTemplate = builder.withNewTemplate();
        podTemplate.withMetadata(createMetadata(podName(keycloak), keycloak));
        PodSpec podSpec = createPodSpec(context);
        KeycloakRealmImportJobDependentResource.handleJobScheduling(
                keycloak,
                Optional.ofNullable(keycloak.getSpec().getUpdateSpec()).map(UpdateSpec::getSchedulingSpec),
                podSpec);
        podTemplate.withSpec(podSpec);
        podTemplate.endTemplate();
    }

    /**
     * 基于期望 StatefulSet 构建 PodSpec，合并当前集群中的镜像拉取密钥与卷，
     * 并挂载 init/主容器执行兼容性检查。
     */
    private PodSpec createPodSpec(Context<Keycloak> context) {
        StatefulSet current = ContextUtils.getCurrentStatefulSet(context).orElseThrow();
        StatefulSet desired = ContextUtils.getDesiredStatefulSet(context);

        // 从期望 StatefulSet 模板起步
        var builder = desired.getSpec().getTemplate().getSpec().edit();
        builder.withRestartPolicy("Never");

        // 移除主 Keycloak 容器及 unsupported PodTemplate 注入的 sidecar
        builder.withContainers();

        // 调度字段保持原样，可按需覆盖

        // 合并当前集群状态
        var desiredPullSecrets = Optional.ofNullable(builder.buildImagePullSecrets()).orElse(List.of());
        current.getSpec().getTemplate().getSpec().getImagePullSecrets().stream().filter(s -> !desiredPullSecrets.contains(s)).forEach(builder::addToImagePullSecrets);
        // TODO: 若卷名相同但内容变更，合并行为可能不一致（例如 cache configmap 切换）
        var desiredVolumes = Optional.ofNullable(builder.buildVolumes()).orElse(List.of()).stream().map(Volume::getName).collect(Collectors.toSet());
        current.getSpec().getTemplate().getSpec().getVolumes().stream().filter(v -> !desiredVolumes.contains(v.getName())).forEach(builder::addToVolumes);
        // TODO: unsupported PodTemplate 可能还有其他需合并的字段

        addInitContainer(builder, current);
        addContainer(builder, desired);
        builder.addNewVolume()
                .withName(WORK_DIR_VOLUME_NAME)
                .withNewEmptyDir()
                .endEmptyDir()
                .endVolume();

        // 测试 KeycloakDeploymentTest#testDeploymentDurability 使用 pause 镜像不会退出；
        // 超时后终止 Job 以便测试完成
        builder.withActiveDeadlineSeconds(ContextUtils.getOperatorConfig(context).keycloak().updatePodDeadlineSeconds());
        return builder.build();
    }

    /** 基于当前 StatefulSet 首容器创建 init 容器。 */
    private static void addInitContainer(PodSpecBuilder builder, StatefulSet current) {
        var existing = CRDUtils.firstContainerOf(current).orElseThrow();
        var containerBuilder = builder.addNewInitContainerLike(existing);
        configureContainer(containerBuilder, INIT_CONTAINER_NAME, INIT_CONTAINER_ARGS);
        containerBuilder.endInitContainer();
    }

    /** 基于期望 StatefulSet 首容器创建兼容性检查主容器。 */
    private static void addContainer(PodSpecBuilder builder, StatefulSet desired) {
        var existing = CRDUtils.firstContainerOf(desired).orElseThrow();
        var containerBuilder = builder.addNewContainerLike(existing);
        configureContainer(containerBuilder, CONTAINER_NAME, CONTAINER_ARGS);
        containerBuilder.endContainer();
    }

    /** 配置容器：替换启动参数、挂载共享卷、移除探针与生命周期。 */
    private static void configureContainer(ContainerFluent<?> containerBuilder, String name, List<String> args) {
        containerBuilder.withName(name);
        containerBuilder.withArgs(replaceStartWithUpdateCommand(containerBuilder.getArgs(), args));

        var volumeMounts = containerBuilder.buildVolumeMounts();
        if (volumeMounts != null) {
            var newVolumeMounts = volumeMounts.stream()
                    .filter(volumeMount -> !volumeMount.getName().startsWith("kube-api"))
                    .toList();
            containerBuilder.withVolumeMounts(newVolumeMounts);
        }

        // 移除 restartPolicy、lifecycle 与各类探针
        containerBuilder.withRestartPolicy(null);
        containerBuilder.withLifecycle(null);
        containerBuilder.withReadinessProbe(null);
        containerBuilder.withLivenessProbe(null);
        containerBuilder.withStartupProbe(null);

        // 挂载共享临时工作目录
        containerBuilder.addNewVolumeMount()
                .withName(WORK_DIR_VOLUME_NAME)
                .withMountPath(WORK_DIR_VOLUME_MOUNT_PATH)
                .endVolumeMount();
    }

    /**
     * 将容器 args 中的 {@code start} 替换为 update-compatibility 子命令参数。
     * 注：通过 unsupported podTemplate 使用 start-dev 会失败，滚动更新场景下可接受。
     */
    private static List<String> replaceStartWithUpdateCommand(List<String> currentArgs, List<String> updateArgs) {
        // note that using start-dev via the unsupported podTemplate will fail - that is fine as rolling updates shouldn't apply
        // TODO: 复用 ConfigArgConfigSource 解析，避免误判实际启动命令
        return Stream.concat(updateArgs.stream(), currentArgs.stream().filter(arg -> !arg.equals("start"))).toList();
    }

    /**
     * 计算 Keycloak spec 的稳定哈希（排除实例数、探针、调度等运行时字段）。
     *
     * @param keycloak Keycloak CR
     * @return 十六进制哈希字符串
     */
    public static String keycloakHash(Keycloak keycloak) {
        return Utils.hash(
                List.of(new KeycloakSpecBuilder(keycloak.getSpec()).withInstances(null).withLivenessProbeSpec(null)
                        .withStartupProbeSpec(null).withReadinessProbeSpec(null).withResourceRequirements(null)
                        .withSchedulingSpec(null).withNetworkPolicySpec(null).withIngressSpec(null)
                        .withImagePullSecrets().withImportSpec(null).withServiceMonitorSpec(null).build()));
    }

    /** 为 Job/Pod 生成标准实例标签与 app 标签。 */
    private static Map<String, String> getLabels(HasMetadata keycloak) {
        var labels = Utils.allInstanceLabels(keycloak);
        labels.put(Constants.APP_LABEL, APP_LABEL_VALUE);
        return labels;
    }
}
