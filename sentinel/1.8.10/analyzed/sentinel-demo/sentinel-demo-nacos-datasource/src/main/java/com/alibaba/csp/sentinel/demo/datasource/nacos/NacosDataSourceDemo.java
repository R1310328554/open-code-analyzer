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
package com.alibaba.csp.sentinel.demo.datasource.nacos;

import java.util.List;
import java.util.Properties;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.PropertyKeyConst;

/**
 * Nacos 动态数据源演示：从 Nacos 读取流控规则并注册到 {@link FlowRuleManager}。
 * 运行前需先启动本地 Nacos，并用 {@link NacosConfigSender} 发布初始规则。
 *
 * @author Eric Zhao
 */
public class NacosDataSourceDemo {

    private static final String KEY = "TestResource";
    // Nacos 服务地址
    private static final String remoteAddress = "localhost:8848";
    // Nacos groupId
    private static final String groupId = "Sentinel_Demo";
    // Nacos dataId
    private static final String dataId = "com.alibaba.csp.sentinel.demo.flow.rule";
    // 设为 true 时使用命名空间，需配置 NACOS_NAMESPACE_ID
    private static boolean isDemoNamespace = false;
    // 命名空间 ID，可在 Nacos 控制台获取，例如 0f5c7314-4983-4022-ad5a-347de1d1057d
    private static final String NACOS_NAMESPACE_ID = "${namespace}";

    public static void main(String[] args) {
        if (isDemoNamespace) {
            loadMyNamespaceRules();
        } else {
            loadRules();
        }

        // 假定 Nacos 中资源 TestResource 初始 QPS 阈值为 5
        FlowQpsRunner runner = new FlowQpsRunner(KEY, 1, 100);
        runner.simulateTraffic();
        runner.tick();
    }

    /** 默认命名空间：直连 Nacos 并注册流控规则 Property。 */
    private static void loadRules() {
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(remoteAddress, groupId, dataId,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                }));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }

    /** 指定命名空间：通过 Properties 连接 Nacos 并注册规则 Property。 */
    private static void loadMyNamespaceRules() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, remoteAddress);
        properties.put(PropertyKeyConst.NAMESPACE, NACOS_NAMESPACE_ID);

        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(properties, groupId, dataId,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                }));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }

}
