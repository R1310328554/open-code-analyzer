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

package org.apache.rocketmq.proxy;

import com.google.common.collect.Lists;
import io.grpc.protobuf.services.ChannelzService;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.broker.BrokerStartup;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.thread.ThreadPoolMonitor;
import org.apache.rocketmq.common.utils.AbstractStartAndShutdown;
import org.apache.rocketmq.common.utils.StartAndShutdown;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.proxy.config.Configuration;
import org.apache.rocketmq.proxy.config.ConfigurationManager;
import org.apache.rocketmq.proxy.config.ProxyConfig;
import org.apache.rocketmq.proxy.grpc.GrpcServer;
import org.apache.rocketmq.proxy.grpc.GrpcServerBuilder;
import org.apache.rocketmq.proxy.grpc.v2.GrpcMessagingApplication;
import org.apache.rocketmq.proxy.metrics.ProxyMetricsManager;
import org.apache.rocketmq.proxy.processor.DefaultMessagingProcessor;
import org.apache.rocketmq.proxy.processor.MessagingProcessor;
import org.apache.rocketmq.proxy.remoting.RemotingProtocolServer;
import org.apache.rocketmq.proxy.service.cert.TlsCertificateManager;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.srvutil.ServerUtil;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * RocketMQ Proxy 启动入口：解析参数、初始化配置并启动 gRPC 与 Remoting 服务。
 */
