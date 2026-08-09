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
package com.alibaba.csp.sentinel.demo.file.rule;

import java.net.URLDecoder;
import java.util.List;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * <p>
 * 演示使用 {@link FileRefreshableDataSource} 从本地文件读取 {@link Rule}。
 * 数据源每 3 秒轮询文件，变更时通知 {@link PropertyListener}。
 * </p>
 * <p>
 * 每个 {@link ReadableDataSource} 持有 {@link SentinelProperty} 保存反序列化后的配置；
 * {@link PropertyListener} 监听 Property 而非数据源本身；
 * {@link Converter} 负责 JSON 反序列化。
 * </p>
 * <p>
 * 可调用 {@link FlowRuleManager#register2Property}、
 * {@link DegradeRuleManager#register2Property}、
 * {@link SystemRuleManager#register2Property} 监听规则热更新。
 * </p>
 * <p>
 * 其他数据源（如 <a href="https://github.com/alibaba/nacos">Nacos</a>、Zookeeper、Git、CSV）
 * 可实现 {@link ReadableDataSource} 接口接入。
 * </p>
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 */
public class FileDataSourceDemo {

    public static void main(String[] args) throws Exception {
        FileDataSourceDemo demo = new FileDataSourceDemo();
        demo.listenRules();

        /* 启动 QPS 压测，速率由 FlowRule.json 中的规则限制 */
        FlowQpsRunner runner = new FlowQpsRunner();
        runner.simulateTraffic();
        runner.tick();
    }

    private void listenRules() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        String flowRulePath = URLDecoder.decode(classLoader.getResource("FlowRule.json").getFile(), "UTF-8");
        String degradeRulePath = URLDecoder.decode(classLoader.getResource("DegradeRule.json").getFile(), "UTF-8");
        String systemRulePath = URLDecoder.decode(classLoader.getResource("SystemRule.json").getFile(), "UTF-8");

        // 流控规则文件数据源
        FileRefreshableDataSource<List<FlowRule>> flowRuleDataSource = new FileRefreshableDataSource<>(
            flowRulePath, flowRuleListParser);
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

        // 熔断规则文件数据源
        FileRefreshableDataSource<List<DegradeRule>> degradeRuleDataSource
            = new FileRefreshableDataSource<>(
            degradeRulePath, degradeRuleListParser);
        DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());

        // 系统保护规则文件数据源
        FileRefreshableDataSource<List<SystemRule>> systemRuleDataSource
            = new FileRefreshableDataSource<>(
            systemRulePath, systemRuleListParser);
        SystemRuleManager.register2Property(systemRuleDataSource.getProperty());
    }

    private Converter<String, List<FlowRule>> flowRuleListParser = source -> JSON.parseObject(source,
        new TypeReference<List<FlowRule>>() {});
    private Converter<String, List<DegradeRule>> degradeRuleListParser = source -> JSON.parseObject(source,
        new TypeReference<List<DegradeRule>>() {});
    private Converter<String, List<SystemRule>> systemRuleListParser = source -> JSON.parseObject(source,
        new TypeReference<List<SystemRule>>() {});
}
