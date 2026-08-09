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
package org.apache.rocketmq.client.impl.mqclient;

import com.google.common.base.Strings;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.common.NameserverAccessConfig;
import org.apache.rocketmq.client.impl.ClientRemotingProcessor;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.ObjectCreator;
import org.apache.rocketmq.common.utils.AsyncShutdownHelper;
import org.apache.rocketmq.common.utils.StartAndShutdown;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.RemotingClient;
import org.apache.rocketmq.remoting.netty.NettyClientConfig;

/**
 * MQClientAPI 工厂：按配置创建并管理多个 {@link MQClientAPIExt} 实例，
 * 负责 NameServer 地址初始化、客户端启动/关闭及负载均衡选取。
 */
public class MQClientAPIFactory implements StartAndShutdown {

    /** 已创建的客户端 API 实例数组。 */
    private MQClientAPIExt[] clients;
    /** 客户端实例名前缀。 */
    private final String namePrefix;
    /** 并行客户端数量，大于 1 时 getClient 随机选取。 */
    private final int clientNum;
    /** 入站 Remoting 请求处理器。 */
    private final ClientRemotingProcessor clientRemotingProcessor;
    /** RPC 钩子，用于鉴权等扩展。 */
    private final RPCHook rpcHook;
    /** 定时任务线程池，用于域名模式下周期性拉取 NameServer 地址。 */
    private final ScheduledExecutorService scheduledExecutorService;
    /** NameServer 访问配置（地址或域名模式）。 */
    private final NameserverAccessConfig nameserverAccessConfig;
    /** 可选的 RemotingClient 创建器，便于测试或自定义实现。 */
    private final ObjectCreator<RemotingClient> remotingClientCreator;

    public MQClientAPIFactory(
        NameserverAccessConfig nameserverAccessConfig,
        String namePrefix,
        int clientNum,
        ClientRemotingProcessor clientRemotingProcessor,
        RPCHook rpcHook,
        ScheduledExecutorService scheduledExecutorService
    ) {
        this(nameserverAccessConfig, namePrefix, clientNum, clientRemotingProcessor, rpcHook, scheduledExecutorService, null);
    }

    public MQClientAPIFactory(
        NameserverAccessConfig nameserverAccessConfig,
        String namePrefix,
        int clientNum,
        ClientRemotingProcessor clientRemotingProcessor,
        RPCHook rpcHook,
        ScheduledExecutorService scheduledExecutorService,
        ObjectCreator<RemotingClient> remotingClientCreator
    ) {
        this.nameserverAccessConfig = nameserverAccessConfig;
        this.namePrefix = namePrefix;
        this.clientNum = clientNum;
        this.clientRemotingProcessor = clientRemotingProcessor;
        this.rpcHook = rpcHook;
        this.scheduledExecutorService = scheduledExecutorService;
        this.remotingClientCreator = remotingClientCreator;

        this.init();
    }

    /** 初始化系统属性：关闭 VIP 通道，并设置 NameServer 地址或域名。 */
    protected void init() {
        System.setProperty(ClientConfig.SEND_MESSAGE_WITH_VIP_CHANNEL_PROPERTY, "false");
        if (StringUtils.isEmpty(nameserverAccessConfig.getNamesrvDomain())) {
            if (Strings.isNullOrEmpty(nameserverAccessConfig.getNamesrvAddr())) {
                throw new RuntimeException("The configuration item NamesrvAddr is not configured");
            }
            System.setProperty(MixAll.NAMESRV_ADDR_PROPERTY, nameserverAccessConfig.getNamesrvAddr());
        } else {
            System.setProperty("rocketmq.namesrv.domain", nameserverAccessConfig.getNamesrvDomain());
            System.setProperty("rocketmq.namesrv.domain.subgroup", nameserverAccessConfig.getNamesrvDomainSubgroup());
        }
    }

    /** 返回一个客户端实例；多实例时随机负载均衡。 */
    public MQClientAPIExt getClient() {
        if (clients.length == 1) {
            return this.clients[0];
        }
        int index = ThreadLocalRandom.current().nextInt(this.clients.length);
        return this.clients[index];
    }

    @Override
    /** 创建并启动 clientNum 个 MQClientAPIExt 实例。 */
    public void start() throws Exception {
        this.clients = new MQClientAPIExt[this.clientNum];

        for (int i = 0; i < this.clientNum; i++) {
            clients[i] = createAndStart(this.namePrefix + "N_" + i);
        }
    }

    @Override
    /** 并行关闭所有客户端实例。 */
    public void shutdown() throws Exception {
        AsyncShutdownHelper helper = new AsyncShutdownHelper();
        for (int i = 0; i < this.clientNum; i++) {
            helper.addTarget(clients[i]);
        }
        helper.shutdown().await(Integer.MAX_VALUE, TimeUnit.SECONDS);
    }

    /** 创建单个 MQClientAPIExt：配置 Netty、注册 NameServer 并启动。 */
    protected MQClientAPIExt createAndStart(String instanceName) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setInstanceName(instanceName);
        clientConfig.setDecodeReadBody(true);
        clientConfig.setDecodeDecompressBody(false);

        NettyClientConfig nettyClientConfig = new NettyClientConfig();
        nettyClientConfig.setDisableCallbackExecutor(true);

        MQClientAPIExt mqClientAPIExt = new MQClientAPIExt(
            clientConfig,
            nettyClientConfig,
            clientRemotingProcessor,
            rpcHook,
            remotingClientCreator
        );

        if (StringUtils.isEmpty(nameserverAccessConfig.getNamesrvDomain())) {
            mqClientAPIExt.updateNameServerAddressList(nameserverAccessConfig.getNamesrvAddr());
        } else {
            mqClientAPIExt.fetchNameServerAddr();
            this.scheduledExecutorService.scheduleAtFixedRate(
                mqClientAPIExt::fetchNameServerAddr,
                Duration.ofSeconds(10).toMillis(),
                Duration.ofMinutes(2).toMillis(),
                TimeUnit.MILLISECONDS
            );
        }

        mqClientAPIExt.start();
        return mqClientAPIExt;
    }

    /** NameServer 地址变更时通知所有客户端更新。 */
    public void onNameServerAddressChange(String namesrvAddress) {
        for (MQClientAPIExt client : clients) {
            client.onNameServerAddressChange(namesrvAddress);
        }
    }

    /** 返回全部客户端实例数组。 */
    public MQClientAPIExt[] getClients() {
        return clients;
    }
}
