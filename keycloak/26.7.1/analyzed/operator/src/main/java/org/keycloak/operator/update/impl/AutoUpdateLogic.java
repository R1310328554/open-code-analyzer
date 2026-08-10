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

package org.keycloak.operator.update.impl;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.keycloak.operator.controllers.KeycloakUpdateJobDependentResource;
import org.keycloak.operator.crds.v2beta1.CRDUtils;
import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;

import io.fabric8.kubernetes.api.model.ContainerState;
import io.fabric8.kubernetes.api.model.ContainerStateTerminated;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.quarkus.logging.Log;

/**
 * 自动更新策略：创建更新 Job 运行兼容性检查命令，根据退出码决定滚动或重建。
 */
public class AutoUpdateLogic extends BaseUpdateLogic {

    private final KeycloakUpdateJobDependentResource updateJobResource;

    public AutoUpdateLogic(Context<Keycloak> context, Keycloak keycloak, KeycloakUpdateJobDependentResource updateJobResource) {
        super(context, keycloak);
        this.updateJobResource = updateJobResource;
    }

    @Override
    Optional<UpdateControl<Keycloak>> onUpdate() {
        var existingJob = context.getSecondaryResource(Job.class);
        if (existingJob.isEmpty()) {
            updateJobResource.reconcile(keycloak, context);
            Log.debug("Creating Update Job");
            return Optional.of(UpdateControl.noUpdate());
        }

        // CR 可能在 Job 运行期间被修改，需删除旧 Job 并重新开始
        if (!KeycloakUpdateJobDependentResource.isJobFromCurrentKeycloakCr(existingJob.get(), keycloak)) {
            context.getClient().resource(existingJob.get()).lockResourceVersion().delete();
            return Optional.of(UpdateControl.noUpdate());
        }

        if (isJobRunning(existingJob.get())) {
            Log.debug("Update Job is running. Waiting until terminated.");
            return Optional.of(UpdateControl.noUpdate());
        }

        var pod = findPodForJob(context.getClient(), existingJob.get());
        if (pod.isEmpty()) {
            Log.warn("Pod for completed Update Job not found, will restart the Job");
            context.getClient().resource(existingJob.get()).lockResourceVersion().delete();
            return Optional.of(UpdateControl.noUpdate());
        }

        checkUpdateType(pod.get());
        return Optional.empty();
    }

    /** 根据 Job 条件判断更新 Job 是否仍在运行。 */
    private boolean isJobRunning(Job job) {
        var status = job.getStatus();
        Log.debugf("Update Job Status:%n%s", CRDUtils.toJsonNode(status, context).toPrettyString());
        return Optional.ofNullable(status)
                .map(s -> s.getConditions().stream().noneMatch(
                        jc -> "True".equals(jc.getStatus()) && List.of("Complete", "Failed").contains(jc.getType())))
                .orElse(true);
    }

    /** 解析 init 容器与主容器的退出码，决定 ROLLING 或 RECREATE。 */
    private void checkUpdateType(Pod pod) {
        // 检查 init 容器（运行 update-compatibility 命令）
        var initContainerExitCode = initContainer(pod)
                .map(AutoUpdateLogic::exitCode);
        if (initContainerExitCode.isEmpty()) {
            Log.warn("InitContainer not found for Update Job.");
            decideRecreateUpdate("InitContainer running update-compatibility command not found. Did it crash? Check update job for details.");
            return;
        }
        if (initContainerExitCode.get() != 0) {
            if (initContainerExitCode.get() == 4) {
                Log.warn("Feature 'rolling-update' not enabled.");
                decideRecreateUpdate("Feature 'rolling-update' not enabled.");
                return;
            }
            Log.warn("InitContainer unexpectedly failed for Update Job.");
            decideRecreateUpdate("Unexpected update-compatibility command exit code (%s). Check update job for details.".formatted(initContainerExitCode.get()));
            return;
        }

        // 检查主容器
        var containerExitCode = container(pod)
                .map(AutoUpdateLogic::exitCode);
        if (containerExitCode.isEmpty()) {
            Log.warn("Container not found for Update Job.");
            decideRecreateUpdate("Container running update-compatibility command not found. Did it crash?");
            return;
        }
        switch (containerExitCode.get()) {
            case 0: {
                decideRollingUpdate("Compatible changes detected.");
                return;
            }
            case 1: {
                Log.warn("Container has an unexpected error for Update Job");
                decideRecreateUpdate("Unexpected update-compatibility command error. Check update job for details.");
                return;
            }
            case 2: {
                Log.warn("Container has an invalid arguments for Update Job.");
                decideRecreateUpdate("Invalid arguments in update-compatibility command. Check update job for details.");
                return;
            }
            case 3: {
                Log.warn("Rolling Update not possible.");
                decideRecreateUpdate("Incompatible changes detected. Check update job for details.");
                return;
            }
            case 4: {
                Log.warn("Feature 'rolling-update' not enabled.");
                decideRecreateUpdate("Feature 'rolling-update' not enabled.");
                return;
            }
            default: {
                Log.warnf("Unexpected Update Job exit code: %s", containerExitCode.get());
                decideRecreateUpdate("Unexpected update-compatibility command exit code (%s). Check update job for details.".formatted(containerExitCode.get()));
            }
        }
    }

    /** 按 Job 标签选择器查找关联 Pod。 */
    public static Optional<Pod> findPodForJob(KubernetesClient client, Job job) {
        return client.pods()
                .inNamespace(job.getMetadata().getNamespace())
                .withLabelSelector(Objects.requireNonNull(job.getSpec().getSelector()))
                .list()
                .getItems()
                .stream()
                .findFirst();
    }

    private static Optional<ContainerStatus> initContainer(Pod pod) {
        return java.util.Optional.ofNullable(pod.getStatus())
                .map(PodStatus::getInitContainerStatuses)
                .map(Collection::stream)
                .flatMap(Stream::findFirst);
    }

    /** 获取 Pod 中第一个主容器的状态。 */
    public static Optional<ContainerStatus> container(Pod pod) {
        return java.util.Optional.ofNullable(pod.getStatus())
                .map(PodStatus::getContainerStatuses)
                .map(Collection::stream)
                .flatMap(Stream::findFirst);
    }

    /** 从容器的 terminated 状态读取退出码，缺失时默认为 1。 */
    public static int exitCode(ContainerStatus containerStatus) {
        return Optional.ofNullable(containerStatus)
                .map(ContainerStatus::getState)
                .map(ContainerState::getTerminated)
                .map(ContainerStateTerminated::getExitCode)
                .orElse(1);
    }

}
