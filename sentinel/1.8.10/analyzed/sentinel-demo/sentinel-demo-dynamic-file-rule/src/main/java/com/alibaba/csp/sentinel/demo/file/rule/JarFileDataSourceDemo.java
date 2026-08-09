package com.alibaba.csp.sentinel.demo.file.rule;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.FileInJarReadableDataSource;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.List;

/**
 * <p>
 * 演示使用 {@link FileInJarReadableDataSource} 从 JAR 内读取 {@link Rule}。
 * 数据源每 3 秒轮询 JAR 内文件，变更时通知监听器。
 * </p>
 * <p>
 * 每个 {@link ReadableDataSource} 持有 {@link SentinelProperty}；
 * {@link PropertyListener} 监听 Property；{@link Converter} 负责反序列化。
 * </p>
 * <p>
 * {@link FlowRuleManager#register2Property(SentinelProperty)},
 * {@link DegradeRuleManager#register2Property(SentinelProperty)},
 * {@link SystemRuleManager#register2Property(SentinelProperty)} could be called for listening the
 * {@link Rule}s change.
 * </p>
 * <p>
 * For other kinds of data source, such as <a href="https://github.com/alibaba/nacos">Nacos</a>,
 * Zookeeper, Git, or even CSV file, We could implement {@link ReadableDataSource} interface to read these
 * configs.
 * </p>
 *
 * @author dingq
 */
public class JarFileDataSourceDemo {

    public static void main(String[] args) throws Exception {
        JarFileDataSourceDemo demo = new JarFileDataSourceDemo();
        demo.listenRules();

        // 启动压测，速率由 JAR 内 FlowRule.json 限制
        FlowQpsRunner runner = new FlowQpsRunner();
        runner.simulateTraffic();
        runner.tick();
    }

    private void listenRules() throws Exception {
        // 请按实际构建产物路径修改 jarPath
        String jarPath = System.getProperty("user.dir") + "/sentinel-demo/sentinel-demo-dynamic-file-rule/target/"
            + "sentinel-demo-dynamic-file-rule.jar";
        // 例：完整路径为 jar!/classes/FlowRule.json 时，flowRuleInJarPath 填 classes/FlowRule.json
        String flowRuleInJarPath = "FlowRule.json";

        FileInJarReadableDataSource<List<FlowRule>> flowRuleDataSource = new FileInJarReadableDataSource<>(
                jarPath,flowRuleInJarPath, flowRuleListParser);
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }

    private Converter<String, List<FlowRule>> flowRuleListParser = source -> JSON.parseObject(source,
            new TypeReference<List<FlowRule>>() {});
}
