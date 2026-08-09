/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.cluster.init;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.demo.cluster.DemoConstants;
import com.alibaba.csp.sentinel.demo.cluster.entity.ClusterGroupEntity;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * 嵌入式集群演示 {@link InitFunc}：从 Nacos 加载流控规则、Client/Server 配置与集群拓扑。
 *
 * @author Eric Zhao
 */
public class DemoClusterInitFunc implements InitFunc {

    private static final String APP_NAME = AppNameUtil.getAppName();

    private final String remoteAddress = "localhost:8848";
    private final String groupId = "SENTINEL_GROUP";

    private final String flowDataId = APP_NAME + DemoConstants.FLOW_POSTFIX;
    private final String paramDataId = APP_NAME + DemoConstants.PARAM_FLOW_POSTFIX;
    private final String configDataId = APP_NAME + "-cluster-client-config";
    private final String clusterMapDataId = APP_NAME + DemoConstants.CLUSTER_MAP_POSTFIX;

    @Override
    public void init() throws Exception {
        // 注册 Client 侧动态流控/热点规则数据源
        initDynamicRuleProperty();

        // 注册 Token Client 相关数据源
        // Client 通用配置：
        initClientConfigProperty();
        // Client 指派配置（目标 Token Server），从 cluster-map 解析：
        initClientServerAssignProperty();

        // 注册 Token Server 相关数据源
        // 为 Token Server 注册按 namespace 拉取规则的 Supplier：
        registerClusterRuleSupplier();
        // Token Server 传输配置，同样从 cluster-map 提取：
        initServerTransportConfigProperty();

        // 根据 cluster-map 推断本机角色（Server / Client / 未启动）
        initStateProperty();
    }

    private void initDynamicRuleProperty() {
        ReadableDataSource<String, List<FlowRule>> ruleSource = new NacosDataSource<>(remoteAddress, groupId,
            flowDataId, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
        FlowRuleManager.register2Property(ruleSource.getProperty());

        ReadableDataSource<String, List<ParamFlowRule>> paramRuleSource = new NacosDataSource<>(remoteAddress, groupId,
            paramDataId, source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
        ParamFlowRuleManager.register2Property(paramRuleSource.getProperty());
    }

    private void initClientConfigProperty() {
        ReadableDataSource<String, ClusterClientConfig> clientConfigDs = new NacosDataSource<>(remoteAddress, groupId,
            configDataId, source -> JSON.parseObject(source, new TypeReference<ClusterClientConfig>() {}));
        ClusterClientConfigManager.registerClientConfigProperty(clientConfigDs.getProperty());
    }

    private void initServerTransportConfigProperty() {
        ReadableDataSource<String, ServerTransportConfig> serverTransportDs = new NacosDataSource<>(remoteAddress, groupId,
            clusterMapDataId, source -> {
            List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
            return Optional.ofNullable(groupList)
                .flatMap(this::extractServerTransportConfig)
                .orElse(null);
        });
        ClusterServerConfigManager.registerServerTransportProperty(serverTransportDs.getProperty());
    }

    private void registerClusterRuleSupplier() {
        // 按 namespace 注册集群流控规则 Supplier，dataId 格式：${namespace}-flow-rules
        ClusterFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<FlowRule>> ds = new NacosDataSource<>(remoteAddress, groupId,
                namespace + DemoConstants.FLOW_POSTFIX, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
            return ds.getProperty();
        });
        // 按 namespace 注册集群热点参数规则 Supplier
        ClusterParamFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<ParamFlowRule>> ds = new NacosDataSource<>(remoteAddress, groupId,
                namespace + DemoConstants.PARAM_FLOW_POSTFIX, source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
            return ds.getProperty();
        });
    }

    private void initClientServerAssignProperty() {
        // cluster-map JSON 示例：
        // [{"clientSet":["112.12.88.66@8729","112.12.88.67@8727"],"ip":"112.12.88.68","machineId":"112.12.88.68@8728","port":11111}]
        // machineId 为 ip@commandPort，commandPort 即 transport 模块暴露给 Dashboard 的端口
        ReadableDataSource<String, ClusterClientAssignConfig> clientAssignDs = new NacosDataSource<>(remoteAddress, groupId,
            clusterMapDataId, source -> {
            List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
            return Optional.ofNullable(groupList)
                .flatMap(this::extractClientAssignment)
                .orElse(null);
        });
        ClusterClientConfigManager.registerServerAssignProperty(clientAssignDs.getProperty());
    }

    private void initStateProperty() {
        // Cluster map format:
        // [{"clientSet":["112.12.88.66@8729","112.12.88.67@8727"],"ip":"112.12.88.68","machineId":"112.12.88.68@8728","port":11111}]
        // machineId: <ip@commandPort>, commandPort for port exposed to Sentinel dashboard (transport module)
        ReadableDataSource<String, Integer> clusterModeDs = new NacosDataSource<>(remoteAddress, groupId,
            clusterMapDataId, source -> {
            List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
            return Optional.ofNullable(groupList)
                .map(this::extractMode)
                .orElse(ClusterStateManager.CLUSTER_NOT_STARTED);
        });
        ClusterStateManager.registerProperty(clusterModeDs.getProperty());
    }

    private int extractMode(List<ClusterGroupEntity> groupList) {
        // 若某 Server 分组的 machineId 与当前机器一致，则本机为 Token Server
        if (groupList.stream().anyMatch(this::machineEqual)) {
            return ClusterStateManager.CLUSTER_SERVER;
        }
        // 若当前 machineId 出现在某 Server 的 clientSet 中，则为 Token Client
        // 否则尚未分配角色，状态为 NOT_STARTED
        boolean canBeClient = groupList.stream()
            .flatMap(e -> e.getClientSet().stream())
            .filter(Objects::nonNull)
            .anyMatch(e -> e.equals(getCurrentMachineId()));
        return canBeClient ? ClusterStateManager.CLUSTER_CLIENT : ClusterStateManager.CLUSTER_NOT_STARTED;
    }

    private Optional<ServerTransportConfig> extractServerTransportConfig(List<ClusterGroupEntity> groupList) {
        return groupList.stream()
            .filter(this::machineEqual)
            .findAny()
            .map(e -> new ServerTransportConfig().setPort(e.getPort()).setIdleSeconds(600));
    }

    private Optional<ClusterClientAssignConfig> extractClientAssignment(List<ClusterGroupEntity> groupList) {
        if (groupList.stream().anyMatch(this::machineEqual)) {
            return Optional.empty();
        }
        // 从目标 Server 分组构建 Client 指派配置（Server IP + 端口）
        for (ClusterGroupEntity group : groupList) {
            if (group.getClientSet().contains(getCurrentMachineId())) {
                String ip = group.getIp();
                Integer port = group.getPort();
                return Optional.of(new ClusterClientAssignConfig(ip, port));
            }
        }
        return Optional.empty();
    }

    private boolean machineEqual(/*@Valid*/ ClusterGroupEntity group) {
        return getCurrentMachineId().equals(group.getMachineId());
    }

    private String getCurrentMachineId() {
        // 注意：容器环境下 ip@port 识别可能不准确
        return HostNameUtil.getIp() + SEPARATOR + TransportConfig.getRuntimePort();
    }

    private static final String SEPARATOR = "@";
}