public class ProxyStartup {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME);
    /** 统一管理 Proxy 各组件启停的生命周期容器。 */
    private static final ProxyStartAndShutdown PROXY_START_AND_SHUTDOWN = new ProxyStartAndShutdown();

    private static class ProxyStartAndShutdown extends AbstractStartAndShutdown {
        @Override
        public void appendStartAndShutdown(StartAndShutdown startAndShutdown) {
            super.appendStartAndShutdown(startAndShutdown);
        }
    }

    /** 主入口：按顺序启动 TLS、gRPC、Remoting 并注册关闭钩子。 */
    public static void main(String[] args) {
        try {
            // 解析命令行参数
            CommandLineArgument commandLineArgument = parseCommandLineArgument(args);
            initConfiguration(commandLineArgument);

            // 初始化 Proxy 线程池监控
            initThreadPoolMonitor();

            ThreadPoolExecutor executor = createServerExecutor();

            MessagingProcessor messagingProcessor = createMessagingProcessor();

            // TLS 证书热更新管理
            TlsCertificateManager tlsCertificateManager = new TlsCertificateManager();
            PROXY_START_AND_SHUTDOWN.appendStartAndShutdown(tlsCertificateManager);

            // 构建 gRPC 服务（含 Channelz 与反射服务）
            GrpcServer grpcServer = GrpcServerBuilder.newBuilder(executor,
                    ConfigurationManager.getProxyConfig().getGrpcServerPort(), tlsCertificateManager)
                .addService(createServiceProcessor(messagingProcessor))
                .addService(ChannelzService.newInstance(100))
                .addService(ProtoReflectionService.newInstance())
                .configInterceptor()
                .shutdownTime(ConfigurationManager.getProxyConfig().getGrpcShutdownTimeSeconds(), TimeUnit.SECONDS)
                .build();
            PROXY_START_AND_SHUTDOWN.appendStartAndShutdown(grpcServer);

            RemotingProtocolServer remotingServer = new RemotingProtocolServer(messagingProcessor, tlsCertificateManager);
            PROXY_START_AND_SHUTDOWN.appendStartAndShutdown(remotingServer);

            // 依次启动已注册组件
            PROXY_START_AND_SHUTDOWN.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("try to shutdown server");
                try {
                    PROXY_START_AND_SHUTDOWN.preShutdown();
                    PROXY_START_AND_SHUTDOWN.shutdown();
                } catch (Exception e) {
                    log.error("err when shutdown rocketmq-proxy", e);
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("find an unexpect err.", e);
            System.exit(1);
        }

        System.out.printf("%s%n", new Date() + " rocketmq-proxy startup successfully");
        log.info(new Date() + " rocketmq-proxy startup successfully");
    }

    /** 加载环境变量与配置文件，并应用命令行覆盖项。 */
    protected static void initConfiguration(CommandLineArgument commandLineArgument) throws Exception {
        if (StringUtils.isNotBlank(commandLineArgument.getProxyConfigPath())) {
            System.setProperty(Configuration.CONFIG_PATH_PROPERTY, commandLineArgument.getProxyConfigPath());
        }
        ConfigurationManager.initEnv();
        ConfigurationManager.initConfig();
        setConfigFromCommandLineArgument(commandLineArgument);
        log.info("Current configuration: " + ConfigurationManager.formatProxyConfig());

    }

    /** 解析 mqproxy 命令行并转为 {@link CommandLineArgument}。 */
    protected static CommandLineArgument parseCommandLineArgument(String[] args) {
        CommandLine commandLine = ServerUtil.parseCmdLine("mqproxy", args,
            buildCommandlineOptions(), new DefaultParser());
        if (commandLine == null) {
            throw new RuntimeException("parse command line argument failed");
        }

        CommandLineArgument commandLineArgument = new CommandLineArgument();
        MixAll.properties2Object(ServerUtil.commandLine2Properties(commandLine), commandLineArgument);
        return commandLineArgument;
    }

    /** 构建 Proxy 专用 CLI 选项（brokerConfigPath、proxyConfigPath、proxyMode）。 */
    private static Options buildCommandlineOptions() {
        Options options = ServerUtil.buildCommandlineOptions(new Options());

        Option opt = new Option("bc", "brokerConfigPath", true, "Broker config file path for local mode");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("pc", "proxyConfigPath", true, "Proxy config file path");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("pm", "proxyMode", true, "Proxy run in local or cluster mode");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    /** 将命令行非空字段写入 {@link ProxyConfig}。 */
    private static void setConfigFromCommandLineArgument(CommandLineArgument commandLineArgument) {
        if (StringUtils.isNotBlank(commandLineArgument.getNamesrvAddr())) {
            ConfigurationManager.getProxyConfig().setNamesrvAddr(commandLineArgument.getNamesrvAddr());
        }
        if (StringUtils.isNotBlank(commandLineArgument.getBrokerConfigPath())) {
            ConfigurationManager.getProxyConfig().setBrokerConfigPath(commandLineArgument.getBrokerConfigPath());
        }
        if (StringUtils.isNotBlank(commandLineArgument.getProxyMode())) {
            ConfigurationManager.getProxyConfig().setProxyMode(commandLineArgument.getProxyMode());
        }
    }

    /** 按 LOCAL/CLUSTER 模式创建 {@link MessagingProcessor} 并注册指标与 Broker。 */
    protected static MessagingProcessor createMessagingProcessor() {
        String proxyModeStr = ConfigurationManager.getProxyConfig().getProxyMode();
        MessagingProcessor messagingProcessor;

        if (ProxyMode.isClusterMode(proxyModeStr)) {
            messagingProcessor = DefaultMessagingProcessor.createForClusterMode();
            ProxyMetricsManager proxyMetricsManager = ProxyMetricsManager.initClusterMode(ConfigurationManager.getProxyConfig());
            PROXY_START_AND_SHUTDOWN.appendStartAndShutdown(proxyMetricsManager);
        } else if (ProxyMode.isLocalMode(proxyModeStr)) {
            BrokerController brokerController = createBrokerController();
            ProxyMetricsManager.initLocalMode(brokerController.getBrokerMetricsManager(), ConfigurationManager.getProxyConfig());
            StartAndShutdown brokerControllerWrapper = new StartAndShutdown() {
                @Override
                public void start() throws Exception {
                    brokerController.start();
                    String tip = "The broker[" + brokerController.getBrokerConfig().getBrokerName() + ", "
                        + brokerController.getBrokerAddr() + "] boot success. serializeType=" + RemotingCommand.getSerializeTypeConfigInThisServer();
                    if (null != brokerController.getBrokerConfig().getNamesrvAddr()) {
                        tip += " and name server is " + brokerController.getBrokerConfig().getNamesrvAddr();
                    }
                    log.info(tip);
                }

                @Override
                public void shutdown() throws Exception {
                    brokerController.shutdown();
                }
            };
            PROXY_START_AND_SHUTDOWN.appendStartAndShutdown(brokerControllerWrapper);
            messagingProcessor = DefaultMessagingProcessor.createForLocalMode(brokerController);
        } else {
            throw new IllegalArgumentException("try to start grpc server with wrong mode, use 'local' or 'cluster'");
        }
        PROXY_START_AND_SHUTDOWN.appendStartAndShutdown(messagingProcessor);
        return messagingProcessor;
    }

    private static GrpcMessagingApplication createServiceProcessor(MessagingProcessor messagingProcessor) {
        GrpcMessagingApplication application = GrpcMessagingApplication.create(messagingProcessor);
        PROXY_START_AND_SHUTDOWN.appendStartAndShutdown(application);
        return application;
    }

    /** 本地模式下根据配置创建并返回 {@link BrokerController}。 */
    protected static BrokerController createBrokerController() {
        ProxyConfig config = ConfigurationManager.getProxyConfig();
        List<String> brokerStartupArgList = Lists.newArrayList("-c", config.getBrokerConfigPath());
        if (StringUtils.isNotBlank(config.getNamesrvAddr())) {
            brokerStartupArgList.add("-n");
            brokerStartupArgList.add(config.getNamesrvAddr());
        }
        String[] brokerStartupArgs = brokerStartupArgList.toArray(new String[0]);
        return BrokerStartup.createBrokerController(brokerStartupArgs);
    }

    /** 创建 gRPC 请求线程池并注册关闭回调。 */
    public static ThreadPoolExecutor createServerExecutor() {
        ProxyConfig config = ConfigurationManager.getProxyConfig();
        int threadPoolNums = config.getGrpcThreadPoolNums();
        int threadPoolQueueCapacity = config.getGrpcThreadPoolQueueCapacity();
        ThreadPoolExecutor executor = ThreadPoolMonitor.createAndMonitor(
            threadPoolNums,
            threadPoolNums,
            1, TimeUnit.MINUTES,
            "GrpcRequestExecutorThread",
            threadPoolQueueCapacity
        );
        PROXY_START_AND_SHUTDOWN.appendShutdown(executor::shutdown);
        return executor;
    }

    /** 按 ProxyConfig 初始化线程池水位与 JStack 打印监控。 */
    public static void initThreadPoolMonitor() {
        ProxyConfig config = ConfigurationManager.getProxyConfig();
        ThreadPoolMonitor.config(
            LoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME),
            LoggerFactory.getLogger(LoggerName.PROXY_WATER_MARK_LOGGER_NAME),
            config.isEnablePrintJstack(), config.getPrintJstackInMillis(),
            config.getPrintThreadPoolStatusInMillis());
        ThreadPoolMonitor.init();
    }
}
