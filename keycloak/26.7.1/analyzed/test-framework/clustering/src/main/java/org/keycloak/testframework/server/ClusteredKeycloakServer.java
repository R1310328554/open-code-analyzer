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

package org.keycloak.testframework.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.keycloak.common.Profile;
import org.keycloak.it.utils.DockerKeycloakDistribution;
import org.keycloak.testframework.clustering.LoadBalancer;
import org.keycloak.testframework.infinispan.CacheType;
import org.keycloak.testframework.logging.JBossContainerLogConsumer;

import org.jboss.logging.Logger;
import org.testcontainers.images.RemoteDockerImage;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.LazyFuture;

/**
 * 基于 Docker 的多节点 {@link KeycloakServer} 实现，用于集群集成测试。
 * 启动多个 Keycloak 容器、等待 Infinispan 集群视图就绪，并托管 {@link LoadBalancer}。
 */
public class ClusteredKeycloakServer implements KeycloakServer {

    /** 匹配 Infinispan 集群成员数日志（ISPN000093/094）的正则。 */
    private static final String CLUSTER_VIEW_REGEX = ".*ISPN000093.*(?<=\\()(%1$d)(?=\\)).*|.*ISPN000094.*(?<=\\()(%1$d)(?=\\)).*";
    /** HTTP 请求端口。 */
    private static final int REQUEST_PORT = 8080;
    /** Quarkus 管理端口。 */
    private static final int MANAGEMENT_PORT = 9000;
    /** 占位符，表示使用本地构建的快照镜像而非远程镜像名。 */
    public static final String SNAPSHOT_IMAGE = "-";

    private final DockerKeycloakDistribution[] containers;
    private final String images;
    private final long startTimeout;
    private final boolean stateless;
    private LoadBalancer loadBalancer;

    /** 懒加载创建默认 Keycloak 快照镜像。 */
    private static LazyFuture<String> defaultImage() {
        return DockerKeycloakDistribution.createImage(true);
    }

    /**
     * @param numServers 集群节点数
     * @param images 逗号分隔的镜像列表，{@link #SNAPSHOT_IMAGE} 表示快照镜像
     * @param startTimeout 就绪探针超时（秒）
     * @param stateless 是否启用无状态（STATELESS）特性
     */
    public ClusteredKeycloakServer(int numServers, String images, long startTimeout, boolean stateless) {
        containers = new DockerKeycloakDistribution[numServers];
        this.images = images;
        this.startTimeout = startTimeout;
        this.stateless = stateless;
    }

