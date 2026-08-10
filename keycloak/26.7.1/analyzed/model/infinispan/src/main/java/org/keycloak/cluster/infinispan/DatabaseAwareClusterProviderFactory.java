/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.cluster.infinispan;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.keycloak.Config;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.common.Profile;
import org.keycloak.common.util.DurationConverter;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.connections.infinispan.NodeInfo;
import org.keycloak.infinispan.util.InfinispanUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.services.scheduled.ScheduledTaskRunner;
import org.keycloak.timer.TimerProvider;

import org.infinispan.commons.marshall.Marshaller;
import org.jboss.logging.Logger;

/**
 * 基于数据库表中继的集群事件提供者工厂：在嵌入式 Infinispan 之上启用跨集群消息传递。
 * <p>
 * 通过定时轮询 JPA 事件表拉取远端写入的事件，并与 {@link DatabaseAwareClusterProvider} 配合发布。
 *
 * This implementation sends information to other clusters via a database table.
 *
 * @author Alexander Schwartz
 */
public class DatabaseAwareClusterProviderFactory extends InfinispanClusterProviderFactory {

    protected static final Logger logger = Logger.getLogger(DatabaseAwareClusterProviderFactory.class);

    /** 默认数据库轮询间隔。 */
    private static final String DEFAULT_POLL_INTERVAL_MS = "100ms";

    /** 初始化后缓存的节点与集群信息。 */
    private volatile NodeInfo nodeInfo;
    /** ProtoStream 序列化器，与 Infinispan 连接共享。 */
    private volatile Marshaller protoStreamMarshaller;
    /** 独立守护 Timer，避免与其它调度任务互相阻塞。 */
    private Timer timer;

    /** 从数据库拉取新集群事件的间隔。 */
    private Duration pollInterval;
    /** 发布方等待事件被消费的最长时长（pollInterval × 5）。 */
    private Duration awaitTimeout;

    public DatabaseAwareClusterProviderFactory() {
    }

    /** 创建包装了 DB 持久化逻辑的 {@link DatabaseAwareClusterProvider}。 */
    @Override
    public ClusterProvider create(KeycloakSession session) {
        return new DatabaseAwareClusterProvider(super.create(session), session,
                nodeInfo, protoStreamMarshaller, awaitTimeout);
    }

    @Override
    public void init(Config.Scope config) {
        pollInterval = DurationConverter.parseDuration(config.get("pollInterval", DEFAULT_POLL_INTERVAL_MS));
        if (pollInterval.compareTo(Duration.ZERO) <= 0) {
            throw new IllegalArgumentException("pollInterval must be a positive number");
        }
        if (pollInterval.compareTo(Duration.ofSeconds(1)) > 0) {
            logger.warnf("Polling interval is %s. This is longer than 1 second, which seems to be too high for a production setting. Please verify.", pollInterval.toString());
        }
        awaitTimeout = pollInterval.multipliedBy(5);
        // 使用独立 Timer，避免被其它定时任务拖慢轮询
        timer = new Timer(true);
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                    .name("pollInterval")
                    .type("string")
                    .helpText("Interval between polling the database for new cluster events (supports duration suffixes like ms, s, m, h). In a multi-cluster setup, a publishing node will pause for up to 5 times this duration for the event to be consumed to ensure the information is received by all nodes before returning to the caller.")
                    .defaultValue(DEFAULT_POLL_INTERVAL_MS)
                    .add()
                .build();
    }

    /**
     * 工厂后置初始化：解析节点信息并启动 {@link DatabaseClusterEventPollerTask} 定时轮询。
     */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        KeycloakModelUtils.runJobInTransaction(factory, session -> {
            InfinispanConnectionProvider ispnConnections = session.getProvider(InfinispanConnectionProvider.class);
            nodeInfo = ispnConnections.getNodeInfo();
            this.protoStreamMarshaller = ispnConnections.getMarshaller();

            var pollerTask = new DatabaseClusterEventPollerTask(nodeInfo.clusterName(), protoStreamMarshaller);
            var runner = new ScheduledTaskRunner(factory, pollerTask);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runner.run();
                }
            }, pollInterval.toMillis(), pollInterval.toMillis());
            logger.infof("Scheduled cluster event poller with interval %s for cluster '%s'",
                    pollInterval, nodeInfo.clusterName());
        });
    }

    @Override
    public void close() {
        super.close();
        if (timer != null) {
            timer.cancel();
        }
    }

    /** SPI 标识：嵌入式 Infinispan 提供者 id 后缀 {@code -db}。 */
    @Override
    public String getId() {
        return InfinispanUtils.EMBEDDED_PROVIDER_ID + "-db";
    }

    @Override
    public Set<Class<? extends Provider>> dependsOn() {
        HashSet<Class<? extends Provider>> result = new HashSet<>(super.dependsOn());
        result.add(TimerProvider.class);
        result.add(InfinispanConnectionProvider.class);
        return result;
    }

    /** 仅在嵌入式 Infinispan 且启用 STATELESS 特性时可用。 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return InfinispanUtils.isEmbeddedInfinispan() &&
                Profile.isFeatureEnabled(Profile.Feature.STATELESS);
    }

}
