/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.container;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.auth.config.AuthConfig;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.broker.out.BrokerOuterAPI;
import org.apache.rocketmq.common.BrokerConfig;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.RemotingServer;
import org.apache.rocketmq.remoting.netty.NettyClientConfig;
import org.apache.rocketmq.remoting.netty.NettyRemotingServer;
import org.apache.rocketmq.remoting.netty.NettyServerConfig;
import org.apache.rocketmq.store.MessageStore;
import org.apache.rocketmq.store.config.MessageStoreConfig;

/**
 * 容器内 Master Broker 控制器：复用容器共享的 Remoting 与 OuterAPI，
 * 按容器策略注册 NameServer 并调度心跳任务。
 */
public class InnerBrokerController extends BrokerController {
    /** 所属 Broker 容器引用。 */
    protected BrokerContainer brokerContainer;

    public InnerBrokerController(
        final BrokerContainer brokerContainer,
        final BrokerConfig brokerConfig,
        final MessageStoreConfig messageStoreConfig,
        final AuthConfig authConfig
    ) {
        super(brokerConfig, messageStoreConfig, authConfig);
        this.brokerContainer = brokerContainer;
        this.brokerOuterAPI = this.brokerContainer.getBrokerOuterAPI();
    }

    /** 从容器 Remoting 工厂创建主/快通道服务端并绑定指标。 */
    @Override
    protected void initializeRemotingServer() {
        RemotingServer remotingServer = this.brokerContainer.getRemotingServer().newRemotingServer(brokerConfig.getListenPort());
        RemotingServer fastRemotingServer = this.brokerContainer.getRemotingServer().newRemotingServer(brokerConfig.getListenPort() - 2);

        if (this.brokerMetricsManager != null && remotingServer instanceof NettyRemotingServer) {
            ((NettyRemotingServer) remotingServer).setRemotingMetricsManager(this.brokerMetricsManager.getRemotingMetricsManager());
        }

        if (this.brokerMetricsManager != null && fastRemotingServer instanceof NettyRemotingServer) {
            ((NettyRemotingServer) fastRemotingServer).setRemotingMetricsManager(this.brokerMetricsManager.getRemotingMetricsManager());
        }

        setRemotingServer(remotingServer);
        setFastRemotingServer(fastRemotingServer);
    }

    /** 仅初始化 Broker 自身定时任务（容器模式不重复注册全局任务）。 */
    @Override
    protected void initializeScheduledTasks() {
        initializeBrokerScheduledTasks();
    }

    /**
     * 启动基础服务并按配置向 NameServer 注册；
     * 支持从节点代主、Controller 模式心跳及成员组同步。
     */
    @Override
    public void start() throws Exception {
        this.shouldStartTime = System.currentTimeMillis() + messageStoreConfig.getDisappearTimeAfterStart();

        if (messageStoreConfig.getTotalReplicas() > 1 && this.brokerConfig.isEnableSlaveActingMaster()) {
            isIsolated = true;
        }

        startBasicService();

        if (!isIsolated && !this.messageStoreConfig.isEnableDLegerCommitLog() && !this.messageStoreConfig.isDuplicationEnable()) {
            changeSpecialServiceStatus(this.brokerConfig.getBrokerId() == MixAll.MASTER_ID);
            this.registerBrokerAll(true, false, true);
        }

        scheduledFutures.add(this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (System.currentTimeMillis() < shouldStartTime) {
                        BrokerController.LOG.info("Register to namesrv after {}", shouldStartTime);
                        return;
                    }
                    if (isIsolated) {
                        BrokerController.LOG.info("Skip register for broker is isolated");
                        return;
                    }
                    InnerBrokerController.this.registerBrokerAll(true, false, brokerConfig.isForceRegister());
                } catch (Throwable e) {
                    BrokerController.LOG.error("registerBrokerAll Exception", e);
                }
            }
        }, 1000 * 10, Math.max(10000, Math.min(brokerConfig.getRegisterNameServerPeriod(), 60000)), TimeUnit.MILLISECONDS));

        if (this.brokerConfig.isEnableSlaveActingMaster()) {
            scheduleSendHeartbeat();

            scheduledFutures.add(this.syncBrokerMemberGroupExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        InnerBrokerController.this.syncBrokerMemberGroup();
                    } catch (Throwable e) {
                        BrokerController.LOG.error("sync BrokerMemberGroup error. ", e);
                    }
                }
            }, 1000, this.brokerConfig.getSyncBrokerMemberGroupPeriod(), TimeUnit.MILLISECONDS));
        }

        if (this.brokerConfig.isEnableControllerMode()) {
            scheduleSendHeartbeat();
        }

        if (brokerConfig.isSkipPreOnline()) {
            startServiceWithoutCondition();
        }
    }

    /** 关闭基础服务、取消定时任务并从容器 Remoting 注销端口。 */
    @Override
    public void shutdown() {

        shutdownBasicService();

        for (ScheduledFuture<?> scheduledFuture : scheduledFutures) {
            scheduledFuture.cancel(true);
        }

        if (getRemotingServer() != null) {
            this.brokerContainer.getRemotingServer().removeRemotingServer(brokerConfig.getListenPort());
        }

        if (getFastRemotingServer() != null) {
            this.brokerContainer.getRemotingServer().removeRemotingServer(brokerConfig.getListenPort() - 2);
        }
    }

    /** 返回 Broker 对外服务地址（IP1:listenPort）。 */
    @Override
    public String getBrokerAddr() {
        return this.brokerConfig.getBrokerIP1() + ":" + this.brokerConfig.getListenPort();
    }

    /** 返回 HA 复制监听地址（IP2:haListenPort）。 */
    @Override
    public String getHAServerAddr() {
        return this.brokerConfig.getBrokerIP2() + ":" + this.messageStoreConfig.getHaListenPort();
    }

    @Override
    public long getMinBrokerIdInGroup() {
        return this.minBrokerIdInGroup;
    }

    @Override
    public int getListenPort() {
        return this.brokerConfig.getListenPort();
    }

    public BrokerOuterAPI getBrokerOuterAPI() {
        return brokerContainer.getBrokerOuterAPI();
    }

    public BrokerContainer getBrokerContainer() {
        return this.brokerContainer;
    }

    public NettyServerConfig getNettyServerConfig() {
        return brokerContainer.getNettyServerConfig();
    }

    public NettyClientConfig getNettyClientConfig() {
        return brokerContainer.getNettyClientConfig();
    }

    /** 按 Broker 名查找本容器内对应的消息存储实例。 */
    public MessageStore getMessageStoreByBrokerName(String brokerName) {
        if (this.brokerConfig.getBrokerName().equals(brokerName)) {
            return this.getMessageStore();
        }
        BrokerController brokerController = this.brokerContainer.findBrokerControllerByBrokerName(brokerName);
        if (brokerController != null) {
            return brokerController.getMessageStore();
        }
        return null;
    }

    /** 若自身为 Master 则返回 this，否则委托容器查找 Master。 */
    @Override
    public BrokerController peekMasterBroker() {
        if (brokerConfig.getBrokerId() == MixAll.MASTER_ID) {
            return this;
        }
        return this.brokerContainer.peekMasterBroker();
    }
}