    /** 启动集群：配置缓存、拉起容器、等待集群视图并创建负载均衡器。 */
    @Override
    public void start(KeycloakServerConfigBuilder configBuilder, boolean tlsEnabled) {
        int numServers = containers.length;

        String[] imagePeServer = null;
        Supplier<CountdownLatchLoggingConsumer> latchSupplier;
        List<CountdownLatchLoggingConsumer> consumers = new ArrayList<>(numServers);

        // Infinispan 集群缓存
        configBuilder.cache(CacheType.ISPN);
        if (stateless) {
            configBuilder.features(Profile.Feature.STATELESS);
            latchSupplier = () -> {
                var clusterLatch = new CountdownLatchLoggingConsumer(1, String.format(CLUSTER_VIEW_REGEX, 1));
                consumers.add(clusterLatch);
                return clusterLatch;
            };
        } else {
            var clusterLatch = new CountdownLatchLoggingConsumer(numServers, String.format(CLUSTER_VIEW_REGEX, numServers));
            latchSupplier = () -> clusterLatch;
            consumers.add(clusterLatch);
        }

        if (images == null || images.isEmpty() || (imagePeServer = images.split(",")).length == 1) {
            startContainersWithSameImage(configBuilder, imagePeServer == null ? SNAPSHOT_IMAGE : imagePeServer[0], latchSupplier);
        } else {
            startContainersWithMixedImage(configBuilder, imagePeServer, latchSupplier);
        }

        try {
            long perLatchTimeout = stateless ? DockerKeycloakDistribution.STARTUP_TIMEOUT_SECONDS : (long) numServers * DockerKeycloakDistribution.STARTUP_TIMEOUT_SECONDS;
            for (var clusterLatch : consumers) {
                clusterLatch.await(perLatchTimeout, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            if (stateless) {
                throw new RuntimeException("One or more nodes failed to start with 'stateless' feature.", e);
            } else {
                throw new RuntimeException("Expected %d cluster members".formatted(numServers), e);
            }
        }
        ReadinessProbe.waitUntilReady(this::getBaseUrl, numServers, startTimeout);
        // Inject* 注解无法将 LoadBalancer 作为依赖注入，须在任意 HTTP 请求前在此启动。
        // 在此统一管理更合适。
        loadBalancer = new LoadBalancer(this);
    }

    /** 为每个节点指定不同镜像时启动容器。 */
    private void startContainersWithMixedImage(KeycloakServerConfigBuilder configBuilder, String[] imagePeServer, Supplier<CountdownLatchLoggingConsumer> clusterLatch) {
        assert imagePeServer != null;
        if (containers.length != imagePeServer.length) {
            throw new IllegalArgumentException("The number of containers and the number of images must match");
        }

        int[] exposedPorts = new int[]{REQUEST_PORT, MANAGEMENT_PORT};
        LazyFuture<String> snapshotImage = null;
        for (int i = 0; i < containers.length; ++i) {
            LazyFuture<String> resolvedImage;
            if (SNAPSHOT_IMAGE.equals(imagePeServer[i])) {
                if (snapshotImage == null) {
                    // 否则迁移到快照库时会报 Incorrect state of migration 并阻止启动
                    configBuilder.option("spi-datastore--legacy--allow-migrate-existing-database-to-snapshot", "true");
                    snapshotImage = defaultImage();
                }
                resolvedImage = snapshotImage;
            } else {
                resolvedImage = new RemoteDockerImage(DockerImageName.parse(imagePeServer[i]));
            }
            var container = new DockerKeycloakDistribution(exposedPorts, resolvedImage);
            containers[i] = container;

            copyProvidersAndConfigs(container, configBuilder);

            configureLogConsumers(container, i, clusterLatch.get());
            configureClusterNameIfStatelessEnabled(configBuilder, i);
            container.runKc(configBuilder.toArgs());
        }
    }

    /** 所有节点使用同一镜像时批量启动容器。 */
    private void startContainersWithSameImage(KeycloakServerConfigBuilder configBuilder, String image, Supplier<CountdownLatchLoggingConsumer> clusterLatch) {
        int[] exposedPorts = new int[]{REQUEST_PORT, MANAGEMENT_PORT};
        LazyFuture<String> imageFuture = image == null || SNAPSHOT_IMAGE.equals(image) ?
                defaultImage() :
                new RemoteDockerImage(DockerImageName.parse(image));
        for (int i = 0; i < containers.length; ++i) {
            var container = new DockerKeycloakDistribution(exposedPorts, imageFuture);
            containers[i] = container;

            copyProvidersAndConfigs(container, configBuilder);
            configureLogConsumers(container, i, clusterLatch.get());
            configureClusterNameIfStatelessEnabled(configBuilder, i);
            container.runKc(configBuilder.toArgs());
        }
    }

    /** 为容器配置 JBoss 日志消费者与集群就绪 latch。 */
    private static void configureLogConsumers(DockerKeycloakDistribution container, int index, CountdownLatchLoggingConsumer clusterLatch) {
        var logger = new JBossContainerLogConsumer(Logger.getLogger("managed.keycloak." + index));
        container.setCustomLogConsumer(logger.andThen(clusterLatch));
    }

    /** 将测试依赖的 Provider JAR 复制到容器。 */
    private void copyProvidersAndConfigs(DockerKeycloakDistribution container, KeycloakServerConfigBuilder configBuilder) {
        for (var dependency : configBuilder.toDependencies()) {
            container.copyProvider(dependency.getGroupId(), dependency.getArtifactId());
        }
    }

    /** 关闭负载均衡器并停止所有容器。 */
    @Override
    public void stop() {
        Optional.ofNullable(loadBalancer).ifPresent(LoadBalancer::close);
        loadBalancer = null;
        Arrays.stream(containers)
                .filter(Objects::nonNull)
                .forEach(DockerKeycloakDistribution::stop);
    }

    /** 返回负载均衡器对外基址（非单节点 URL）。 */
    @Override
    public String getBaseUrl() {
        return LoadBalancer.HOSTNAME;
    }

    /** 返回第一个节点的管理端基址。 */
    @Override
    public String getManagementBaseUrl() {
        return getManagementBaseUrl(0);
    }

    /** @param index 节点索引 @return 映射到宿主机的 HTTP 端口 */
    public int getBasePort(int index) {
        return containers[index].getMappedPort(REQUEST_PORT);
    }

    /** @param index 节点索引 @return 该节点直连 HTTP 基址 */
    public String getBaseUrl(int index) {
        return "http://localhost:%d".formatted(getBasePort(index));
    }

    /** @param index 节点索引 @return 该节点管理端基址 */
    public String getManagementBaseUrl(int index) {
        return "http://localhost:%d".formatted(containers[index].getMappedPort(MANAGEMENT_PORT));
    }

    /** @return 集群节点数量 */
    public int clusterSize() {
        return containers.length;
    }

    /** @return 集群 HTTP 负载均衡器实例 */
    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    /** 无状态模式下为各节点设置独立 embedded cache 集群名。 */
    private void configureClusterNameIfStatelessEnabled(KeycloakServerConfigBuilder configBuilder, int id) {
        if (!stateless) {
            return;
        }
        configBuilder.spiOption("cache-embedded", "default", "cluster-name", "cluster-" + id);
    }
}
